package screact

/*
def never[T]:Events[T]				= events { None }
*/

// NOTE never notifies any dependents
private final class NeverEvents[T] extends Events[T] {
	val	msg:Option[T]	= None
	def calculate() {}
	def reset() 	{}		// msg stays None anyways
	// init()			// not necessary, we don't have dependencies
}  
