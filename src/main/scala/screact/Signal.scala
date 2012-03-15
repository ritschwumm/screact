package screact

import scutil.Functions._
import scutil.Disposable

/** A Reactive with a current value emitting change events. change Events are emitted only if the value has changed. */
trait Signal[+T] extends Reactive[T,T] { 
	// convert to Events
	
	final def changes:Events[T]	= 
			events { message }
		
	final def slideChanges[U](func:(T,T)=>U):Events[U] = {
		var	prev	= current
		changes map { next =>
			val	out	= func(prev,next)
			prev	= next
			out
		}
	}
	
	// functor
	
	final def map[U](func:T=>U):Signal[U]	= 
			signal { func(current) }
		
	// applicative functor
	
	final def ap[U,V](source:Signal[U])(implicit ev:T=>U=>V):Signal[V]	= 
			signal { ev(current)(source.current) }
		
	final def pa[U](func:Signal[T=>U]):Signal[U]	= 
			signal { func.current apply current }
	
	// monad
	
	final def flatMap[U](func:T=>Signal[U]):Signal[U]	= 
			signal { func(current).current }
	
	final def flatten[U](implicit ev:T=>Signal[U]):Signal[U]	= 
			this flatMap ev 
	
	// monad to Events
	
	final def flattenMapEvents[U](func:T=>Events[U]):Events[U]	=
			events {
				func(current).message
			}
			
	final def flattenEvents[U](implicit ev:T=>Events[U]):Events[U]	=
			flattenMapEvents(ev)
		
	// monad to Cell
	
	final def flatMapCell[U](func:T=>Cell[U]):Cell[U]	= new Cell[U] {
		val signal	= screact.signal { func(current).current }
		def set(it:U) { func(current) set it }
	}
					
	final def flattenCell[U](implicit ev:T=>Cell[U]):Cell[U]	=
			flatMapCell(ev)
	
	// foldable
		
	// TODO add fold ?
	
	final def reduce[U](func:(T,T)=>U):Signal[U] = {
		var	prev	= this.current
		signal {
			val next	= current
			val	out		= func(prev, next)
			prev	= next
			out
		}
	}
	
	// delayable
	
	final def delay[U>:T](initial:U)(implicit observing:Observing):Signal[U]	= 
			changes.delay hold initial

	// other
		
	final def zip[U](that:Signal[U]):Signal[(T,U)]	= 
			signal { (this.current, that.current) }
		
	final def choose[U](sourceTrue:Signal[U], sourceFalse:Signal[U])(implicit ev:T=>Boolean):Signal[U]	=
			signal { if (current) sourceTrue.current else sourceFalse.current }

	//------------------------------------------------------------------------------
	//## Observing forwarder
	
	def observeNow(effect:Effect[T])(implicit observing:Observing):Disposable	=
			observing observeNow (this, effect)
}
