package org.chorus_oss.chorus.block

class BlockFloweringAzalea @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockAzalea(blockstate) {
    override val name: String
        get() = "FloweringAzalea"

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.FLOWERING_AZALEA)
    }
}
