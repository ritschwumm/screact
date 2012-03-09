package screact

import scala.collection.mutable

import scutil.Implicits._
import scutil.Functions._
import scutil.log.Logging

import screact.Updates._

/** this is the main workhorse which schedules all activities on Reactive Nodes */
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
	
	/*
	//* execute all set and emit calls within code after it within a single update cycle
	private[screact] def batch(effect: =>Unit) {
		updating {
			logged {
				effect
			}
		}
		scheduleDelayed()
	}
	*/
		
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
		val	queue	= new NodeQueue
		queue insertMany start
		while (queue.nonEmpty) {
			val	head	= queue extract () getOrError "oops, empty queue"
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
	
	private [screact] def withReader[T](readCallback:Effect[Node])(block: =>T):T	= {
		updating push readCallback
		try { block }
		finally { updating.pop() }
	}
	
	private[screact] def notifyReader(node:Node) {
		if (updating.nonEmpty) {
			updating top node
		}
	}
	
	//------------------------------------------------------------------------------
	//## source debug

	// TODO hardcoded
	val ignoredPrefixes = Set(
		"screact.",
		"scala.",
		"java.",
		"javax.",
		"com.sun."
	)
		
	def clientCall:Option[StackTraceElement] =
			Thread.currentThread.getStackTrace find { it => clientClass(it.getClassName) }
		
	def clientClass(name:String):Boolean	= 
			!(ignoredPrefixes exists { name startsWith _ })
}
