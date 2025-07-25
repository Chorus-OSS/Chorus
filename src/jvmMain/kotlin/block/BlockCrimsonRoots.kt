package org.chorus_oss.chorus.block

class BlockCrimsonRoots @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockHanging(blockstate), BlockFlowerPot.FlowerPotBlock, Natural {
    override val name: String
        get() = "Crimson Roots"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.CRIMSON_ROOTS)
    }
}