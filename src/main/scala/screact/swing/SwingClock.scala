package screact.swing

import java.lang.ref.WeakReference
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.Timer
import java.util.TimerTask

import scutil.lang._
import scutil.Implicits._
import scutil.gui.SwingUtil._
import scutil.time._

import screact._

/** emits time events at regular intervals */
object SwingClock {
	private val timer	= new Timer(true)
	
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
		require(insideEDT, "SwingClock may not be constructed outside the EDT")
		
		val output		= new SourceEvents[Instant]
		val outputRef	= new WeakReference(output)
		
		val task	= new MyTimerTask(outputRef)
		timer schedule (task, delay.millis, cycle.millis)
		
		output
	}
	
	private class MyTimerTask(outputRef:WeakReference[SourceEvents[Instant]]) extends TimerTask {
		def run() {
			val alive	= edtWait {
				val output	= outputRef.get
				val alive	= output != null && !output.disposed
				if (alive) {
					output emit Instant.now
				}
				alive
			}
			if (!alive) {
				cancel()
			}
		}
	}
	
	def repeat[T](cycle:Duration, delay:Duration, input:Events[Option[T]]):Events[T] =
			input.filterOption orElse 
			(input flatMap { _ cata (never, SwingClock(cycle, delay) tag _) })
}
