package screact

import scutil.lang._

object Cell {
	implicit def asSignal[T](it:Cell[T]):Signal[T]	= it.signal
}

/** An Cell is a source for a Signal and can trigger an update cycle in the Engine */
trait Cell[T] extends AutoCloseable { outer =>
	val signal:Signal[T]
	def set(value:T):Unit

	final def modify(func:T=>T):Unit	= {
		set(func(signal.current))
	}

	final def xmap[S](bijection:Bijection[S,T]):Cell[S]	= new Cell[S] {
		val signal	= outer.signal map bijection.set
		def set(it:S):Unit	= { outer set (bijection get it) }
		override def close():Unit	=	{ signal.close() }
	}

	final def view[U](lens:Lens[T,U]):Cell[U]	= new Cell[U] {
		val signal		= outer.signal map lens.get
		def set(it:U):Unit	= 	{ outer modify (lens set it) }
		override def close():Unit	=	{ signal.close() }
	}

	def close():Unit	= {
		signal.close()
	}
}
