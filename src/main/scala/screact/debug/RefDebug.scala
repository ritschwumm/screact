package screact.debug

import java.lang.ref.*

/** debugging utility to show when some object is garbage-collected */
class RefDebugger(what:String) {
	val	queue	= new ReferenceQueue[AnyRef]
	var refs	= List[Reference[?]]()

	def start():Unit	= {
		val	thread	= new Thread {
			override def run():Unit	= {
				work()
			}
		}
		thread.start
	}

	def add(target:AnyRef):Unit	= {
		val	ref	= new  WeakReference(target, queue)
		refs	=	ref :: refs
		println(what + "\t+\t" + refs.size.toString)
	}

	private def work():Unit	= {
		while (true) {
			val	ref	= queue.remove()
			refs	= refs filterNot { _ == ref }
			println(what + "\t-\t" + refs.size.toString)
		}
	}
}
