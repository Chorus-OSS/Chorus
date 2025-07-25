package org.chorus_oss.chorus.block

class BlockDeepslateEmeraldOre @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockEmeraldOre(blockstate) {
    override val name: String
        get() = "Deepslate Emerald Ore"

    override val hardness: Double
        get() = 4.5

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.DEEPSLATE_EMERALD_ORE)
    }
}