package org.chorus_oss.chorus.block

class BlockDeepslateCopperOre @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockCopperOre(blockstate) {
    override val name: String
        get() = "Deepslate Copper Ore"

    override val hardness: Double
        get() = 4.5

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.DEEPSLATE_COPPER_ORE)
    }
}