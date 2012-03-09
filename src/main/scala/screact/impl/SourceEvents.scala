package screact

import scutil.Functions._

private [screact] class SourceEvents[T] extends Events[T] { outer =>
	var	msg:Option[T]	= None
	
	
	def emit(value:T) {
		Engine scheduleSingle thunk { emitImpl(value) }
	}  
	
	private def emitImpl(value:T):Option[Node]	= {
		require(msg.isEmpty,	
				"cannot emit an event twice within the same update cycle" +
				" for: " + origin + 
				" message: " + msg.get)
		
		msg	= Some(value)
		Some(outer)
	}
	
	def calculate() {}	// msg does not change in here
	
	def reset() { 
		msg	= None
		// last = "" 
	}
	
	// init()			// not necessary, we don't have dependencies
}
