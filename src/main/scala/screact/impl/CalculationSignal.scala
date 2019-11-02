package screact

/** Signal depending on other Signals */
private final class CalculationSignal[T](next: =>T) extends Signal[T] {
	var	cur:T			= _
	var msg:Option[T]	= None

	def calculate():Unit	= {
		val	old	= cur
		cur	= next
		if (cur == old)	return
		msg	= Some(cur)
	}

	def reset():Unit	= {
		msg = None
	}

	init()
	// important, because init will set a message in calculate
	msg	= None
}
