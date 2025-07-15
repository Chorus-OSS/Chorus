package org.chorus_oss.chorus.experimental.network.protocol.utils

import org.chorus_oss.chorus.network.protocol.types.PropertySyncData
import org.chorus_oss.protocol.types.ActorProperties

operator fun ActorProperties.Companion.invoke(value: PropertySyncData): ActorProperties {
    return ActorProperties(
        intProperties = value.intProperties.mapIndexed { i, v -> ActorProperties.Companion.IntProperty(i.toUInt(), v) },
        floatProperties = value.floatProperties.mapIndexed { i, v ->
            ActorProperties.Companion.FloatProperty(
                i.toUInt(),
                v
            )
        },
    )
}