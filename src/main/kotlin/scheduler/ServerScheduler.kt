package org.chorus_oss.chorus.scheduler

import org.chorus_oss.chorus.plugin.Plugin
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.chorus.utils.PluginException
import org.chorus_oss.chorus.utils.Utils
import org.jetbrains.annotations.ApiStatus
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.Volatile
import kotlin.math.max

class ServerScheduler {
    @get:ApiStatus.Internal
    val asyncTaskThreadPool: AsyncPool = AsyncPool(WORKERS)

    private val pending: Queue<TaskHandler> =
        ConcurrentLinkedQueue()
    private val queueMap: MutableMap<Int, ArrayDeque<TaskHandler>> =
        ConcurrentHashMap()
    private val taskMap: MutableMap<Int, TaskHandler> =
        ConcurrentHashMap()

    private val currentTaskId = AtomicInteger()

    @Volatile
    private var currentTick = -1

    /**
     * 设置一个只执行一次的任务 delay=0 period=0 asynchronous=false
     *
     *
     * Set a task to be executed only once,delay=0 period=0 asynchronous=false
     *
     * @param task the task
     * @return the task handler
     */
    fun scheduleTask(task: Task): TaskHandler {
        return addTask(task, 0, 0, false)
    }

    /**
     * Schedule a task using runnable
     *
     * @param task the task
     * @return the task handler
     */
    fun scheduleTask(task: Runnable): TaskHandler {
        return addTask(null, task, 0, 0, false)
    }

    /**
     * Schedule a task using runnable
     *
     * @param task         the task
     * @param asynchronous whether is executed in other thread(not main thread)
     * @return the task handler
     */
    fun scheduleTask(task: Runnable, asynchronous: Boolean): TaskHandler {
        return addTask(null, task, 0, 0, asynchronous)
    }

    /**
     * Schedule an async task
     *
     *
     * the async task is executed in other thread(not main thread)
     *
     * @param task the async task
     * @return the task handler
     */
    fun scheduleAsyncTask(task: AsyncTask): TaskHandler {
        return addTask(null, task, 0, 0, true)
    }

    /**
     * Schedule async task to worker.
     *
     * @param task   the task
     * @param worker the worker
     */
    fun scheduleAsyncTaskToWorker(task: AsyncTask, worker: Int) {
        scheduleAsyncTask(task)
    }

    /**
     * Schedule a task with delay.
     *
     * @param task         the task
     * @param delay        the delay, use game tick,20tick = 1s
     * @param asynchronous whether is executed in other thread(not main thread)
     * @return the task handler
     */
    fun scheduleDelayedTask(task: Runnable, delay: Int, asynchronous: Boolean): TaskHandler {
        return addTask(null, task, delay, 0, asynchronous)
    }

    /**
     * Schedule a task with delay and repeat to execute.
     *
     * @param task         the task
     * @param period       the period of repeat,use game tick,20tick = 1s
     * @param asynchronous whether is executed in other thread(not main thread)
     * @return the task handler
     */
    fun scheduleRepeatingTask(task: Runnable, period: Int, asynchronous: Boolean): TaskHandler {
        return addTask(null, task, 0, period, asynchronous)
    }

    /**
     * Schedule delayed repeating task
     *
     * @param task   the task
     * @param delay  the delay, use game tick,20tick = 1s
     * @param period the period of repeat,use game tick,20tick = 1s
     * @return the task handler
     */
    fun scheduleDelayedRepeatingTask(task: Runnable, delay: Int, period: Int): TaskHandler {
        return addTask(null, task, delay, period, false)
    }

    /**
     * Schedule delayed repeating task.
     *
     * @param task         the task
     * @param delay        the delay, use game tick,20tick = 1s
     * @param period       the period of repeat,use game tick,20tick = 1s
     * @param asynchronous whether is executed in other thread(not main thread)
     * @return the task handler
     */
    fun scheduleDelayedRepeatingTask(task: Runnable, delay: Int, period: Int, asynchronous: Boolean): TaskHandler {
        return addTask(null, task, delay, period, asynchronous)
    }

