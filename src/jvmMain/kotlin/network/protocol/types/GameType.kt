package org.chorus_oss.chorus.network.protocol.types

enum class GameType {
    SURVIVAL,
    CREATIVE,
    ADVENTURE,
    SURVIVAL_VIEWER,
    CREATIVE_VIEWER,
    DEFAULT,
    SPECTATOR;

    companion object {
        private val VALUES = entries.toTypedArray()

        @JvmStatic
        fun from(id: Int): GameType {
            return VALUES[id]
        }
    }
}
