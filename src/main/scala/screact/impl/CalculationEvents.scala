package screact

private final class CalculationEvents[T](next: =>Option[T]) extends Events[T] {
	var msg:Option[T]	= None
	def calculate() { msg = next }
	def reset() 	{ msg = None }
	init()
}    
