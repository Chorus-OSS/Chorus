package org.chorus_oss.chorus.entity.data

enum class PlayerFlag(private val value: Int) {
    SLEEP(1),
    DEAD(2);

    fun getValue(): Int {
        return value
    }
}
