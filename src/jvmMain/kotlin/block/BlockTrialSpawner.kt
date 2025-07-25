package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockTrialSpawner(blockstate: BlockState) : Block(blockstate) {
    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.TRIAL_SPAWNER,
            CommonBlockProperties.OMINOUS,
            CommonBlockProperties.TRIAL_SPAWNER_STATE
        )
    }
}