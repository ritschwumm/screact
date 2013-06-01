package screact.swing

import scutil.lang._
import scutil.Implicits._
import scutil.gui.SwingUtil._
import scutil.tried._

import screact._

object SwingWork {
	def async[T](in:Events[Thunk[T]])(implicit observing:Observing):Events[T]	=
			emitter[T] doto { out =>
				in observe { input =>
					swingWorker[T](
						input,
						out.emit
					)
				}
			}
			
	def asyncException[T](in:Events[Thunk[T]])(implicit observing:Observing):Events[Tried[Exception,T]]	=
			emitter[Tried[Exception,T]] doto { out =>
				in observe { input =>
					swingWorkerException[T](
						input,
						out.emit
					)
				}
			}
}
