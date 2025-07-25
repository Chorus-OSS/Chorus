package org.chorus_oss.chorus.level.generator.`object`.legacytree

import org.chorus_oss.chorus.block.BlockState
import org.chorus_oss.chorus.block.BlockWarpedStem
import org.chorus_oss.chorus.block.BlockWarpedWartBlock

class LegacyWarpedTree : LegacyNetherTree() {
    override val trunkBlockState: BlockState
        get() = BlockWarpedStem.properties.defaultState

    override val leafBlockState: BlockState
        get() = BlockWarpedWartBlock.properties.defaultState
}