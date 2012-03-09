package screact

import scala.collection.mutable

import scutil.Functions._
import scutil.Disposable

import screact.Updates._

/** base trait for reactive values with some current value (may be Unit) and emitting events. */
trait Reactive[+Cur,+Msg] extends Node with Disposable { 
	private[screact] final var rank:Int	= 0
	private[screact] def cur:Cur
	private[screact] def msg:Option[Msg]
	
	private var disposed	= false
	
	// TODO why does keeping strong refs to sources here help against lost connections?
	// Nodes thi Reactive reads from
	private var sources	= mutable.ArrayBuffer[Node]()
	
	// Nodes readin data from this Reactive
	private val deps		= new NodeSet
	private[screact] final def dependents:Iterable[Node]	= deps.all
	private[screact] final def addDependent(node:Node) 		{ deps add		node }
	private[screact] final def removeDependent(node:Node)	{ deps remove	node }
	
	private[screact] final def update():Update = {
		if (disposed)	return Unchanged
		
		val	ok	= updateInternal(true)
		if (!ok)		return Rerank
		
		val	notify	= msg.isDefined
		if (!notify)	return Unchanged
			
		Changed
	}
	
	protected final def init() {
		updateInternal(false)
	}
	
	// returns true on ok, false on rank mismatch
	private def updateInternal(checkRank:Boolean):Boolean = {
		val	oldRank	= rank
		rank	= 0
		
		// NOTE i'm already in the queue, so even if i get reranked this will not affect whether i'm scheduled or not
		sources foreach { _ removeDependent this }
		// TODO is this a good idea? i needed to hold my sources to avoid missing dependencies in the previous version
		sources.clear()
		
		// called when calculate reads a source
		def readCallback(source:Node) {
			val	sourceRank	= source.rank
			if (sourceRank >= rank) {
				rank	= sourceRank + 1
			}
			if (checkRank && rank > oldRank) {
				// newSources	= Nil
				throw RankMismatch
			}
			sources += source
		}
			
		Engine.withReader(readCallback) {
			try {
				calculate()	// sets rank and sources via readCallback
				sources foreach { _ addDependent this }  
				true
			}
			catch { 
				case RankMismatch	=>
					false
			}
		}
	}
	
	// calls read on sources, sources call back from read
	protected def calculate():Unit
	
	// called by deps between update and reset 
	final def current:Cur = {
		Engine notifyReader this
		cur
	}
	
	// called by deps between update and reset
	final def message:Option[Msg] = {
		Engine notifyReader this
		msg
	}
	
	final def dispose() {
		disposed	= true
		sources foreach { _ removeDependent this }
		sources.clear()
		deps.clear()
	}
	
	//------------------------------------------------------------------------------
	//## Observing forwarder
	
	def observe(effect:Effect[Msg])(implicit observing:Observing):Disposable =
			observing observe (this, effect)
			
	def observeOnce(effect:Effect[Msg])(implicit observing:Observing):Disposable =
			observing observeOnce (this, effect)
}
