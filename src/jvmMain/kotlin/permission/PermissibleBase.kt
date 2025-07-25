package org.chorus_oss.chorus.permission

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.plugin.Plugin
import org.chorus_oss.chorus.utils.PluginException
import org.chorus_oss.chorus.utils.ServerException


class PermissibleBase(opable: ServerOperator?) : Permissible {
    var opable: ServerOperator? = null

    private var parent: Permissible? = null

    private val attachments: MutableSet<PermissionAttachment> = HashSet()

    private val permissions: MutableMap<String?, PermissionAttachmentInfo> = HashMap()

    init {
        this.opable = opable
        if (opable is Permissible) {
            this.parent = opable
        }
    }

    override var isOp: Boolean
        get() = this.opable != null && opable!!.isOp
        set(value) {
            if (this.opable == null) {
                throw ServerException("Cannot change op value as no ServerOperator is set")
            } else {
                opable!!.isOp = value
            }
        }

    override fun isPermissionSet(name: String): Boolean {
        return permissions.containsKey(name)
    }

    override fun isPermissionSet(permission: Permission): Boolean {
        return this.isPermissionSet(permission.name)
    }

    override fun hasPermission(name: String): Boolean {
        if (this.isPermissionSet(name)) {
            return permissions[name]!!.value
        }

        val perm = Server.instance.pluginManager.getPermission(name)

        if (perm != null) {
            val permission = perm.default

            return Permission.DEFAULT_TRUE == permission || (this.isOp && Permission.DEFAULT_OP == permission) || (!this.isOp && Permission.DEFAULT_NOT_OP == permission)
        } else {
            return Permission.DEFAULT_TRUE == Permission.DEFAULT_PERMISSION || (this.isOp && Permission.DEFAULT_OP == Permission.DEFAULT_PERMISSION) || (!this.isOp && Permission.DEFAULT_NOT_OP == Permission.DEFAULT_PERMISSION)
        }
    }

    override fun hasPermission(permission: Permission): Boolean {
        return this.hasPermission(permission.name)
    }

    override fun addAttachment(plugin: Plugin): PermissionAttachment {
        return this.addAttachment(plugin, null, null)
    }

    override fun addAttachment(plugin: Plugin, name: String?): PermissionAttachment {
        return this.addAttachment(plugin, name, null)
    }

    override fun addAttachment(plugin: Plugin, name: String?, value: Boolean?): PermissionAttachment {
        if (!plugin.isEnabled) {
            throw PluginException("Plugin " + plugin.description.name + " is disabled")
        }

        val result = PermissionAttachment(plugin, if (this.parent != null) this.parent else this)
        attachments.add(result)
        if (name != null && value != null) {
            result.setPermission(name, value)
        }
        this.recalculatePermissions()

        return result
    }

    override fun removeAttachment(attachment: PermissionAttachment) {
        if (attachments.contains(attachment)) {
            attachments.remove(attachment)
            this.recalculatePermissions()
        }
    }

    override fun recalculatePermissions() {
        this.clearPermissions()
        val defaults = Server.instance.pluginManager.getDefaultPermissions(
            isOp
        )
        Server.instance.pluginManager.subscribeToDefaultPerms(
            this.isOp,
            this.parent ?: this
        )

        for (perm in defaults.values) {
            val name = perm.name
            permissions[name] = PermissionAttachmentInfo(
                if (this.parent != null) this.parent else this,
                name, null, true
            )
            Server.instance.pluginManager.subscribeToPermission(
                name,
                this.parent ?: this
            )
            this.calculateChildPermissions(perm.children, false, null)
        }

        for (attachment in this.attachments) {
            this.calculateChildPermissions(attachment.getPermissions(), false, attachment)
        }
    }

    fun clearPermissions() {
        for (name in permissions.keys) {
            Server.instance.pluginManager.unsubscribeFromPermission(
                name!!,
                this.parent ?: this
            )
        }

        Server.instance.pluginManager.unsubscribeFromDefaultPerms(
            false,
            this.parent ?: this
        )
        Server.instance.pluginManager.unsubscribeFromDefaultPerms(
            true,
            this.parent ?: this
        )

        permissions.clear()
    }

    private fun calculateChildPermissions(
        children: Map<String?, Boolean>,
        invert: Boolean,
        attachment: PermissionAttachment?
    ) {
        for ((key, v) in children) {
            val name = key!!
            val perm = Server.instance.pluginManager.getPermission(name)
            val value = (v xor invert)
            permissions[name] = PermissionAttachmentInfo(
                if (this.parent != null) parent else this,
                name,
                attachment,
                value
            )
            Server.instance.pluginManager.subscribeToPermission(
                name,
                this.parent ?: this
            )

            if (perm != null) {
                this.calculateChildPermissions(perm.children, !value, attachment)
            }
        }
    }

    val effectivePermissions: Map<String?, PermissionAttachmentInfo>
        get() = this.permissions
}
