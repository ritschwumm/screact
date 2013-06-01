package screact

import screact.swing.ext._

package object swing {
	implicit def toSwingSignalExt[T](delegate:Signal[T]):SwingSignalExt[T]	=
			new SwingSignalExt[T](delegate)
		
	implicit def toSwingCellExt[T](delegate:Cell[T]):SwingCellExt[T]	=
			new SwingCellExt[T](delegate)
		
	implicit def toSwingEmitterExt[T](delegate:Emitter[T]):SwingEmitterExt[T]	=
			new SwingEmitterExt[T](delegate)
}
