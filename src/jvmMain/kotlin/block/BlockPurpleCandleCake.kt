package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockPurpleCandleCake @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockCandleCake(blockstate) {
    override fun toCandleForm(): BlockCandle {
        return BlockPurpleCandle()
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.PURPLE_CANDLE_CAKE, CommonBlockProperties.LIT)

    }
}