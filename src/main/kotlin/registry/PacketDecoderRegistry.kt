package org.chorus_oss.chorus.registry

import org.chorus_oss.chorus.network.DataPacket
import org.chorus_oss.chorus.network.PacketDecoder
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.network.protocol.*
import java.util.concurrent.atomic.AtomicBoolean

class PacketDecoderRegistry : IRegistry<Int, PacketDecoder<out DataPacket>?, PacketDecoder<out DataPacket>> {
    private val packets = mutableMapOf<Int, PacketDecoder<out DataPacket>>()
    private val initialized = AtomicBoolean(false)

    override fun init() {
        if (initialized.getAndSet(true)) return

        // Register all packets that are Client -> Server
        register(ProtocolInfo.ANIMATE_PACKET, AnimatePacket)
        register(ProtocolInfo.ENTITY_EVENT_PACKET, EntityEventPacket)
        register(ProtocolInfo.LOGIN_PACKET, LoginPacket)
        register(ProtocolInfo.PLAYER_SKIN_PACKET, PlayerSkinPacket)
    }

    override operator fun get(key: Int): PacketDecoder<out DataPacket>? {
        return packets[key]
    }

    override fun reload() {
        initialized.set(false)
        packets.clear()
        init()
    }

    @Throws(RegisterException::class)
    override fun register(key: Int, value: PacketDecoder<*>) {
        packets[key] = value
    }
}