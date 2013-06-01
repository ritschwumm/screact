package screact

/** enum used to signal what happened with a Node in an internal update cycle */
private sealed trait Update
private case object Rerank		extends Update
private case object Unchanged	extends Update
private case object Changed		extends Update

