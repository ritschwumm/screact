package screact

import scala.collection.mutable

import scutil.lang._
import scutil.log._

/** base trait for reactive values with some current value (may be Unit) and emitting events. */
trait Reactive[+Cur,+Msg] extends Node with Disposable with Logging { 
	private[screact] final var rank:Int	= 0
	/** current value, usable outside an update cycle */
	private[screact] def cur:Cur
	/** current message sent to dependent nodes, always None outside an update cycle */
	private[screact] def msg:Option[Msg]
	
	private var disposedFlag	= false
	
	// NOTE without keeping strong references to the source Nodes like this we loose connections
	/** Nodes this Reactive reads from */
	private var sources	= mutable.ArrayBuffer[Node]()
	
	/** Nodes reading data from this Reactive */
	private[screact] val sinks	= new HasSinks(engine.sinksCache)
	
	private[screact] final def update():Update = {
		if (disposedFlag)	return Unchanged
		
		val	ok	= updateInternal(true)
		if (!ok)			return Rerank
		
		val	notify	= msg.isDefined
		if (!notify)		return Unchanged
			
		Changed
	}
	
	protected final def init() {
		updateInternal(false)
	}
	
	/** returns true on ok, false on rank mismatch */
	private def updateInternal(checkRank:Boolean):Boolean = {
		val	oldRank	= rank
		rank	= 0
		
		// just to keep sources alive during this update cycle
		val oldSources	= sources
		// NOTE i'm already in the queue, so even if i get reranked this will not affect whether i'm scheduled or not
		removeSelfFromSources()
		
		/**
		called back from the parent when we read it so
		we can notify it about us depending on it
		and update our rank
		*/
		def readCallback(source:Node) {
			val	sourceRank	= source.rank
			if (sourceRank >= rank) {
				rank	= sourceRank + 1
			}
			if (checkRank && rank > oldRank) {
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
					// TODO move this into the Domain
					ERROR("calculate failed", this, origin, e)
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
	
	final def dispose() {
		if (engine != Engine.access)	throw WrongThreadException
		disposedFlag	= true
		sources.clear()
		sinks.clear()
	}
	
	/** recursively increase the rank of all dependent nodes */
	private [screact] def pushDown(rank:Int) {
		if (rank > this.rank) {
			this.rank	= rank
			pushDownDependents()
		}
	}
	
	private def pushDownDependents() {
		sinks.all foreach { _ pushDown rank+1 }
	}
	
	private def removeSelfFromSources() {
		sources foreach { _.sinks remove this }
		sources.clear()
	}
	
	//------------------------------------------------------------------------------
	//## Observing forwarder
	
	def observe(effect:Effect[Msg])(implicit observing:Observing):Disposable =
			observing observe (this, effect)
			
	def observeOnce(effect:Effect[Msg])(implicit observing:Observing):Disposable =
			observing observeOnce (this, effect)
}
