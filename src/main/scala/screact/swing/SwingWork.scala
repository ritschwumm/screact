package screact.swing

import scutil.core.implicits.*
import scutil.lang.*
import scutil.gui.SwingUtil.*

import screact.*

object SwingWork {
	def async[T](in:Events[Thunk[T]])(using observing:Observing):Events[T]	=
		emitter[T]
		.doto { out =>
			in observe { input =>
				swingWorker[T](
					input,
					out.emit
				)
			}
		}
		.events
}
