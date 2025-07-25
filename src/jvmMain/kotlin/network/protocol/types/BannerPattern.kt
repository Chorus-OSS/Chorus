package org.chorus_oss.chorus.network.protocol.types

import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.utils.DyeColor

@JvmRecord
data class BannerPattern(
    val type: BannerPatternType,
    val color: DyeColor
) {
    companion object {
        fun fromCompoundTag(compoundTag: CompoundTag): BannerPattern {
            val bannerPatternType =
                BannerPatternType.fromCode(if (compoundTag.contains("Pattern")) compoundTag.getString("Pattern") else "bo")
            return BannerPattern(
                bannerPatternType,
                if (compoundTag.contains("Color")) DyeColor.getByDyeData(compoundTag.getInt("Color")) else DyeColor.BLACK
            )
        }
    }
}
