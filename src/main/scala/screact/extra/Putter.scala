package screact.extra

import scutil.lang._

import screact._

object Putter {
	def multiOn[T](value:Signal[T], putters:Iterable[Events[Endo[T]]]):Events[T]	=
		on(value, sum(putters))

	def on[T](value:Signal[T], puts:Events[Endo[T]]):Events[T]	=
		(puts snapshotWith value) { _(_) }

	//------------------------------------------------------------------------------

	// Endo[T] with identity forms a monoid and
	// orElse with never forms a monoid so
	// Events[Endo[T]] with never[Endo[T]] forms a monoid, too

	def zero[T]:Events[Endo[T]]	= never[Endo[T]]

	def append[T](a:Events[Endo[T]], b:Events[Endo[T]]):Events[Endo[T]]	=
		events {
			(a.message, b.message) match {
				case (Some(a),Some(b))	=> Some(a andThen b)
				case (Some(a),None)		=> Some(a)
				case (None,Some(b))		=> Some(b)
				case (None,None)		=> None
			}
		}

	def sum[T](in:Iterable[Events[Endo[T]]]):Events[Endo[T]]	=
		(in foldLeft zero[T])(append)
}
