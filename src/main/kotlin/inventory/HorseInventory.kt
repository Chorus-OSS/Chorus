package org.chorus_oss.chorus.inventory

import kotlinx.io.Buffer
import kotlinx.io.readByteString
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.mob.animal.EntityHorse
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.network.protocol.LevelSoundEventPacket
import org.chorus_oss.chorus.network.protocol.types.itemstack.ContainerSlotType
import org.chorus_oss.nbt.TagSerialization
import org.chorus_oss.protocol.types.ContainerType
import org.chorus_oss.protocol.types.item.ItemStack

class HorseInventory(holder: EntityHorse) : BaseInventory(holder, InventoryType.HORSE, 2) {
    var saddle: Item
        get() = this.getItem(0)
        set(item) {
            this.setItem(0, item)
        }

    var horseArmor: Item
        get() = this.getItem(1)
        set(item) {
            this.setItem(1, item)
        }

    override fun init() {
        slotTypeMap[0] = ContainerSlotType.HORSE_EQUIP
        slotTypeMap[1] = ContainerSlotType.HORSE_EQUIP
    }

    override fun onSlotChange(index: Int, before: Item, send: Boolean) {
        super.onSlotChange(index, before, send)
        if (index == 0) {
            if (saddle.isNothing) {
                (holder as EntityHorse).setDataFlag(EntityFlag.SADDLED, false)
                (holder as EntityHorse).setDataFlag(EntityFlag.WASD_CONTROLLED, false)
                (holder as EntityHorse).setDataFlag(EntityFlag.CAN_POWER_JUMP, false)
            } else {
                holder.level!!.addLevelSoundEvent(
                    holder.vector3, LevelSoundEventPacket.SOUND_SADDLE, -1,
                    (holder as EntityHorse).getEntityIdentifier(), false, false
                )
                (holder as EntityHorse).setDataFlag(EntityFlag.SADDLED)
                (holder as EntityHorse).setDataFlag(EntityFlag.WASD_CONTROLLED)
                (holder as EntityHorse).setDataFlag(EntityFlag.CAN_POWER_JUMP)
            }
        } else if (index == 1) {
            if (!horseArmor.isNothing) {
                holder.level!!.addSound(holder.vector3, Sound.MOB_HORSE_ARMOR)
            }
            val mobArmorEquipmentPacket = org.chorus_oss.protocol.packets.MobArmorEquipmentPacket(
                entityRuntimeID = (holder as EntityHorse).getRuntimeID().toULong(),
                head = ItemStack(Item.AIR),
                torso = ItemStack(horseArmor),
                legs = ItemStack(Item.AIR),
                feet = ItemStack(Item.AIR),
                body = ItemStack(Item.AIR),
            )
            Server.broadcastPacket(this.viewers, mobArmorEquipmentPacket)
        }
    }

    override fun onClose(who: Player) {
        val containerId = who.getWindowId(this)
        who.sendPacket(
            org.chorus_oss.protocol.packets.ContainerClosePacket(
                containerID = containerId.toByte(),
                containerType = ContainerType(type),
                serverInitiatedClose = who.closingWindowId != containerId
            )
        )
        super.onClose(who)
    }

    override fun onOpen(who: Player) {
        super.onOpen(who)
        who.sendPacket(createUpdateEquipmentPacket(who))
        sendContents(this.viewers)
    }

    protected fun createUpdateEquipmentPacket(who: Player): org.chorus_oss.protocol.packets.UpdateEquipmentPacket {
        val slots = ListTag<CompoundTag>()
        val saddle = saddle
        val horseArmor = horseArmor
        if (!saddle.isNothing) {
            slots.add(
                slot0.copy().putCompound(
                    "item",
                    CompoundTag().putString("Name", saddle.id).putShort("Aux", Short.MAX_VALUE.toInt())
                )
            )
        } else slots.add(slot0.copy())
        if (!horseArmor.isNothing) {
            slots.add(
                slot1.copy().putCompound(
                    "item",
                    CompoundTag().putString("Name", horseArmor.id).putShort("Aux", Short.MAX_VALUE.toInt())
                )
            )
        } else slots.add(slot1.copy())
        val nbt = CompoundTag().putList("slots", slots)
        val updateEquipmentPacket = org.chorus_oss.protocol.packets.UpdateEquipmentPacket(
            windowID = who.getWindowId(this).toByte(),
            windowType = type.networkType.toByte(),
            size = 0,
            entityUniqueID = (holder as EntityHorse).getUniqueID(),
            serializedInventoryData = Buffer().apply {
                org.chorus_oss.nbt.Tag.serialize(
                    org.chorus_oss.nbt.tags.CompoundTag(nbt),
                    this,
                    TagSerialization.NetLE
                )
            }.readByteString()
        )
        return updateEquipmentPacket
    }

    companion object {
        private val slot0: CompoundTag
        private val slot1: CompoundTag

        init {
            val saddle = ListTag<CompoundTag>().add(
                CompoundTag().putCompound(
                    "slotItem", CompoundTag()
                        .putShort("Aux", Short.MAX_VALUE.toInt())
                        .putString("Name", ItemID.SADDLE)
                )
            )
            val horseArmor = ListTag<CompoundTag>()
            for (h in listOf(
                ItemID.LEATHER_HORSE_ARMOR,
                ItemID.IRON_HORSE_ARMOR,
                ItemID.GOLDEN_HORSE_ARMOR,
                ItemID.DIAMOND_HORSE_ARMOR
            )) {
                horseArmor.add(
                    CompoundTag().putCompound(
                        "slotItem", CompoundTag().putShort("Aux", Short.MAX_VALUE.toInt()).putString(
                            "Name",
                            h
                        )
                    )
                )
            }
            slot0 = CompoundTag().putList("acceptedItems", saddle).putInt("slotNumber", 0)
            slot1 = CompoundTag().putList("acceptedItems", horseArmor).putInt("slotNumber", 1)
        }
    }
}
