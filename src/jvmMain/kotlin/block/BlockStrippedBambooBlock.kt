package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.WoodType
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.math.BlockFace

class BlockStrippedBambooBlock @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockWoodStripped(blockState) {
    override val name: String
        get() = "Stripped Bamboo Block"

    override fun getWoodType(): WoodType {
        throw UnsupportedOperationException()
    }

    override fun getStrippedState(): BlockState {
        return blockState
    }

    override fun canBeActivated(): Boolean {
        return false
    }

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        return false
    }

    override val hardness: Double
        get() = 2.0

    override val resistance: Double
        get() = 15.0

    override val burnChance: Int
        get() = 5

    override val burnAbility: Int
        get() = 20

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.STRIPPED_BAMBOO_BLOCK, CommonBlockProperties.PILLAR_AXIS)
    }
}