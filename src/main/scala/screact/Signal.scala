package screact

import scutil.lang._

/** A Reactive with a current value emitting change events. change Events are emitted only if the value has changed. */
trait Signal[+T] extends Reactive[T,T] {
	// convert to Events
	
	final def edge:Events[T]	=
			events { message }
	
	/** value before a change occured, like slide but throwing away the current value */
	final def previous:Events[T]	= {
		// modify state only after evaluation of source nodes
		var	previous	= current
		edge map { next =>
			val	out		= previous
			previous	= next
			out
		}
	}
	
	/** apply a function to previous and current value on change */	
	final def slide[U](func:(T,T)=>U):Events[U] = {
		// modify state only after evaluation of source nodes
		var	previous	= current
		edge map { next =>
			val	out		= func(previous, next)
			previous	= next
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
	
	final def flatMapEvents[U](func:T=>Events[U]):Events[U]	=
			events { func(current).message }
			
	final def flattenEvents[U](implicit ev:T=>Events[U]):Events[U]	=
			this flatMapEvents ev
		
	// monad to Cell
	
	final def flatMapCell[U](func:T=>Cell[U]):Cell[U]	= new Cell[U] {
		val signal	= screact.signal { func(current).current }
		def set(it:U) { func(current) set it }
	}
					
	final def flattenCell[U](implicit ev:T=>Cell[U]):Cell[U]	=
			this flatMapCell ev
	
	// delayable
	
	final def delay[U>:T](initial:U)(implicit observing:Observing):Signal[U]	=
			edge.delay hold initial

	// other
		
	final def zip[U](that:Signal[U]):Signal[(T,U)]	=
			zipWith(that) { (_,_) }
		
	final def zipWith[U,V](that:Signal[U])(func:(T,U)=>V):Signal[V]	=
			signal { func(this.current, that.current) }
		
	final def zipBy[U](func:T=>U):Signal[(T,U)]	=
			this map { it => (it,func(it)) }
		
	final def unzip[U,V](implicit ev:T=>(U,V)):(Signal[U],Signal[V])	=
			(map(_._1), map(_._2))
		
	final def choose[U](sourceTrue:Signal[U], sourceFalse:Signal[U])(implicit ev:T=>Boolean):Signal[U]	=
			signal { if (current) sourceTrue.current else sourceFalse.current }
	
	//------------------------------------------------------------------------------
	//## Observing forwarder
	
	def observeNow(effect:Effect[T])(implicit observing:Observing):Disposable	=
			observing observeNow (this, effect)
}
