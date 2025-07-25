package org.chorus_oss.chorus.level.util

import org.chorus_oss.chorus.level.DimensionData
import org.chorus_oss.chorus.level.DimensionEnum
import java.nio.ByteBuffer
import java.nio.ByteOrder

enum class LevelDBKeyUtil(encoded: Char) {
    /**
     * Biome IDs are written as 8-bit integers. No longer written since v1.18.0.
     */
    @Deprecated("No longer written since v1.18.0")
    DATA_2D('-'),

    /**
     * Biomes are stored as palettes similar to blocks. Exactly 25 palettes are written. Biome IDs are written as integers.
     */
    DATA_3D('+'),

    /**
     * Version levelDB key after v1.16.100
     */
    VERSION(','),

    /**
     * Each entry of the biome array contains a biome ID in the first byte, and the final 3 bytes are red/green/blue respectively. No longer written since v1.0.0.
     */
    @Deprecated("No longer written since v1.0.0")
    DATA_2D_LEGACY('.'),

    /**
     * Block data for a 16×16×16 chunk section
     */
    CHUNK_SECTION_PREFIX('/'),

    /**
     * Data ordered in XZY order, unlike Java.
     * No longer written since v1.0.0.
     *
     *
     * Biomes are IDs plus RGB colours similar to Data2DLegacy.
     */
    @Deprecated("No longer written since v1.0.0")
    LEGACY_TERRAIN('0'),

    /**
     * Block entity data (little-endian NBT)
     */
    BLOCK_ENTITIES('1'),

    /**
     * Entity data (little-endian NBT)
     */
    ENTITIES('2'),

    /**
     * Pending tick data (little-endian NBT)
     */
    PENDING_TICKS('3'),

    /**
     * Array of blocks that appear in the same place as other blocks. Used for grass appearing inside snow layers prior to v1.2.13. No longer written as of v1.2.13.
     */
    @Deprecated("No longer written since v1.2.13")
    LEGACY_BLOCK_EXTRA_DATA('4'),

    BIOME_STATE('5'),

    FINALIZED_STATE('6'),

    /**
     * Education Edition Feature
     */
    BORDER_BLOCKS('8'),

    /**
     * Bounding boxes for structure spawns stored in binary format
     */
    HARDCODED_SPAWNERS('9'),

    @Deprecated("Moved to 44 in v1.16.100")
    LEGACY_VERSION('v');

    private val encoded = encoded.code.toByte()

    fun getKey(chunkX: Int, chunkZ: Int): ByteArray {
        return byteArrayOf(
            (chunkX and 0xff).toByte(),
            ((chunkX ushr 8) and 0xff).toByte(),
            ((chunkX ushr 16) and 0xff).toByte(),
            ((chunkX ushr 24) and 0xff).toByte(),
            (chunkZ and 0xff).toByte(),
            ((chunkZ ushr 8) and 0xff).toByte(),
            ((chunkZ ushr 16) and 0xff).toByte(),
            ((chunkZ ushr 24) and 0xff).toByte(),
            this.encoded
        )
    }

    fun getKey(chunkX: Int, chunkZ: Int, dimension: DimensionData): ByteArray {
        if (dimension == DimensionEnum.OVERWORLD.dimensionData) {
            return getKey(chunkX, chunkZ)
        } else {
            val dimensionId = dimension.dimensionId
            return byteArrayOf(
                (chunkX and 0xff).toByte(),
                ((chunkX ushr 8) and 0xff).toByte(),
                ((chunkX ushr 16) and 0xff).toByte(),
                ((chunkX ushr 24) and 0xff).toByte(),
                (chunkZ and 0xff).toByte(),
                ((chunkZ ushr 8) and 0xff).toByte(),
                ((chunkZ ushr 16) and 0xff).toByte(),
                ((chunkZ ushr 24) and 0xff).toByte(),
                (dimensionId and 0xff).toByte(),
                ((dimensionId ushr 8) and 0xff).toByte(),
                ((dimensionId ushr 16) and 0xff).toByte(),
                ((dimensionId ushr 24) and 0xff).toByte(),
                this.encoded
            )
        }
    }

