package screact.extra

import scutil.base.implicits._

/** a simple semaphore */
final class Blocker {
	private var state	= 0

	def exclusive[T](block: =>T):T	= {
		state	+= 1
		val out	= block
		state	-= 1
		out
	}

	def attempt[T](block: =>T):Option[T]	=
			state == 0 option block

	// def attemptExclusive[T](block: =>T):Option[T]	=
	// 		attempt { exclusive { block } }
}
