package screact

private final class CalculationSignal[T](next: =>T) extends Signal[T] {
	var	cur:T			= _
	var msg:Option[T]	= None
	def calculate() {
		val	old	= cur
		cur	= next
		if (cur == old)	return
		msg	= Some(cur)
	}
	def reset() { 
		msg = None 
	}
	init()
	// important, because init will set a message in calculate
	msg	= None
}
