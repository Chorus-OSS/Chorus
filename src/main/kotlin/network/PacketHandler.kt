package org.chorus_oss.chorus.network

import org.chorus_oss.chorus.experimental.network.MigrationPacket
import org.chorus_oss.chorus.network.protocol.*

interface PacketHandler {
    fun handle(pk: MigrationPacket<*>) {}

    fun handle(pk: AnimatePacket) {}

    fun handle(pk: EntityEventPacket) {}

    fun handle(pk: InteractPacket) {}

    fun handle(pk: InventoryTransactionPacket) {}

    fun handle(pk: ItemStackRequestPacket) {}

    fun handle(pk: LevelEventGenericPacket) {}

    fun handle(pk: LevelEventPacket) {}

    fun handle(pk: LevelSoundEventPacket) {}

    fun handle(pk: LoginPacket) {}

    fun handle(pk: MobArmorEquipmentPacket) {}

    fun handle(pk: MobEffectPacket) {}

    fun handle(pk: MobEquipmentPacket) {}

    fun handle(pk: ModalFormResponsePacket) {}

    fun handle(pk: MoveEntityAbsolutePacket) {}

    fun handle(pk: MoveEntityDeltaPacket) {}

    fun handle(pk: MovePlayerPacket) {}

    fun handle(pk: NPCRequestPacket) {}

    fun handle(pk: PlayerActionPacket) {}

    fun handle(pk: PlayerAuthInputPacket) {}

    fun handle(pk: PlayerEnchantOptionsPacket) {}

    fun handle(pk: PlayerListPacket) {}

    fun handle(pk: PlayerSkinPacket) {}

    fun handle(pk: RequestPermissionsPacket) {}

    fun handle(pk: SetScorePacket) {}

    fun handle(pk: SetTitlePacket) {}

    fun handle(pk: StartGamePacket) {}

    fun handle(pk: StructureBlockUpdatePacket) {}

    fun handle(pk: TextPacket) {}

    fun handle(pk: UpdateAttributesPacket) {}
}