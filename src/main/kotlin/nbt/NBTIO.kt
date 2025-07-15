package org.chorus_oss.chorus.nbt

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.block.BlockState
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.Item.Companion.get
import org.chorus_oss.chorus.item.UnknownItem
import org.chorus_oss.chorus.level.updater.block.BlockStateUpdaters.updateBlockState
import org.chorus_oss.chorus.level.updater.item.ItemUpdaters.updateItem
import org.chorus_oss.chorus.nbt.stream.NBTInputStream
import org.chorus_oss.chorus.nbt.stream.NBTOutputStream
import org.chorus_oss.chorus.nbt.stream.PGZIPOutputStream
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.LinkedCompoundTag
import org.chorus_oss.chorus.nbt.tag.Tag
import org.chorus_oss.chorus.nbt.tag.TreeMapCompoundTag
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.registry.Registries
import org.chorus_oss.chorus.utils.HashUtils
import java.io.*
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream
import java.util.zip.GZIPInputStream

/**
 * A Named Binary Tag library for Nukkit Project
 */

object NBTIO {
    @JvmOverloads
    fun putItemHelper(item: Item, slot: Int? = null): CompoundTag {
        val tag = CompoundTag()
            .putByte("Count", item.getCount())
            .putShort("Damage", item.damage)
        tag.putString("Name", item.id)
        if (slot != null) {
            tag.putByte("Slot", slot)
        }
        if (item.hasCompoundTag()) {
            tag.putCompound("tag", item.namedTag!!)
        }
        //ItemID same with blockID, represent this is a pure blockitem,
        //and only blockitem need to be written `Block` to NBT,
        //whereas for `minecraft:potato` item, the corresponding block is `minecraft:potatos`
        //these items do not need to be written
        if (item.isBlock() && item.blockId == item.id) {
            tag.putCompound("Block", item.getSafeBlockState().blockStateTag.copy())
        }
        tag.putInt("version", ProtocolInfo.BLOCK_STATE_VERSION_NO_REVISION)
        return tag
    }

    fun getItemHelper(tag: CompoundTag?): Item {
        var tag1 = tag ?: return Item.AIR

        val name = tag1.getString("Name")
        if (name.isBlank() || name == BlockID.AIR) {
            return Item.AIR
        }
        if (!tag1.containsByte("Count")) {
            return Item.AIR
        }

        //upgrade item
        if (tag1.contains("version")) {
            val ver = tag1.getInt("version")
            if (ver < ProtocolInfo.BLOCK_STATE_VERSION_NO_REVISION) {
                tag1 = updateItem(tag1, ProtocolInfo.BLOCK_STATE_VERSION_NO_REVISION)
            }
        }

        val damage = (if (!tag1.containsShort("Damage")) 0 else tag1.getShort("Damage")).toInt()
        val amount = tag1.getByte("Count").toInt()
        var item = get(name, damage, amount)
        val tagTag = tag1["tag"]
        if (!item.isNothing && tagTag is CompoundTag && !tagTag.isEmpty) {
            item.setNamedTag(tagTag)
        }

        if (tag1.containsCompound("Block")) {
            var block = tag1.getCompound("Block")
            val isUnknownBlock = block.getString("name") == BlockID.UNKNOWN && block.containsCompound("Block")
            if (isUnknownBlock) {
                block = block.getCompound("Block") //originBlock
            }
            //upgrade block
            if (block.contains("version")) {
                val ver = block.getInt("version")
                if (ver < ProtocolInfo.BLOCK_STATE_VERSION_NO_REVISION) {
                    block = updateBlockState(block, ProtocolInfo.BLOCK_STATE_VERSION_NO_REVISION)
                }
            }
            val blockState = getBlockStateHelper(block)
            if (blockState != null) {
                if (isUnknownBlock) { //restore unknown block item
                    item = blockState.toItem()
                    if (damage != 0) {
                        item.damage = damage
                    }
                    item.setCount(amount)
                }
                item.blockState = blockState
            } else if (item.isNothing) { //write unknown block item
                item = UnknownItem(BlockID.UNKNOWN, damage, amount)
                val compoundTag = LinkedCompoundTag()
                    .putString("name", block.getString("name"))
                    .putCompound("states", TreeMapCompoundTag(block.getCompound("states").tags))
                val hash = HashUtils.fnv1a_32_nbt(compoundTag)
                compoundTag.putInt("version", block.getInt("version"))
                val unknownBlockState = BlockState.makeUnknownBlockState(hash, compoundTag)
                item.blockState = unknownBlockState
            }
        } else {
            if (item.isNothing) { //write unknown item
                item = UnknownItem(BlockID.UNKNOWN, damage, amount)
                item.getOrCreateNamedTag().putCompound("Item", CompoundTag().putString("Name", name))
            } else if (item.id == BlockID.UNKNOWN && item.getOrCreateNamedTag()
                    .containsCompound("Item")
            ) { //restore unknown item
                val removeTag = item.namedTag!!.removeAndGet<CompoundTag>("Item")
                val originItemName = removeTag!!.getString("Name")
                val originItem = get(originItemName, damage, amount)
                originItem.setNamedTag(item.namedTag)
                item = originItem
            }
        }
        return item
    }

