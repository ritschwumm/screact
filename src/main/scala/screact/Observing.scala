package screact

import scala.collection.mutable

import scutil.lang.*

/**
Observing keeps hard references to observed Reactives so they are not garbage
collected before their last observer. Every class observing a Reactive must
extend this trait.
*/
trait Observing {
	protected given observing:Observing	= this

	/** keeps hard references */
	private val connections	= new mutable.ArrayBuffer[Disposer]

	// used in Reactive and Signal

	private[screact] def observe[T](source:Reactive[?,T], effect:Effect[T]):Disposer = {
		val	target	= new Target(effect, source)
		lazy val connection:Disposer	=
			Disposer delay {
				target.close()
				connections	-= connection
			}
		connections += connection
		connection
	}

	private[screact] def observeOnce[T](source:Reactive[?,T], effect:Effect[T]):Disposer = {
		lazy val connection:Disposer	=
			observe(source, (value:T) => {
				effect(value)
				connection.dispose()
			})
		connection
	}

	private[screact] def observeNow[T](source:Signal[T], effect:Effect[T]):Disposer = {
		val connection	= observe(source, effect)
		// TODO display exceptions caught here?
		effect(source.current)
		connection
	}
}
