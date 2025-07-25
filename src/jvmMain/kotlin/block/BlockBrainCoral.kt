package org.chorus_oss.chorus.block

open class BlockBrainCoral @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockCoral(blockstate) {

    override fun isDead(): Boolean {
        return false
    }

    override fun getDeadCoral(): Block {
        return BlockDeadBrainCoral()
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.BRAIN_CORAL)
    }
}