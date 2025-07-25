package org.chorus_oss.chorus.experimental.block.generated.definitions

import org.chorus_oss.chorus.experimental.block.BlockDefinition
import org.chorus_oss.chorus.experimental.block.state.CommonStates

object LightGrayCandle :
    BlockDefinition(identifier = "minecraft:light_gray_candle", states = listOf(CommonStates.candles, CommonStates.lit))
