package org.chorus_oss.chorus.scheduler

import org.chorus_oss.chorus.utils.Loggable
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class AsyncPool(size: Int) :
    ThreadPoolExecutor(size, Int.MAX_VALUE, 60, TimeUnit.MILLISECONDS, SynchronousQueue()), Loggable {
    init {
        this.threadFactory = ThreadFactory { runnable ->
            object : Thread(runnable) {
                init {
                    isDaemon = true
                    name = "Chorus Async TaskHandler #$poolSize"
                }
            }
        }
    }

    override fun afterExecute(runnable: Runnable, throwable: Throwable?) {
        if (throwable != null) {
            log.error("Exception in asynchronous task", throwable)
        }
    }
}