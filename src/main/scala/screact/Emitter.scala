package screact

import scutil.lang._

object Emitter {
	implicit def asEvents[T](it:Emitter[T]):Events[T]	= it.events
}

/** An Emitter is a source for Events and can trigger an update cycle in the Engine */
trait Emitter[T] extends Disposable { outer =>
	val events:Events[T]
	def emit(value:T):Unit
	
	final def xmap[S](bijection:Bijection[S,T]):Emitter[S]	= new Emitter[S] {
		val events	= outer.events map bijection.set
		def emit(it:S) { outer emit (bijection get it) }
	}
	
	def dispose() {
		events.dispose()
	}
}
