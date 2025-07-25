package org.chorus_oss.chorus.block

class BlockDandelion @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockFlower(blockstate) {

    override val uncommonFlower: Block
        get() = get(BlockID.RED_TULIP)

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.DANDELION)
    }
}