package screact.swing

import scutil.core.implicits._
import scutil.lang._
import scutil.gui.SwingUtil._

import screact._

object SwingWork {
	def async[T](in:Events[Thunk[T]])(using observing:Observing):Events[T]	=
		emitter[T] doto { out =>
			in observe { input =>
				swingWorker[T](
					input,
					out.emit
				)
			}
		}
}
