package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.AdventureSettings
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.event.player.PlayerIllegalFlightEvent
import org.chorus_oss.chorus.event.player.PlayerKickEvent
import org.chorus_oss.chorus.event.player.PlayerToggleFlightEvent
import org.chorus_oss.chorus.experimental.network.MigrationPacket
import org.chorus_oss.chorus.network.process.DataPacketProcessor
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.packets.RequestAbilityPacket


class RequestAbilityProcessor : DataPacketProcessor<MigrationPacket<RequestAbilityPacket>>() {
    override fun handle(player: Player, pk: MigrationPacket<RequestAbilityPacket>) {
        val packet = pk.packet

        val player = player.player
        val ability = packet.ability
        if (ability != org.chorus_oss.protocol.types.PlayerAbility.Flying) {
            log.info("[" + player.getEntityName() + "] has tried to trigger " + ability + " ability " + (if (packet.boolValue!!) "on" else "off"))
            return
        }

        if (!Server.instance.allowFlight && packet.boolValue!! && !player.adventureSettings[AdventureSettings.Type.ALLOW_FLIGHT]) {
            val pife = PlayerIllegalFlightEvent(player)
            Server.instance.pluginManager.callEvent(pife)
            if (!pife.isKick) return
            player.kick(PlayerKickEvent.Reason.FLYING_DISABLED, "Flying is not enabled on this server")
            return
        }

        val playerToggleFlightEvent = PlayerToggleFlightEvent(player, packet.boolValue!!)
        Server.instance.pluginManager.callEvent(playerToggleFlightEvent)
        if (playerToggleFlightEvent.cancelled) {
            player.adventureSettings.update()
        } else {
            player.adventureSettings[AdventureSettings.Type.FLYING] = playerToggleFlightEvent.isFlying
        }
    }

    override val packetId: Int = RequestAbilityPacket.id

    companion object : Loggable
}
