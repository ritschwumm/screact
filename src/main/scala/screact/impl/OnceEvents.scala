package screact

/*
def once[T](value:T):Events[T]	= { 
	var first = true
	events { 
		if (first)	{ first = false;	Some(value) }
		else		{ 					None		}
	}
}
*/

// NOTE as soon as first becomes false, nobody needs to depend on this any more
private final class OnceEvents[T](value:T) extends Events[T] {
	var	msg:Option[T]	= Some(value)	// TODO hack: has already fired before calculation
	def calculate()	{ msg	= None }
	def reset()		{ msg	= None }
	// init()		// not necessary, we don't have dependencies
}  
