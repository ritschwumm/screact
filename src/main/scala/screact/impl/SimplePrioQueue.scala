package screact

import scala.collection.mutable

final class SimplePrioQueue extends PrioQueue {
	private val items	= mutable.ArrayBuffer.empty[Node]
	
	def insert(item:Node) { 
		items += item
	}
	
	def insertMany(itemMany:Iterable[Node]) { 
		items ++= itemMany
	}
	
	def extract():Option[Node] = {
		var	out:Option[Node]	= None
		var rank				= Integer.MAX_VALUE
		items foreach { item =>
			if (item.rank < rank || out.isEmpty) {
				out		= Some(item)
				rank	= item.rank
			}
		}
		out foreach { item =>
			items	-= item
		}
		out
	}
	
	def isEmpty:Boolean	= items.isEmpty
}
