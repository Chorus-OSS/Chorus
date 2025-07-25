package org.chorus_oss.chorus.permission

import kotlinx.serialization.Serializable
import org.chorus_oss.chorus.Server

@Serializable
class Permission(
    val name: String,
    var description: String = "",
    private var defaultValue: String = DEFAULT_PERMISSION,
    val children: MutableMap<String?, Boolean> = HashMap()
) {
    init {
        this.recalculatePermissibles()
    }

    var default: String
        get() = defaultValue
        set(value) {
            if (value != this.defaultValue) {
                this.defaultValue = value
                this.recalculatePermissibles()
            }
        }

    val permissibles: Set<Permissible>
        get() = Server.instance.pluginManager.getPermissionSubscriptions(this.name)

    fun recalculatePermissibles() {
        val perms = this.permissibles

        Server.instance.pluginManager.recalculatePermissionDefaults(this)

        for (p in perms) {
            p.recalculatePermissions()
        }
    }

    fun addParent(permission: Permission, value: Boolean) {
        children[name] = value
        permission.recalculatePermissibles()
    }

    fun addParent(name: String, value: Boolean): Permission {
        var perm = Server.instance.pluginManager.getPermission(name)
        if (perm == null) {
            perm = Permission(name)
            Server.instance.pluginManager.addPermission(perm)
        }

        this.addParent(perm, value)

        return perm
    }

    companion object {
        const val DEFAULT_OP: String = "op"
        const val DEFAULT_NOT_OP: String = "notop"
        const val DEFAULT_TRUE: String = "true"
        const val DEFAULT_FALSE: String = "false"

        const val DEFAULT_PERMISSION: String = DEFAULT_OP

        fun getByName(value: String): String {
            return when (value.lowercase()) {
                "op", "isop", "operator", "isoperator", "admin", "isadmin" -> DEFAULT_OP
                "!op", "notop", "!operator", "notoperator", "!admin", "notadmin" -> DEFAULT_NOT_OP
                "true" -> DEFAULT_TRUE
                else -> DEFAULT_FALSE
            }
        }

        @JvmOverloads
        fun loadPermissions(data: Map<String, Any>?, defaultValue: String? = DEFAULT_OP): List<Permission> {
            val result: MutableList<Permission> = ArrayList()
            if (data != null) {
                for ((key, value) in data) {
                    val entry = value as Map<String, Any>
                    result.add(loadPermission(key, entry, defaultValue, result))
                }
            }
            return result
        }

        @JvmOverloads
        fun loadPermission(
            name: String,
            data: Map<String, Any>,
            defaultValue: String? = DEFAULT_OP,
            output: MutableList<Permission> = ArrayList()
        ): Permission {
            var defaultValue = defaultValue
            var desc: String? = null
            val children: MutableMap<String?, Boolean> = HashMap()
            if (data.containsKey("default")) {
                val value = getByName(data["default"].toString())
                if (value != null) {
                    defaultValue = value
                } else {
                    throw IllegalStateException("'default' key contained unknown value")
                }
            }

            if (data.containsKey("children")) {
                if (data["children"] is Map<*, *>) {
                    for ((k, v) in (data["children"] as Map<String, Any>)) {
                        if (v is Map<*, *>) {
                            val permission = loadPermission(k, v as Map<String, Any>, defaultValue, output)
                            if (permission != null) {
                                output.add(permission)
                            }
                        }
                        children[k] = true
                    }
                } else {
                    throw IllegalStateException("'children' key is of wrong type")
                }
            }

            if (data.containsKey("description")) {
                desc = data["description"] as String?
            }

            return Permission(name, desc ?: "", defaultValue ?: DEFAULT_PERMISSION, children)
        }
    }
}
