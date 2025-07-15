package org.chorus_oss.chorus.experimental.network.protocol.utils.biome

import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.protocol.types.biome.BiomeCoordinateData
import org.chorus_oss.protocol.types.biome.ExpressionOp
import org.chorus_oss.protocol.types.biome.RandomDistributionType

operator fun BiomeCoordinateData.Companion.invoke(nbt: CompoundTag): BiomeCoordinateData {
    return BiomeCoordinateData(
        minValueType = ExpressionOp.entries[nbt.getInt("maxValueType")],
        minValue = nbt.getShort("minValue"),
        maxValueType = ExpressionOp.entries[nbt.getInt("maxValueType")],
        maxValue = nbt.getShort("maxValue"),
        gridOffset = nbt.getInt("gridOffset").toUInt(),
        gridStepSize = nbt.getInt("gridStepSize").toUInt(),
        distribution = RandomDistributionType.entries[nbt.getInt("maxDistributionType")],
    )
}