package screact

import scutil.lang._
import scutil.log._

/** root of a propagation tree */
private final class SourceSignal[T](initial:T) extends Signal[T] with Logging { outer =>
	var cur:T			= initial
	var msg:Option[T]	= None

	/** schedules as an external event */
	def set(value:T) {
		engine schedule thunk { setImpl(value) }
	}

	private def setImpl(value:T):Option[Node]	=
			if (value != cur) {
				msg match {
					case None	=>
						cur	= value
						msg	= Some(value)
						Some(outer)
					case Some(x)	=>
						// TODO move logging into the Domain
						ERROR(
							"cannot set a signal twice within the same update cycle",
							origin.toString,
							x.toString,
							cur.toString,
							value.toString
						)
						None
				}
			}
			else {
				None
			}

	// msg does not change in here
	def calculate() {}

	def reset() {
		msg	= None
	}

	// not necessary, we don't have dependencies
	// init()
}
