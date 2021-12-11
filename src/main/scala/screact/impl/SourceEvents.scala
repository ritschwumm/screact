package screact

import scutil.lang.*
import scutil.log.*

/** root of a propagation tree */
private final class SourceEvents[T] extends Events[T] with Logging { outer =>
	var	msg:Option[T]	= None

	/** schedules as an external event */
	def emit(value:T):Unit	= {
		engine schedule thunk { emitImpl(value) }
	}

	private def emitImpl(value:T):Option[Node]	=
		msg match {
			case None	=>
				msg	= Some(value)
				Some(outer)
			case Some(x) =>
				// TODO move logging into the Domain
				ERROR(
					"cannot emit an event twice within the same update cycle",
					origin.toString,
					x.toString,
					value.toString
				)
				None
		}

	// msg does not change in here
	def calculate():Unit	= {}

	def reset():Unit	= {
		msg	= None
	}

	// not necessary, we don't have dependencies
	// init()
}
