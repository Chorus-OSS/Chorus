package org.chorus_oss.chorus.block

class BlockDeadHornCoral @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockHornCoral(blockstate) {

    override fun isDead() = true

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.DEAD_HORN_CORAL)
    }
}