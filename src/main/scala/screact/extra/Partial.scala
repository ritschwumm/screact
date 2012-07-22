package screact.extra

import scutil.lang._
import scutil.Implicits._

import screact._

object Partial {
	def apply[S,T](s:Signal[S], l:Lens[S,T]):Partial[S,T]	= new MasterPartial[S,T] {
		val master:Signal[S]					= s
		val get:Signal[T]						= s map l.get
		def putter(t:Events[T]):Events[S=>S]	= t map l.putter
	}
	
	def masterDetail[S,T](s:Signal[S], l:Signal[Option[Lens[S,T]]], d:T):Partial[S,T]	= new MasterPartial[S,T] {
		val master:Signal[S]					= s
		val get:Signal[T]						= signal { l.current cata (_ get s.current, d) }
		def putter(t:Events[T]):Events[S=>S]	= t snapshotWith l collect { case (t,Some(l)) => l putter t }
	}
}

trait Partial[S,T] {
	def get:Signal[T]
	def put(t:Events[T]):Events[S]
	def putter(s:Events[T]):Events[S=>S]
}

private trait MasterPartial[S,T] extends Partial[S,T] {
	def master:Signal[S]
	final def put(t:Events[T]):Events[S]	= putter(t) snapshotWith master map { case (f,s) => f(s) }
}
