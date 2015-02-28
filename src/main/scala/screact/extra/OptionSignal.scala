package screact.extra

import scutil.lang._

import screact._

// NOTE a monad transformer would make sense here
	
/** helpers for the Signal[Option[_]] monad */
object OptionSignal {
	def apply[T](delegate:Signal[Option[T]]):OptionSignal[T]	= new OptionSignal[T](delegate)
}

final class OptionSignal[T](private val delegate:Signal[Option[T]]) { self =>
	def unwrap:Signal[Option[T]]	= delegate
	
	def map[U](func:T=>U):OptionSignal[U]	=
			new OptionSignal(signal {
				delegate.current map func
			})
		
	def flatMap[U](func:T=>OptionSignal[U]):OptionSignal[U]	=
			new OptionSignal(signal {
				delegate.current flatMap { s =>
					func(s).unwrap.current
				}
			})
	
	def flatten(implicit ev:T=>OptionSignal[T]):OptionSignal[T]	=
			flatMap(ev)
		
	def filter(pred:Predicate[T]):OptionSignal[T]	=
			new OptionSignal(signal {
				delegate.current filter pred
			})
			
	def orElse(that:OptionSignal[T]):OptionSignal[T]	=
			new OptionSignal[T](signal {
				self.delegate.current orElse that.delegate.current
			})
}
