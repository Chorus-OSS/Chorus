package org.chorus_oss.chorus

import org.chorus_oss.chorus.nbt.tag.CompoundTag
import java.util.*

/**
 * Describes an offline player.
 *
 * @see org.chorus_oss.chorus.Player
 */
class OfflinePlayer(override var uuid: UUID) :
    IPlayer {
    private val namedTag: CompoundTag

    init {
        var nbt: CompoundTag?
        nbt = Server.instance.getOfflinePlayerData(uuid, false)
        if (nbt == null) {
            nbt = CompoundTag()
        }
        this.namedTag = nbt

        namedTag.putLong("UUIDMost", uuid.mostSignificantBits)
        namedTag.putLong("UUIDLeast", uuid.leastSignificantBits)
    }

    override val isOnline: Boolean
        get() = this.player != null

    override fun getEntityName(): String {
        if (namedTag.contains("NameTag")) {
            return namedTag.getString("NameTag")
        }
        return "OfflinePlayer"
    }

    override var isOp: Boolean
        get() = Server.instance.isOp(getEntityName().lowercase())
        set(value) {
            if (value) {
                Server.instance.addOp(getEntityName().lowercase())
            } else {
                Server.instance.removeOp(getEntityName().lowercase())
            }
        }

    override var isBanned: Boolean
        get() = Server.instance.bannedPlayers.isBanned(this.getEntityName())
        set(value) {
            if (value) {
                Server.instance.bannedPlayers.addBan(this.getEntityName()!!, null, null, null)
            } else {
                Server.instance.bannedPlayers.remove(this.getEntityName()!!)
            }
        }

    override var isWhitelisted: Boolean
        get() = Server.instance.isWhitelisted(getEntityName()!!.lowercase())
        set(value) {
            if (value) {
                Server.instance.addWhitelist(getEntityName()!!.lowercase())
            } else {
                Server.instance.removeWhitelist(getEntityName()!!.lowercase())
            }
        }

    override val player: Player?
        get() = if (getEntityName() != null) Server.instance.getPlayerExact(getEntityName()!!) else null

    override val firstPlayed: Long
        get() = namedTag.getLong("firstPlayed")

    override val lastPlayed: Long
        get() = namedTag.getLong("lastPlayed")

    override fun hasPlayedBefore(): Boolean {
        return true
    }

}
