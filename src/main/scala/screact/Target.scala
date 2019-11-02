package screact

import scutil.lang._
import scutil.log._

/** A final target for events emitted by an Event. Targets always get notified after all other Nodes. */
private final class Target[T](effect:Effect[T], source:Reactive[_,T]) extends Node with Disposable with Logging {
	val sinks	= NoSinks
	val	rank	= Integer.MAX_VALUE

	def update():Update	= {
		try {
			source.msg foreach effect
		}
		catch { case e:Exception	=>
			// TODO move logging into the Domain
			ERROR("update failed", this.toString, e)
		}
		Unchanged
	}

	// TODO deal with overflows
	private [screact] def pushDown(rank:Int):Unit	= {}

	def reset():Unit	= {}

	def dispose():Unit	= {
		source.sinks remove this
	}

	source.sinks add this
}
