package org.chorus_oss.chorus.command

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.event.server.ConsoleCommandOutputEvent
import org.chorus_oss.chorus.lang.CommandOutputContainer
import org.chorus_oss.chorus.lang.TextContainer
import org.chorus_oss.chorus.lang.TranslationContainer
import org.chorus_oss.chorus.level.GameRule
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.Transform
import org.chorus_oss.chorus.permission.PermissibleBase
import org.chorus_oss.chorus.permission.Permission
import org.chorus_oss.chorus.permission.PermissionAttachment
import org.chorus_oss.chorus.plugin.Plugin
import org.chorus_oss.chorus.utils.Loggable


open class ConsoleCommandSender : CommandSender {
    private val perm: PermissibleBase = PermissibleBase(this)

    override fun isPermissionSet(name: String): Boolean {
        return perm.isPermissionSet(name)
    }

    override fun isPermissionSet(permission: Permission): Boolean {
        return perm.isPermissionSet(permission)
    }

    override fun hasPermission(name: String): Boolean {
        return perm.hasPermission(name)
    }

    override fun hasPermission(permission: Permission): Boolean {
        return perm.hasPermission(permission)
    }

    override fun addAttachment(plugin: Plugin): PermissionAttachment {
        return perm.addAttachment(plugin)
    }

    override fun addAttachment(plugin: Plugin, name: String?): PermissionAttachment {
        return perm.addAttachment(plugin, name)
    }

    override fun addAttachment(plugin: Plugin, name: String?, value: Boolean?): PermissionAttachment {
        return perm.addAttachment(plugin, name, value)
    }

    override fun removeAttachment(attachment: PermissionAttachment) {
        perm.removeAttachment(attachment)
    }

    override fun recalculatePermissions() {
        perm.recalculatePermissions()
    }

    override val isPlayer: Boolean
        get() = false

    override fun sendMessage(message: String) {
        for (line in message.trim { it <= ' ' }.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            log.info(line)
        }
    }

    override fun sendMessage(message: TextContainer) {
        this.sendMessage(Server.instance.lang.tr(message))
    }

    override fun sendCommandOutput(container: CommandOutputContainer) {
        if (this.transform.level.gameRules.getBoolean(GameRule.SEND_COMMAND_FEEDBACK)) {
            for (msg in container.messages) {
                var text = Server.instance.lang.tr(TranslationContainer(msg.messageId, *msg.parameters))
                val event = ConsoleCommandOutputEvent(this, text)
                Server.instance.pluginManager.callEvent(event)
                if (event.cancelled) continue
                text = event.message
                this.sendMessage(text)
            }
        }
    }

    override val locator: Locator
        get() = Locator(Server.instance.defaultLevel!!)

    override val transform: Transform
        get() = Transform(Server.instance.defaultLevel!!)

    override val senderName get() = "CONSOLE"

    override var isOp = true
        set(_) {}

    companion object : Loggable
}
