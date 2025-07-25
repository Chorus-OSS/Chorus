package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockYellowCandleCake @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockCandleCake(blockstate) {
    override fun toCandleForm(): BlockCandle {
        return BlockYellowCandle()
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.YELLOW_CANDLE_CAKE, CommonBlockProperties.LIT)

    }
}