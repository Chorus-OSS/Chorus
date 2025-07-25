package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.math.BlockFace

class BlockWeepingVines @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockVinesNether(blockstate) {
    override val name: String
        get() = "Weeping Vines"

    override val growthDirection: BlockFace
        get() = BlockFace.DOWN

    override var vineAge: Int
        get() = getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.WEEPING_VINES_AGE)
        set(vineAge) {
            setPropertyValue<Int, IntPropertyType>(CommonBlockProperties.WEEPING_VINES_AGE, vineAge)
        }

    override val maxVineAge: Int
        get() = CommonBlockProperties.WEEPING_VINES_AGE.max

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.WEEPING_VINES, CommonBlockProperties.WEEPING_VINES_AGE)
    }
}