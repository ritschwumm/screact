package screact

import java.lang.ref.WeakReference

import scala.collection.mutable

/** saves weak references by only keeping one per node */
object NodeSet {
	val	nodes	= new mutable.HashMap[Long,WeakReference[Node]]	// TODO maybe use a LongMap instead
	
	var	id	= 0L
	
	def register(node:Node):Long = {
		val	cur	= id
		id	= id + 1
		nodes	+= (cur -> new WeakReference[Node](node))
		cur
	}
	
	def lookup(id:Long):Option[Node]	= {
		nodes get id flatMap { ref => Option(ref.get) }
	}
	
	def gc() {
		val	obsoleteKeys:Iterable[Long]	= nodes collect { case (id,ref) if ref.get == null => id }
		nodes	--= obsoleteKeys
	}
	
	def compact() {
		id	= 0
		val	newNodes	= nodes.values flatMap { ref =>
			Option(ref.get) map { node =>
				val cur	= id
				id	= id+1
				(cur, ref)
			}
		}
		nodes.clear()
		nodes	++= newNodes
	}
}

final class NodeSet {
	val ids	= new mutable.HashSet[Long]
	
	def add(node:Node) {
		ids	+= node.id
	}
	def remove(node:Node) {
		ids	-= node.id
	}
	def clear() {
		ids.clear()
	}
	def all:Set[Node]	= ids flatMap { NodeSet.lookup } toSet;
}
