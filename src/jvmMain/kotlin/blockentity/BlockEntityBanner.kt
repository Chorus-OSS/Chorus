package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.network.protocol.types.BannerPattern
import org.chorus_oss.chorus.utils.DyeColor

class BlockEntityBanner(chunk: IChunk, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt) {
    var color: Int = 0

    override fun loadNBT() {
        super.loadNBT()
        if (!namedTag.contains("color")) {
            namedTag.putByte("color", 0)
        }

        this.color = namedTag.getByte("color").toInt()
    }

    override val isBlockEntityValid: Boolean
        get() = this.block
            .id == BlockID.WALL_BANNER || this.block
            .id == BlockID.STANDING_BANNER

    override fun saveNBT() {
        super.saveNBT()
        namedTag.putByte("color", this.color)
    }

    override var name: String
        get() = "Banner"
        set(name) {
            super.name = name
        }

    val baseColor: Int
        get() = namedTag.getInt("Base")

    fun setBaseColor(color: DyeColor) {
        namedTag.putInt("Base", color.dyeData and 0x0f)
    }

    var type: Int
        get() = namedTag.getInt("Type")
        set(type) {
            namedTag.putInt("Type", type)
        }

    fun addPattern(pattern: BannerPattern) {
        val patterns = namedTag.getList("Patterns", CompoundTag::class.java)
        patterns.add(
            CompoundTag().putInt
                ("Color", pattern.color.dyeData and 0x0f).putString
                ("Pattern", pattern.type.code)
        )
        namedTag.putList("Patterns", patterns)
    }

    fun getPattern(index: Int): BannerPattern {
        return BannerPattern.fromCompoundTag(
            if (namedTag.getList("Patterns").size() > index && index >= 0) namedTag.getList(
                "Patterns",
                CompoundTag::class.java
            )[index] else CompoundTag()
        )
    }

    fun removePattern(index: Int) {
        val patterns = namedTag.getList("Patterns", CompoundTag::class.java)
        if (patterns.size() > index && index >= 0) {
            patterns.remove(index)
        }
    }

    val patternsSize: Int
        get() = namedTag.getList("Patterns").size()

    override val spawnCompound: CompoundTag
        get() = super.spawnCompound
            .putInt("Base", baseColor)
            .putList("Patterns", namedTag.getList("Patterns"))
            .putInt("Type", type)
            .putByte("color", this.color)

    val dyeColor: DyeColor
        get() = DyeColor.getByWoolData(color)
}
