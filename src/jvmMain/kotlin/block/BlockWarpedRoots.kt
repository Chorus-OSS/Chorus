package org.chorus_oss.chorus.block

class BlockWarpedRoots @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockHanging(blockstate), BlockFlowerPot.FlowerPotBlock, Natural {
    override val name: String
        get() = "Warped Roots"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.WARPED_ROOTS)
    }
}