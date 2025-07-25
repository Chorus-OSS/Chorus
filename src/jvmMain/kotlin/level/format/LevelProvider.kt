package org.chorus_oss.chorus.level.format

import org.chorus_oss.chorus.level.DimensionData
import org.chorus_oss.chorus.level.GameRules
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.Vector3

interface LevelProvider {
    val dimensionData: DimensionData

    val path: String

    fun requestChunkData(x: Int, z: Int): Pair<ByteArray, Int>

    fun getLoadedChunk(x: Int, z: Int): IChunk?

    fun getLoadedChunk(hash: Long): IChunk?

    fun getChunk(x: Int, z: Int): IChunk?

    fun getChunk(x: Int, z: Int, create: Boolean): IChunk?

    fun getEmptyChunk(x: Int, z: Int): IChunk

    fun saveChunks()

    fun saveChunk(x: Int, z: Int)

    fun saveChunk(x: Int, z: Int, chunk: IChunk)

    fun unloadChunks()

    fun loadChunk(x: Int, z: Int): Boolean

    fun loadChunk(x: Int, z: Int, create: Boolean): Boolean

    fun unloadChunk(x: Int, z: Int): Boolean

    fun unloadChunk(x: Int, z: Int, safe: Boolean): Boolean

    fun isChunkGenerated(x: Int, z: Int): Boolean

    fun isChunkPopulated(x: Int, z: Int): Boolean

    fun isChunkLoaded(x: Int, z: Int): Boolean

    fun isChunkLoaded(hash: Long): Boolean

    fun setChunk(chunkX: Int, chunkZ: Int, chunk: IChunk)

    val name: String

    var rainTime: Int

    var thunderTime: Int

    var currentTick: Long

    var time: Long

    var seed: Long

    var spawn: Vector3

    val loadedChunks: Map<Long, IChunk>

    val level: Level

    fun close()

    fun saveLevelData()

    fun updateLevelName(name: String)

    val gamerules: GameRules

    fun setGameRules(rules: GameRules)

    val maximumLayer: Int
        get() = 1 // Vanilla has 2 layers, 0 (Normal), and 1 (Water)

    companion object {
        const val ORDER_YZX: Byte = 0
        const val ORDER_ZXY: Byte = 1
    }
}
