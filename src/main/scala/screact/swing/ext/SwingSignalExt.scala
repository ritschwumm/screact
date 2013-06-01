package screact.swing.ext

import scutil.gui.SwingUtil._

import screact._

final class SwingSignalExt[T](delegate:Signal[T]) extends EdtHelper {
	/** ensure current is called inside the EDT */
	def currentInEdt:T	= doInEdt { decouple { delegate.current } }
}
