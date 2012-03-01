package screact

import scutil.Functions._
import scutil.Disposable

import screact.Updates._

private[screact] final class Target[T](effect:Effect[T], source:Reactive[_,T]) extends Node with Disposable {
	// dependents stays empty all the time
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
