package screact

/** Events depending on other Events */
private final class CalculationEvents[T](next: =>Option[T]) extends Events[T] {
	var msg:Option[T]	= None

	def calculate():Unit	= {
		msg = next
	}

	def reset():Unit	= {
		msg = None
	}

	init()
}
