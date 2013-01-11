package screact

import scala.collection.mutable

import scutil.lang._
import scutil.log._

import screact.Updates._

// BETTER aggregate logging

/** base trait for reactive values with some current value (may be Unit) and emitting events. */
trait Reactive[+Cur,+Msg] extends Node with Disposable with Logging { 
	private[screact] final var rank:Int	= 0
	private[screact] def cur:Cur
	private[screact] def msg:Option[Msg]
	
	private var disposedFlag	= false
	
	// Nodes this Reactive reads from
	// NOTE without this keeping string refs to the source Nodes we loose connections
	private var sources	= mutable.ArrayBuffer[Node]()
	
	// Nodes reading data from this Reactive
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
	
	// returns true on ok, false on rank mismatch
	private def updateInternal(checkRank:Boolean):Boolean = {
		val	oldRank	= rank
		rank	= 0
		
		// just to keep references during this update cycle
		val oldSources	= sources
		// NOTE i'm already in the queue, so even if i get reranked this will not affect whether i'm scheduled or not
		sources foreach { _.sinks remove this }
		sources.clear()
		
		// called when calculate reads a source
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
					false
				case e	=>
					ERROR("calculate failed", this, e)
					true
			}
		}
	}
	
	// calls read on sources, sources call back from read
	protected def calculate():Unit
	
	// called by deps between update and reset 
	final def current:Cur = {
		engine notifyReader this
		cur
	}
	
	// called by deps between update and reset
	final def message:Option[Msg] = {
		engine notifyReader this
		msg
	}
	
	final def disposed:Boolean	= disposedFlag
	
	final def dispose() {
		disposedFlag	= true
		sources foreach { _.sinks remove this }
		sources.clear()
		sinks.clear()
	}
	
	//------------------------------------------------------------------------------
	//## Observing forwarder
	
	def observe(effect:Effect[Msg])(implicit observing:Observing):Disposable =
			observing observe (this, effect)
			
	def observeOnce(effect:Effect[Msg])(implicit observing:Observing):Disposable =
			observing observeOnce (this, effect)
}
