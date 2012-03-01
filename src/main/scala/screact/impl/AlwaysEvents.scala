package screact

/*
def always[T](value:T):Events[T]	= events { Some(value) }
*/

private final class AlwaysEvents[T](value:T) extends Events[T] {
	var	msg:Option[T]	= Some(value)	// TODO hack
	def calculate() {}
	def reset() 	{}
	// init()		// not necessary, we don't have dependencies
}
