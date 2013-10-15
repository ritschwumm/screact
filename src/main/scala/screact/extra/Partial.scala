package screact.extra

import scutil.lang._
import scutil.Implicits._
import scutil.lens._

import screact._

object Partial {
	/** edit a total part of an object */
	def apply[S,T](s:Signal[S], l:TLens[S,T]):Partial[S,T]	=
			new Partial[S,T] {
				val get:Signal[T]						= s map l.get
				def put(t:Events[T]):Events[S]			= Putter on (s, putter(t))
				def putter(t:Events[T]):Events[Endo[S]]	= t map l.putter
			}
	
	/**
	edit a selected part of an object:
	the lens signal selects which poart is edited
	and might be None if no detail is selected for editing.
	in these cases the editor component is fed with default values
	and it's change events are ignored.
	*/
	def masterDetail[S,T](s:Signal[S], l:Signal[Option[TLens[S,T]]], d:T):Partial[S,T]	=
			new Partial[S,T] {
				val get:Signal[T]						= signal { l.current cata (d, _ get s.current) }
				def put(t:Events[T]):Events[S]			= Putter on (s, putter(t))
				def putter(t:Events[T]):Events[Endo[S]]	= t snapshot l collect { case (t,Some(l)) => l putter t }
			}
}

/**
helper to edit some part of an object:
provides an input Signal for an editor component and can 
turn the editor's output Events into changes of the complete object. 
*/
trait Partial[S,T] {
	def get:Signal[T]
	def put(t:Events[T]):Events[S]
	def putter(s:Events[T]):Events[Endo[S]]
}
