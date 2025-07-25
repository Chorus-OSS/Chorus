package org.chorus_oss.chorus.entity

/**
 * 实现了此接口的实体可飞行
 */
interface EntityFlyable {
    /**
     * @return 是否具有摔落伤害
     */
    fun hasFallingDamage(): Boolean {
        return false
    }
}
