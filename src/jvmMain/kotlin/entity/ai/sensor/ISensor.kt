package org.chorus_oss.chorus.entity.ai.sensor

import org.chorus_oss.chorus.entity.ai.memory.IMemoryStorage
import org.chorus_oss.chorus.entity.mob.EntityMob

/**
 * 此接口抽象了一个传感器 <br></br>
 * 传感器用于搜集环境信息并向记忆存储器[IMemoryStorage]写入一个记忆[org.chorus_oss.chorus.entity.ai.memory.MemoryType]
 *
 *
 * This interface abstracts a sensor<br></br>
 * The sensor is used to collect environmental information and write a memory [org.chorus_oss.chorus.entity.ai.memory.MemoryType] to the memory storage [IMemoryStorage]
 */
fun interface ISensor {
    /**
     * @param entity 目标实体
     */
    fun sense(entity: EntityMob)

    val period: Int
        /**
         * 返回此传感器的刷新周期，小的刷新周期会使得传感器被更频繁的调用
         *
         *
         * Returns the refresh period of this sensor, a small refresh period will make the sensor be called more frequently
         *
         * @return 刷新周期
         */
        get() = 1
}
