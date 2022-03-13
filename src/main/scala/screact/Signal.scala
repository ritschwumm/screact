package screact

import scutil.lang.*

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

	final def ap[U,V](source:Signal[U])(using ev:T <:< (U=>V)):Signal[V]	=
		signal { ev(current)(source.current) }

	/*
	final def pa[U](func:Signal[T=>U]):Signal[U]	=
		signal { func.current apply current }
	*/

	// monad

	final def flatMap[U](func:T=>Signal[U]):Signal[U]	=
		signal { func(current).current }

	final def flatten[U](using ev:T <:< Signal[U]):Signal[U]	=
		this flatMap ev

	// monad to Events

	final def flatMapEvents[U](func:T=>Events[U]):Events[U]	=
		events { func(current).message }

	final def flattenEvents[U](using ev:T <:< Events[U]):Events[U]	=
		this flatMapEvents ev

	// monad to Cell

	final def flatMapCell[U](func:T=>Cell[U]):Cell[U]	= new Cell[U] {
		val signal	= screact.signal { func(current).current }
		def set(it:U):Unit	= { func(current) set it }
	}

	final def flattenCell[U](using ev:T <:< Cell[U]):Cell[U]	=
		this flatMapCell ev

	// delayable

	final def delay[U>:T](initial:U)(using observing:Observing):Signal[U]	=
		edge.delay hold initial

	// other

	@deprecated("use product", "0.207.0")
	final def tuple[U](that:Signal[U]):Signal[(T,U)]	=
		product(that)

	final def product[U](that:Signal[U]):Signal[(T,U)]	=
		map2(that) { (_,_) }

	final def map2[U,V](that:Signal[U])(func:(T,U)=>V):Signal[V]	=
		signal { func(this.current, that.current) }

	@deprecated("use fproduct", "0.207.0")
	final def tupleBy[U](func:T=>U):Signal[(T,U)]	=
		fproduct(func)

	final def fproduct[U](func:T=>U):Signal[(T,U)]	=
		this map { it => (it,func(it)) }

	final def untuple[U,V](using ev:T <:< (U,V)):(Signal[U],Signal[V])	=
		(map(_._1), map(_._2))

	final def choose[U](sourceTrue:Signal[U], sourceFalse:Signal[U])(using ev:T <:< Boolean):Signal[U]	=
		signal { if (current) sourceTrue.current else sourceFalse.current }

	//------------------------------------------------------------------------------
	//## Observing forwarder

	def observeNow(effect:Effect[T])(using observing:Observing):Disposer	=
		observing.observeNow(this, effect)
}
