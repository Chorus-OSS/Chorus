package org.chorus_oss.chorus.entity.ai

import java.util.*

/**
 * 存放一些AI框架的全局参数
 */
object EntityAI {
    private var routeParticleSpawnInterval: Long = 500 //ms

    private val debugOptions: MutableSet<DebugOption> = EnumSet.noneOf(
        DebugOption::class.java
    )

    fun setDebugOption(option: DebugOption, open: Boolean) {
        if (open) debugOptions.add(option)
        else debugOptions.remove(option)
    }

    fun hasDebugOptions(): Boolean {
        return !debugOptions.isEmpty()
    }

    fun checkDebugOption(option: DebugOption): Boolean {
        return debugOptions.contains(option)
    }

    /**
     * Sets route particle spawn interval.(Unit millisecond)
     *
     * @param routeParticleSpawnInterval the route particle spawn interval
     */
    fun setRouteParticleSpawnInterval(routeParticleSpawnInterval: Long) {
        EntityAI.routeParticleSpawnInterval = routeParticleSpawnInterval
    }

    /**
     * Gets route particle spawn interval.
     *
     * @return the route particle spawn interval
     */
    fun getRouteParticleSpawnInterval(): Long {
        return routeParticleSpawnInterval
    }

    enum class DebugOption {
        /**
         * 显示路径点
         */
        ROUTE,

        /**
         * 在生物名称tag中显示behavior状态
         */
        BEHAVIOR,

        /**
         * 允许使用木棍右击生物查询memory状态
         */
        MEMORY
    }
}
