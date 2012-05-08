package screact

import scala.collection.mutable

import scutil.Implicits._
import scutil.Functions._
import scutil.log.Logging

import screact.Updates._

/** managed one Engine per Thread */
object Engine {
	private val	threadLocal	= new ThreadLocal[Engine]
	
	def access:Engine	= {
		val	oldEngine	= threadLocal.get
		if (oldEngine != null)	return oldEngine
		
		val newEngine	= new Engine
		threadLocal set newEngine
		newEngine
	}
}

/** this is the main workhorse which schedules all activities on Reactive Nodes */
class Engine extends Logging {
	private[screact] val sinksCache	= new SinksCache
	
	//------------------------------------------------------------------------------
	//## scheduler entrypoint
	
	/** external events */
	private val external	= mutable.ArrayBuffer.empty[Scheduled]
	
	// true within an update cycle
	private var updating	= false
	
	private[screact] def schedule(node:Scheduled) {
		scheduleMany(Iterable(node))
	}
	
	// TODO what if this is called while we are already scheduled?
	private def scheduleMany(nodes:Iterable[Scheduled]) {
		external	++= nodes
		
		// delayed and new external events are immediately re-scheduled
		while (!updating && external.nonEmpty) {
			scheduleInternal()
		}
	}
	
	private def scheduleInternal() {
		try  {
			updating	= true
			val	internal	= external flatMap { _ apply () }
			external.clear()
			// this may schedule new delayed events, those are treated as external
			updateCycle(internal)
		}
		catch { case e => 
			ERROR(e) 
		}
		finally {
			updating	= false
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
			sinksCache.gc()
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
		if (node.engine != this)	throw WrongThreadException
		
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
