package screact

import scutil.Functions._
import scutil.Disposable

import screact.Updates._

/** A final target for events emitted by an Event. Targets always get notified after all other Nodes. */
private final class Target[T](effect:Effect[T], source:Reactive[_,T]) extends Node with Disposable {
	// dependents stays empty at all times
	def dependents:Iterable[Node]	= Nil
	def addDependent(node:Node)		{}
	def removeDependent(node:Node)	{}	
	
	val	rank	= Integer.MAX_VALUE
	
	def update():Update	= {
		source.msg foreach effect
		Unchanged
	}
	
	def reset() {}
	
	def dispose() {
		source removeDependent this
	}
	
	source addDependent this
}
