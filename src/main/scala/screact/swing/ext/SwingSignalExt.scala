package screact.swing.ext

import screact.*

final class SwingSignalExt[T](delegate:Signal[T]) extends EdtReactive {
	/** ensure current is called inside the EDT */
	def currentInEdt:T	= readInEdt { decouple { delegate.current } }
}
