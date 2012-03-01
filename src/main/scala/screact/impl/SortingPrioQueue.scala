package screact

import scala.collection.mutable

// NOTE unused
final class SortingPrioQueue extends PrioQueue {
	private var items	= List[Node]()
	
	def insert(item:Node) { 
		items = (item :: items) sortWith { _.rank < _.rank }
	}
	
	def insertMany(itemMany:Iterable[Node]) {
		items = (items ++ itemMany) sortWith { _.rank < _.rank }
	}
	
	def extract:Option[Node] = {
		items match {
			case head :: tail	=>
				items	= tail
				Some(head)
			case Nil	=>
				None
		}
	}
	
	def isEmpty:Boolean	= items.isEmpty
}
