package screact

import scala.language.implicitConversions

import scutil.lang.*

object Emitter {
	implicit def asEvents[T](it:Emitter[T]):Events[T]	= it.events
}

/** An Emitter is a source for Events and can trigger an update cycle in the Engine */
trait Emitter[T] extends AutoCloseable { outer =>
	val events:Events[T]
	def emit(value:T):Unit

	final def xmap[S](bijection:Bijection[S,T]):Emitter[S]	= new Emitter[S] {
		val events	= outer.events map bijection.set
		def emit(it:S):Unit	= { outer emit (bijection get it) }
	}

	def close():Unit	= {
		events.close()
	}
}
