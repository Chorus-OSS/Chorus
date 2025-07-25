package org.chorus_oss.chorus.entity

import org.chorus_oss.chorus.nbt.tag.CompoundTag

class EntityTimer {
    var timeStamp: Long = 0L
    var hasExecuted: Boolean = false
    var countTime: Int = 0

    constructor()
    constructor(timer: CompoundTag) {
        this.timeStamp = timer.getLong(TAG_TIME_STAMP)
        this.hasExecuted = timer.getBoolean(TAG_HAS_EXECUTED)
        this.countTime = timer.getInt(TAG_COUNT_TIME)
    }

    fun get(): CompoundTag {
        val timer: CompoundTag = CompoundTag()
        timer.putLong(TAG_TIME_STAMP, this.timeStamp)
        timer.putBoolean(TAG_HAS_EXECUTED, this.hasExecuted)
        timer.putInt(TAG_COUNT_TIME, this.countTime)
        return timer
    }

    companion object {
        const val TAG_TIME_STAMP: String = "TimeStamp"
        const val TAG_HAS_EXECUTED: String = "HasExecuted"
        const val TAG_COUNT_TIME: String = "CountTime"
    }
}
