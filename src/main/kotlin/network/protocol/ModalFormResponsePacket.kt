package org.chorus_oss.chorus.network.protocol

import org.chorus_oss.chorus.network.DataPacket
import org.chorus_oss.chorus.network.PacketDecoder
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.network.connection.util.HandleByteBuf


class ModalFormResponsePacket : DataPacket() {
    var formId: Int = 0
    var data: String = "null"
    var cancelReason: Int = 0

    override fun pid(): Int {
        return ProtocolInfo.MODAL_FORM_RESPONSE_PACKET
    }

    companion object : PacketDecoder<ModalFormResponsePacket> {
        override fun decode(byteBuf: HandleByteBuf): ModalFormResponsePacket {
            val packet = ModalFormResponsePacket()
            packet.formId = byteBuf.readVarInt()
            if (byteBuf.readBoolean()) {
                packet.data = byteBuf.readString()
            }
            if (byteBuf.readBoolean()) {
                packet.cancelReason = byteBuf.readByte().toInt()
            }
            return packet
        }
    }
}
