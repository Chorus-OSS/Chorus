package org.chorus_oss.chorus.level.vibration

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.math.Vector3


/**
 * 振动监听器
 */
interface VibrationListener {
    /**
     * 返回振动监听器的位置
     *
     * @return Vector3
     */
    val listenerVector: Vector3

    /**
     * 是否响应此振动
     * 若响应，将会从声波源发射声波到监听器位置，并在到达时调用 onVibrationArrive() 方法
     * 请注意，若此方法被调用，则声波必定可到达
     *
     * @param event 振动事件
     * @return boolean
     */
    fun onVibrationOccur(event: VibrationEvent): Boolean

    /**
     * 声波到达事件
     *
     * @param event 振动事件
     */
    fun onVibrationArrive(event: VibrationEvent)

    /**
     * 返回振动监听半径
     *
     * @return double
     */
    val listenRange: Double

    val isEntity: Boolean
        /**
         * 是否是实体
         * 若为实体，则在发送声波粒子时会使用实体专属的nbt tag
         * 若不是，则将此监听器作为方块处理（eg: 潜声传感器）
         *
         * @return boolean
         */
        get() = this is Entity

    /**
     * 在 isEntity() 为true的前提下,返回此振动监听器对应实体对象
     *
     * @return Entity
     */
    fun asEntity(): Entity {
        return this as Entity
    }
}
