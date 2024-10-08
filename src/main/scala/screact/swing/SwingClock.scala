package screact.swing

import java.lang.ref.WeakReference
import java.util.Timer
import java.util.TimerTask

import scutil.core.implicits.*
import scutil.gui.SwingUtil.*
import scutil.time.*

import screact.*

/** emits time events at regular intervals */
object SwingClock {
	private val timer	= new Timer(true)

	/*
	NOTE this doesn't work as desired if there are any hard references to output.events left:
	-	Swing tends to keep hard references to JFrames around.
		-Dswing.bufferPerWindow=false helps.
		calling Events#dispose helps.
	-	scala keeps hard references to objects (as opposed to vals).
		nulling a reference from an object to an Events object helps.
	*/
	def apply(cycle:MilliDuration, delay:MilliDuration):Events[MilliInstant] = {
		require(insideEdt, "SwingClock may not be constructed outside the EDT")

		val output		= new SourceEvents[MilliInstant]
		val outputRef	= new WeakReference(output)

		val task	= new MyTimerTask(outputRef)
		timer.schedule(task, delay.millis, cycle.millis)

		output
	}

	private class MyTimerTask(outputRef:WeakReference[SourceEvents[MilliInstant]]) extends TimerTask {
		def run():Unit	= {
			val alive	=
				edt {
					val output	= outputRef.get
					val alive	= (output ne null) && !output.disposed
					if (alive) {
						output.emit(MilliInstant.now())
					}
					alive
				}()
			if (!alive) {
				cancel()
			}
		}
	}

	def repeat[T](cycle:MilliDuration, delay:MilliDuration, input:Events[Option[T]]):Events[T] =
		input.flattenOption `orElse`
		input.flatMap { _.cata(never, SwingClock(cycle, delay).tag(_)) }
}
