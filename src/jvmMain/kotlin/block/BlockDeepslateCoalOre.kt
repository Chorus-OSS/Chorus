package org.chorus_oss.chorus.block

class BlockDeepslateCoalOre @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockCoalOre(blockstate) {
    override val name: String
        get() = "Deeplsate Coal Ore"

    override val hardness: Double
        get() = 4.5

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.DEEPSLATE_COAL_ORE)
    }
}