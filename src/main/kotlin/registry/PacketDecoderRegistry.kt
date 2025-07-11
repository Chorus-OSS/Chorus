package org.chorus_oss.chorus.registry

import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.network.protocol.*
import java.util.concurrent.atomic.AtomicBoolean

class PacketDecoderRegistry : IRegistry<Int, PacketDecoder<out DataPacket>?, PacketDecoder<out DataPacket>> {
    private val packets = HashMap<Int, PacketDecoder<out DataPacket>>(256)

    override fun init() {
        if (isLoad.getAndSet(true)) return
        registerPackets()
    }

    override operator fun get(key: Int): PacketDecoder<out DataPacket>? {
        return packets[key]
    }

    override fun reload() {
        isLoad.set(false)
        packets.clear()
        init()
    }

    @Throws(RegisterException::class)
    override fun register(key: Int, value: PacketDecoder<*>) {
        try {
            if (packets.putIfAbsent(key, value) != null) {
                throw RegisterException("The packet has been registered!")
            }
        } catch (e: Exception) {
            throw RegisterException(e)
        }
    }

    private fun registerPackets() {
        packets.clear()

        // Register all packets that are Client -> Server

        this.register(ProtocolInfo.INVENTORY_TRANSACTION_PACKET, InventoryTransactionPacket) // 30
        this.register(ProtocolInfo.INTERACT_PACKET, InteractPacket) // 33
        this.register(ProtocolInfo.ANIMATE_PACKET, AnimatePacket) // 44

        this.register(ProtocolInfo.ENTITY_EVENT_PACKET, EntityEventPacket)

        this.register(ProtocolInfo.LOGIN_PACKET, LoginPacket) // 1
        this.register(ProtocolInfo.MOB_ARMOR_EQUIPMENT_PACKET, MobArmorEquipmentPacket) // 32
        this.register(ProtocolInfo.MOB_EQUIPMENT_PACKET, MobEquipmentPacket) // 31
        this.register(ProtocolInfo.MODAL_FORM_RESPONSE_PACKET, ModalFormResponsePacket) // 101
        this.register(ProtocolInfo.MOVE_ENTITY_ABSOLUTE_PACKET, MoveEntityAbsolutePacket) // 18
        this.register(ProtocolInfo.MOVE_PLAYER_PACKET, MovePlayerPacket) // 19
        this.register(ProtocolInfo.PLAYER_ACTION_PACKET, PlayerActionPacket) // 36
        this.register(ProtocolInfo.PLAYER_HOTBAR_PACKET, PlayerHotbarPacket) // 48
        this.register(ProtocolInfo.PLAYER_SKIN_PACKET, PlayerSkinPacket) // 93
        this.register(ProtocolInfo.RESPAWN_PACKET, RespawnPacket) // 45
        this.register(ProtocolInfo.SET_DIFFICULTY_PACKET, SetDifficultyPacket) // 60
        this.register(ProtocolInfo.SET_PLAYER_GAME_TYPE_PACKET, SetPlayerGameTypePacket) // 62
        this.register(ProtocolInfo.SET_TITLE_PACKET, SetTitlePacket) // 88
        this.register(ProtocolInfo.SERVER_SETTINGS_REQUEST_PACKET, ServerSettingsRequestPacket) // 102
        //this.register(ProtocolInfo.TAKE_ITEM_ENTITY_PACKET, TakeItemEntityPacket) // 17
        //this.register(ProtocolInfo.TEXT_PACKET, TextPacket) // 9
        this.register(ProtocolInfo.MOVE_ENTITY_DELTA_PACKET, MoveEntityDeltaPacket) // 111
        this.register(ProtocolInfo.SET_LOCAL_PLAYER_AS_INITIALIZED_PACKET, SetLocalPlayerAsInitializedPacket) // 113
        this.register(ProtocolInfo.NETWORK_CHUNK_PUBLISHER_UPDATE_PACKET, NetworkChunkPublisherUpdatePacket) // 121
        this.register(ProtocolInfo.LEVEL_SOUND_EVENT_PACKET, LevelSoundEventPacket) // 123
        this.register(ProtocolInfo.PLAYER_AUTH_INPUT_PACKET, PlayerAuthInputPacket) // 144
        this.register(ProtocolInfo.ITEM_STACK_REQUEST_PACKET, ItemStackRequestPacket) // 147
        this.register(ProtocolInfo.NPC_DIALOGUE_PACKET, NPCDialoguePacket) // 169
        this.register(ProtocolInfo.NPC_REQUEST_PACKET, NPCRequestPacket) // 98
        this.register(ProtocolInfo.PLAYER_START_ITEM_COOL_DOWN_PACKET, PlayerStartItemCoolDownPacket) // 176
        this.register(ProtocolInfo.REQUEST_PERMISSIONS_PACKET, RequestPermissionsPacket) // 185
        this.register(ProtocolInfo.REQUEST_ABILITY_PACKET, RequestAbilityPacket) // 184
        this.register(ProtocolInfo.REQUEST_NETWORK_SETTINGS_PACKET, RequestNetworkSettingsPacket) // 193
        this.register(ProtocolInfo.NETWORK_SETTINGS_PACKET, NetworkSettingsPacket) // 143
        this.register(ProtocolInfo.SET_DEFAULT_GAME_TYPE_PACKET, SetDefaultGameTypePacket) // 105
        this.register(ProtocolInfo.STRUCTURE_BLOCK_UPDATE_PACKET, StructureBlockUpdatePacket) // 90
    }

    companion object {
        private val isLoad = AtomicBoolean(false)
    }
}
