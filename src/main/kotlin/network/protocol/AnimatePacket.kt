package org.chorus_oss.chorus.network.protocol

import org.chorus_oss.chorus.network.DataPacket
import org.chorus_oss.chorus.network.PacketDecoder
import org.chorus_oss.chorus.network.PacketEncoder
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.network.connection.util.HandleByteBuf
import org.chorus_oss.chorus.network.protocol.types.ActorRuntimeID

data class AnimatePacket(
    val action: Action,
    val targetRuntimeID: ActorRuntimeID,
    val actionData: Action.ActionData? = null,
) : DataPacket(), PacketEncoder {
    enum class Action(val id: Int) {
        NO_ACTION(0),
        SWING_ARM(1),
        WAKE_UP(3),
        CRITICAL_HIT(4),
        MAGIC_CRITICAL_HIT(5),
        ROW_RIGHT(128),
        ROW_LEFT(129);

        companion object {
            private val ID_LOOKUP: Map<Int, Action> = entries.associateBy { it.id }

            fun fromId(id: Int): Action {
                return ID_LOOKUP[id] ?: throw RuntimeException("Unknown Action ID: $id")
            }
        }

        interface ActionData
        data class RowingData(
            val rowingTime: Float
        ) : ActionData
    }

    override fun encode(byteBuf: HandleByteBuf) {
        byteBuf.writeVarInt(action.id)
        byteBuf.writeActorRuntimeID(this.targetRuntimeID)
        when (this.action) {
            Action.ROW_LEFT,
            Action.ROW_RIGHT -> {
                val actionData = this.actionData as Action.RowingData
                byteBuf.writeFloatLE(actionData.rowingTime)
            }

            else -> Unit
        }

    }

    override fun pid(): Int {
        return ProtocolInfo.ANIMATE_PACKET
    }

    companion object : PacketDecoder<AnimatePacket> {
        override fun decode(byteBuf: HandleByteBuf): AnimatePacket {
            val action: Action
            return AnimatePacket(
                action = Action.fromId(byteBuf.readVarInt()).also { action = it },
                targetRuntimeID = byteBuf.readActorRuntimeID(),
                actionData = when (action) {
                    Action.ROW_LEFT,
                    Action.ROW_RIGHT -> Action.RowingData(
                        rowingTime = byteBuf.readFloatLE()
                    )

                    else -> null
                }
            )
        }
    }
}
