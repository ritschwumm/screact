package screact

private final class AlwaysEvents[T](value:T) extends Events[T] {
	// NOTE this fires in the cycle it is created
	var	msg:Option[T]	= Some(value)
	def calculate() {}
	def reset() 	{}
	// init()		// not necessary, we don't have dependencies
}
