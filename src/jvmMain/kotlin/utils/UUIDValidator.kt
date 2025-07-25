package org.chorus_oss.chorus.utils

import java.util.regex.Pattern

object UUIDValidator {
    private val UUID_PATTERN: Pattern = Pattern.compile(
        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    )

    fun isValidUUID(uuid: String?): Boolean {
        if (uuid == null) {
            return false
        }
        return UUID_PATTERN.matcher(uuid).matches()
    }
}
