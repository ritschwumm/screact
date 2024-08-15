package screact.extra

import screact.*

object Putter {
	def multiOn[T](value:Signal[T], putters:Iterable[Events[T=>T]]):Events[T]	=
		on(value, sum(putters))

	def on[T](value:Signal[T], puts:Events[T=>T]):Events[T]	=
		puts.snapshotWith(value) { _(_) }

	//------------------------------------------------------------------------------

	// Endo[T] with identity forms a monoid and
	// orElse with never forms a monoid so
	// Events[Endo[T]] with never[Endo[T]] forms a monoid, too

	def zero[T]:Events[T=>T]	= never[T=>T]

	def append[T](a:Events[T=>T], b:Events[T=>T]):Events[T=>T]	=
		events {
			(a.message, b.message) match {
				case (Some(a),Some(b))	=> Some(a andThen b)
				case (Some(a),None)		=> Some(a)
				case (None,Some(b))		=> Some(b)
				case (None,None)		=> None
			}
		}

	def sum[T](in:Iterable[Events[T=>T]]):Events[T=>T]	=
		in.foldLeft(zero[T])(append)
}
