package org.chorus_oss.chorus.inventory

enum class SpecialWindowId(@JvmField val id: Int) {
    CREATIVE(-2),
    NONE(-1),
    PLAYER(0),
    OFFHAND(119),
    ARMOR(120),
    CURSOR(124),
    CONTAINER_ID_REGISTRY(125);

    companion object {
        fun getWindowIdById(windowId: Int): SpecialWindowId? {
            for (value in entries) {
                if (value.id == windowId) {
                    return value
                }
            }
            return null
        }
    }
}
