package screact

/** enum used to signal what happened with a Node in an internal update cycle */
private object Updates {
	sealed trait Update
	case object Rerank		extends Update
	case object Unchanged	extends Update
	case object Changed		extends Update
}
