package screact.swing

import scutil.gui.SwingUtil.*

import screact.*

// TODO wartremover fix overloading errors somehow

extension[T](delegate:Signal[T])  {
	/** ensure current is called inside the EDT */
	def currentInEdt:T	= readInEdt { decouple { delegate.current } }
}

extension[T](delegate:Cell[T]) {
	/** ensure current is called inside the EDT */
	def currentInEdt:T			= readInEdt { decouple { delegate.current } }

	/** ensure set is called inside the EDT */
	def setInEdt(value:T):Unit	= writeInEdt { delegate set value }
}

extension[T](delegate:Emitter[T]) {
	/** ensure emit is called inside the EDT */
	def emitInEdt(value:T):Unit	= writeInEdt { delegate emit value }
}

private def readInEdt[T](block: =>T):T	=
	if (insideEdt)	block
	else			edt{ block }()

private def writeInEdt[T](block: =>Unit):Unit	=
	if (insideEdt)	block
	else			edt { block }
