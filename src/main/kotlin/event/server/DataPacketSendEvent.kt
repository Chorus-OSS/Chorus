package org.chorus_oss.chorus.event.server

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.network.DataPacket


class DataPacketSendEvent(val player: Player?, val packet: DataPacket) : ServerEvent(), Cancellable {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
