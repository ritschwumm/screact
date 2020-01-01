package screact.swing.ext

import scutil.gui.SwingUtil._

protected trait EdtReactive {
	def readInEdt[T](block: =>T):T	=
		if (insideEDT)	block
		else			edtWait { block }

	def writeInEdt[T](block: =>Unit):Unit	=
		if (insideEDT)	block
		else			edt { block }
}
