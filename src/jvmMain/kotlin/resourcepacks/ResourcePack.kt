package org.chorus_oss.chorus.resourcepacks

import java.util.*

interface ResourcePack {
    val packName: String?
    val packId: UUID
    val packVersion: String
    val packSize: Int
    val sha256: ByteArray

    fun getPackChunk(offset: Int, length: Int): ByteArray

    val isAddonPack: Boolean

    fun cdnUrl(): String

    val encryptionKey: String
        get() = ""

    companion object {
        val EMPTY_ARRAY: Array<ResourcePack> = emptyArray()
    }
}
