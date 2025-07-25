package org.chorus_oss.chorus.level.tickingarea

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.level.tickingarea.manager.TickingAreaManager
import java.util.concurrent.ThreadLocalRandom

class TickingArea(name: String, levelName: String, vararg chunks: ChunkPos) {
    var name: String
        private set
    var levelName: String
        private set
    var chunks: MutableSet<ChunkPos> = HashSet()
        private set

    init {
        if (name.isNotEmpty()) this.name = name
        else {
            var randomName = randomName()
            val manager: TickingAreaManager = Server.instance.tickingAreaManager
            while (manager.containTickingArea(randomName)) randomName = randomName()
            this.name = randomName
        }
        this.levelName = levelName
        for (chunk in chunks) {
            addChunk(chunk)
        }
    }

    fun addChunk(chunk: ChunkPos) {
        chunks.add(chunk)
    }

    fun loadAllChunk(): Boolean {
        if (!Server.instance.loadLevel(levelName)) return false
        val level = Server.instance.getLevelByName(levelName)
        for (pos in chunks) {
            level?.loadChunk(pos.x, pos.z)
        }
        return true
    }

    // two entry [0] => min, [1] => max
    fun minAndMaxChunkPos(): List<ChunkPos> {
        val min = ChunkPos(Integer.MAX_VALUE, Integer.MAX_VALUE)
        val max = ChunkPos(Integer.MIN_VALUE, Integer.MIN_VALUE)
        for (pos in chunks) {
            if (pos.x < min.x) min.x = pos.x
            if (pos.z < min.z) min.z = pos.z
            if (pos.x > max.x) max.x = pos.x
            if (pos.z > max.z) max.z = pos.z
        }
        return listOf(min, max)
    }

    private fun randomName(): String {
        return "Area" + ThreadLocalRandom.current().nextInt(0, java.lang.Short.MAX_VALUE - java.lang.Short.MIN_VALUE)
    }

    class ChunkPos(var x: Int, var z: Int) {
        override fun equals(other: Any?): Boolean {
            if (other is ChunkPos) return other.x == this.x && other.z == this.z
            return false
        }

        override fun hashCode(): Int {
            return x xor (z shl 12)
        }
    }
}
