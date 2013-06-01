package screact

import scutil.lang._
import scutil.lens._

object Cell {
	implicit def asSignal[T](it:Cell[T]):Signal[T]	= it.signal
}

/** An Cell is a source for a Signal and can trigger an update cycle in the Engine */
trait Cell[T] extends Disposable { outer =>
	val signal:Signal[T]
	def set(value:T):Unit
	
	final def modify(func:Endo[T]) {
		set(func(signal.current))
	}
	
	final def xmap[S](bijection:Bijection[S,T]):Cell[S]	= new Cell[S] {
		val signal	= outer.signal map bijection.read
		def set(it:S) { outer set (bijection write it) }
		override def dispose()	{ signal.dispose() }
	}
	
	final def view[U](lens:TLens[T,U]):Cell[U]	= new Cell[U] {
		val signal		= outer.signal map lens.get
		def set(it:U) 	{ outer modify (lens put (_, it)) }
		override def dispose()	{ signal.dispose() }
	}
	
	def dispose() { 
		signal.dispose() 
	}
}
