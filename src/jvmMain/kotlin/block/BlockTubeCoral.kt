package org.chorus_oss.chorus.block

open class BlockTubeCoral @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockCoral(blockstate) {

    override fun isDead() = false

    override fun getDeadCoral() = BlockDeadTubeCoral()

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.TUBE_CORAL)
    }
}