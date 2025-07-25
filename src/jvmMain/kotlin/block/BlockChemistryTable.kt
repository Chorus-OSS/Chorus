package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockChemistryTable @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    Block(blockState) {

    override val properties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.CHEMISTRY_TABLE,
            CommonBlockProperties.CHEMISTRY_TABLE_TYPE,
            CommonBlockProperties.DIRECTION
        )
    }
}