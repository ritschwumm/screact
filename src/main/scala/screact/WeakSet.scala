/*
package screact

trait WeakSet[T] {
	def all:Set[T]
	def add(item:T):Unit
	def remove(item:T):Unit
	def clear():Unit
}

// BETTER use Unit instead of AnyRef
final class JclWeakSet[T] {
	import java.util.WeakHashMap
	import scala.collection.JavaConversions

	private val	map	= new WeakHashMap[T,AnyRef]
	
	def all:Set[T]	= JavaConversions.asSet(map.keySet).toSet
	
	def add(item:T) {
		map put (item, null)
	}
	
	def remove(item:T) {
		map remove item
	}
	
	def clear() {
		map.clear()
	}
}
	
// NOTE this is a lot faster than using a WeakHashMap
final class ListWeakSet[T] {	
	import java.lang.ref.WeakReference
	
	private var	entries	= List[WeakReference[T]]()
	
	def all:Set[T]	= (entries map { _.get } filterNot { _ == null }).toSet
	
	def add(item:T) {
		entries	= new WeakReference(item) :: (entries filterNot { _.get == item })
	}
	
	def remove(item:T) {
		entries	= entries filterNot { ref => val candidate = ref.get; candidate == null || candidate == item }
	}
	
	def clear() {
		entries foreach { _.clear() }
		entries	= Nil
	}
}

// TODO rethink this
final class ArrayBufferWeakSet[T] {	
	import java.lang.ref.WeakReference
	import scala.collection.mutable.ArrayBuffer
	
	private val	entries	= ArrayBuffer[WeakReference[T]]()
	private var access	= 0
	
	def all:Set[T]	= (entries.view filterNot { _ == null } map { _.get } filterNot { _ == null }).toSet
	
	def add(item:T) {
		entries	+= new WeakReference(item)
	}
	
	def remove(item:T) {
		entries.indices foreach { index => 
			val entry = entries(index)
			if (entry != null) {
				val	ref	= entry.get
				if (ref == item || ref == null) {
					entries(index)	= null
				}
			}
		}
		// TODO check compaction
		access	+= 1
		if (access > 1000) {
			val	newEntries	= entries filterNot { it => it == null || it.get == null }
			entries.clear()
			entries	++= newEntries
			access	= 0
		}
	}
	
	def clear() {
		entries.indices foreach { index => 
			val	entry	= entries(index)
			if (entry != null) {
				entries(index)	= null
			}
		}
		entries.clear()
	}
}

final class UnWeakSet[T] {	
	import scala.collection.mutable.ArrayBuffer
	
	private var	entries	= ArrayBuffer[T]()
	
	def all:Set[T]	= entries.toSet
	
	def add(item:T) {
		entries += item
	}
	
	def remove(item:T) {
		entries	-= item
	}
	
	def clear() {
		entries.clear()
	}
}
*/
