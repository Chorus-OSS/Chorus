package org.chorus_oss.chorus.network.process

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.network.process.processor.*
import org.chorus_oss.protocol.core.Packet

class PacketManager {
    private val processors = HashMap<Int, PacketProcessor<out Packet>>(300)

    init {
        registerDefaultProcessors()
    }

    fun register(vararg processors: PacketProcessor<out Packet>) {
        for (processor in processors) {
            this@PacketManager.processors[processor.packetID] = processor
        }
    }

    fun canProcess(packetId: Int): Boolean {
        return processors.containsKey(packetId)
    }

    fun <T : Packet> processPacket(player: Player, packet: T) {
        @Suppress("UNCHECKED_CAST")
        val processor = processors[packet.id] as? PacketProcessor<T>?
        if (processor != null) {
            processor.handle(player, packet)
        } else {
            throw UnsupportedOperationException(
                "No processor found for packet " + packet::class.java.name + " with id " + packet.id + "."
            )
        }
    }

    fun registerDefaultProcessors() {
        register(
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