    /**
     * 设置一个只执行一次的任务 delay=0 period=0 asynchronous=false
     *
     *
     * Set a task to be executed only once,delay=0 period=0 asynchronous=false
     *
     * @param plugin the plugin
     * @param task   the task
     * @return the task handler
     */
    fun scheduleTask(plugin: Plugin?, task: Runnable): TaskHandler {
        return addTask(plugin, task, 0, 0, false)
    }

    /**
     * 设置一个只执行一次的任务 delay=0 period=0
     *
     *
     * Set a task to be executed only once,delay=0 period=0
     *
     * @param plugin       the plugin
     * @param task         the task
     * @param asynchronous 是否异步执行<br></br>Whether it executes asynchronously
     * @return the task handler
     */
    fun scheduleTask(plugin: Plugin?, task: Runnable, asynchronous: Boolean): TaskHandler {
        return addTask(plugin, task, 0, 0, asynchronous)
    }

    /**
     * 设置一个只执行一次的异步任务
     * Set up an asynchronous task to be executed only once
     *
     * @param plugin 插件实例,
     * @param task   异步任务
     */
    fun scheduleAsyncTask(plugin: Plugin?, task: AsyncTask): TaskHandler {
        return addTask(plugin, task, 0, 0, true)
    }

    val asyncTaskPoolSize: Int
        get() = asyncTaskThreadPool.corePoolSize

    /**
     * 设置一个只执行一次的非异步延迟任务
     * Set up a delayed task to be executed only once
     *
     * @param task  任务,可用匿名类创建
     * @param delay 延迟时间,单位tick(20tick = 1s)
     */
    fun scheduleDelayedTask(task: Task, delay: Int): TaskHandler {
        return this.addTask(task, delay, 0, false)
    }

    /**
     * 设置一个只执行一次的延迟任务
     *
     *
     * Set up a deferred task that executes only once
     *
     * @param task         任务,可用lambda表达式创建<br></br>Tasks, which can be created using lambda expressions
     * @param delay        延迟时间,单位tick(20tick = 1s)<br></br>Delay time, in tick (20tick = 1s)
     * @param asynchronous 是否异步执行，如果是，会启用一个新线程执行任务<br></br>Whether it executes asynchronously, and if so, enables a new thread to execute the task
     * @return the task handler
     */
    fun scheduleDelayedTask(task: Task, delay: Int, asynchronous: Boolean): TaskHandler {
        return this.addTask(task, delay, 0, asynchronous)
    }

    /**
     * Use [.scheduleDelayedTask]
     */
    fun scheduleDelayedTask(task: Runnable, delay: Int): TaskHandler {
        return addTask(null, task, delay, 0, false)
    }

    /**
     * 设置一个只执行一次的非异步(在主线程执行)延迟任务
     *
     *
     * Set up a non-asynchronous (execute on the main thread) deferred task that executes only once
     *
     * @param plugin the plugin
     * @param task   任务,可用lambda表达式创建<br></br>Tasks, which can be created using lambda expressions
     * @return the task handler
     */
    fun scheduleDelayedTask(plugin: Plugin?, task: Runnable, delay: Int): TaskHandler {
        return addTask(plugin, task, delay, 0, false)
    }

    /**
     * Schedule delayed task.
     *
     * @param plugin       the task executor (plugin)
     * @param task         the task
     * @param delay        the delay, use game tick,20tick = 1s
     * @param asynchronous whether is executed in other thread(not main thread)
     * @return the task handler
     */
    fun scheduleDelayedTask(plugin: Plugin?, task: Runnable, delay: Int, asynchronous: Boolean): TaskHandler {
        return addTask(plugin, task, delay, 0, asynchronous)
    }

    /**
     * Schedule repeating task .
     *
     * @param plugin the task executor (plugin)
     * @param task   the task
     * @param period the period of repeat,use game tick,20tick = 1s
     * @return the task handler
     */
    fun scheduleRepeatingTask(plugin: Plugin?, task: Runnable, period: Int): TaskHandler {
        return addTask(plugin, task, 0, period, false)
    }


