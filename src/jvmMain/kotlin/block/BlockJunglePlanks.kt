package org.chorus_oss.chorus.block

class BlockJunglePlanks @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockPlanks(blockstate) {

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.JUNGLE_PLANKS)
    }
}