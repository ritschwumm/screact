package screact

import scutil.Functions._

private [screact] class SourceSignal[T](initial:T) extends Signal[T] { outer =>
	var cur:T			= initial
	var msg:Option[T]	= None
	
	// var last:String	= ""
	
	def set(value:T) {
		// import scutil.Implicits._
		// last	= msg + "\n" + new Exception().stackTrace
		Engine scheduleSingle thunk { setImpl(value) }
	}  
	
	private def setImpl(value:T):Option[Node]	= {
		if (value != cur) {
			// if (msg.nonEmpty) {
			// 	System.err.println("### SourceSignal withinUpdate=" + Engine.withinUpdate)
			// 	System.err.println("### SourceSignal last ###\n" + last)
			// 	last	= ""
			// 	System.err.println("### SourceSignal now =" + value)
			// }
			
			require(msg.isEmpty,	
					"cannot set a signal twice within the same update cycle"	+
					" for: " + origin + 
					" message: " + msg.get +
					" was: " + cur +
					" now: " + value)
				
			cur	= value
			msg	= Some(value)
			Some(outer)
		}
		else {
			None
		}
	}
	
	def calculate() {}	// msg does not change in here
	
	def reset() {
		msg	= None
		// last = "" 
	}
	
	// init()			// not necessary, we don't have dependencies
}
