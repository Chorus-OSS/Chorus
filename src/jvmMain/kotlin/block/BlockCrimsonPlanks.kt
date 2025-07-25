package org.chorus_oss.chorus.block

class BlockCrimsonPlanks @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockPlanks(blockstate) {
    override val name: String
        get() = "Crimson Planks"

    override val resistance: Double
        get() = 3.0

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.CRIMSON_PLANKS)
    }
}