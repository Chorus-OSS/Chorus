package org.chorus_oss.chorus.block

class BlockMangrovePlanks @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockPlanks(blockstate) {
    override val name: String
        get() = "Mangrove Planks"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.MANGROVE_PLANKS)
    }
}