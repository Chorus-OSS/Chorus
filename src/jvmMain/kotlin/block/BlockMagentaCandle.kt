package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties

class BlockMagentaCandle @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockCandle(blockstate) {
    override fun toCakeForm(): Block {
        return BlockMagentaCandleCake()
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.MAGENTA_CANDLE, CommonBlockProperties.CANDLES, CommonBlockProperties.LIT)

    }
}