package org.chorus_oss.chorus.permission

import org.chorus_oss.chorus.plugin.Plugin


interface Permissible : ServerOperator {
    fun isPermissionSet(name: String): Boolean

    fun isPermissionSet(permission: Permission): Boolean

    fun hasPermission(name: String): Boolean

    fun hasPermission(permission: Permission): Boolean

    fun addAttachment(plugin: Plugin): PermissionAttachment

    fun addAttachment(plugin: Plugin, name: String?): PermissionAttachment

    fun addAttachment(plugin: Plugin, name: String?, value: Boolean?): PermissionAttachment

    fun removeAttachment(attachment: PermissionAttachment)

    fun recalculatePermissions()
}
