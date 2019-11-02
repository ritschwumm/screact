package screact.extra

import screact._

object Feedback {
	def localSignal[T,S](initial:S)(mkEditor:Signal[S]=>(T,Events[S]))(implicit ob:Observing):(T,Signal[S])	= {
		val state				= cell[S](initial)
		val (editor,changes)	= mkEditor(state)
		changes observe state.set
		(editor, state)
	}

	def localEvents[T,S](mkEditor:Events[S]=>(T,Events[S]))(implicit ob:Observing):(T,Events[S])	= {
		val state				= emitter[S]
		val (editor,changes)	= mkEditor(state)
		changes observe state.emit
		(editor, state)
	}
}
