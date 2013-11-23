package screact

import scala.collection.mutable

trait Sinks {
	def all:Set[Node]
	def add(node:Node):Unit
	def remove(node:Node):Unit
	def clear():Unit
}

final object NoSinks extends Sinks {
	val all:Set[Node]	= Set.empty
	def add(node:Node) {}
	def remove(node:Node) {}
	def clear() {}
}

private final class HasSinks(cache:SinksCache) extends Sinks {
	// BETTER check whether a LongMap makes sense here
	private val ids	= new mutable.HashSet[Long]
	
	def all:Set[Node]	= ids flatMap cache.lookup toSet;
	
	def add(node:Node) {
		ids	+= node.id
	}
	
	def remove(node:Node) {
		ids	-= node.id
	}
	
	def clear() {
		ids.clear()
	}
}
