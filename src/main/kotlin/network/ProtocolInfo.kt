package org.chorus_oss.chorus.network

import org.chorus_oss.chorus.utils.SemVersion

object ProtocolInfo {
    const val GAME_VERSION_NET = "1.21.93"
    const val GAME_VERSION_STR = "v$GAME_VERSION_NET"

    val GAME_VERSION = SemVersion(1, 21, 9, 3, 0)

    val BLOCK_STATE_VERSION_NO_REVISION =
        (GAME_VERSION.major shl 24) or (GAME_VERSION.minor shl 16) or (GAME_VERSION.patch shl 8)

    const val START_GAME_PACKET = 11
    const val LEVEL_EVENT_PACKET = 25
    const val CRAFTING_DATA_PACKET = 52
}