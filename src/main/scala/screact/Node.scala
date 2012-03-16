package screact

import screact.Updates._

/** base trait for all nodes in a dependency graph. direct children are either Reactive or Target. */
private[screact] trait Node {
	val origin	= Engine.clientCall
	
	private[screact] def rank:Int
	private[screact] def sinks:Sinks

	private[screact] def update():Update
	private[screact] def reset():Unit
	
	private[screact] var id	= HasSinks register this
}
