import scutil.lang._

package object screact {
	type Scheduled	= Thunk[Option[Node]]
	
	def static[T](value:T):Signal[T]				= new StaticSignal(value)
	def signal[T](value: =>T):Signal[T]				= new CalculationSignal(value)
	def cell[T](value:T):Cell[T]					= new Cell[T] {
		val	signal	= new SourceSignal[T](value)
		def set(it:T) { signal set it }
	}
	
	def never[T]:Events[T]							= new NeverEvents
	def once[T](value:T):Events[T]					= new OnceEvents(value)
	def always[T](value:T):Events[T]				= new AlwaysEvents(value)
	def events[T](value: =>Option[T]):Events[T]		= new CalculationEvents(value)
	def emitter[T]:Emitter[T]						= new Emitter[T] {
		val events	= new SourceEvents[T]
		def emit(it:T) { events emit it }
	}
	
	def decouple[T](value: =>T):T					= Engine.access withoutReader value
}
