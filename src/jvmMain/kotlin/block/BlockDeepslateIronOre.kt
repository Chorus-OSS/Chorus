package org.chorus_oss.chorus.block

class BlockDeepslateIronOre @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockIronOre(blockstate) {
    override val name: String
        get() = "Deepslate Iron Ore"

    override val hardness: Double
        get() = 4.5

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.DEEPSLATE_IRON_ORE)
    }
}