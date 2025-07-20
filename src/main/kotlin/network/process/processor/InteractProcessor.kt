package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.EntityRideable
import org.chorus_oss.chorus.entity.custom.CustomEntity
import org.chorus_oss.chorus.entity.item.EntityChestBoat
import org.chorus_oss.chorus.entity.item.EntityItem
import org.chorus_oss.chorus.entity.item.EntityXpOrb
import org.chorus_oss.chorus.entity.mob.animal.EntityHorse
import org.chorus_oss.chorus.entity.projectile.abstract_arrow.EntityArrow
import org.chorus_oss.chorus.event.player.PlayerHackDetectedEvent
import org.chorus_oss.chorus.event.player.PlayerKickEvent
import org.chorus_oss.chorus.event.player.PlayerMouseOverEntityEvent
import org.chorus_oss.chorus.network.process.PacketProcessor
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.packets.InteractPacket

class InteractProcessor : PacketProcessor<InteractPacket> {
    override fun handle(player: Player, packet: InteractPacket) {

        if (!player.spawned || !player.isAlive()) {
            return
        }

        val targetEntity = player.level!!.getEntity(packet.targetRuntimeID.toLong())

        if (targetEntity == null || !player.isAlive() || !targetEntity.isAlive()) {
            return
        }

        if (targetEntity is EntityItem || targetEntity is EntityArrow || targetEntity is EntityXpOrb) {
            // Custom entities can interact in the client, so they do not kick out the player.
            if (targetEntity is CustomEntity) {
                return
            }

            val event = PlayerHackDetectedEvent(player, PlayerHackDetectedEvent.HackType.INVALID_PVE)
            Server.instance.pluginManager.callEvent(event)

            if (event.isKick) player.kick(
                PlayerKickEvent.Reason.INVALID_PVE,
                "Attempting to interact with an invalid entity"
            )

            log.warn(
                Server.instance.lang.tr("chorus.player.invalidEntity", player.getEntityName())
            )
            return
        }

        when (packet.action) {
            InteractPacket.Companion.Action.InteractUpdate -> {
                if (packet.targetRuntimeID.toLong() == 0L) {
                    return
                }
                Server.instance.pluginManager.callEvent(PlayerMouseOverEntityEvent(player, targetEntity))
            }

            InteractPacket.Companion.Action.StopRiding -> {
                if (targetEntity !is EntityRideable || player.riding == null) {
                    return
                }
                (player.riding as EntityRideable).dismountEntity(player)
            }

            InteractPacket.Companion.Action.OpenInventory -> {
                if (targetEntity is EntityRideable) {
                    if (targetEntity is EntityChestBoat) {
                        player.addWindow(targetEntity.inventory)
                        return
                    } else if (targetEntity is EntityHorse) {
                        if (targetEntity.hasOwner(false) && targetEntity.getOwnerName() == player.getEntityName()) {
                            player.addWindow(targetEntity.inventory)
                            return
                        }
                    }
                } else if (targetEntity.getRuntimeID() != player.getRuntimeID()) {
                    return
                }
                if (!player.player.inventoryOpen) {
                    player.player.inventoryOpen = player.inventory.open(player)
                }
            }

            else -> Unit
        }
    }

    override val packetID: Int = InteractPacket.id

    companion object : Loggable
}
