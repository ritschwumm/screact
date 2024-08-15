package screact

import scala.collection.mutable

import scutil.core.implicits.*

private trait Sinks {
	def all:Set[Node]
	def add(node:Node):Unit
	def remove(node:Node):Unit
	def clear():Unit
}

// TODO unused why the warning if not restricted to screact?
private[screact] object NoSinks extends Sinks {
	val all:Set[Node]	= Set.empty
	def add(node:Node):Unit	= {}
	def remove(node:Node):Unit	= {}
	def clear():Unit	= {}
}

private object HasSinks {
	private val sentinel	= new AnyRef
}

private final class HasSinks(cache:SinksCache) extends Sinks {
	private val ids	= new mutable.LongMap[AnyRef]

	def all:Set[Node]	= ids.keySet.toSet.mapFilter(cache.lookup)

	def add(node:Node):Unit	= {
		ids	+= (node.id -> HasSinks.sentinel)
	}

	def remove(node:Node):Unit	= {
		ids	-= node.id
	}

	def clear():Unit	= {
		ids.clear()
	}
}
