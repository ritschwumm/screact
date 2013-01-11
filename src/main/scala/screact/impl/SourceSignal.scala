package screact

import scutil.lang._
import scutil.log._

// BETTER aggregate logging

private [screact] class SourceSignal[T](initial:T) extends Signal[T] with Logging { outer =>
	var cur:T			= initial
	var msg:Option[T]	= None
	
	/** schedules as an external event */
	def set(value:T) {
		engine schedule thunk { setImpl(value) }
	}  
	
	private def setImpl(value:T):Option[Node]	= {
		if (value != cur) {
			if (msg.isEmpty) {
				cur	= value
				msg	= Some(value)
				Some(outer)
			}
			else {
				ERROR(
						"cannot set a signal twice within the same update cycle",
						origin,
						msg.get,
						cur,
						value)
				None
			}
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
