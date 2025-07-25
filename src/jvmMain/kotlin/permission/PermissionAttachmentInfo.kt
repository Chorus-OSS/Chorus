package org.chorus_oss.chorus.permission


class PermissionAttachmentInfo(
    permissible: Permissible?,
    permission: String,
    attachment: PermissionAttachment?,
    value: Boolean
) {
    val permissible: Permissible?

    val permission: String

    val attachment: PermissionAttachment?

    val value: Boolean

    init {
        checkNotNull(permission) { "Permission may not be null" }

        this.permissible = permissible
        this.permission = permission
        this.attachment = attachment
        this.value = value
    }
}
