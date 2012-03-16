package screact

// NOTE never notifies any dependents
private final class StaticSignal[T](value:T) extends Signal[T] {
	val	cur	= value
	val msg	= None
	def calculate() {}	// msg does not change in here
	def reset() {}		// msg stays None anyways
	// init()			// not necessary, we don't have dependencies
}
