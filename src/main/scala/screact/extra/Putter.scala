package screact.extra

import screact._

object Putter {
	def onMany[T](value:Signal[T], putters:Iterable[Events[T=>T]]):Events[T]	=
			on(value, sum(putters))
		
	// TODO looks like Partial#put
	def on[T](value:Signal[T], putter:Events[T=>T]):Events[T]	=
			putter snapshotWith value map { case (f,v) => f(v) } 
	
	//------------------------------------------------------------------------------
	
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
