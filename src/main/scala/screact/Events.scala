package screact

import scutil.Implicits._
import scutil.Functions._

// AKA Publisher

/** a reactive without a (useful) current value just emitting events */
trait Events[+T] extends Reactive[Unit,T] { 
	private[screact] val cur:Unit	= ()
	
	// convert to Signal
	
	final def hold[U>:T](initial:U):Signal[U]	= {
		var value	= initial
		signal {
			message foreach { msgval => 
				value = msgval 
			}
			value
		}
	}
	
	// with a zero
	
	final def filter(func:Predicate[T]):Events[T]	= 
			events { message filter func }
		
	final def filterNot(func:Predicate[T]):Events[T]	= 
			events { message filter !func }
			// events { message collect { case it if func(it) => it } }
			
	final def when(func: =>Boolean):Events[T]	= 
			this filter { _ => func }
			// events { message filter { _ => func } }
			
	final def collect[U](func:PartialFunction[T,U]):Events[U]	= 
			events { message collect func }
		
	final def filterMap[U](func:T=>Option[U]):Events[U] =
			this map func collect { case Some(value) => value }
		
	// final def filterMap[U](implicit witness:T=>Option[U]):Events[U] =
	// 		this map witness collect { case Some(value) => value }
		
	// functor
	
	final def map[U](func:T=>U):Events[U]	= 
			events { message map func }
	
	// TODO applicative functor: ap/pa
	
	// monad
	
	final def flatMap[U](func:T=>Events[U]):Events[U] =
			this map func hold never flattenEvents;
		
	final def flatten[U](implicit witness:T=>Events[U]):Events[U]	= 
			this flatMap witness
		
	// foldable
	
	// TODO add reduce which swallows the first event
			
	final def fold[U](initial:U, func:(U,T)=>U):Events[U]	= {
		var	previous	= initial
		events {
			message map { messageVal => 
				previous	= func(previous, messageVal) 
				previous
			}
		}
	}
	
	// monoid with never
	
	final def orElse[U>:T](that:Events[U]):Events[U]	=  (this,that) match {
		case (_,_:NeverEvents[_])	=> this
		case (_:NeverEvents[_],_)	=> that
		case _ =>
			events {
				// NOTE needs to access both message methods or registration fails!
				// TODO look for other cases where this may happen
				val thisMessage	= this.message
				val thatMessage	= that.message
				thisMessage orElse thatMessage 
			}
	}
	
	// snapshotting
	
	// TODO fails if that accesses other reactives. really? and if so, why?
	final def tag[U](that: =>U):Events[U]	=
			this map { _ => that }
		
	final def tag2[U](that: =>U):Events[(T,U)]	=
			this map { (_, that) }
		
	final def snapshot[U](that:Signal[U]):Events[U]	= events {
		val when	= this.message
		val what	= that.current
		when map { _ => what }
	}    
	
	final def snapshot2[U](that:Signal[U]):Events[(T,U)]	= events {
		val when	= this.message
		val what	= that.current
		when map { (_, what) }
	}
	
	// delayable
	
	/** emits in the next update cycle */
	final def delay(implicit observing:Observing):Events[T]	= {
		val	out	= new SourceEvents[T]
		observing observe (this, out.emit)
		out
	}
	
	// other
	
	/** emits an event if both inputs fire at the same instant */
	final def zip[U](that:Events[U]):Events[(T,U)] = events {
		/*
		(this.message, that.message) match {
			case (Some(here),Some(there))	=> Some((here, there))
			case _							=> None
		}
		*/
		val thisMessage	= this.message
		val thatMessage	= that.message
		if (thisMessage.isDefined && thatMessage.isDefined)	Some((thisMessage.get, thatMessage.get)) 
		else												None
	}
	
	final def sum[U](that:Events[U]):Events[Either[T,U]]	= 
			(this map { Left(_) }) orElse (that map { Right(_) })
		
	final def partition(func:T=>Boolean):(Events[T],Events[T])	= 
			(	events { message filter  func },
				events { message filter !func }
			)
	
	/*
	// NOTE just shifting one back is stupid. i need "immediately after"! 
	//* emits the previously emitted event of the source
	final def delay:Events[T] = {
		var	previous:Option[T]	= None
		events {
			val	out	= previous
			if (message.isDefined)	{ previous	= message; out }
			else					None
		}
	}
	*/
	
	/*
	// slide = delay pair this
	// aka zipTail
	final def slide:Events[(T,T)] = {
		var previous:Option[T]	= None
		events {
			val	previousOld	= previous
			if (message.isDefined)							previous	= message
			if (message.isDefined && previousOld.isDefined)	Some((previousOld.get,message.get))
			else											None
		}
	}
	
	// TODO add stateful combinator?
	*/
			
	/** take the first count events, drop the rest */
	final def take(count:Int):Events[T]	= {
		var	todo	= count
		events {
			if (todo != 0) {
				todo	-= 1
				message
			}
			else {
				None
			}
		}
	}
	
	/** drop the first count events, take the rest */
	final def drop(count:Int):Events[T] = {
		var	todo	= count
		events {
			// need to access message every time to avoid loss of connection
			var	value	= message
			if (todo != 0) {
				todo	-= 1
				None
			}
			else {
				value
			}
		}
	}
	
	// TODO ugly
	final def someUntil(that:Events[_]):Signal[Option[T]]	=
			// (this map Some.apply _) merge (that tag None)
			this sum that map { _.left.toOption } hold None
}
