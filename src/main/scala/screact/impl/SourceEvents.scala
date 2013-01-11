package screact

import scutil.lang._
import scutil.log._

// BETTER aggregate logging

private [screact] class SourceEvents[T] extends Events[T] with Logging { outer =>
	var	msg:Option[T]	= None
	
	/** schedules as an external event */
	def emit(value:T) {
		engine schedule thunk { emitImpl(value) }
	}  
	
	private def emitImpl(value:T):Option[Node]	= {
		if (msg.isEmpty) {
			msg	= Some(value)
			Some(outer)
		}
		else {
			ERROR(
					"cannot emit an event twice within the same update cycle", 
					origin, 
					msg.get,
					value)
			None
		}
	}
	
	def calculate() {}	// msg does not change in here
	
	def reset() { 
		msg	= None
	}
	
	// init()			// not necessary, we don't have dependencies
}
