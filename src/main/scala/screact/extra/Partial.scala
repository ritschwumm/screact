package screact.extra

import scutil.Lens

import screact._

object Partial {
	def apply[S,T](s:Signal[S], lens:Lens[S,T]):Partial[S,T]	= new Partial[S,T] {
		val get:Signal[T]				= s map lens.get
		def put(t:Events[T]):Events[S]	= t snapshot2 s map { case (t,s) => lens put (s,t) }
	}
}

/** curried Lens lifted to Reactive and applied to a Signal */
trait Partial[S,T] {
	def get:Signal[T]
	def put(s:Events[T]):Events[S]
}
