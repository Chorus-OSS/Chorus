package org.chorus_oss.chorus.block

open class BlockDeepslateRedstoneOre @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockRedstoneOre(blockstate) {
    override val name: String
        get() = "Deepslate Redstone Ore"

    override val hardness: Double
        get() = 4.5

    override val litBlock: Block
        get() = BlockLitDeepslateRedstoneOre()

    override val unlitBlock: Block
        get() = BlockDeepslateRedstoneOre()

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.DEEPSLATE_REDSTONE_ORE)
    }
}