package screact

import scutil.lang._
import scutil.Implicits._

// AKA Publisher

/** a Reactive without a (useful) current value just emitting events */
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
	
	final def scan[U](initial:U)(func:(U,T)=>U):Signal[U]	= {
		var value	= initial
		signal {
			message foreach { msgval => 
				value = func(value, msgval) 
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
		
	// final def filterMap[U](implicit ev:T=>Option[U]):Events[U] =
	// 		this map witness collect { case Some(value) => value }
		
	// functor
	
	final def map[U](func:T=>U):Events[U]	= 
			events { message map func }
	
	// TODO applicative functor: ap/pa
	
	// monad
	
	final def flatMap[U](func:T=>Events[U]):Events[U] =
			this map func hold never flattenEvents;
		
	final def flatten[U](implicit ev:T=>Events[U]):Events[U]	= 
			this flatMap ev
		
	// monoid with never
	
	final def orElse[U>:T](that:Events[U]):Events[U]	=  (this,that) match {
		case (_,_:NeverEvents[_])	=> this
		case (_:NeverEvents[_],_)	=> that
		case _ =>
			events {
				// NOTE needs to access both message methods or registration fails!
				val thisMessage	= this.message
				val thatMessage	= that.message
				thisMessage orElse thatMessage 
			}
	}
	
	// snapshotting
	
	final def tag[U](that: =>U):Events[U]	=
			this map { _ => that }
		
	final def tagWith[U](that: =>U):Events[(T,U)]	=
			this map { (_, that) }
		
	final def snapshot[U](that:Signal[U]):Events[U]	= events {
		val when	= this.message
		val what	= that.current
		when map { _ => what }
	}    
	
	final def snapshotWith[U](that:Signal[U]):Events[(T,U)]	= events {
		val when	= this.message
		val what	= that.current
		when map { (_, what) }
	}
	
	final def gate(that:Signal[Boolean]):Events[T]	= events {
		val when	= this.message
		val gate	= that.current
		when filter { _ => gate }
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
		(this.message, that.message) match {
			case (Some(thisMessage),Some(thatMessage))	=> Some((thisMessage, thatMessage))
			case _										=> None
		}
	}
	
	final def unzip[U,V](implicit ev:T=>(U,V)):(Events[U],Events[V])	=
			(map(_._1), map(_._2))
	
	final def sum[U](that:Events[U]):Events[Either[T,U]]	= 
			(this map { Left(_) }) orElse (that map { Right(_) })
		
	final def unsum[U,V](implicit ev:T=>Either[U,V]):(Events[U],Events[V])	= 
			(	events { message flatMap { it:T => ev(it).left.toOption		} },
				events { message flatMap { it:T => ev(it).right.toOption	} }
			)
		
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
}
