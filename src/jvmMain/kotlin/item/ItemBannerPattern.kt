package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.network.protocol.types.BannerPatternType
import org.jetbrains.annotations.ApiStatus

open class ItemBannerPattern : Item {
    @JvmOverloads
    constructor(meta: Int = 0, count: Int = 1) : super(ItemID.Companion.BANNER_PATTERN, meta, count, "Bone")

    constructor(id: String) : super(id)

    @ApiStatus.Internal
    override fun internalAdjust() {
        val patternType = patternType
        name = patternType!!.patternName + " Pattern"
    }

    override val maxStackSize: Int
        get() = 1

    open val patternType: BannerPatternType?
        get() = BannerPatternType.fromTypeId(damage)
}
