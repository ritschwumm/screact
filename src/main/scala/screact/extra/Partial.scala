package screact.extra

import scutil.core.implicits._
import scutil.lang._

import screact._

object Partial {
	/** edit a total part of an object */
	def apply[S,T](master:Signal[S], select:Lens[S,T]):Partial[S,T]	=
		new Partial[S,T] {
			def container:Signal[S]	= master

			val get:Signal[T]	=
				master map select.get

			def putter(detail:Events[T]):Events[S=>S]	=
				detail map select.set
		}

	/**
	edit a selected part of an object:
	the lens signal selects which part is edited
	and might be None if no detail is selected for editing.
	in these cases the editor component is fed with default values
	and its change events are ignored.
	*/
	def masterDetail[S,T](master:Signal[S], select:Signal[Option[Lens[S,T]]], default:T):Partial[S,T]	=
		new Partial[S,T] {
			def container:Signal[S]	= master

			val get:Signal[T]	=
				signal {
					select.current.cata(default, _ get master.current)
				}

			def putter(detail:Events[T]):Events[S=>S]	=
				detail snapshot select collect {
					case (detail, Some(view)) => view set detail
				}
		}

	/*
	// edit a maybe-existing part of an object
	// NOTE PLens is now Optional
	def partial[S,T](master:Signal[S], select:PLens[S,T], default:T):Partial[S,T]	=
		new Partial[S,T] {
			def container:Signal[S]	= master

			val get:Signal[T]	=
				master map { it => select get it getOrElse default }

			// NOTE this emits events that don't change anything
			def putter(detail:Events[T]):Events[Endo[S]]	=
				detail map { dv => (select putter dv).toEndo }
		}
	*/
}

/**
helper to edit some part of an object:
provides an input Signal for an editor component and can
turn the editor's output Events into changes of the complete object.
*/
trait Partial[S,T] {
	def container:Signal[S]

	def get:Signal[T]

	def putter(detail:Events[T]):Events[S=>S]

	def put(detail:Events[T]):Events[S]	=
			Putter.on(container, putter(detail))
}
