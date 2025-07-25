package org.chorus_oss.chorus.event.player

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class PlayerChatEvent @JvmOverloads constructor(
    override var player: Player,
    message: String?,
    format: String = "chat.type.text",
    recipients: MutableSet<CommandSender>? = null
) :
    PlayerMessageEvent(), Cancellable {
    var recipients: MutableSet<CommandSender> = HashSet()

    @JvmField
    var format: String

    init {
        this.message = message

        this.format = format

        if (recipients == null) {
            for (permissible in Server.instance.pluginManager.getPermissionSubscriptions(Server.BROADCAST_CHANNEL_USERS)) {
                if (permissible is CommandSender) {
                    this.recipients.add(permissible)
                }
            }
        } else {
            this.recipients = recipients
        }
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
