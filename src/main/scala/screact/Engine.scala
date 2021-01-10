package screact

import scala.collection.mutable

import scutil.core.implicits._
import scutil.lang._
import scutil.log._

/** manages one Engine per Thread */
object Engine {
	private val	threadLocal	= new ThreadLocal[Engine]

	def access:Engine	= {
		val	oldEngine	= threadLocal.get
		if (oldEngine ne null)	return oldEngine

		val newEngine	= new Engine
		threadLocal set newEngine
		newEngine
	}
}

/** this is the main workhorse which schedules all activities on Reactive Nodes */
final class Engine extends Logging {
	private val sinksCache	= new SinksCache

	private[screact] def newHasSinks():Sinks	=
			new HasSinks(sinksCache)

	private [screact] def registerNode(node:Node):Long	=
			sinksCache register node

	//------------------------------------------------------------------------------
	//## scheduler entrypoint

	/** external events */
	private val external	= mutable.ArrayBuffer.empty[Scheduled]

	// true within an update cycle
	private var updating	= false

	// NOTE could schedule multiple first-entry nodes
	private[screact] def schedule(node:Scheduled):Unit	= {
		external	+= node
		scheduleLoop()
	}

	// delayed and new external events are immediately re-scheduled
	private def scheduleLoop():Unit	= {
		while (!updating && external.nonEmpty) {
			scheduleInternal()
		}
	}

	private def scheduleInternal():Unit	= {
		try  {
			updating	= true
			val	internal	= external mapFilter { _.apply() }
			external.clear()
			// this may schedule new delayed events, those are treated as external
			updateCycle(internal)
		}
		catch { case e:Exception =>
			// TODO move this into the Domain
			ERROR(e)
		}
		finally {
			updating	= false
		}
	}

	//------------------------------------------------------------------------------
	//## update cycle

	// BETTER start should be an immutable Set
	private def updateCycle(start:Iterable[Node]):Unit	= {
		val done	= mutable.Set.empty[Node]
		val	queue	= new NodeQueue
		queue insertMany start
		while (queue.nonEmpty) {
			val	head	= queue.extract() getOrError "oops, empty queue"
			if (!(done contains head)) {
				head.update() match {
					case Changed	=>
						// value has changed, unschedule the head node and schedule the head node's dependencies
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

		// NOTE ugly hack to get rid of WeakReferences
		if (doneSize > 10) {
			sinksCache.gc()
		}
	}

	//------------------------------------------------------------------------------
	//## dependency callbacks

	// dynamic variable
	private val	readCallbacks	= mutable.Stack.empty[Effect[Node]]

	// used in decoupled calculations
	private [screact] def withoutReader[T](block: =>T):T	=
			withReader((_:Node) => ())(block)

	private [screact] def withReader[T](readCallback:Effect[Node])(block: =>T):T	= {
		readCallbacks push readCallback
		try { block }
		finally { readCallbacks.pop() }
	}

	private[screact] def notifyReader(node:Node):Unit	= {
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

	// BETTER use something like scutil.SourceLocation
	def clientCall:Option[StackTraceElement] =
		Thread.currentThread.getStackTrace find { it => clientClass(it.getClassName) }

	private def clientClass(name:String):Boolean	=
		!(ignoredPrefixes exists { name startsWith _ })
}