    /**
     * Schedule repeating task .
     *
     * @param plugin       the task executor (plugin)
     * @param task         the task
     * @param period       the period of repeat,use game tick,20tick = 1s
     * @param asynchronous whether is executed in other thread(not main thread)
     * @return the task handler
     */
    fun scheduleRepeatingTask(plugin: Plugin?, task: Runnable, period: Int, asynchronous: Boolean): TaskHandler {
        return addTask(plugin, task, 0, period, asynchronous)
    }

    /**
     * Schedule repeating task .
     *
     * @param task   the task
     * @param period the period of repeat,use game tick,20tick = 1s
     * @return the task handler
     */
    fun scheduleRepeatingTask(task: Task, period: Int): TaskHandler {
        return addTask(task, 0, period, false)
    }

    /**
     * Schedule repeating task.
     *
     * @param task         the task
     * @param period       the period
     * @param asynchronous the asynchronous
     * @return the task handler
     */
    fun scheduleRepeatingTask(task: Task, period: Int, asynchronous: Boolean): TaskHandler {
        return addTask(task, 0, period, asynchronous)
    }

    /**
     * Schedule repeating task.
     *
     * @param task   the task
     * @param period the period
     * @return the task handler
     */
    fun scheduleRepeatingTask(task: Runnable, period: Int): TaskHandler {
        return addTask(null, task, 0, period, false)
    }

    /**
     * Schedule delayed repeating task.
     *
     * @param task   the task
     * @param delay  the delay
     * @param period the period
     * @return the task handler
     */
    fun scheduleDelayedRepeatingTask(task: Task, delay: Int, period: Int): TaskHandler {
        return addTask(task, delay, period, false)
    }

    /**
     * Schedule delayed repeating task.
     *
     * @param task         the task
     * @param delay        the delay
     * @param period       the period
     * @param asynchronous the asynchronous
     * @return the task handler
     */
    fun scheduleDelayedRepeatingTask(task: Task, delay: Int, period: Int, asynchronous: Boolean): TaskHandler {
        return addTask(task, delay, period, asynchronous)
    }


    /**
     * Schedule delayed repeating task.
     *
     * @param plugin the plugin
     * @param task   the task
     * @param delay  the delay
     * @param period the period
     * @return the task handler
     */
    fun scheduleDelayedRepeatingTask(plugin: Plugin?, task: Runnable, delay: Int, period: Int): TaskHandler {
        return addTask(plugin, task, delay, period, false)
    }


    /**
     * 设置一个延迟周期任务
     *
     *
     * Set a deferral period task
     *
     * @param plugin       the plugin
     * @param task         the task
     * @param delay        延迟开始的时间，单位tick<br></br>The time to delay the start in tick
     * @param period       周期执行的时间，单位tick<br></br>The time of the cycle execution, in tick
     * @param asynchronous 是否异步执行<br></br>Whether it executes asynchronously
     * @return the task handler
     */
    fun scheduleDelayedRepeatingTask(
        plugin: Plugin?,
        task: Runnable,
        delay: Int,
        period: Int,
        asynchronous: Boolean
    ): TaskHandler {
        return addTask(plugin, task, delay, period, asynchronous)
    }

    fun cancelTask(taskId: Int) {
        if (taskMap.containsKey(taskId)) {
            try {
                taskMap.remove(taskId)!!.cancel()
            } catch (ex: RuntimeException) {
                log.error("Exception while invoking onCancel", ex)
            }
        }
    }

    /**
     * Cancel all task of specific plugin.
     *
     * @param plugin the specific plugin.
     */
    fun cancelTask(plugin: Plugin) {
        for ((_, taskHandler) in taskMap) {
            // It is only there for backwards compatibility!
            if (taskHandler.plugin == null || plugin == taskHandler.plugin) {
                try {
                    taskHandler.cancel() /* It will remove from task map automatic in next main heartbeat. */
                } catch (ex: RuntimeException) {
                    log.error("Exception while invoking onCancel", ex)
                }
            }
        }
    }

