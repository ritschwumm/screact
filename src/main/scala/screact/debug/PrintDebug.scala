package screact.debug

import scutil.Implicits._
import scutil.log.Logging

final class PrintDebug {
	val indent	= "  "
	var prefix	= ""
	
	def line(msg: =>String) {
		println(prefix + "!!! " + msg)
	}
	
	def block[T](msg: =>String)(block: =>T):T	= {
		try {
			println(prefix + ">>> " + msg)
			prefix	= prefix + indent
			block
		}
		finally {
			prefix	= prefix substring indent.length
			println(prefix + "<<< " + msg)
		}
	}
}
