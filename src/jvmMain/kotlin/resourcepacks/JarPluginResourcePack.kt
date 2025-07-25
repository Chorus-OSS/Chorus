package org.chorus_oss.chorus.resourcepacks

import com.google.gson.JsonParser
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.utils.Loggable
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * 此类描述了放在jar插件文件内assets/resource_pack目录的资源包相关文件
 */

class JarPluginResourcePack(jarPluginFile: File) : AbstractResourcePack() {
    private var jarPluginFile: File
    private var zippedByteBuffer: ByteBuffer? = null

    override lateinit var sha256: ByteArray
        private set

    override var encryptionKey: String = ""
        private set

    init {
        require(jarPluginFile.exists()) {
            Server.instance.lang
                .tr("chorus.resources.zip.not-found", jarPluginFile.name)
        }

        this.jarPluginFile = jarPluginFile


        try {
            val jar = ZipFile(jarPluginFile)
            val byteArrayOutputStream = ByteArrayOutputStream()
            val zipOutputStream = ZipOutputStream(byteArrayOutputStream)
            val manifest = findManifestInJar(jar)
            requireNotNull(manifest) { Server.instance.lang.tr("chorus.resources.zip.no-manifest") }

            this.manifest = JsonParser
                .parseReader(InputStreamReader(jar.getInputStream(manifest), StandardCharsets.UTF_8))
                .asJsonObject

            val encryptionKeyEntry = jar.getEntry(RESOURCE_PACK_PATH + "encryption.key")
            if (encryptionKeyEntry != null) {
                this.encryptionKey =
                    String(jar.getInputStream(encryptionKeyEntry).readAllBytes(), StandardCharsets.UTF_8)
                log.debug(this.encryptionKey)
            }

            jar.stream().forEach { entry: ZipEntry ->
                if (entry.name.startsWith(RESOURCE_PACK_PATH) && !entry.isDirectory && (entry.name != RESOURCE_PACK_PATH + "encryption.key")) {
                    try {
                        zipOutputStream.putNextEntry(ZipEntry(entry.name.substring(RESOURCE_PACK_PATH.length)))
                        zipOutputStream.write(jar.getInputStream(entry).readAllBytes())
                    } catch (e: IOException) {
                        throw RuntimeException(e)
                    }
                }
            }

            jar.close()
            zipOutputStream.close()
            byteArrayOutputStream.close()

            zippedByteBuffer = ByteBuffer.allocateDirect(byteArrayOutputStream.size())
            val bytes = byteArrayOutputStream.toByteArray()
            zippedByteBuffer!!.put(bytes)
            zippedByteBuffer!!.flip()

            try {
                this.sha256 = MessageDigest.getInstance("SHA-256").digest(bytes)
            } catch (e: Exception) {
                log.error(
                    "Failed to parse the SHA-256 of the resource pack inside of jar plugin {}",
                    jarPluginFile.name,
                    e
                )
            }
        } catch (e: IOException) {
            log.error(
                "An error occurred while loading the resource pack inside of a jar plugin {}",
                jarPluginFile,
                e
            )
        }

        require(this.verifyManifest()) {
            Server.instance.lang
                .tr("chorus.resources.zip.invalid-manifest")
        }
    }

    override val packSize: Int
        get() = zippedByteBuffer!!.limit()

    override fun getPackChunk(offset: Int, length: Int): ByteArray {
        val chunk = if (this.packSize - offset > length) {
            ByteArray(length)
        } else {
            ByteArray(this.packSize - offset)
        }

        try {
            zippedByteBuffer!![offset, chunk]
        } catch (e: Exception) {
            log.error(
                "An error occurred while processing the resource pack {} at offset:{} and length:{}",
                packName, offset, length, e
            )
        }

        return chunk
    }

    override val isAddonPack: Boolean
        get() = false

    override fun cdnUrl(): String {
        return ""
    }

    companion object : Loggable {
        const val RESOURCE_PACK_PATH: String = "assets/resource_pack/"
        fun hasResourcePack(jarPluginFile: File): Boolean {
            return try {
                findManifestInJar(ZipFile(jarPluginFile)) != null
            } catch (e: IOException) {
                false
            }
        }

        protected fun findManifestInJar(jar: ZipFile): ZipEntry? {
            var manifest = jar.getEntry(RESOURCE_PACK_PATH + "manifest.json")
            if (manifest == null) {
                manifest = jar.stream()
                    .filter { e: ZipEntry -> e.name.lowercase().endsWith("manifest.json") && !e.isDirectory }
                    .filter { e: ZipEntry ->
                        val fe = File(e.name)
                        if (!fe.name.equals("manifest.json", ignoreCase = true)) {
                            return@filter false
                        }
                        fe.parent == null || fe.parentFile.parent == null
                    }
                    .findFirst()
                    .orElse(null)
            }
            return manifest
        }
    }
}
