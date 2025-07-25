package org.chorus_oss.chorus.resourcepacks

import com.google.gson.JsonParser
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.utils.Loggable
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile


class ZippedResourcePack(file: File) : AbstractResourcePack() {
    private var file: File
    private var byteBuffer: ByteBuffer? = null

    override lateinit var sha256: ByteArray
        private set
    override var encryptionKey: String = ""
        private set


    init {
        require(file.exists()) {
            Server.instance.lang.tr("chorus.resources.zip.not-found", file.name)
        }

        this.file = file

        try {
            this.sha256 = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(this.file.toPath()))
        } catch (e: Exception) {
            log.error("Failed to parse the SHA-256 of the resource pack {}", file, e)
        }

        try {
            ZipFile(file).use { zip ->
                loadZip(zip)
            }
        } catch (exception: ZipException) {
            if (exception.message == "invalid CEN header (bad entry name)") {
                try {
                    ZipFile(file, Charset.forName("GBK")).use { zip ->
                        loadZip(zip)
                    }
                } catch (e: IOException) {
                    log.error("An error occurred while loading the zipped resource pack {}", file, e)
                }
            } else {
                log.error(
                    "An error occurred while loading the zipped resource pack {}",
                    file,
                    exception
                )
            }
        } catch (e: IOException) {
            log.error("An error occurred while loading the zipped resource pack {}", file, e)
        }

        require(this.verifyManifest()) {
            Server.instance.lang
                .tr("chorus.resources.zip.invalid-manifest")
        }
    }

    @Throws(IOException::class)
    private fun loadZip(zip: ZipFile) {
        var entry = zip.getEntry("manifest.json")
        if (entry == null) {
            entry = zip.stream()
                .filter { e: ZipEntry -> e.name.lowercase().endsWith("manifest.json") && !e.isDirectory }
                .filter { e: ZipEntry ->
                    val fe = File(e.name)
                    if (!fe.name.equals("manifest.json", ignoreCase = true)) {
                        return@filter false
                    }
                    fe.parent == null || fe.parentFile.parent == null
                }
                .findFirst()
                .orElseThrow {
                    IllegalArgumentException(
                        Server.instance.lang.tr("chorus.resources.zip.no-manifest")
                    )
                }
        }

        this.manifest = JsonParser
            .parseReader(InputStreamReader(zip.getInputStream(entry), StandardCharsets.UTF_8))
            .asJsonObject
        val parentFolder = file.parentFile
        if (parentFolder == null || !parentFolder.isDirectory) {
            throw IOException("Invalid resource pack path")
        }
        val keyFile = File(parentFolder, file.name + ".key")
        if (keyFile.exists()) {
            this.encryptionKey = String(Files.readAllBytes(keyFile.toPath()), StandardCharsets.UTF_8)
            log.debug(this.encryptionKey)
        }

        val bytes = Files.readAllBytes(file.toPath())
        //使用java nio bytebuffer以获得更好性能
        byteBuffer = ByteBuffer.allocateDirect(bytes.size)
        byteBuffer!!.put(bytes)
        byteBuffer!!.flip()
    }

    override fun getPackChunk(offset: Int, length: Int): ByteArray {
        val chunk = if (this.packSize - offset > length) {
            ByteArray(length)
        } else {
            ByteArray(this.packSize - offset)
        }

        try {
            byteBuffer!![offset, chunk]
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

    override val packSize: Int
        get() = file.length().toInt()

    companion object : Loggable
}
