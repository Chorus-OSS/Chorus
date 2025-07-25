package org.chorus_oss.chorus.entity

import org.chorus_oss.chorus.entity.data.EntityFlag


interface EntityAgeable {
    fun getDataFlag(id: EntityFlag?): Boolean
    fun setDataFlag(entityFlag: EntityFlag, value: Boolean)
    fun setScale(scale: Float)

    fun isBaby(): Boolean {
        return this.getDataFlag(EntityFlag.BABY)
    }

    fun setBaby(flag: Boolean) {
        this.setDataFlag(EntityFlag.BABY, flag)
        this.setScale(if (flag) 0.5f else 1f)
    }
}
