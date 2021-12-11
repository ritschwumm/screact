package screact.swing.ext

import scutil.gui.SwingUtil.*

protected trait EdtReactive {
	def readInEdt[T](block: =>T):T	=
		if (insideEdt)	block
		else			edt{ block }()

	def writeInEdt[T](block: =>Unit):Unit	=
		if (insideEdt)	block
		else			edt { block }
}
