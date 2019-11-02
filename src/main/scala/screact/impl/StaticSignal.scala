package screact

/** a Signal never changing its value */
private final class StaticSignal[T](value:T) extends Signal[T] {
	val	cur:T			= value
	val msg:Option[T]	= None

	// msg does not change in here
	def calculate() {}

	// msg stays None anyway
	def reset() {}

	// not necessary, we don't have dependencies
	// init()
}
