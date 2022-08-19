package screact

/** enum used to signal what happened with a Node in an internal update cycle */
private enum Update {
	case Rerank
	case Unchanged
	case Changed
}