    fun getBlockStateHelper(tag: CompoundTag): BlockState? {
        return Registries.BLOCKSTATE[HashUtils.fnv1a_32_nbt_palette(tag)]
    }

    @JvmOverloads
    @Throws(IOException::class)
    fun read(file: File, endianness: ByteOrder = ByteOrder.BIG_ENDIAN): CompoundTag? {
        if (!file.exists()) return null
        FileInputStream(file).use { inputStream ->
            return read(inputStream, endianness)
        }
    }

    @JvmOverloads
    @Throws(IOException::class)
    fun read(data: ByteArray, endianness: ByteOrder = ByteOrder.BIG_ENDIAN): CompoundTag {
        ByteArrayInputStream(data).use { inputStream ->
            return read(inputStream, endianness)
        }
    }

    @Throws(IOException::class)
    fun read(data: ByteArray, endianness: ByteOrder, network: Boolean): CompoundTag {
        ByteArrayInputStream(data).use { inputStream ->
            return read(inputStream, endianness, network)
        }
    }

    @JvmOverloads
    @Throws(IOException::class)
    fun read(
        inputStream: InputStream,
        endianness: ByteOrder = ByteOrder.BIG_ENDIAN,
        network: Boolean = false
    ): CompoundTag {
        val tag = NBTInputStream(inputStream, endianness, network).readTag()
        if (tag is CompoundTag) {
            return tag
        }
        throw IOException("Root tag must be a named compound tag")
    }

    @JvmOverloads
    @Throws(IOException::class)
    fun readCompressed(data: ByteArray, endianness: ByteOrder = ByteOrder.BIG_ENDIAN): CompoundTag {
        ByteArrayInputStream(data).use { bytes ->
            BufferedInputStream(bytes).use { buffered ->
                GZIPInputStream(buffered).use { gzip ->
                    return read(gzip, endianness, false)
                }
            }
        }
    }

    @JvmOverloads
    @Throws(IOException::class)
    fun readCompressed(inputStream: InputStream, endianness: ByteOrder = ByteOrder.BIG_ENDIAN): CompoundTag {
        val gzip: InputStream = GZIPInputStream(inputStream)
        val buffered: InputStream = BufferedInputStream(gzip)
        return read(buffered, endianness)
    }

    @JvmOverloads
    @Throws(IOException::class)
    fun readNetworkCompressed(inputStream: InputStream, endianness: ByteOrder = ByteOrder.BIG_ENDIAN): CompoundTag {
        val gzip: InputStream = GZIPInputStream(inputStream)
        val buffered: InputStream = BufferedInputStream(gzip)
        return read(buffered, endianness)
    }

    @JvmOverloads
    @Throws(IOException::class)
    fun readNetworkCompressed(data: ByteArray, endianness: ByteOrder = ByteOrder.BIG_ENDIAN): CompoundTag {
        ByteArrayInputStream(data).use { bytes ->
            GZIPInputStream(bytes).use { gzip ->
                BufferedInputStream(gzip).use { buffered ->
                    return read(buffered, endianness, true)
                }
            }
        }
    }

    @JvmOverloads
    @Throws(IOException::class)
    fun write(
        tags: Collection<CompoundTag>,
        endianness: ByteOrder = ByteOrder.BIG_ENDIAN,
        network: Boolean = false
    ): ByteArray {
        val baos = ByteArrayOutputStream(1024)
        NBTOutputStream(baos, endianness, network).use { stream ->
            for (tag in tags) {
                stream.writeTag(tag)
            }
            return baos.toByteArray()
        }
    }

    @JvmOverloads
    @Throws(IOException::class)
    fun write(tag: CompoundTag, endianness: ByteOrder = ByteOrder.BIG_ENDIAN, network: Boolean = false): ByteArray {
        val baos = ByteArrayOutputStream(1024)
        NBTOutputStream(baos, endianness, network).use { stream ->
            stream.writeTag(tag)
            return baos.toByteArray()
        }
    }

    @JvmOverloads
    @Throws(IOException::class)
    fun write(tag: CompoundTag, file: File, endianness: ByteOrder = ByteOrder.BIG_ENDIAN) {
        FileOutputStream(file).use { outputStream ->
            write(tag, outputStream, endianness)
        }
    }

    @JvmOverloads
    @Throws(IOException::class)
    fun write(
        tag: CompoundTag,
        outputStream: OutputStream?,
        endianness: ByteOrder = ByteOrder.BIG_ENDIAN,
        network: Boolean = false
    ) {
        val stream = NBTOutputStream(outputStream, endianness, network)
        stream.writeTag(tag)
    }

