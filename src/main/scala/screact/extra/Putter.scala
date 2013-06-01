package screact.extra

import scutil.Implicits._

import screact._

object Putter {
	def multiOn[T](value:Signal[T], putters:Iterable[Events[T=>T]]):Events[T]	=
			on(value, sum(putters))
		
	// TODO looks suspiciously like MasterPartial#put
	def on[T](value:Signal[T], puts:Events[T=>T]):Events[T]	=
			puts snapshotWith value map {_ .apply1to2 }
	
	//------------------------------------------------------------------------------
	
	// T=>T with identity forms a monoid and
	// orElse with never forms a monoid so
	// Events[T=>T] with never[T=>T] forms a monoid, too
	
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
			(in foldLeft zero[T])(append)
}
