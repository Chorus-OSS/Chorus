package org.chorus_oss.chorus.block

class BlockDeepslateGoldOre @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockGoldOre(blockstate) {
    override val name: String
        get() = "Deepslate Gold Ore"

    override val hardness: Double
        get() = 4.5

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.DEEPSLATE_GOLD_ORE)
    }
}