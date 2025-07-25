package org.chorus_oss.chorus.level.generator.`object`.legacytree

import org.chorus_oss.chorus.block.BlockCrimsonStem
import org.chorus_oss.chorus.block.BlockNetherWartBlock
import org.chorus_oss.chorus.block.BlockState

class LegacyCrimsonTree : LegacyNetherTree() {
    override val trunkBlockState: BlockState
        get() = BlockCrimsonStem.properties.defaultState

    override val leafBlockState: BlockState
        get() = BlockNetherWartBlock.properties.defaultState
}
