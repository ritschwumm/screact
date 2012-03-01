package screact.extra

import screact._

// NOTE a monad transformer would make sense here
	
/** helpers for the Signal[Option[_]] monad */
object SignalOption {
	def apply[T](delegate:Signal[Option[T]]):SignalOption[T]	= new SignalOption[T](delegate)
}

final class SignalOption[T](delegate:Signal[Option[T]]) {
	def unwrap:Signal[Option[T]]	= delegate
	
	def map[U](func:T=>U):SignalOption[U]					= new SignalOption(signal { delegate.current map { s => func(s) } })
	def flatMap[U](func:T=>SignalOption[U]):SignalOption[U]	= new SignalOption(signal { delegate.current flatMap { s => func(s).unwrap.current } })
	
	def flatten(implicit func:T=>SignalOption[T]):SignalOption[T]	= flatMap(func)
}
