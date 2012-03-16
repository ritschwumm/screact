package screact

private final class OnceEvents[T](value:T) extends Events[T] {
	// NOTE this fires in the same update cycle it is created
	var	msg:Option[T]	= Some(value)
	def calculate()	{ msg	= None }
	def reset()		{ msg	= None }
	// init()		// not necessary, we don't have dependencies
}  
