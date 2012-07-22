package screact.extra

import java.lang.ref.WeakReference
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.Timer

import scutil.lang._
import scutil.gui.SwingUtil._
import scutil.time._

import screact._

/** emits time events at regular intervals */
object SwingClock {
	def apply(cycle:Duration):Events[Instant] = apply(cycle, cycle)
	
	/*
	NOTE this doesn't work as desired if there are any hard references to output.events left:
	-	Swing tends to keep hard references to JFrames around. 
		-Dswing.bufferPerWindow=false helps.
		calling Events#dispose helps.
	-	scala keeps hard references to objects (as opposed to vals).
		nulling a reference from an object to an Events object helps.
	*/
	def apply(cycle:Duration, delay:Duration):Events[Instant] = {
		require(withinEDT, "SwingClock may not be constructed outside the EDT")
		
		val output		= new SourceEvents[Instant]
		val outputRef	= new WeakReference(output)
		
		lazy val timer:Timer	= new Timer(
				millis(cycle), 
				new MyActionListener(
						() => timer.stop(), 
						outputRef))
		
		timer setInitialDelay millis(delay)
		timer.start()
		
		// NOTE this only references the Events, but not the complete Emitter
		output
	}
	
	// BETTER ensure this doesn't exceed Int.MaxValue
	private def millis(duration:Duration):Int	= duration.millis.toInt
	
	private class MyActionListener(stop:Task, outputRef:WeakReference[SourceEvents[Instant]]) extends ActionListener {
		def actionPerformed(ev:ActionEvent) {
			val output	= outputRef.get
			if (output != null && !output.disposed) {
				output emit Instant.now
			}
			else {
				// NOTE this happens only when the reference to the SwingClock is dropped
				stop()
			}
		}
	}
	
	// BETTER take a Signal[Option[T]] instead of an Events[Option[T]]?
	def repeat[T](cycle:Duration, delay:Duration, input:Events[Option[T]]):Events[T] =
			input flatMap {
				_ map { it => once(it) orElse (SwingClock(cycle, delay) tag it) } getOrElse never
			}
}
