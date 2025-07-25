package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Event


abstract class PlayerEvent : Event() {
    open lateinit var player: Player
        protected set
}
