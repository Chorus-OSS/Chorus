package org.chorus_oss.chorus.block

abstract class BlockStem(blockstate: BlockState) : BlockLog(blockstate) {
    override val hardness: Double
        get() = 2.0

    override val resistance: Double
        get() = 2.0
}
