package screact

import scutil.lang._
import scutil.log._

/** root of a propagation tree */
private final class SourceEvents[T] extends Events[T] with Logging { outer =>
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
			// TODO move logging into the Domain
			ERROR(
				"cannot emit an event twice within the same update cycle",
				origin,
				msg.get,
				value
			)
			None
		}
	}
	
	// msg does not change in here
	def calculate() {}
	
	def reset() {
		msg	= None
	}
	
	// not necessary, we don't have dependencies
	// init()
}
