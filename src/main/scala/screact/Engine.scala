package screact

import scala.collection.mutable

import scutil.Implicits._
import scutil.Functions._
import scutil.log.Logging

import screact.Updates._

object Engine extends Logging {
	// while an update cycle is running, new emissions on source nodes
	// are stored in a Buffer to be scheduled afterwards in a new cycle
	private var isUpdating	= 0
	
	private def updating(block: =>Unit) {
		try {
			isUpdating	+= 1
			block
		}
		finally {
			isUpdating	-= 1
		}
	}
	
	private def logged(block: =>Unit) {
		try {
			block
		}
		catch { case e => 
			ERROR(e) 
		}
	}
	
	//------------------------------------------------------------------------------
	
	private val delay	= mutable.ArrayBuffer.empty[Scheduled]
	
	/** execute all set and emit calls within code after it within a single update cycle */
	private[screact] def batch(effect: =>Unit) {
		updating {
			logged {
				effect
			}
		}
		scheduleDelayed()
	}
		
	private[screact] def scheduleSingle(node:Scheduled) {
		schedule(Iterable(node))
	}
	
	private[screact] def schedule(nodes:Iterable[Scheduled]) {
		if (isUpdating != 0) {
			delay ++= nodes
			return
		}
		
		updating {
			logged {
				val	todo	= nodes flatMap { _ apply () }
				updateCycle(todo)
			}
		}
			
		if (delay.nonEmpty) {
			scheduleDelayed()
		}
	}
	
	private def scheduleDelayed() {
		if (isUpdating != 0)	{
			return
		}
		
		if (delay.isEmpty) {
			// Debug.line("empty scheduleDelayed")
			return
		}
		
		logged {
			val	todo	= delay.toArray
			// Debug.line("delayed nodes: " + todo.size)
			delay.clear()
			schedule(todo)
		}
	}
	
	//------------------------------------------------------------------------------
	
	// TODO start should be an immutable Set
	private def updateCycle(start:Iterable[Node]) {
		val done	= mutable.Set.empty[Node]
		val	queue	= new SimplePrioQueue
		queue insertMany start
		while (queue.nonEmpty) {
			val	head	= queue extract () getOrError "oops"
			if (!(done contains head)) {
				head.update() match {
					case Changed	=>
						// value has changed, unschedule the head and schedule the head node's dependencies
						queue insertMany head.dependents
						done	+= head
					case Unchanged	=>
						// value has not changed, unschedule the head and be done.
						done	+= head
					case Rerank	=>
						// re-ranked. re-insert the head into the queue.
						queue insert head
				}
			}
		}
		done foreach { _.reset() }
		
		// to allow done nodes to be collected
		val	doneSize	= done.size
		done.clear()
		
		// TODO ugly hack to get rid of WeakReferences
		if (doneSize > 10) {
			NodeSet.gc()
		}
		if (doneSize > 100) {
			System.gc()
		}
		if (NodeSet.id > (1L<<32)) {
			NodeSet.compact()
		}
	}

	//------------------------------------------------------------------------------
	
	private var	updating	= mutable.Stack.empty[Effect[Node]]
	
	private [screact] def withReader[T](readCallback:Effect[Node], block: =>T):T	= {
		pushReader(readCallback)
		try { block }
		finally { popReader() }
	}
	
	private[screact] def pushReader(readCallback:Effect[Node]) {
		updating push readCallback
	}
	
	private[screact] def popReader() {
		updating.pop()
	}
	
	private[screact] def notifyReader(node:Node) {
		if (updating.nonEmpty) {
			updating.top apply node
		}
	}
	
	//------------------------------------------------------------------------------
	/*
	//## name debug
	
	// NOTE deactivated in Reactive atm
	
	case class Tag(node:Node, name:String)
	
	private var tags	= List[Tag]()
	
	def addTag(node:Node, name:String) {
		val	tag	= Tag(node, name)
		tags	= tag :: tags
	}
	
	def readTag(node:Node):Option[String] = 
			tags find { _.node == node } map { _.name }
	
	// called when a dependency has been added
	def debugEdge(source:Node, target:Node) {
		val sourceTag	= readTag(source)
		val targetTag	= readTag(target)
		if (sourceTag.isDefined || targetTag.isDefined)	{
			println("!!!\t" + sourceTag + "\t=>\t" + targetTag)
		}
	}
	*/
	
	//------------------------------------------------------------------------------
	//## debug sourcedebug

	val ignoredPrefixes = Set(
		"screact.",
		"scala.",
		"java.",
		"javax.",
		"com.sun."
	)
		
	// TODO hardcoded
	def clientClass(name:String):Boolean	= 
			name.startsWith("screact.demo") || !ignoredPrefixes.exists{ name startsWith _ }
	
	def clientCall:Option[StackTraceElement] =
			Thread.currentThread.getStackTrace find { it => clientClass(it.getClassName) }
}
