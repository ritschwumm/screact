package screact

private[screact] object Updates {
	sealed trait Update
	case object Rerank		extends Update
	case object Unchanged	extends Update
	case object Changed		extends Update
}
