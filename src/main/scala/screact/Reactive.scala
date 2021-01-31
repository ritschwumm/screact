package screact

import scala.collection.mutable

import scutil.lang._
import scutil.log._

/** base trait for reactive values with some current value (may be Unit) and emitting events. */
trait Reactive[+Cur,+Msg] extends Node with AutoCloseable with Logging {
	private var rankVar:Int				= 0
	private var disposedFlag:Boolean	= false

	private[screact] final def rank:Int	= rankVar

	/** current value, usable outside an update cycle */
	private[screact] def cur:Cur
	/** current message sent to dependent nodes, always None outside an update cycle */
	private[screact] def msg:Option[Msg]

	// NOTE without keeping strong references to the source Nodes like this we loose connections
	/** Nodes this Reactive reads from */
	private val sources	= mutable.ArrayBuffer[Node]()

	/** Nodes reading data from this Reactive */
	private[screact] val sinks	= engine.newHasSinks()

	private[screact] final def update():Update = {
		if (disposedFlag)	return Unchanged

		val	ok	= updateInternal(true)
		if (!ok)			return Rerank

		val	notify	= msg.isDefined
		if (!notify)		return Unchanged

		Changed
	}

	protected final def init():Unit	= {
		updateInternal(false)
	}

	/** returns true on ok, false on rank mismatch */
	private def updateInternal(checkRank:Boolean):Boolean = {
		val	oldRank	= rankVar
		rankVar	= 0

		// just to keep sources alive during this update cycle
		val oldSources	= sources
		val _ 			= oldSources
		// NOTE i'm already in the queue, so even if i get reranked this will not affect whether i'm scheduled or not
		removeSelfFromSources()

		/*
		called back from the parent when we read it so
		we can notify it about us depending on it
		and update our rank
		*/
		def readCallback(source:Node):Unit	= {
			val	sourceRank	= source.rank
			if (sourceRank >= rankVar) {
				rankVar	= sourceRank + 1
			}
			if (checkRank && rankVar > oldRank) {
				throw RankMismatch
			}
			source.sinks add this
			sources += source
		}

		engine.withReader(readCallback) {
			try {
				// call readCallback which in turn
				// -	updates our rank
				// -	updates our sources
				// -	registers us as a dependent on the source
				calculate()
				true
			}
			catch {
				case RankMismatch	=>
					pushDownDependents()
					false
				case e:Exception	=>
					// TODO move logging into the Domain
					ERROR("calculate failed", this.toString, origin.toString, e)
					true
			}
		}
	}

	/** calls read on sources, sources call back from read */
	protected def calculate():Unit

	/** called by deps between update and reset */
	final def current:Cur = {
		if (engine != Engine.access)	throw WrongThreadException
		engine notifyReader this
		cur
	}

	/** called by deps between update and reset */
	final def message:Option[Msg] = {
		if (engine != Engine.access)	throw WrongThreadException
		engine notifyReader this
		msg
	}

	final def disposed:Boolean	= disposedFlag

	final def close():Unit	= {
		if (engine != Engine.access)	throw WrongThreadException
		disposedFlag	= true
		sources.clear()
		sinks.clear()
	}

	/** recursively increase the rank of all dependent nodes */
	private [screact] def pushDown(newRank:Int):Unit	= {
		if (newRank > rank) {
			rankVar	= newRank
			pushDownDependents()
		}
	}

	private def pushDownDependents():Unit	= {
		sinks.all foreach { _ pushDown rankVar+1 }
	}

	private def removeSelfFromSources():Unit	= {
		sources foreach { _.sinks remove this }
		sources.clear()
	}

	//------------------------------------------------------------------------------
	//## Observing forwarder

	def observe(effect:Effect[Msg])(implicit observing:Observing):Disposer =
		observing.observe(this, effect)

	def observeOnce(effect:Effect[Msg])(implicit observing:Observing):Disposer =
		observing.observeOnce(this, effect)
}
