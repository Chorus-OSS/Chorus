package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.item.ItemTool

open class BlockWoodenPressurePlate @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockPressurePlateBase(blockstate) {
    init {
        this.onPitch = 0.8f
        this.offPitch = 0.7f
    }

    override val name: String
        get() = "Oak Pressure Plate"

    override val toolType: Int
        get() = ItemTool.TYPE_AXE

    override val hardness: Double
        get() = 0.5

    override val resistance: Double
        get() = 0.5

    override fun computeRedstoneStrength(): Int {
        val bb = collisionBoundingBox

        for (entity in level.getCollidingEntities(bb!!)) {
            if (entity.doesTriggerPressurePlate()) {
                return 15
            }
        }

        return 0
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.WOODEN_PRESSURE_PLATE, CommonBlockProperties.REDSTONE_SIGNAL)
    }
}