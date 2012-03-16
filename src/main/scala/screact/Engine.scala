package screact

import scala.collection.mutable

import scutil.Implicits._
import scutil.Functions._
import scutil.log.Logging

import screact.Updates._

/** this is the main workhorse which schedules all activities on Reactive Nodes */
object Engine extends Logging {
	//------------------------------------------------------------------------------
	//## scheduler entrypoint
	
	// while an update cycle is running, new emissions on source nodes
	// are stored in a Buffer to be scheduled afterwards in a new cycle
	private var updating	= 0
	
	private val delay	= mutable.ArrayBuffer.empty[Scheduled]
	
	private[screact] def schedule(node:Scheduled) {
		scheduleMany(Iterable(node))
	}
	
	private def scheduleMany(nodes:Iterable[Scheduled]) {
		if (updating != 0) {
			delay ++= nodes
			return
		}
		
		updating	+= 1
		try  {
			val	todo	= nodes flatMap { _ apply () }
			updateCycle(todo)
		}
		catch { case e => 
			ERROR(e) 
		}
		finally {
			updating	-= 1
		}
			
		if (delay.nonEmpty) {
			val	todo	= delay.toArray
			// Debug.line("delayed nodes: " + todo.size)
			delay.clear()
			scheduleMany(todo)
		}
	}
	
	//------------------------------------------------------------------------------
	//## update cycle
	
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
						queue insertMany head.sinks.all
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
			HasSinks.gc()
		}
		if (doneSize > 100) {
			System.gc()
		}
	}

	//------------------------------------------------------------------------------
	//## dependency callbacks
	
	private var	readCallbacks	= mutable.Stack.empty[Effect[Node]]
	
	private [screact] def withReader[T](readCallback:Effect[Node])(block: =>T):T	= {
		readCallbacks push readCallback
		try { block }
		finally { readCallbacks.pop() }
	}
	
	private[screact] def notifyReader(node:Node) {
		if (readCallbacks.nonEmpty) {
			readCallbacks top node
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
