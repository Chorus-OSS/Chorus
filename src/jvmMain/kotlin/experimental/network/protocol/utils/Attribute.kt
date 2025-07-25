package org.chorus_oss.chorus.experimental.network.protocol.utils

import org.chorus_oss.protocol.types.attribute.Attribute

operator fun Attribute.Companion.invoke(from: org.chorus_oss.chorus.entity.Attribute): Attribute {
    return Attribute(
        name = from.name,
        value = from.getValue(),
        min = from.minValue,
        max = from.maxValue,
        defaultMin = from.defaultMinimum,
        defaultMax = from.defaultMaximum,
        defaultValue = from.defaultValue,
        modifiers = emptyList(),
    )
}