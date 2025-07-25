package org.chorus_oss.chorus.utils

import java.util.concurrent.ConcurrentHashMap

object ThreadStore {
    val store: MutableMap<String, Any> = ConcurrentHashMap()
}
