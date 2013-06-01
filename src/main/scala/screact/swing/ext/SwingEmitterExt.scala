package screact.swing.ext

import scutil.gui.SwingUtil._

import screact._

final class SwingEmitterExt[T](delegate:Emitter[T]) extends EdtHelper {
	/** ensure emit is called inside the EDT */
	def emitInEdt(value:T):Unit	= doInEdt { delegate emit value }
}
