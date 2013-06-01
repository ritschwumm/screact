package screact.swing.ext

import scutil.gui.SwingUtil._

import screact._

final class SwingCellExt[T](delegate:Cell[T]) extends EdtReactive {
	/** ensure current is called inside the EDT */
	def currentInEdt:T			= readInEdt { decouple { delegate.current } }
	
	/** ensure set is called inside the EDT */
	def setInEdt(value:T):Unit	= writeInEdt { delegate set value }
}
