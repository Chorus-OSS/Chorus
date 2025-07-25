package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.block.BlockCauldron
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.nbt.tag.Tag
import org.chorus_oss.chorus.utils.BlockColor

class BlockEntityCauldron(chunk: IChunk, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt) {
    override fun loadNBT() {
        super.loadNBT()
        if (!namedTag.contains("PotionId")) {
            namedTag.putShort("PotionId", 0xffff)
        }
        val potionId = namedTag.getShort("PotionId").toInt()
        var potionType =
            if ((potionId and 0xFFFF) == 0xFFFF) PotionType.EMPTY.potionTypeData else PotionType.NORMAL.potionTypeData
        if (namedTag.getBoolean("SplashPotion")) {
            potionType = PotionType.SPLASH.potionTypeData
            namedTag.remove("SplashPotion")
        }

        if (!namedTag.contains("PotionType")) {
            namedTag.putShort("PotionType", potionType)
        }
    }

    override fun saveNBT() {
        super.saveNBT()
        namedTag.putShort("PotionId", potionId)
        val potionId = namedTag.getShort("PotionId").toInt()
        val potionType =
            if ((potionId and 0xFFFF) == 0xFFFF) PotionType.EMPTY.potionTypeData else PotionType.NORMAL.potionTypeData
        namedTag.putShort("PotionType", potionType)
    }

    var potionId: Int
        get() = namedTag.getShort("PotionId").toInt()
        set(potionId) {
            namedTag.putShort("PotionId", potionId)
            this.spawnToAll()
        }

    fun hasPotion(): Boolean {
        return (potionId and 0xffff) != 0xffff
    }

    var potionType: Int
        get() = (namedTag.getShort("PotionType").toInt() and 0xFFFF).toShort().toInt()
        set(potionType) {
            namedTag.putShort("PotionType", (potionType and 0xFFFF).toShort().toInt())
        }

    var type: PotionType
        get() = PotionType.getByTypeData(potionType)
        set(type) {
            potionType = type.potionTypeData
        }

    val isSplashPotion: Boolean
        get() = namedTag.getShort("PotionType")
            .toInt() == PotionType.SPLASH.potionTypeData

    var customColor: BlockColor?
        get() {
            if (isCustomColor()) {
                val color = namedTag.getInt("CustomColor")

                val red = (color shr 16) and 0xff
                val green = (color shr 8) and 0xff
                val blue = (color) and 0xff

                return BlockColor(red, green, blue)
            }

            return null
        }
        set(color) {
            setCustomColor(color!!.red, color.green, color.blue)
        }

    fun isCustomColor(): Boolean {
        return namedTag.contains("CustomColor")
    }

    fun setCustomColor(r: Int, g: Int, b: Int) {
        val color = (r shl 16 or (g shl 8) or b) and 0xffffff

        namedTag.putInt("CustomColor", color)

        spawnToAll()
    }

    fun clearCustomColor() {
        namedTag.remove("CustomColor")
        spawnToAll()
    }

    override fun spawnToAll() {
        if (!this.isBlockEntityValid) {
            return
        }
        val block = block as BlockCauldron
        val viewers = level.getChunkPlayers(position.chunkX, position.chunkZ).values.toTypedArray()
        level.sendBlocks(viewers, arrayOf(block.position))
        super.spawnToAll()
        level.scheduler.scheduleTask(null) {
            val cauldron =
                level.getBlockEntity(this.position)
            if (cauldron === this@BlockEntityCauldron) {
                level.sendBlocks(
                    viewers,
                    arrayOf(this.position)
                )
                super.spawnToAll()
            }
        }
    }

    override val isBlockEntityValid: Boolean
        get() {
            val id = block.id
            return id == BlockID.CAULDRON
        }

    override val spawnCompound: CompoundTag
        get() {
            val compoundTag = super.spawnCompound
                .putBoolean("isMovable", this.isMovable)
                .putList("Items", ListTag<Tag<*>>())
                .putShort("PotionId", namedTag.getShort("PotionId").toInt())
                .putShort("PotionType", namedTag.getShort("PotionType").toInt())
            if (namedTag.contains("CustomColor")) {
                compoundTag.putInt("CustomColor", namedTag.getInt("CustomColor") shl 8 shr 8)
            }
            return compoundTag
        }

    enum class PotionType(val potionTypeData: Int = 0) {
        EMPTY(-1),
        NORMAL(0),
        SPLASH(1),
        LINGERING(2),
        LAVA(0xF19B),
        UNKNOWN(-2);

        companion object {
            private val BY_DATA: MutableMap<Int, PotionType>

            init {
                val types = entries.toTypedArray()
                BY_DATA = mutableMapOf()
                for (type in types) {
                    BY_DATA[type.potionTypeData] = type
                }
            }

            fun getByTypeData(typeData: Int): PotionType {
                return BY_DATA.getOrDefault(typeData, UNKNOWN)
            }
        }
    }
}
