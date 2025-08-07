package org.chorus_oss.chorus.network

import org.chorus_oss.chorus.utils.SemVersion

object ProtocolInfo {
    const val VERSION = "1.21.100"

    val GAME_VERSION = SemVersion(1, 21, 10, 0, 0)

    val BLOCK_STATE_VERSION = (GAME_VERSION.major shl 24) or (GAME_VERSION.minor shl 16) or (GAME_VERSION.patch shl 8)
}