package org.chorus_oss.chorus.level.tickingarea.manager

import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.tickingarea.TickingArea
import org.chorus_oss.chorus.level.tickingarea.storage.TickingAreaStorage

class SimpleTickingAreaManager(storage: TickingAreaStorage) : TickingAreaManager(storage) {
    private var areaMap = storage.readTickingArea()

    override fun addTickingArea(area: TickingArea) {
        areaMap[area.name] = area
        storage.addTickingArea(area)
    }

    override fun removeTickingArea(name: String) {
        areaMap.remove(name)
        storage.removeTickingArea(name)
    }

    override fun removeAllTickingArea() {
        areaMap.clear()
        storage.removeAllTickingArea()
    }

    override fun getTickingArea(name: String): TickingArea? {
        return areaMap[name]
    }

    override fun containTickingArea(name: String): Boolean {
        return areaMap.containsKey(name)
    }

    override val allTickingArea: Set<TickingArea>
        get() = HashSet<TickingArea>(areaMap.values)

    override fun getTickingAreaByChunk(levelName: String, chunkPos: TickingArea.ChunkPos): TickingArea? {
        var matchedArea: TickingArea? = null
        for (area in areaMap.values) {
            val matched = area.levelName == levelName && area.chunks.stream()
                .anyMatch { it == chunkPos }
            if (matched) {
                matchedArea = area
                break
            }
        }
        return matchedArea
    }

    override fun getTickingAreaByPos(pos: Locator): TickingArea? {
        return getTickingAreaByChunk(pos.levelName, TickingArea.ChunkPos(pos.position.chunkX, pos.position.chunkZ))
    }

    override fun loadAllTickingArea() {
        for (area in areaMap.values) if (!area.loadAllChunk()) removeTickingArea(area.name)
    }
}
