package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.AdventureSettings
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.item.EntityBoat
import org.chorus_oss.chorus.entity.item.EntityMinecartAbstract
import org.chorus_oss.chorus.entity.mob.animal.EntityHorse
import org.chorus_oss.chorus.event.player.*
import org.chorus_oss.chorus.experimental.network.MigrationPacket
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.level.Transform
import org.chorus_oss.chorus.level.Transform.Companion.fromObject
import org.chorus_oss.chorus.math.BlockFace.Companion.fromIndex
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.network.process.DataPacketProcessor
import org.chorus_oss.chorus.network.protocol.types.AuthInputAction
import org.chorus_oss.protocol.packets.PlayerAuthInputPacket
import org.chorus_oss.protocol.types.PlayerActionType
import kotlin.math.abs
import kotlin.math.sqrt

class PlayerAuthInputProcessor : DataPacketProcessor<MigrationPacket<PlayerAuthInputPacket>>() {
    override fun handle(player: Player, pk: MigrationPacket<PlayerAuthInputPacket>) {
        val packet = pk.packet

        val player = player.player
        if (!packet.blockActions.isNullOrEmpty()) {
            for (action in packet.blockActions) {
                //hack 自从1.19.70开始，创造模式剑客户端不会发送PREDICT_DESTROY_BLOCK，但仍然发送START_DESTROY_BLOCK，过滤掉
                if (player.inventory.itemInHand.isSword && player.isCreative && action.action == PlayerActionType.StartDestroyBlock) {
                    continue
                }
                val blockPos = action.position!!
                val blockFace = fromIndex(action.facing!!)

                val lastBreakPos =
                    if (player.player.lastBlockAction == null) null else player.player.lastBlockAction!!.position
                if (lastBreakPos != null && (lastBreakPos.x != blockPos.x || lastBreakPos.y != blockPos.y || lastBreakPos.z != blockPos.z)) {
                    player.onBlockBreakAbort(Vector3(lastBreakPos))
                    player.onBlockBreakStart(Vector3(blockPos), blockFace)
                }

                when (action.action) {
                    PlayerActionType.StartDestroyBlock, PlayerActionType.ContinueDestroyBlock -> player.onBlockBreakStart(
                        Vector3(blockPos),
                        blockFace
                    )

                    PlayerActionType.AbortDestroyBlock, PlayerActionType.StopDestroyBlock -> player.onBlockBreakAbort(
                        Vector3(blockPos)
                    )

                    PlayerActionType.PredictDestroyBlock -> {
                        player.onBlockBreakAbort(Vector3(blockPos))
                        player.onBlockBreakComplete(Vector3(blockPos).asBlockVector3(), blockFace)
                    }

                    else -> Unit
                }
                player.lastBlockAction = action
            }
        }

        // As of 1.18 this is now used for sending item stack requests such as when mining a block.
        val itemStackRequest = packet.itemStackRequest
        if (itemStackRequest != null) {
            val dataPacketManager = player.session.dataPacketManager
            if (dataPacketManager != null) {
                val itemStackRequestPacket = org.chorus_oss.protocol.packets.ItemStackRequestPacket(
                    requests = listOf(itemStackRequest)
                )
                dataPacketManager.processPacket(player, MigrationPacket(itemStackRequestPacket))
            }
        }

        if (packet.inputData[AuthInputAction.START_SPRINTING.ordinal]) {
            val event = PlayerToggleSprintEvent(player, true)
            Server.instance.pluginManager.callEvent(event)
            if (event.cancelled) {
                player.sendData(player)
            } else {
                player.setSprinting(true)
            }
        }
        if (packet.inputData[AuthInputAction.STOP_SPRINTING.ordinal]) {
            val event = PlayerToggleSprintEvent(player, false)
            Server.instance.pluginManager.callEvent(event)
            if (event.cancelled) {
                player.sendData(player)
            } else {
                player.setSprinting(false)
            }
        }
        if (packet.inputData[AuthInputAction.START_SNEAKING.ordinal]) {
            val event = PlayerToggleSneakEvent(player, true)
            Server.instance.pluginManager.callEvent(event)
            if (event.cancelled) {
                player.sendData(player)
            } else {
                player.setSneaking(true)
            }
        }
        if (packet.inputData[AuthInputAction.STOP_SNEAKING.ordinal]) {
            val event = PlayerToggleSneakEvent(player, false)
            Server.instance.pluginManager.callEvent(event)
            if (event.cancelled) {
                player.sendData(player)
            } else {
                player.setSneaking(false)
            }
        }
        if (player.adventureSettings[AdventureSettings.Type.FLYING]) {
            player.isFlySneaking = packet.inputData[AuthInputAction.SNEAKING.ordinal]
        }
        if (packet.inputData[AuthInputAction.START_JUMPING.ordinal]) {
            val playerJumpEvent = PlayerJumpEvent(player)
            Server.instance.pluginManager.callEvent(playerJumpEvent)
        }
        if (packet.inputData[AuthInputAction.START_SWIMMING.ordinal]) {
            val playerSwimmingEvent = PlayerToggleSwimEvent(player, true)
            Server.instance.pluginManager.callEvent(playerSwimmingEvent)
            if (playerSwimmingEvent.cancelled) {
                player.sendData(player)
            } else {
                player.setSwimming(true)
            }
        }
        if (packet.inputData[AuthInputAction.STOP_SWIMMING.ordinal]) {
            val playerSwimmingEvent = PlayerToggleSwimEvent(player, false)
            Server.instance.pluginManager.callEvent(playerSwimmingEvent)
            if (playerSwimmingEvent.cancelled) {
                player.sendData(player)
            } else {
                player.setSwimming(false)
            }
        }
        if (packet.inputData[AuthInputAction.START_GLIDING.ordinal]) {
            val playerToggleGlideEvent = PlayerToggleGlideEvent(player, true)
            Server.instance.pluginManager.callEvent(playerToggleGlideEvent)
            if (playerToggleGlideEvent.cancelled) {
                player.sendData(player)
            } else {
                player.setGliding(true)
            }
        }
        if (packet.inputData[AuthInputAction.STOP_GLIDING.ordinal]) {
            val playerToggleGlideEvent = PlayerToggleGlideEvent(player, false)
            Server.instance.pluginManager.callEvent(playerToggleGlideEvent)
            if (playerToggleGlideEvent.cancelled) {
                player.sendData(player)
            } else {
                player.setGliding(false)
            }
        }
        if (packet.inputData[AuthInputAction.START_FLYING.ordinal]) {
            if (!Server.instance.allowFlight && !player.adventureSettings[AdventureSettings.Type.ALLOW_FLIGHT]
            ) {
                player.kick(PlayerKickEvent.Reason.FLYING_DISABLED, "Flying is not enabled on this server")
                return
            }
            val playerToggleFlightEvent = PlayerToggleFlightEvent(player, true)
            Server.instance.pluginManager.callEvent(playerToggleFlightEvent)
            if (playerToggleFlightEvent.cancelled) {
                player.adventureSettings.update()
            } else {
                player.adventureSettings[AdventureSettings.Type.FLYING] = playerToggleFlightEvent.isFlying
            }
        }
        if (packet.inputData[AuthInputAction.STOP_FLYING.ordinal]) {
            val playerToggleFlightEvent = PlayerToggleFlightEvent(player, false)
            Server.instance.pluginManager.callEvent(playerToggleFlightEvent)
            if (playerToggleFlightEvent.cancelled) {
                player.adventureSettings.update()
            } else {
                player.adventureSettings[AdventureSettings.Type.FLYING] = playerToggleFlightEvent.isFlying
            }
        }
        val clientPosition = Vector3(packet.position).subtract(0.0, player.player.getBaseOffset().toDouble(), 0.0)
        var yaw = packet.yaw % 360
        val pitch = packet.pitch % 360
        var headYaw = packet.headYaw % 360
        if (headYaw < 0) {
            headYaw += 360f
        }
        if (yaw < 0) {
            yaw += 360f
        }
        val clientLoc = fromObject(
            clientPosition,
            player.level!!, yaw.toDouble(), pitch.toDouble(), headYaw.toDouble()
        )
        // Proper player.isPassenger() check may be needed
        val riding = player.riding
        if (riding is EntityMinecartAbstract) {
            val inputY = packet.moveVector.y
            if (inputY >= -1.001 && inputY <= 1.001) {
                riding.setCurrentSpeed(inputY.toDouble())
            }
        } else if (riding is EntityBoat && packet.inputData[AuthInputAction.IN_CLIENT_PREDICTED_IN_VEHICLE.ordinal]) {
            if (riding.getRuntimeID() == packet.clientPredictedVehicle && riding.isControlling(player)) {
                if (check(clientLoc, player)) {
                    val offsetLoc = clientLoc.add(0.0, player.player.getBaseOffset().toDouble(), 0.0)
                    riding.onInput(offsetLoc)
                    player.player.handleMovement(offsetLoc)
                }
                return
            }
        } else if (riding is EntityHorse) {
            if (check(clientLoc, player)) {
                val playerLoc: Transform
                if (riding.hasOwner() && !riding.getSaddle().isNothing) {
                    riding.onInput(clientLoc.add(0.0, riding.getHeight().toDouble(), 0.0))
                    playerLoc = clientLoc.add(0.0, (player.player.getBaseOffset() + riding.getHeight()).toDouble(), 0.0)
                } else {
                    playerLoc = clientLoc.add(0.0, 0.8, 0.0)
                }
                player.player.handleMovement(playerLoc)
                return
            }
        }
        player.player.offerMovementTask(clientLoc)
    }

    override val packetId: Int = PlayerAuthInputPacket.id

    companion object {
        private fun check(clientLoc: Transform, player: Player): Boolean {
            val distance = clientLoc.position.distanceSquared(player.position)
            val updatePosition = sqrt(distance).toFloat() > 0.1f
            val updateRotation =
                abs(player.rotation.pitch - clientLoc.rotation.pitch).toFloat() > 1 || abs(player.rotation.yaw - clientLoc.rotation.yaw).toFloat() > 1 || abs(
                    player.headYaw - clientLoc.headYaw
                ).toFloat() > 1
            return updatePosition || updateRotation
        }
    }
}
