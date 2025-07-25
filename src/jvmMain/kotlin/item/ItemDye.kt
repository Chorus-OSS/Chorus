package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.utils.DyeColor


open class ItemDye : Item {
    constructor(dyeColor: DyeColor) : this(dyeColor.itemDyeMeta, 1)

    constructor(dyeColor: DyeColor, amount: Int) : this(dyeColor.itemDyeMeta, amount)

    @JvmOverloads
    constructor(meta: Int = 0, amount: Int = 1) : super(
        ItemID.Companion.DYE,
        meta,
        amount,
        if (meta <= 15) DyeColor.getByDyeData(meta).dyeName else DyeColor.getByDyeData(meta).name + " Dye"
    )

    constructor(id: String) : super(id)

    override val isFertilizer: Boolean
        get() = meta == 15

    open val dyeColor: DyeColor
        get() = DyeColor.getByDyeData(meta)
}
