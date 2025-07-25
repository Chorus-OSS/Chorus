package org.chorus_oss.chorus.scheduler

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.chorus.utils.ThreadStore
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

abstract class AsyncTask : Runnable {
    var result: Any? = null
    var taskId: Int = 0
    var isFinished: Boolean = false
        private set

    override fun run() {
        this.result = null
        this.onRun()
        this.isFinished = true
        FINISHED_LIST.offer(this)
    }

    fun hasResult(): Boolean {
        return this.result != null
    }

    fun getFromThreadStore(identifier: String): Any? {
        return if (this.isFinished) null else ThreadStore.store[identifier]
    }

    fun saveToThreadStore(identifier: String, value: Any?) {
        if (!this.isFinished) {
            if (value == null) {
                ThreadStore.store.remove(identifier)
            } else {
                ThreadStore.store[identifier] = value
            }
        }
    }

    abstract fun onRun()

    fun onCompletion(server: Server?) {}

    fun cleanObject() {
        this.result = null
        this.taskId = 0
        this.isFinished = false
    }

    companion object : Loggable {
        val FINISHED_LIST: Queue<AsyncTask> = ConcurrentLinkedQueue()

        fun collectTask() {
            while (!FINISHED_LIST.isEmpty()) {
                val task = FINISHED_LIST.poll()
                try {
                    task.onCompletion(Server.instance)
                } catch (e: Exception) {
                    log.error("Exception while async task {} invoking onCompletion", task.taskId, e)
                }
            }
        }
    }
}
