package org.chorus_oss.chorus.level

enum class DimensionEnum(@JvmField val dimensionData: DimensionData) {
    OVERWORLD(DimensionData(Level.DIMENSION_OVERWORLD, -64, 319, 24)),
    NETHER(DimensionData(Level.DIMENSION_NETHER, 0, 127, 8)),
    END(DimensionData(Level.DIMENSION_THE_END, 0, 255, 16));

    companion object {
        fun getDataFromId(dimension: Int): DimensionData? {
            for (value in entries) {
                if (value.dimensionData.dimensionId == dimension) {
                    return value.dimensionData
                }
            }
            return null
        }
    }
}
