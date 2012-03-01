package screact.extra

import javax.swing.Timer
import java.lang.ref.WeakReference
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

import scutil.Functions._

import screact._

object SwingClock {
	def apply(tickMillis:Int):Events[Unit] = apply(tickMillis, tickMillis)
	
	/*
	NOTE this doesn't work as desired if there are any hard references to output.events left
	-	Swing tends to keep hard references to JFrames around. 
		-Dswing.bufferPerWindow=false helps.
		calling Events#dispose helps.
	-	scala keeps hard references to objects. 
		nulling a reference from an object to an Events object helps
	*/
	def apply(tickMillis:Int, initialMillis:Int):Events[Unit] = {
		val output		= new SourceEvents[Unit]
		val outputRef	= new WeakReference(output)
		
		lazy val timer:Timer	= new Timer(
				tickMillis, 
				new MyActionListener(
						() => timer.stop(), 
						outputRef))
		
		timer setInitialDelay initialMillis
		timer.start()
		
		// NOTE this only references the Events, but not the complete Emitter
		output
	}
	
	private class MyActionListener(stop:Task, outputRef:WeakReference[SourceEvents[Unit]]) extends ActionListener {
		def actionPerformed(ev:ActionEvent) {
			val output	= outputRef.get
			if (output != null) {
				output emit ()
			}
			else {
				// NOTE this happens only when ther reference to the SwingClock is dropped
				stop()
			}
		}
	}
	
	// BETTER input a Signal[Option[T]] ?
	def repeat[T](tickMillis:Int, delayMillis:Int, input:Events[Option[T]]):Events[T] =
			input flatMap {
				_ map { it => once(it) orElse (SwingClock(tickMillis, delayMillis) tag it) } getOrElse never
			}
}
