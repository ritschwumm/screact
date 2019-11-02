package screact

import java.lang.ref.WeakReference

import scala.collection.immutable

/** saves weak references by only keeping one per node */
private final class SinksCache {
	private var	nodes	= immutable.LongMap.empty[WeakReference[Node]]

	private var	nextId	= 0L

	def register(node:Node):Long = {
		while (nodes contains nextId) { nextId += 1 }
		nodes	+= (nextId -> new WeakReference[Node](node))
		nextId
	}

	def lookup(id:Long):Option[Node]	= {
		nodes get id flatMap { ref => Option(ref.get) }
	}

	def gc():Unit	= {
		nodes	= nodes filterNot { _._2.get eq null }
	}
}
