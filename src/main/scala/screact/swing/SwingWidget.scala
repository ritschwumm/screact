package screact.swing

import scutil.lang._
import scutil.gui.SwingUtil._

import screact._
import screact.extra.Blocker

/** used to connect Swing widgets to the reactive world */ 
object SwingWidget {
	/** simply emit events from some Connectable */
	def events[T](connect:Effect[T]=>Disposable):Events[T]	= {
		require(insideEDT, "SwingWidget events may not be constructed outside the EDT")
		
		val	events		= new SourceEvents[T]
		// BETTER call this at some time
		val disposable	= connect(events.emit)
		events
	}
	
	/** Signal values by some getter, changing on events from some Connectable */
	def signal[T,X](connect:Effect[X]=>Disposable, getter:Thunk[T]):Signal[T]	=
			events(connect) tag getter() hold getter()
	
	/** 
	wraps a swing component to take an input Signal and mit change Events.
	the input signal determines the state of the component.
	change events are only fired on user interaction, but not on changes 
	of the input signal.
	*/
	def transformer[S,T,X](input:Signal[S], connect:Effect[X]=>Disposable, getter:Thunk[T], setter:Effect[S])(implicit ob:Observing):Events[T]	= {
		require(insideEDT, "SwingWidget transformer may not be constructed outside the EDT")
		
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
	
	/** in contrast to SourceEvents, this allows multiple calls to emit within the same cycle. the last emit wins. */
	private final class WidgetEvents[T] extends Events[T] { outer =>
		var	msg:Option[T]	= None
		
		var delayed:Option[T]	= None
		
		def emit(value:T) {
			val	first	= delayed.isEmpty
			delayed		= Some(value)
			if (first) {
				// TODO use the (Swing-)Domain to schedule
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
