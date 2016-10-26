package screact

import scala.collection.mutable

import scutil.base.implicits._

private trait Sinks {
	def all:Set[Node]
	def add(node:Node):Unit
	def remove(node:Node):Unit
	def clear():Unit
}

private final object NoSinks extends Sinks {
	val all:Set[Node]	= Set.empty
	def add(node:Node) {}
	def remove(node:Node) {}
	def clear() {}
}

private object HasSinks {
	private val sentinel	= new AnyRef
}

private final class HasSinks(cache:SinksCache) extends Sinks {
	private val ids	= new mutable.LongMap[AnyRef]
	
	def all:Set[Node]	= ids.keySet.toSet collapseMap cache.lookup
	
	def add(node:Node) {
		ids	+= (node.id -> HasSinks.sentinel)
	}
	
	def remove(node:Node) {
		ids	-= node.id
	}
	
	def clear() {
		ids.clear()
	}
}
