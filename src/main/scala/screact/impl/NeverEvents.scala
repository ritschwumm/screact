package screact

/** an Events never emitting anything */
private final class NeverEvents[T] extends Events[T] {
	val msg:Option[T]	= None
	
	// msg does not change in here
	def calculate() {}
	
	// msg stays None anyway
	def reset() {}
	
	// not necessary, we don't have dependencies
	// init()
}  
