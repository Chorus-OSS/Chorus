package org.chorus_oss.chorus.utils

@JvmRecord
data class SemVersion(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val revision: Int,
    val build: Int
) {
    companion object
}