package org.chorus_oss.chorus.entity

/**
 * 实现了此接口的生物可游泳
 */
interface EntitySwimmable {
    /**
     * @return 此实体是否会受到溺水伤害
     */
    fun canDrown(): Boolean {
        return false
    }
}