    fun cancelAllTasks() {
        for ((_, value) in this.taskMap) {
            try {
                value.cancel()
            } catch (ex: RuntimeException) {
                log.error("Exception while invoking onCancel", ex)
            }
        }
        taskMap.clear()
        queueMap.clear()
        currentTaskId.set(0)
    }

    fun isQueued(taskId: Int): Boolean {
        return taskMap.containsKey(taskId)
    }

    private fun addTask(task: Task, delay: Int, period: Int, asynchronous: Boolean): TaskHandler {
        return addTask(if (task is PluginTask<*>) task.owner else null, task, delay, period, asynchronous)
    }

    private fun addTask(plugin: Plugin?, task: Runnable, delay: Int, period: Int, asynchronous: Boolean): TaskHandler {
        if (plugin != null && plugin.isDisabled) {
            throw PluginException("Plugin '" + plugin.name + "' attempted to register a task while disabled.")
        }
        if (delay < 0 || period < 0) {
            throw PluginException("Attempted to register a task with negative delay or period.")
        }

        val taskHandler = TaskHandler(plugin, task, nextTaskId(), asynchronous)
        taskHandler.delay = delay
        taskHandler.period = period
        taskHandler.nextRunTick = if (taskHandler.isDelayed) currentTick + taskHandler.delay else currentTick

        if (task is Task) {
            task.handler = taskHandler
        }

        pending.offer(taskHandler)
        taskMap[taskHandler.taskId] = taskHandler

        return taskHandler
    }

    fun mainThreadHeartbeat(currentTick: Int) {
        // Accepts pending.
        while (true) {
            val task = pending.poll() ?: break
            val tick = max(currentTick.toDouble(), task.nextRunTick.toDouble()).toInt() // Do not schedule in the past
            val queue = Utils.getOrCreate(
                queueMap,
                ArrayDeque<TaskHandler>()::class.java, tick
            )
            queue.add(task)
        }
        if (currentTick - this.currentTick > queueMap.size) { // A large number of ticks have passed since the last execution
            for ((tick) in queueMap) {
                if (tick <= currentTick) {
                    runTasks(tick)
                }
            }
        } else { // Normal server tick
            for (i in this.currentTick + 1..currentTick) {
                runTasks(currentTick)
            }
        }
        this.currentTick = currentTick
        AsyncTask.Companion.collectTask()
    }

    private fun runTasks(currentTick: Int) {
        val queue = queueMap.remove(currentTick)
        if (queue != null) {
            for (taskHandler in queue) {
                if (taskHandler.isCancelled) {
                    taskMap.remove(taskHandler.taskId)
                    continue
                } else if (taskHandler.isAsynchronous) {
                    asyncTaskThreadPool.execute(taskHandler.task)
                } else {
                    try {
                        taskHandler.run(currentTick)
                    } catch (e: Throwable) {
                        log.error("Could not execute taskHandler {}", taskHandler.taskId, e)
                    }
                }
                if (taskHandler.isRepeating) {
                    taskHandler.nextRunTick = currentTick + taskHandler.period
                    pending.offer(taskHandler)
                } else {
                    try {
                        val removed = taskMap.remove(taskHandler.taskId)
                        removed?.cancel()
                    } catch (ex: RuntimeException) {
                        log.error("Exception while invoking onCancel", ex)
                    }
                }
            }
        }
    }

    val queueSize: Int
        get() {
            var size = pending.size
            for (queue in queueMap.values) {
                size += queue.size
            }
            return size
        }

    private fun nextTaskId(): Int {
        return currentTaskId.incrementAndGet()
    }

    fun close() {
        asyncTaskThreadPool.shutdownNow()
    }

    companion object : Loggable {
        @JvmField
        var WORKERS: Int = 4
    }
}
