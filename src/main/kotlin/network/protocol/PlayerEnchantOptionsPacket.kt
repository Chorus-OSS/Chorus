package org.chorus_oss.chorus.network.protocol

import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.network.DataPacket
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.network.connection.util.HandleByteBuf
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger


class PlayerEnchantOptionsPacket : DataPacket() {
    var options: List<EnchantOptionData> = ArrayList()

    override fun encode(byteBuf: HandleByteBuf) {
        byteBuf.writeUnsignedVarInt(options.size)
        for (option in this.options) {
            byteBuf.writeUnsignedVarInt(option.minLevel)
            byteBuf.writeInt(0)
            byteBuf.writeUnsignedVarInt(option.enchantments.size)
            for (data in option.enchantments) {
                byteBuf.writeByte(data.id.toByte().toInt())
                byteBuf.writeByte(data.level.toByte().toInt())
            }
            byteBuf.writeUnsignedVarInt(0)
            byteBuf.writeUnsignedVarInt(0)
            byteBuf.writeString(option.enchantName)
            val netid = ENCH_RECIPE_NETID.getAndIncrement()
            byteBuf.writeUnsignedVarInt(netid)
            RECIPE_MAP[netid] = option
        }
    }

    @JvmRecord
    data class EnchantOptionData(
        val minLevel: Int,
        val enchantName: String,
        val enchantments: List<Enchantment>,
        val entry: Int
    )

    override fun pid(): Int {
        return ProtocolInfo.PLAYER_ENCHANT_OPTIONS_PACKET
    }

    companion object {
        const val ENCH_RECIPEID: Int = 100000
        val RECIPE_MAP: ConcurrentHashMap<Int, EnchantOptionData> = ConcurrentHashMap()
        private val ENCH_RECIPE_NETID = AtomicInteger(ENCH_RECIPEID)
    }
}
