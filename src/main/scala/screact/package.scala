import scutil.lang._

package object screact {
	type Scheduled	= Thunk[Option[Node]]

	def static[T](value:T):Signal[T]			= new StaticSignal(value)
	def signal[T](value: =>T):Signal[T]			= new CalculationSignal(value)
	def cell[T](value:T):Cell[T]				= new Cell[T] {
		val	signal:SourceSignal[T]	= new SourceSignal[T](value)
		def set(it:T):Unit	= { signal set it }
	}

	def never[T]:Events[T]						= new NeverEvents
	def events[T](value: =>Option[T]):Events[T]	= new CalculationEvents(value)
	def emitter[T]:Emitter[T]					= new Emitter[T] {
		val events:SourceEvents[T]	= new SourceEvents[T]
		def emit(it:T):Unit	= { events emit it }
	}

	def decouple[T](value: =>T):T	= Engine.access withoutReader value
}