    @Throws(IOException::class)
    fun write(tags: Collection<CompoundTag>, outputStream: OutputStream?, endianness: ByteOrder, network: Boolean) {
        NBTOutputStream(outputStream, endianness, network).use { stream ->
            for (tag in tags) {
                stream.writeTag(tag)
            }
        }
    }

    @Throws(IOException::class)
    fun writeNetwork(tag: CompoundTag): ByteArray {
        val baos = ByteArrayOutputStream(1024)
        NBTOutputStream(baos, ByteOrder.LITTLE_ENDIAN, true).use { stream ->
            stream.writeTag(tag)
        }
        return baos.toByteArray()
    }

    @JvmOverloads
    @Throws(IOException::class)
    fun writeGZIPCompressed(tag: CompoundTag, endianness: ByteOrder = ByteOrder.BIG_ENDIAN): ByteArray {
        val baos = ByteArrayOutputStream(1024)
        writeGZIPCompressed(tag, baos, endianness)
        return baos.toByteArray()
    }

    @JvmOverloads
    @Throws(IOException::class)
    fun writeGZIPCompressed(
        tag: CompoundTag,
        outputStream: OutputStream?,
        endianness: ByteOrder = ByteOrder.BIG_ENDIAN
    ) {
        val gzip = PGZIPOutputStream(outputStream)
        write(tag, gzip, endianness)
        gzip.finish()
    }

    @JvmOverloads
    @Throws(IOException::class)
    fun writeNetworkGZIPCompressed(tag: CompoundTag, endianness: ByteOrder = ByteOrder.BIG_ENDIAN): ByteArray {
        val baos = ByteArrayOutputStream(1024)
        writeNetworkGZIPCompressed(tag, baos, endianness)
        return baos.toByteArray()
    }

    @JvmOverloads
    @Throws(IOException::class)
    fun writeNetworkGZIPCompressed(
        tag: CompoundTag,
        outputStream: OutputStream?,
        endianness: ByteOrder = ByteOrder.BIG_ENDIAN
    ) {
        val gzip = PGZIPOutputStream(outputStream)
        write(tag, gzip, endianness, true)
        gzip.finish()
    }

    @JvmOverloads
    @Throws(IOException::class)
    fun writeZLIBCompressed(
        tag: CompoundTag,
        outputStream: OutputStream,
        endianness: ByteOrder = ByteOrder.BIG_ENDIAN
    ) {
        writeZLIBCompressed(tag, outputStream, Deflater.DEFAULT_COMPRESSION, endianness)
    }

    @JvmOverloads
    @Throws(IOException::class)
    fun writeZLIBCompressed(
        tag: CompoundTag,
        outputStream: OutputStream,
        level: Int,
        endianness: ByteOrder = ByteOrder.BIG_ENDIAN
    ) {
        val out = DeflaterOutputStream(outputStream, Deflater(level))
        write(tag, out, endianness)
        out.finish()
    }

    @Throws(IOException::class)
    fun safeWrite(tag: CompoundTag, file: File) {
        val tmpFile = File(file.absolutePath + "_tmp")
        if (tmpFile.exists()) {
            tmpFile.delete()
        }
        write(tag, tmpFile)
        Files.move(tmpFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
    }

    /**
     * The following methods
     * only used for LevelEventGenericPacket
     * which do not write/read tag id and name
     */
    @JvmOverloads
    @Throws(IOException::class)
    fun writeValue(
        tag: CompoundTag,
        endianness: ByteOrder = ByteOrder.BIG_ENDIAN,
        network: Boolean = false
    ): ByteArray {
        val baos = ByteArrayOutputStream(1024)
        NBTOutputStream(baos, endianness, network).use { stream ->
            stream.writeValue(tag)
            return baos.toByteArray()
        }
    }

    @Throws(IOException::class)
    fun readValue(inputStream: InputStream, endianness: ByteOrder, network: Boolean): CompoundTag {
        val nbtInputStream = NBTInputStream(inputStream, endianness, network)
        return nbtInputStream.readValue(Tag.TAG_COMPOUND.toInt())
    }

    @Throws(IOException::class)
    fun readTreeMapCompoundTag(inputStream: InputStream, endianness: ByteOrder, network: Boolean): TreeMapCompoundTag {
        val nbtInputStream = NBTInputStream(inputStream, endianness, network)
        val nbt = nbtInputStream.readTag()
        if (nbt is CompoundTag) {
            return TreeMapCompoundTag(nbt.tags)
        }
        throw IOException("Root tag must be a named compound tag")
    }

    @Throws(IOException::class)
    fun readCompressedTreeMapCompoundTag(inputStream: InputStream, endianness: ByteOrder): TreeMapCompoundTag {
        GZIPInputStream(inputStream).use { gzip ->
            BufferedInputStream(gzip).use { buffered ->
                return readTreeMapCompoundTag(buffered, endianness, false)
            }
        }
    }
}
