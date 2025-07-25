package org.chorus_oss.chorus.utils

import org.chorus_oss.chorus.Server
import java.lang.management.ManagementFactory
import java.lang.management.ThreadInfo
import kotlin.concurrent.Volatile
import kotlin.math.max
import kotlin.system.exitProcess


class Watchdog(private val server: Server, private val time: Long) : Thread() {
    @JvmField
    @Volatile
    var running: Boolean
    private var responding = true

    init {
        this.running = true
        this.name = "Watchdog"
        this.isDaemon = true
        this.priority = MIN_PRIORITY
    }

    fun kill() {
        running = false
        interrupt()
    }

    override fun run() {
        while (this.running) {
            //Refresh the advanced network information in watchdog, as this is time-consuming operate and will block the main thread
            server.network.resetStatistics()

            val current = server.nextTick
            if (current != 0L) {
                val now = System.currentTimeMillis()
                val diff = now - current
                if (!responding && diff > time * 2) {
                    exitProcess(1) // Kill the server if it gets stuck on shutdown
                }

                if (diff <= time) {
                    responding = true
                } else if (responding && now - server.getBusyingTime() < 60) {
                    val builder = StringBuilder(
                        "--------- Server stopped responding --------- (" + Math.round(diff / 1000.0) + "s)"
                    ).append('\n')
                        .append("Please report this to Chorus:").append('\n')
                        .append(" - https://github.com/Chorus-OSS/Chorus/issues/new").append('\n')
                        .append("---------------- Main thread ----------------").append('\n')

                    dumpThread(
                        ManagementFactory.getThreadMXBean().getThreadInfo(
                            server.thread.threadId(), Int.MAX_VALUE
                        ), builder
                    )

                    builder.append("---------------- All threads ----------------").append('\n')
                    val threads = ManagementFactory.getThreadMXBean().dumpAllThreads(true, true)
                    for (i in threads.indices) {
                        if (i != 0) builder.append("------------------------------").append('\n')
                        dumpThread(threads[i], builder)
                    }
                    builder.append("---------------------------------------------").append('\n')
                    log.error(builder.toString())
                    responding = false
                    server.forceShutdown()
                }
            }
            try {
                sleep(max((time / 4).toDouble(), 1000.0).toLong())
            } catch (interruption: InterruptedException) {
                log.error(
                    "The Watchdog Thread has been interrupted and is no longer monitoring the server state",
                    interruption
                )
                running = false
                return
            }
        }
        log.warn("Watchdog was stopped")
    }

    companion object : Loggable {
        private fun dumpThread(thread: ThreadInfo?, builder: StringBuilder) {
            if (thread == null) {
                builder.append("Attempted to dump a null thread!").append('\n')
                return
            }
            builder.append("Current Thread: ").append(thread.threadName).append('\n')
            builder.append("\tPID: ").append(thread.threadId).append(" | Suspended: ").append(thread.isSuspended)
                .append(" | Native: ").append(thread.isInNative).append(" | State: ").append(thread.threadState)
                .append('\n')
            // Monitors
            if (thread.lockedMonitors.isNotEmpty()) {
                builder.append("\tThread is waiting on monitor(s):").append('\n')
                for (monitor in thread.lockedMonitors) {
                    builder.append("\t\tLocked on:").append(monitor.lockedStackFrame).append('\n')
                }
            }

            builder.append("\tStack:").append('\n')
            for (stack in thread.stackTrace) {
                builder.append("\t\t").append(stack).append('\n')
            }
        }
    }
}
