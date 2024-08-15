package screact

/** base trait for all nodes in a dependency graph. direct children are either Reactive or Target. */
private[screact] trait Node {
	val engine	= Engine.access
	/** where this node was created in the source code */
	val origin	= engine.clientCall

	/** order of updates, higher values means "later" */
	private[screact] def rank:Int
	/** Nodes reading data from this Reactive */
	private[screact] def sinks:Sinks

	/** called when this node might need to update its state */
	private[screact] def update():Update
	/** recursively raise the rank of dependent nodes above the rank of this node  */
	private [screact] def pushDown(rank:Int):Unit
	/** called on all updated nodes at the end of an update cycle */
	private[screact] def reset():Unit

	private[screact] val id:Long	= engine.registerNode(this)
}
