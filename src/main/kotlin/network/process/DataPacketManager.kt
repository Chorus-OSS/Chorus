package org.chorus_oss.chorus.network.process

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.network.DataPacket
import org.chorus_oss.chorus.network.process.processor.*

/**
 * DataPacketManager is a static class to manage DataPacketProcessors and process DataPackets.
 */
class DataPacketManager {
    private val PROCESSORS = HashMap<Int, DataPacketProcessor<out DataPacket>>(300)

    init {
        registerDefaultProcessors()
    }

    fun registerProcessor(vararg processors: DataPacketProcessor<out DataPacket>) {
        for (processor in processors) {
            PROCESSORS[processor.packetId] = processor
        }
    }

    fun canProcess(packetId: Int): Boolean {
        return PROCESSORS.containsKey(packetId)
    }

    fun <T : DataPacket> processPacket(player: Player, packet: T) {
        val processor = PROCESSORS[packet.pid()] as DataPacketProcessor<T>?
        if (processor != null) {
            processor.handle(player, packet)
        } else {
            throw UnsupportedOperationException(
                "No processor found for packet " + packet::class.java.name + " with id " + packet.pid() + "."
            )
        }
    }

    fun registerDefaultProcessors() {
        registerProcessor(
            LoginProcessor(),
            InventoryTransactionProcessor(),
            PlayerSkinProcessor(),
            PacketViolationWarningProcessor(),
            EmoteProcessor(),
            MovePlayerProcessor(),
            PlayerAuthInputProcessor(),
            RequestAbilityProcessor(),
            MobEquipmentProcessor(),
            PlayerActionProcessor(),
            ModalFormResponseProcessor(),
            NPCRequestProcessor(),
            InteractProcessor(),
            BlockPickRequestProcessor(),
            AnimateProcessor(),
            EntityEventProcessor(),
            CommandRequestProcessor(),
            CommandBlockUpdateProcessor(),
            StructureBlockUpdateProcessor(),
            TextProcessor(),
            ContainerCloseProcessor(),
            SetPlayerGameTypeProcessor(),
            LecternUpdateProcessor(),
            MapInfoRequestProcessor(),
            // LevelSoundEventProcessor(),
            // PlayerHotbarProcessor(),
            ServerSettingsRequestProcessor(),
            RespawnProcessor(),
            BookEditProcessor(),
            SetDifficultyProcessor(),
            SettingsCommandProcessor(),
            PositionTrackingDBClientRequestProcessor(),
            ShowCreditsProcessor(),
            RequestPermissionsProcessor(),
            ItemStackRequestPacketProcessor(),
            SetLocalPlayerAsInitializedPacketProcessor()
        )
    }
}
