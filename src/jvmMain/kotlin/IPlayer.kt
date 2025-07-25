package org.chorus_oss.chorus

import org.chorus_oss.chorus.permission.ServerOperator
import java.util.*

/**
 * An interface to describe a player and get its information.
 *
 * This player can be online or offline.
 *
 * @see org.chorus_oss.chorus.Player
 * @see org.chorus_oss.chorus.OfflinePlayer
 */
interface IPlayer : ServerOperator {
    val isOnline: Boolean

    fun getEntityName(): String

    var uuid: UUID

    var isBanned: Boolean

    var isWhitelisted: Boolean

    val player: Player?

    val firstPlayed: Long?

    val lastPlayed: Long?

    fun hasPlayedBefore(): Boolean
}
