package org.chorus_oss.chorus.experimental.network.protocol.utils.biome

import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.protocol.types.biome.BiomeCoordinateData
import org.chorus_oss.protocol.types.biome.BiomeScatterParamData
import org.chorus_oss.protocol.types.biome.CoordinateEvaluationOrder
import org.chorus_oss.protocol.types.biome.ExpressionOp

operator fun BiomeScatterParamData.Companion.invoke(nbt: CompoundTag): BiomeScatterParamData {
    return BiomeScatterParamData(
        coordinate = nbt.getList("coordinates", CompoundTag::class.java).all.map { BiomeCoordinateData.invoke(it) },
        evalOrder = CoordinateEvaluationOrder.entries[nbt.getInt("evalOrder")],
        chancePercentType = ExpressionOp.entries[nbt.getInt("changePercentType")],
        chancePercent = nbt.getShort("changePercent"),
        chanceNumerator = nbt.getInt("changeNumerator"),
        chanceDenominator = nbt.getInt("changeDenominator"),
        iterationsType = ExpressionOp.entries[nbt.getInt("iterationsType")],
        iterations = nbt.getShort("iterations"),
    )
}