package org.chorus_oss.chorus.experimental.network.protocol.utils

import org.chorus_oss.chorus.entity.data.EntityDataMap
import org.chorus_oss.chorus.math.BlockVector3
import org.chorus_oss.chorus.math.Vector3f
import org.chorus_oss.protocol.types.BlockPos
import org.chorus_oss.protocol.types.actor_data.ActorDataKey
import org.chorus_oss.protocol.types.actor_data.ActorDataMap

operator fun ActorDataMap.Companion.invoke(value: EntityDataMap): ActorDataMap {
    return ActorDataMap().also {
        val transformed = value.entries.map { (key, data) ->
            var actual = ((key.getTransformer() as? java.util.function.Function<Any, *>?)?.apply(data) ?: data)
            when (actual) {
                is BlockVector3 -> actual = BlockPos(actual.x, actual.y, actual.z)
                is Vector3f -> actual = org.chorus_oss.protocol.types.Vector3f(actual)
            }
            ActorDataKey.entries[key.getValue()] to actual
        }
        it.putAll(transformed)
    }
}