package screact.extra

import scutil.Lens

import screact._

object Partial {
	def apply[S,T](s:Signal[S], l:Lens[S,T]):Partial[S,T]	= new Partial[S,T] {
		val get:Signal[T]						= s map l.get
		def put(t:Events[T]):Events[S]			= putter(t) snapshotWith s map { case (f,s) => f(s) }
		def putter(t:Events[T]):Events[S=>S]	= t map l.putter
	}
	
	def masterDetail[S,T](s:Signal[S], l:Signal[Option[Lens[S,T]]], d:T):Partial[S,T]	= new Partial[S,T] {
		val get:Signal[T]						= signal { l.current map { _ get s.current } getOrElse d }
		def put(t:Events[T]):Events[S]			= putter(t) snapshotWith s map { case (f,s) => f(s) }
		def putter(t:Events[T]):Events[S=>S]	= t snapshotWith l collect { case (t,Some(l)) => l putter t }
	}
}

trait Partial[S,T] {
	def get:Signal[T]
	def put(s:Events[T]):Events[S]	// = putter(t) snapshotWith s map { case (f,s) => f(s) }
	def putter(s:Events[T]):Events[S=>S]
}
