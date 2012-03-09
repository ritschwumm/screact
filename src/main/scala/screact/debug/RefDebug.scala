package screact.debug

import java.lang.ref._

/** debugging utility to show when some object is garbage-collected */
class RefDebugger(what:String) {
	val	queue	= new ReferenceQueue[AnyRef]
	var refs	= List[Reference[_]]()
	
	def start() {
		val	thread	= new Thread {
			override def run() {
				work()
			}
		}
		thread.start
	}
	
	def add(target:AnyRef) {
		val	ref	= new  WeakReference(target, queue)
		refs	=	ref :: refs
		println(what + "\t+\t" + refs.size)
	}
	
	private def work() {
		while (true) {
			val	ref	= queue.remove()
			refs	= refs filterNot { _ == ref }
			println(what + "\t-\t" + refs.size)
		}
	}
}
