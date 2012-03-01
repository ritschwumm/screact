package screact

import scutil.Functions._

private [screact] class SourceEvents[T] extends Events[T] { outer =>
	var	msg:Option[T]	= None
	
	// var last:String	= ""
	
	def emit(value:T) {
		// import scutil.Implicits._
		// last	= msg + "\n" + new Exception().stackTrace
		Engine scheduleSingle thunk { emitImpl(value) }
	}  
	
	private def emitImpl(value:T):Option[Node]	= {
		// if (msg.nonEmpty) {
		// 	System.err.println("### SourceEvents withinUpdate=" + Engine.withinUpdate)
		// 	System.err.println("### SourceEvents last ###\n" + last)
		// 	last	= ""
		// 	System.err.println("### SourceEvents now =" + value)
		// }
			
		require(msg.isEmpty,	
				"cannot emit an event twice within the same update cycle" +
				" for: " + origin + 
				" message: " + msg.get)
		
		msg	= Some(value)
		Some(outer)
	}
	
	def calculate() {}	// msg does not change in here
	
	def reset() { 
		msg	= None
		// last = "" 
	}
	
	// init()			// not necessary, we don't have dependencies
}
