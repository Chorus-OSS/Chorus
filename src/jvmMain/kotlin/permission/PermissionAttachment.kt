package org.chorus_oss.chorus.permission

import org.chorus_oss.chorus.plugin.Plugin
import org.chorus_oss.chorus.utils.PluginException


class PermissionAttachment(plugin: Plugin, permissible: Permissible?) {
    private val permissions: MutableMap<String?, Boolean> = HashMap()

    private val permissible: Permissible?

    val plugin: Plugin

    init {
        if (!plugin.isEnabled) {
            throw PluginException("Plugin " + plugin.description.name + " is disabled")
        }
        this.permissible = permissible
        this.plugin = plugin
    }

    fun getPermissions(): Map<String?, Boolean> {
        return permissions
    }

    fun clearPermissions() {
        permissions.clear()
        permissible!!.recalculatePermissions()
    }

    fun setPermissions(permissions: Map<String, Boolean>) {
        for ((key, value) in permissions) {
            this.permissions[key] = value
        }
        permissible!!.recalculatePermissions()
    }

    fun unsetPermissions(permissions: List<String?>) {
        for (node in permissions) {
            this.permissions.remove(node)
        }
        permissible!!.recalculatePermissions()
    }

    fun setPermission(permission: Permission, value: Boolean) {
        this.setPermission(permission.name, value)
    }

    fun setPermission(name: String?, value: Boolean) {
        if (permissions.containsKey(name)) {
            if (permissions[name] == value) {
                return
            }
            permissions.remove(name)
        }
        permissions[name] = value
        permissible!!.recalculatePermissions()
    }

    fun unsetPermission(permission: Permission, value: Boolean) {
        this.unsetPermission(permission.name, value)
    }

    fun unsetPermission(name: String?, value: Boolean) {
        if (permissions.containsKey(name)) {
            permissions.remove(name)
            permissible!!.recalculatePermissions()
        }
    }

    fun remove() {
        permissible!!.removeAttachment(this)
    }
}
