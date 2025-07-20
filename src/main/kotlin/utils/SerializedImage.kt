package org.chorus_oss.chorus.utils

import org.chorus_oss.chorus.entity.data.Skin

class SerializedImage(val width: Int, val height: Int, val data: ByteArray) {
    companion object {
        val EMPTY: SerializedImage = SerializedImage(0, 0, byteArrayOf())

        fun fromLegacy(skinData: ByteArray): SerializedImage {
            return when (skinData.size) {
                Skin.SINGLE_SKIN_SIZE -> SerializedImage(32, 32, skinData)
                Skin.SKIN_64_32_SIZE -> SerializedImage(64, 32, skinData)
                Skin.DOUBLE_SKIN_SIZE -> SerializedImage(64, 64, skinData)
                Skin.SKIN_128_64_SIZE -> SerializedImage(128, 64, skinData)
                Skin.SKIN_128_128_SIZE -> SerializedImage(128, 128, skinData)
                else -> throw IllegalArgumentException("Unknown legacy skin size")
            }
        }
    }
}
