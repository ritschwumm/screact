package screact.extra

import scutil.lang._
import scutil.Implicits._
import scutil.lens._

import screact._

object Partial {
	def apply[S,T](s:Signal[S], l:TLens[S,T]):Partial[S,T]	= new Partial[S,T] {
		val master:Signal[S]					= s
		val get:Signal[T]						= s map l.get
		 def put(t:Events[T]):Events[S]			= (putter(t) snapshotWith master) { _(_) }
		def putter(t:Events[T]):Events[Endo[S]]	= t map l.putter
	}
	
	def masterDetail[S,T](s:Signal[S], l:Signal[Option[TLens[S,T]]], d:T):Partial[S,T]	= new Partial[S,T] {
		val master:Signal[S]					= s
		val get:Signal[T]						= signal { l.current cataSwapped (_ get s.current, d) }
		def put(t:Events[T]):Events[S]			= (putter(t) snapshotWith master) { _(_) }
		def putter(t:Events[T]):Events[Endo[S]]	= t snapshot l collect { case (t,Some(l)) => l putter t }
	}
}

trait Partial[S,T] {
	def get:Signal[T]
	def put(t:Events[T]):Events[S]
	def putter(s:Events[T]):Events[Endo[S]]
}
