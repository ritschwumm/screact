package screact.extra

import scutil.Lens

import screact._

// TODO rename this to ReactiveCoState
object Partial {
	def apply[S,T](s:Signal[S], lens:Lens[S,T]):Partial[S,T]	= new Partial[S,T] {
		val get:Signal[T]				= s map lens.get
		def put(t:Events[T]):Events[S]	= t snapshotWith s map { case (t,s) => lens put (s,t) }
	}
}

/** Lens[S,T] seen as S=>CoState[S,T] lifted to reactive values */
trait Partial[S,T] {
	def get:Signal[T]
	def put(s:Events[T]):Events[S]
}
