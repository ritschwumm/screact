package screact

import scala.collection.mutable

// TODO generify

trait PrioQueue {
	def insert(node:Node)       
	def insertMany(nodes:Iterable[Node])
	def extract():Option[Node]
	def isEmpty:Boolean
	final def nonEmpty:Boolean	= !isEmpty
}
