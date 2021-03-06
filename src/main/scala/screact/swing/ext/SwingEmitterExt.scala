package screact.swing.ext

import screact._

final class SwingEmitterExt[T](delegate:Emitter[T]) extends EdtReactive {
	/** ensure emit is called inside the EDT */
	def emitInEdt(value:T):Unit	= writeInEdt { delegate emit value }
}
