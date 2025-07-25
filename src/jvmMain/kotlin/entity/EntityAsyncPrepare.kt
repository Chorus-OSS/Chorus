package org.chorus_oss.chorus.entity

/**
 * This method will be called asynchronously and in parallel for entities to perform tick-independent operations
 */
interface EntityAsyncPrepare {
    /**
     * This method will be executed in parallel, every moment, and ensure that it is executed before every onUpdate
     */
    fun asyncPrepare(currentTick: Int)
}
