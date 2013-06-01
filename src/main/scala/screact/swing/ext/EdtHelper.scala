package screact.swing.ext

import scutil.gui.SwingUtil._

protected trait EdtHelper {
	def doInEdt[T](block: =>T):T	=
			if (insideEDT)	block
			else			edtWait { block }
}
