package screact

import scala.collection.mutable

import scutil.Functions._
import scutil.Disposable

trait Observing {
	implicit protected val observing	= this
	
	private val connections	= new mutable.ArrayBuffer[Disposable]
	
	// used in Reactive and Signal
	
	private[screact] def observe[T](source:Reactive[_,T], effect:Effect[T]):Disposable = {
		val	target		= new Target(effect, source)
		lazy val connection:Disposable	= Disposable {
			target.dispose()
			connections	-= connection
		}
		connections += connection
		connection
	}
	
	private[screact] def observeOnce[T](source:Reactive[_,T], effect:Effect[T]):Disposable = {
		lazy val connection:Disposable	= observe(source, { value:T =>
			effect(value)
			connection.dispose()
		})
		connection
	}
	
	private[screact] def observeNow[T](source:Signal[T], effect:Effect[T]):Disposable = {
		val connection	= observe(source, effect)
		effect(source.current)
		connection
	}
}
