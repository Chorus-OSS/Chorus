package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.AdventureSettings
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.BlockFrame
import org.chorus_oss.chorus.block.BlockLectern
import org.chorus_oss.chorus.event.player.*
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.math.BlockFace.Companion.fromIndex
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.network.process.PacketProcessor
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.packets.PlayerActionPacket
import org.chorus_oss.protocol.types.PlayerActionType


class PlayerActionProcessor : PacketProcessor<PlayerActionPacket> {
    override fun handle(player: Player, packet: PlayerActionPacket) {
        if (!player.spawned || (!player.isAlive() && packet.actionType != PlayerActionType.Respawn && packet.actionType != PlayerActionType.ChangeDimensionACK)) {
            return
        }

        if (packet.entityRuntimeID != player.getRuntimeID().toULong()) {
            return
        }

        val pos = Vector3(packet.blockPosition)
        val face = fromIndex(packet.blockFace)

        run switch@{
            when (packet.actionType) {
                PlayerActionType.StartDestroyBlock -> {
                    if (Server.instance.getServerAuthoritativeMovement() > 0) {
                        return
                    }

                    player.player.onBlockBreakStart(pos, face)
                }

                PlayerActionType.AbortDestroyBlock, PlayerActionType.StopDestroyBlock -> {
                    if (Server.instance.getServerAuthoritativeMovement() > 0) {
                        return
                    }

                    player.player.onBlockBreakAbort(pos)
                }

                PlayerActionType.CreativeDestroyBlock -> {
                    // Used by client to get book from lecterns and items from item frame in creative mode since 1.20.70
                    val blockLectern = player.player.level!!.getBlock(pos)
                    if (blockLectern is BlockLectern && blockLectern.position.distance(player.player.position) <= 6) {
                        blockLectern.dropBook(player.player)
                    }
                    if (blockLectern is BlockFrame && blockLectern.blockEntity != null) {
                        blockLectern.blockEntity!!.dropItem(player.player)
                    }
                    if (Server.instance.getServerAuthoritativeMovement() > 0) return@switch //ServerAuthorInput not use player

                    player.player.onBlockBreakComplete(Vector3(packet.blockPosition).asBlockVector3(), face)
                }

                PlayerActionType.ContinueDestroyBlock -> {
                    if (Server.instance.getServerAuthoritativeMovement() > 0) {
                        return
                    }

                    player.player.onBlockBreakContinue(pos, face)
                }

                PlayerActionType.GetUpdatedBlock -> {
                    // TODO
                }

                PlayerActionType.DropItem -> {
                    // TODO
                }

                PlayerActionType.StopSleeping -> player.stopSleep()
                PlayerActionType.Respawn -> {
                    if (!player.spawned || player.isAlive() || !player.isOnline) {
                        return
                    }
                    player.respawn()
                }

                PlayerActionType.StartJump -> {
                    if (Server.instance.getServerAuthoritativeMovement() > 0) {
                        return
                    }

                    val playerJumpEvent = PlayerJumpEvent(player)
                    Server.instance.pluginManager.callEvent(playerJumpEvent)
                }

                PlayerActionType.StartSprinting -> {
                    if (Server.instance.getServerAuthoritativeMovement() > 0) {
                        return
                    }

                    val playerToggleSprintEvent = PlayerToggleSprintEvent(player, true)
                    Server.instance.pluginManager.callEvent(playerToggleSprintEvent)
                    if (playerToggleSprintEvent.cancelled) {
                        player.sendData(player)
                    } else {
                        player.setSprinting(true)
                    }
                }

                PlayerActionType.StopSprinting -> {
                    if (Server.instance.getServerAuthoritativeMovement() > 0) {
                        return
                    }

                    val playerToggleSprintEvent = PlayerToggleSprintEvent(player, false)
                    Server.instance.pluginManager.callEvent(playerToggleSprintEvent)
                    if (playerToggleSprintEvent.cancelled) {
                        player.sendData(player)
                    } else {
                        player.setSprinting(false)
                    }
                }

                PlayerActionType.StartSneaking -> {
                    if (Server.instance.getServerAuthoritativeMovement() > 0) {
                        return
                    }

                    val playerToggleSneakEvent = PlayerToggleSneakEvent(player, true)
                    Server.instance.pluginManager.callEvent(playerToggleSneakEvent)
                    if (playerToggleSneakEvent.cancelled) {
                        player.sendData(player)
                    } else {
                        player.setSneaking(true)
                    }
                }

                PlayerActionType.StopSneaking -> {
                    if (Server.instance.getServerAuthoritativeMovement() > 0) {
                        return
                    }

                    val playerToggleSneakEvent = PlayerToggleSneakEvent(player, false)
                    Server.instance.pluginManager.callEvent(playerToggleSneakEvent)
                    if (playerToggleSneakEvent.cancelled) {
                        player.sendData(player)
                    } else {
                        player.setSneaking(false)
                    }
                }

                PlayerActionType.ChangeDimensionACK -> player.sendPosition(
                    player.position,
                    player.rotation.yaw,
                    player.rotation.pitch,
                    org.chorus_oss.protocol.packets.MovePlayerPacket.Companion.Mode.Normal
                )

                PlayerActionType.StartGliding -> {
                    if (Server.instance.getServerAuthoritativeMovement() > 0) {
                        return
                    }

                    val playerToggleGlideEvent = PlayerToggleGlideEvent(player, true)
                    Server.instance.pluginManager.callEvent(playerToggleGlideEvent)
                    if (playerToggleGlideEvent.cancelled) {
                        player.sendData(player)
                    } else {
                        player.setGliding(true)
                    }
                }

                PlayerActionType.StopGliding -> {
                    if (Server.instance.getServerAuthoritativeMovement() > 0) {
                        return
                    }

                    val playerToggleGlideEvent = PlayerToggleGlideEvent(player, false)
                    Server.instance.pluginManager.callEvent(playerToggleGlideEvent)
                    if (playerToggleGlideEvent.cancelled) {
                        player.sendData(player)
                    } else {
                        player.setGliding(false)
                    }
                }

                PlayerActionType.StartSwimming -> {
                    if (Server.instance.getServerAuthoritativeMovement() > 0) {
                        return
                    }

                    val ptse = PlayerToggleSwimEvent(player, true)
                    Server.instance.pluginManager.callEvent(ptse)

                    if (ptse.cancelled) {
                        player.sendData(player)
                    } else {
                        player.setSwimming(true)
                    }
                }

                PlayerActionType.StopSwimming -> {
                    if (Server.instance.getServerAuthoritativeMovement() > 0) {
                        return
                    }

                    val ev = PlayerToggleSwimEvent(player, false)
                    Server.instance.pluginManager.callEvent(ev)

                    if (ev.cancelled) {
                        player.sendData(player)
                    } else {
                        player.setSwimming(false)
                    }
                }

                PlayerActionType.StartSpinAttack -> {
                    if (player.inventory.itemInHand.id != ItemID.TRIDENT) {
                        player.sendPosition(
                            player.position,
                            player.rotation.yaw,
                            player.rotation.pitch,
                            org.chorus_oss.protocol.packets.MovePlayerPacket.Companion.Mode.Reset
                        )
                        return@switch
                    }

                    val riptideLevel =
                        player.inventory.itemInHand.getEnchantmentLevel(Enchantment.ID_TRIDENT_RIPTIDE)
                    if (riptideLevel < 1) {
                        player.sendPosition(
                            player.position,
                            player.rotation.yaw,
                            player.rotation.pitch,
                            org.chorus_oss.protocol.packets.MovePlayerPacket.Companion.Mode.Reset
                        )
                        return@switch
                    }

                    if (!(player.isTouchingWater() || (player.level!!.isRaining && player.level!!.canBlockSeeSky(player.position)))) {
                        player.sendPosition(
                            player.position,
                            player.rotation.yaw,
                            player.rotation.pitch,
                            org.chorus_oss.protocol.packets.MovePlayerPacket.Companion.Mode.Reset
                        )
                        return@switch
                    }

                    val playerToggleSpinAttackEvent = PlayerToggleSpinAttackEvent(player, true)
                    Server.instance.pluginManager.callEvent(playerToggleSpinAttackEvent)

                    if (playerToggleSpinAttackEvent.cancelled) {
                        player.sendPosition(
                            player.position,
                            player.rotation.yaw,
                            player.rotation.pitch,
                            org.chorus_oss.protocol.packets.MovePlayerPacket.Companion.Mode.Reset
                        )
                    } else {
                        player.setSpinAttacking(true)
                        val riptideSound = if (riptideLevel >= 3) {
                            Sound.ITEM_TRIDENT_RIPTIDE_3
                        } else if (riptideLevel == 2) {
                            Sound.ITEM_TRIDENT_RIPTIDE_2
                        } else {
                            Sound.ITEM_TRIDENT_RIPTIDE_1
                        }
                        player.level!!.addSound(player.position, riptideSound)
                    }
                }

                PlayerActionType.StopSpinAttack -> {
                    val playerToggleSpinAttackEvent = PlayerToggleSpinAttackEvent(player, false)
                    Server.instance.pluginManager.callEvent(playerToggleSpinAttackEvent)

                    if (playerToggleSpinAttackEvent.cancelled) {
                        player.sendData(player)
                    } else {
                        player.setSpinAttacking(false)
                    }
                }

                PlayerActionType.StartFlying -> {
                    if (Server.instance.getServerAuthoritativeMovement() > 0) {
                        return
                    }

                    if (!Server.instance.allowFlight && !player.adventureSettings[AdventureSettings.Type.ALLOW_FLIGHT]
                    ) {
                        player.kick(PlayerKickEvent.Reason.FLYING_DISABLED, "Flying is not enabled on this server")
                        return@switch
                    }
                    val playerToggleFlightEvent = PlayerToggleFlightEvent(player, true)
                    Server.instance.pluginManager.callEvent(playerToggleFlightEvent)
                    if (playerToggleFlightEvent.cancelled) {
                        player.adventureSettings.update()
                    } else {
                        player.adventureSettings[AdventureSettings.Type.FLYING] = playerToggleFlightEvent.isFlying
                    }
                }

                PlayerActionType.StopFlying -> {
                    if (Server.instance.getServerAuthoritativeMovement() > 0) {
                        return
                    }

                    val playerToggleFlightEvent = PlayerToggleFlightEvent(player, false)
                    Server.instance.pluginManager.callEvent(playerToggleFlightEvent)
                    if (playerToggleFlightEvent.cancelled) {
                        player.adventureSettings.update()
                    } else {
                        player.adventureSettings[AdventureSettings.Type.FLYING] = playerToggleFlightEvent.isFlying
                    }
                }

                PlayerActionType.StartItemUseOn, PlayerActionType.StopItemUseOn -> {
                    // TODO
                }

                else -> log.warn(
                    "{} sent invalid action: {}",
                    player.getEntityName(),
                    packet.actionType
                )
            }
        }
    }

    override val packetID: Int = PlayerActionPacket.id

    companion object : Loggable
}