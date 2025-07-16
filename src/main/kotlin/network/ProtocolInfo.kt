package org.chorus_oss.chorus.network

import org.chorus_oss.chorus.utils.SemVersion

object ProtocolInfo {
    const val GAME_VERSION_NET = "1.21.93"
    const val GAME_VERSION_STR = "v$GAME_VERSION_NET"

    val GAME_VERSION = SemVersion(1, 21, 9, 3, 0)

    val BLOCK_STATE_VERSION_NO_REVISION =
        (GAME_VERSION.major shl 24) or (GAME_VERSION.minor shl 16) or (GAME_VERSION.patch shl 8)

    const val LOGIN_PACKET = 1
    const val START_GAME_PACKET = 11
    const val LEVEL_EVENT_PACKET = 25
    const val ENTITY_EVENT_PACKET = 27
    const val MOB_EFFECT_PACKET = 28
    const val UPDATE_ATTRIBUTES_PACKET = 29
    const val ANIMATE_PACKET = 44
    const val CRAFTING_DATA_PACKET = 52
    const val AVAILABLE_COMMANDS_PACKET = 76
    const val SET_SCORE_PACKET = 108
    const val MOVE_ENTITY_DELTA_PACKET = 111
    const val LEVEL_SOUND_EVENT_PACKET = 123
    const val LEVEL_EVENT_GENERIC_PACKET = 124
    const val PLAYER_ENCHANT_OPTIONS_PACKET = 146
}