package screact

// NOTE never notifies any dependents
private final class NeverEvents[T] extends Events[T] {
	val	msg:Option[T]	= None
	def calculate() {}
	def reset() 	{}	// msg stays None anyway
	// init()			// not necessary, we don't have dependencies
}  
