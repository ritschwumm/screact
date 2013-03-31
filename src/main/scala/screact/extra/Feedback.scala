package screact.extra

import screact._

object Feedback {
	def localState[T,S](initial:S)(mkEditor:Signal[S]=>(T,Events[S]))(implicit ob:Observing):(T,Signal[S])	= {
		val state				= cell[S](initial)
		val (editor,changes)	= mkEditor(state)
		changes observe state.set
		(editor, state)
	} 
}
