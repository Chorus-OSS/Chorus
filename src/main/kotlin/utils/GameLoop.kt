package org.chorus_oss.chorus.utils

import com.google.common.base.Preconditions
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Consumer
import kotlin.math.max
import kotlin.math.min

class GameLoop private constructor(
    onStart: Runnable,
    onTick: Consumer<GameLoop>,
    onStop: Runnable,
    loopCountPerSec: Int
) {
    private val isRunning = AtomicBoolean(false)
    private val onStart: Runnable
    private val onTick: Consumer<GameLoop>
    private val onStop: Runnable


    private val loopCountPerSec: Int
    private val tickSummary = FloatArray(20)
    private val msPtSummary = FloatArray(20)


    private var tick = 0

    fun getTick() = tick

    init {
        require(loopCountPerSec > 0) { "Loop count per second must be greater than 0! (loopCountPerSec=$loopCountPerSec)" }
        this.onStart = onStart
        this.onTick = onTick
        this.onStop = onStop
        this.loopCountPerSec = loopCountPerSec
        Arrays.fill(tickSummary, 20f)
        Arrays.fill(msPtSummary, 0f)
    }

    val tickUsage: Float
        get() = msPt / (1000f / loopCountPerSec)

    val tps: Float
        get() {
            var sum = 0f
            val count = tickSummary.size
            for (tick in tickSummary) {
                sum += tick
            }
            return sum / count
        }

    val msPt: Float
        get() {
            var sum = 0f
            val count = msPtSummary.size
            for (mspt in msPtSummary) {
                sum += mspt
            }
            return sum / count
        }

    fun startLoop() {
        isRunning.set(true)
        onStart.run()
        var nanoSleepTime: Long = 0
        val idealNanoSleepPerTick = (1000000000 / loopCountPerSec).toLong()
        while (isRunning.get()) {
            // Figure out how long it took to tick
            val startTickTime = System.nanoTime()
            onTick.accept(this)
            tick++
            val timeTakenToTick = System.nanoTime() - startTickTime
            updateMSTP(timeTakenToTick.toFloat(), msPtSummary)
            updateTPS(timeTakenToTick)

            val sumOperateTime = System.nanoTime() - startTickTime
            // Sleep for the ideal time but take into account the time spent running the tick
            nanoSleepTime += idealNanoSleepPerTick - sumOperateTime
            val sleepStart = System.nanoTime()
            try {
                if (nanoSleepTime > 0) {
                    // noinspection BusyWait
                    runBlocking { delay(TimeUnit.NANOSECONDS.toMillis(nanoSleepTime)) }
                }
            } catch (exception: InterruptedException) {
                log.error("GameLoop interrupted", exception)
                onStop.run()
                return
            }
            // How long did it actually take to sleep?
            // If we didn't sleep for the correct amount,
            // take that into account for the next sleep by
            // leaving extra/less for the next sleep.
            nanoSleepTime -= System.nanoTime() - sleepStart
        }
        onStop.run()
    }

    private fun updateTPS(timeTakenToTick: Long) {
        val tick = max(
            0.0,
            min(20.0, (1000000000f / (if (timeTakenToTick == 0L) 1 else timeTakenToTick)).toDouble())
        ).toFloat()
        System.arraycopy(tickSummary, 1, tickSummary, 0, tickSummary.size - 1)
        tickSummary[tickSummary.size - 1] = tick
    }

    private fun updateMSTP(timeTakenToTick: Float, mstpSummary: FloatArray) {
        System.arraycopy(mstpSummary, 1, mstpSummary, 0, mstpSummary.size - 1)
        mstpSummary[mstpSummary.size - 1] = timeTakenToTick / 1000000f
    }

    fun stop() {
        isRunning.set(false)
    }

    fun isRunning(): Boolean {
        return isRunning.get()
    }

    class GameLoopBuilder {
        private var onStart = Runnable {}
        private var onTick = Consumer<GameLoop> {}
        private var onStop = Runnable {}
        private var loopCountPerSec = 20

        fun onStart(onStart: Runnable): GameLoopBuilder {
            this.onStart = onStart
            return this
        }

        fun onTick(onTick: Consumer<GameLoop>): GameLoopBuilder {
            this.onTick = onTick
            return this
        }

        fun onStop(onStop: Runnable): GameLoopBuilder {
            this.onStop = onStop
            return this
        }

        fun loopCountPerSec(loopCountPerSec: Int): GameLoopBuilder {
            check(loopCountPerSec in 1..1024)
            this.loopCountPerSec = loopCountPerSec
            return this
        }

        fun build(): GameLoop {
            return GameLoop(onStart, onTick, onStop, loopCountPerSec)
        }
    }

    companion object : Loggable {
        fun builder(): GameLoopBuilder {
            return GameLoopBuilder()
        }
    }
}
