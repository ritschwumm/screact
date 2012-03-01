package screact

import scutil.Implicits._

final class Blocker {
	private var state	= 0
	
	def exclusive[T](block: =>T):T	= {
		state	+= 1
		val out	= block
		state	-= 1
		out
	}
	
	def exclusiveCycle[T](block: =>T):T	= {
		state	+= 1
		val out	= block
		Engine schedule {
			state	-= 1
			None
		}
		out
	}
	
	def attempt[T](block: =>T):Option[T]	=
			state == 0 guard block
	
	// def attemptExclusive[T](block: =>T):Option[T]	=
	// 		attempt { exclusive { block } }
}
