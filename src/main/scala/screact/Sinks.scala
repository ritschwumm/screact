package screact

import java.lang.ref.WeakReference

import scala.collection.mutable
import scala.collection.immutable

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
	// BETTER check if a LongMap makes sense here
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

/** saves weak references by only keeping one per node */
private class SinksCache {
	var	nodes	= immutable.LongMap.empty[WeakReference[Node]]
	
	var	nextId	= 0L
	
	def register(node:Node):Long = {
		while (nodes contains nextId) { nextId += 1 }
		nodes	+= (nextId -> new WeakReference[Node](node))
		nextId
	}
	
	def lookup(id:Long):Option[Node]	= {
		nodes get id flatMap { ref => Option(ref.get) }
	}
	
	def gc() {
		nodes	= nodes filterNot { _._2.get == null }
	}
}
