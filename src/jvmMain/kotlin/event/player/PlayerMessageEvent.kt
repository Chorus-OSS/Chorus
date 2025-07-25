package org.chorus_oss.chorus.event.player

abstract class PlayerMessageEvent : PlayerEvent() {
    @JvmField
    var message: String? = null
}
