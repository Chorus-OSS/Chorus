package org.chorus_oss.chorus.entity

import org.chorus_oss.chorus.Player

interface EntityInteractable {
    // Todo: Passive entity?? i18n and boat leaving text
    fun getInteractButtonText(player: Player): String

    fun canDoInteraction(): Boolean
}
