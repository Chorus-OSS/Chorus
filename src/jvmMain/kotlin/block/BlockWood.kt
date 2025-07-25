package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.WoodType
import org.chorus_oss.chorus.registry.Registries

abstract class BlockWood(blockstate: BlockState) : BlockLog(blockstate) {
    abstract fun getWoodType(): WoodType

    override val name: String
        get() = getWoodType().woodName + " Wood"

    override fun getStrippedState(): BlockState {
        val strippedId = when (getWoodType()) {
            WoodType.OAK -> BlockID.STRIPPED_OAK_WOOD
            WoodType.SPRUCE -> BlockID.STRIPPED_SPRUCE_WOOD
            WoodType.BIRCH -> BlockID.STRIPPED_BIRCH_WOOD
            WoodType.JUNGLE -> BlockID.STRIPPED_JUNGLE_WOOD
            WoodType.ACACIA -> BlockID.STRIPPED_ACACIA_WOOD
            WoodType.DARK_OAK -> BlockID.STRIPPED_DARK_OAK_WOOD
            WoodType.PALE_OAK -> BlockID.STRIPPED_PALE_OAK_WOOD
            WoodType.CHERRY -> BlockID.STRIPPED_CHERRY_WOOD
            WoodType.MANGROVE -> BlockID.STRIPPED_MANGROVE_WOOD
        }
        return Registries.BLOCK.getBlockProperties(strippedId)
            .getBlockState(CommonBlockProperties.PILLAR_AXIS, pillarAxis)
    }
}
