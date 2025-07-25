package org.chorus_oss.chorus.level.tickingarea.storage

import org.chorus_oss.chorus.level.tickingarea.TickingArea

interface TickingAreaStorage {
    fun addTickingArea(area: TickingArea)

    fun addTickingArea(vararg areas: TickingArea) {
        for (area in areas) {
            addTickingArea(area)
        }
    }

    fun readTickingArea(): MutableMap<String, TickingArea>

    fun removeTickingArea(name: String)

    fun removeAllTickingArea()

    fun containTickingArea(name: String?): Boolean
}