    fun getKey(chunkX: Int, chunkZ: Int, chunkSectionY: Int): ByteArray {
        require(this.encoded == encoded) { "The method must be used with CHUNK_SECTION_PREFIX!" }
        return byteArrayOf(
            (chunkX and 0xff).toByte(),
            ((chunkX ushr 8) and 0xff).toByte(),
            ((chunkX ushr 16) and 0xff).toByte(),
            ((chunkX ushr 24) and 0xff).toByte(),
            (chunkZ and 0xff).toByte(),
            ((chunkZ ushr 8) and 0xff).toByte(),
            ((chunkZ ushr 16) and 0xff).toByte(),
            ((chunkZ ushr 24) and 0xff).toByte(),
            this.encoded,
            chunkSectionY.toByte()
        )
    }

    fun getKey(chunkX: Int, chunkZ: Int, chunkSectionY: Int, dimension: DimensionData): ByteArray {
        if (dimension == DimensionEnum.OVERWORLD.dimensionData) {
            return getKey(chunkX, chunkZ, chunkSectionY)
        } else {
            val dimensionId = dimension.dimensionId.toByte()
            return byteArrayOf(
                (chunkX and 0xff).toByte(),
                ((chunkX ushr 8) and 0xff).toByte(),
                ((chunkX ushr 16) and 0xff).toByte(),
                ((chunkX ushr 24) and 0xff).toByte(),
                (chunkZ and 0xff).toByte(),
                ((chunkZ ushr 8) and 0xff).toByte(),
                ((chunkZ ushr 16) and 0xff).toByte(),
                ((chunkZ ushr 24) and 0xff).toByte(),
                (dimensionId.toInt() and 0xff).toByte(),
                ((dimensionId.toInt() ushr 8) and 0xff).toByte(),
                ((dimensionId.toInt() ushr 16) and 0xff).toByte(),
                ((dimensionId.toInt() ushr 24) and 0xff).toByte(),
                this.encoded,
                chunkSectionY.toByte()
            )
        }
    }

    companion object {
        val NEW_ENTITIES_KEY: ByteArray = "digp".toByteArray()
        val NEW_ENTITIES_INDEX_KEY: ByteArray = "actorprefix".toByteArray()

        fun getEntityKey(entityId: Long): ByteArray {
            val bb = ByteBuffer.allocate(NEW_ENTITIES_INDEX_KEY.size + java.lang.Long.BYTES)
            bb.order(ByteOrder.LITTLE_ENDIAN)
            bb.put(NEW_ENTITIES_INDEX_KEY)
            bb.putLong(entityId)
            return bb.array()
        }

        fun getChunkKey(chunkX: Int, chunkZ: Int): ByteArray {
            return byteArrayOf(
                (chunkX and 0xff).toByte(),
                ((chunkX ushr 8) and 0xff).toByte(),
                ((chunkX ushr 16) and 0xff).toByte(),
                ((chunkX ushr 24) and 0xff).toByte(),
                (chunkZ and 0xff).toByte(),
                ((chunkZ ushr 8) and 0xff).toByte(),
                ((chunkZ ushr 16) and 0xff).toByte(),
                ((chunkZ ushr 24) and 0xff).toByte(),
            )
        }

        fun getChunkKey(chunkX: Int, chunkZ: Int, dimension: DimensionData): ByteArray {
            if (dimension == DimensionEnum.OVERWORLD.dimensionData) {
                return getChunkKey(chunkX, chunkZ)
            } else {
                val dimensionId = dimension.dimensionId.toByte()
                return byteArrayOf(
                    (chunkX and 0xff).toByte(),
                    ((chunkX ushr 8) and 0xff).toByte(),
                    ((chunkX ushr 16) and 0xff).toByte(),
                    ((chunkX ushr 24) and 0xff).toByte(),
                    (chunkZ and 0xff).toByte(),
                    ((chunkZ ushr 8) and 0xff).toByte(),
                    ((chunkZ ushr 16) and 0xff).toByte(),
                    ((chunkZ ushr 24) and 0xff).toByte(),
                    (dimensionId.toInt() and 0xff).toByte(),
                    ((dimensionId.toInt() ushr 8) and 0xff).toByte(),
                    ((dimensionId.toInt() ushr 16) and 0xff).toByte(),
                    ((dimensionId.toInt() ushr 24) and 0xff).toByte()
                )
            }
        }
    }
}
