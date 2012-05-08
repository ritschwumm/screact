package screact

import scutil.Functions._

private [screact] class SourceSignal[T](initial:T) extends Signal[T] { outer =>
	var cur:T			= initial
	var msg:Option[T]	= None
	
	/** schedules as an external event */
	def set(value:T) {
		engine schedule thunk { setImpl(value) }
	}  
	
	private def setImpl(value:T):Option[Node]	= {
		if (value != cur) {
			require(msg.isEmpty,	
					"cannot set a signal twice within the same update cycle"	+
					" for: " + origin + 
					" message: " + msg.get +
					" was: " + cur +
					" now: " + value)
				
			cur	= value
			msg	= Some(value)
			Some(outer)
		}
		else {
			None
		}
	}
	
	def calculate() {}	// msg does not change in here
	
	def reset() {
		msg	= None
	}
	
	// init()			// not necessary, we don't have dependencies
}
