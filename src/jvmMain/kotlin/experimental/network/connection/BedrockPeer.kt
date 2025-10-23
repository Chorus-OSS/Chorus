package org.chorus_oss.chorus.experimental.network.connection

import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.AES
import io.ktor.network.sockets.InetSocketAddress
import kotlinx.io.Buffer
import kotlinx.io.Source
import kotlinx.io.bytestring.ByteString
import kotlinx.io.readByteArray
import kotlinx.io.readByteString
import kotlinx.io.readUByte
import org.chorus_oss.chorus.experimental.network.connection.encryption.EncryptionUtils.createIv
import org.chorus_oss.chorus.experimental.network.connection.encryption.EncryptionUtils.createTrailer
import org.chorus_oss.chorus.experimental.network.connection.compression.Compressor
import org.chorus_oss.chorus.experimental.network.connection.compression.NOOPCompressor
import org.chorus_oss.chorus.experimental.network.connection.compression.SnappyCompressor
import org.chorus_oss.chorus.experimental.network.connection.compression.DeflateCompressor
import org.chorus_oss.chorus.network.connection.BedrockSession
import org.chorus_oss.chorus.network.protocol.types.PacketCompressionAlgorithm
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.core.Packet
import org.chorus_oss.protocol.core.Proto
import org.chorus_oss.protocol.core.types.UByte
import org.chorus_oss.raknet.session.RakSession
import org.chorus_oss.raknet.types.RakPriority
import org.chorus_oss.raknet.types.RakReliability
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi

class BedrockPeer(val rakSession: RakSession) {
    init {
        rakSession.onPacket = this::onPacket
    }

    private var compressor: Compressor? = null

    private var encryption: AES.CTR.Key? = null
    @OptIn(ExperimentalAtomicApi::class)
    private val encryptedCounter: AtomicLong = AtomicLong(0)
    @OptIn(ExperimentalAtomicApi::class)
    private val decryptedCounter: AtomicLong = AtomicLong(0)

    private val sessions: MutableMap<Int, BedrockSession> = mutableMapOf()

    val address: InetSocketAddress
        get() = rakSession.address

    fun close() {
        for (session in sessions.values) {
            session.close(null)
            session.onClose()
        }
    }

    @OptIn(DelicateCryptographyApi::class)
    private fun onPacket(stream: Source) {
        if (stream.exhausted()) return
        if (stream.readUByte().toUInt() != 0xFEu) return

        var data = stream.readByteArray()

        encryption?.let { key ->
            val raw = key.cipher().decryptWithIvBlocking(createIv(key), data)

            data = raw.copyOf(raw.size - 8)
        }

        compressor?.let { compressor ->
            val compressor = when (val alg = data[0].toUByte().toUInt()) {
                0x00u -> compressor as? DeflateCompressor ?: DeflateCompressor()
                0x01u -> compressor as? SnappyCompressor ?: SnappyCompressor()
                0xFFu -> compressor as? NOOPCompressor ?: NOOPCompressor()
                else -> throw IllegalStateException("invalid compression algorithm: $alg")
            }

            data = compressor.decompress(data.copyOfRange(1, data.size))
        }

        val batch = Buffer().run {
            write(data)
            BatchWrapper.deserialize(this)
        }

        for (packet in batch.packets) {
            val id = packet.header.targetSubClientID.toInt()
            val session = sessions.getOrPut(id) { BedrockSession(this, id) }
            session.onPacket(packet)
        }
    }

    @OptIn(ExperimentalAtomicApi::class, DelicateCryptographyApi::class, ExperimentalUnsignedTypes::class)
    fun sendRaw(priority: RakPriority, raw: Buffer) {
        var data = raw.readByteArray()

        compressor?.let { compressor ->
            val compressionByte = when (compressor) {
                is DeflateCompressor -> 0x00u
                is SnappyCompressor -> 0x01u
                is NOOPCompressor -> 0xFFu
            }.toByte()

            data = byteArrayOf(compressionByte) + compressor.compress(data)
        }

        encryption?.let { key ->
            val trailer = createTrailer(
                data,
                key,
                encryptedCounter
            )

            data = key
                .cipher()
                .encryptWithIvBlocking(
                    createIv(key),
                    data + trailer
                )
        }

        data = byteArrayOf(0xFEu.toByte()) + data

        rakSession.send(ByteString(data), RakReliability.ReliableOrdered, priority)
    }

    @OptIn(ExperimentalAtomicApi::class)
    fun send(priority: RakPriority, vararg packets: PacketWrapper) {
        val stream = Buffer()
        val batch = BatchWrapper(packets.toList())
        BatchWrapper.serialize(batch, stream)

        sendRaw(priority, stream)
    }

    fun sendPacket(sender: Int, target: Int, packet: Packet) {
        val wrapper = PacketWrapper(
            header = PacketHeader(
                packetID = packet.id.toUShort(),
                senderSubClientID = sender.toUByte(),
                targetSubClientID = target.toUByte(),
            ),
            packet = packet
        )

        this.send(RakPriority.Normal, wrapper)
    }

    fun sendPacketImmediately(sender: Int, target: Int, packet: Packet) {
        val wrapper = PacketWrapper(
            header = PacketHeader(
                packetID = packet.id.toUShort(),
                senderSubClientID = sender.toUByte(),
                targetSubClientID = target.toUByte(),
            ),
            packet = packet
        )

        this.send(RakPriority.Immediate, wrapper)
    }

    fun removeSession(session: BedrockSession) {
        sessions.remove(session.subClientId)
    }

    fun enableEncryption(key: AES.CTR.Key) {
        check(encryption == null) { "Encryption is already enabled" }

        encryption = key

        log.debug("Encryption enabled for {}", address)
    }

    fun setCompression(algorithm: PacketCompressionAlgorithm) {
        compressor = when (algorithm) {
            PacketCompressionAlgorithm.ZLIB -> DeflateCompressor()
            PacketCompressionAlgorithm.SNAPPY -> SnappyCompressor()
            PacketCompressionAlgorithm.NONE -> NOOPCompressor()
        }
    }

    fun tick() {
        for (session in sessions.values.filter { it.isConnected && it.player == null }) {
            session.tick()
        }
    }

    companion object : Loggable
}