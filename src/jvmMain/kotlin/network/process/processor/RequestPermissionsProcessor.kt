package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.event.player.PlayerHackDetectedEvent
import org.chorus_oss.chorus.experimental.network.protocol.utils.Controllable
import org.chorus_oss.chorus.network.process.PacketProcessor
import org.chorus_oss.protocol.packets.RequestPermissionsPacket

class RequestPermissionsProcessor : PacketProcessor<RequestPermissionsPacket> {
    override fun handle(player: Player, packet: RequestPermissionsPacket) {
        if (!player.isOp) {
            val event =
                PlayerHackDetectedEvent(player, PlayerHackDetectedEvent.HackType.PERMISSION_REQUEST)
            Server.instance.pluginManager.callEvent(event)

            if (event.isKick) player.kick("Illegal permission operation", true)

            return
        }
        val player = Server.instance.onlinePlayers.values.find { it.getUniqueID() == packet.entityUniqueID }
        if (player != null && player.isOnline) {
            for (controllableAbility in org.chorus_oss.protocol.types.PlayerAbility.Controllable) {
                player.adventureSettings[controllableAbility] =
                    (packet.requestedPermissions and controllableAbility.bit) != 0.toUShort()
            }
            player.adventureSettings.playerPermission = packet.permissionLevel
            player.adventureSettings.update()
        }
    }

    override val packetID: Int = RequestPermissionsPacket.id
}
