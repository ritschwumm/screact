package screact

import scutil.lang._
import scutil.Implicits._

// AKA Publisher

object Events {
	// (in foldLeft never[T]) { _ orElse _ }
	def multiOrElse[T](in:Seq[Events[T]]):Events[T]	=
			events {
				in flatMap { _.message } headOption;
			}
			
	def multiOccurs[T](in:Seq[Events[T]]):Events[Seq[T]]	=
			events {
				in flatMap { _.message } guardBy { _.nonEmpty }
			}
}

/** a Reactive without a (useful) current value just emitting events */
trait Events[+T] extends Reactive[Unit,T] { 
	private[screact] val cur:Unit	= ()
	
	// convert to Signal
	
	final def hold[U>:T](initial:U):Signal[U]	= {
		// TODO must not be stateful, rank mismatch re-evaluation might occur
		var value	= initial
		signal {
			message foreach { msgval => 
				value = msgval 
			}
			value
		}
	}
	
	final def scan[U](initial:U)(func:(U,T)=>U):Signal[U]	= {
		// TODO must not be stateful, rank mismatch re-evaluation might occur
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
			
	final def collect[U](func:PartialFunction[T,U]):Events[U]	= 
			events { message collect func }
		
	final def filterMap[U](func:T=>Option[U]):Events[U] =
			this map func collect { case Some(value) => value }
		
	final def filterOption[U](implicit ev:T=>Option[U]):Events[U] =
			this filterMap ev
		
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
	
	// combine with values
	
	final def tag[U](that: =>U):Events[U]	=
			this map { _ => that }
		
	final def when(func: =>Boolean):Events[T]	= 
			this filter { _ => func }
	
	final def trueUnit(implicit ev:T=>Boolean):Events[Unit]	=
			this collect { case true => () }
		
	final def falseUnit(implicit ev:T=>Boolean):Events[Unit]	=
			this collect { case false => () }
	
	// combine with signals
		
	final def snapshot[U](that:Signal[U]):Events[(T,U)]	=
			snapshotWith(that) { (_,_) }
	
	final def snapshotOnly[U](that:Signal[U]):Events[U]	=
			snapshotWith(that) { (_, it) => it }
	
	final def snapshotWith[U,V](that:Signal[U])(func:(T,U)=>V):Events[V]	= events {
		val when	= this.message
		val what	= that.current
		when map { it => func(it, what) }
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
	final def zip[U](that:Events[U]):Events[(T,U)] =
			zipWith(that) { (_,_) }
	
	/** emits an event if both inputs fire at the same instant */
	final def zipWith[U,V](that:Events[U])(func:(T,U)=>V):Events[V]	= events {
		(this.message, that.message) match {
			case (Some(thisMessage),Some(thatMessage))	=> Some(func(thisMessage, thatMessage))
			case _										=> None
		}
	}
	
	final def zipBy[U](func:T=>U):Events[(T,U)]	=
			this map { it => (it,func(it)) }
	
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
	
	/** take the first count events, drop the rest */
	final def take(count:Int):Events[T]	= {
		// TODO must not be stateful, rank mismatch re-evaluation might occur
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
		// TODO must not be stateful, rank mismatch re-evaluation might occur
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
