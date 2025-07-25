package org.chorus_oss.chorus.experimental.network.protocol.utils

import java.util.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
operator fun Uuid.Companion.invoke(value: UUID): Uuid {
    return Uuid.fromLongs(value.mostSignificantBits, value.leastSignificantBits)
}