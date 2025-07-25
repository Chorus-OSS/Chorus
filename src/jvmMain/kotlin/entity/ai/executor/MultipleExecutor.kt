package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.utils.Loggable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException


class MultipleExecutor : IBehaviorExecutor {
    protected var executors: Set<IBehaviorExecutor>

    constructor(executors: Set<IBehaviorExecutor>) {
        this.executors = executors
    }

    constructor(vararg executors: IBehaviorExecutor) {
        this.executors = setOf(*executors)
    }

    override fun execute(entity: EntityMob): Boolean {
        val tasks = ArrayList<CompletableFuture<*>>()
        for (executor in executors) {
            tasks.add(CompletableFuture.supplyAsync { executor.execute(entity) })
        }
        try {
            return CompletableFuture.allOf(*tasks.toTypedArray<CompletableFuture<*>>())
                .whenComplete { s: Void?, t: Throwable? ->
                    if (t != null) {
                        log.error("阶段执行过程中存在异常：", t)
                    }
                }.thenApply {
                    tasks.stream().map { task: CompletableFuture<*> ->
                        try {
                            return@map task.get() as Boolean
                        } catch (e: InterruptedException) {
                            throw RuntimeException(e)
                        } catch (e: ExecutionException) {
                            throw RuntimeException(e)
                        }
                    }.reduce(false) { a: Boolean, b: Boolean -> a || b }
                }.get()
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        } catch (e: ExecutionException) {
            throw RuntimeException(e)
        }
    }

    companion object : Loggable
}
