package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockBlueCandle @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockCandle(blockstate) {
    override fun toCakeForm(): Block {
        return BlockBlueCandleCake()
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.BLUE_CANDLE, CommonBlockProperties.CANDLES, CommonBlockProperties.LIT)
    }
}