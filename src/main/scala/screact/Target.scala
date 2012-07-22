package screact

import scutil.lang._

import screact.Updates._

/** A final target for events emitted by an Event. Targets always get notified after all other Nodes. */
private final class Target[T](effect:Effect[T], source:Reactive[_,T]) extends Node with Disposable {
	def sinks	= NoSinks
	val	rank	= Integer.MAX_VALUE
	
	def update():Update	= {
		source.msg foreach effect
		Unchanged
	}
	
	def reset() {}
	
	def dispose() {
		source.sinks remove this
	}
	
	source.sinks add this
}
