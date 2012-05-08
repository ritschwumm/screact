package screact.extra

import scutil.Functions._
import scutil.Connectable
import scutil.Disposable
import scutil.gui.SwingUtil._

import screact._

/** used to connect Swing widgets to the reactive world */ 
object SwingWidget {
	/** simply emit events from some Connectable */
	def events[T](connect:Effect[T]=>Disposable):Events[T]	= {
		require(withinEDT, "SwingWidget may not be constructed outside the EDT")
		
		val	out			= new SourceEvents[T]
		val disposable	= connect(out.emit)
		out
	}
	
	/** signal values by some getter, changing on events from some Connectable */
	def signal[T,X](connect:Effect[X]=>Disposable, getter:Thunk[T]):Signal[T]	=
			events(connect) tag getter() hold getter()
	
	/** 
	gui components in signal transformer style:
	the signal determines the state of the component,
	events are fired on user interaction but never on
	signal changes.
	*/
	def transformer[S,T,X](input:Signal[S], connect:Effect[X]=>Disposable, getter:Thunk[T], setter:Effect[S])(implicit ob:Observing):Events[T]	= {
		require(withinEDT, "SwingWidget may not be constructed outside the EDT")
		
		val blocker	= new Blocker
		val events	= new WidgetEvents[T]
		
		input observeNow { it =>
			blocker exclusive {
				if (getter() != it) {
					setter(it)
				}
			}
		}
		
		// BETTER call this at some time
		val dispose	= connect { _ =>
			blocker attempt {
				events emit getter()
			}
		}
		
		events
	}
	
	//------------------------------------------------------------------------------
	
	// NOTE in contrast to SourceEvents, this does multiple calls to emit
	// within the same cycle. the last emit wins.
	private class WidgetEvents[T] extends Events[T] { outer =>
		var	msg:Option[T]	= None
		
		var delayed:Option[T]	= None
		
		def emit(value:T) {
			val	first	= delayed.isEmpty
			delayed		= Some(value)
			if (first) {
				// TODO use the Domain in to schedule
				edt {
					engine schedule thunk { 
						msg		= delayed
						delayed	= None
						Some(outer) 
					}
				}
			}
		}  
		
		def calculate() {}	// msg does not change in here
		
		def reset() { 
			msg		= None
		}
		
		// init()			// not necessary, we don't have dependencies
	}
}
