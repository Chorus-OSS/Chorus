package org.chorus_oss.chorus.entity.ai.route

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.ai.route.finder.IRouteFinder
import org.chorus_oss.chorus.math.Vector3
import java.util.concurrent.RecursiveAction
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * 寻路管理器，所有的寻路任务都应该提交到这个管理器中，管理器负责调度寻路任务，实现资源利用最大化
 */
class RouteFindingManager private constructor() {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    fun submit(task: RouteFindingTask) {
        task.setStartTime(Server.instance.nextTick)
        scope.launch { task() }
    }

    class RouteFindingTask(private val routeFinder: IRouteFinder, private val onFinish: FinishCallback) :
        RecursiveAction() {
        private val startTime = AtomicLong(0)
        private val started = AtomicBoolean(false)
        private val finished = AtomicBoolean(false)
        var start: Vector3? = null
            private set
        var target: Vector3? = null
            private set

        fun setStart(start: Vector3?): RouteFindingTask {
            this.start = start
            return this
        }

        fun setTarget(target: Vector3?): RouteFindingTask {
            this.target = target
            return this
        }

        /**
         * @return 是否开始寻路
         */
        fun getStarted(): Boolean {
            return started.get()
        }

        fun setStarted(started: Boolean) {
            this.started.set(started)
        }

        /**
         * @return 是否已经完成寻路，寻路失败也会返回完成
         */
        fun getFinished(): Boolean {
            return finished.get()
        }

        protected fun setFinished(finished: Boolean) {
            this.finished.set(finished)
        }

        fun getStartTime(): Long {
            return startTime.get()
        }

        fun setStartTime(startTime: Long): RouteFindingTask {
            this.startTime.set(startTime)
            return this
        }

        override fun compute() {
            setStarted(true)
            routeFinder.start = start
            routeFinder.target = target
            routeFinder.search()
            setFinished(true)
            onFinish.onFinish(this)
        }

        interface FinishCallback {
            fun onFinish(task: RouteFindingTask?)
        }
    }

    companion object {
        private val threadCount = AtomicInteger(0)
        var instance: RouteFindingManager = RouteFindingManager()
            protected set
    }
}
