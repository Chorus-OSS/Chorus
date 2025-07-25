package org.chorus_oss.chorus.experimental.network.protocol.utils

import org.chorus_oss.protocol.types.TrimMaterial
import org.chorus_oss.protocol.types.TrimPattern

operator fun TrimPattern.Companion.invoke(from: org.chorus_oss.chorus.network.protocol.types.TrimPattern): TrimPattern {
    return TrimPattern(
        from.itemName,
        from.patternId
    )
}

operator fun TrimMaterial.Companion.invoke(from: org.chorus_oss.chorus.network.protocol.types.TrimMaterial): TrimMaterial {
    return TrimMaterial(
        from.materialId,
        from.color,
        from.itemName
    )
}