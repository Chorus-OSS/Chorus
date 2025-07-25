package org.chorus_oss.chorus.inventory

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.blockentity.BlockEntityBeacon
import org.chorus_oss.chorus.blockentity.BlockEntityNameable
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.network.protocol.types.itemstack.ContainerSlotType
import org.chorus_oss.protocol.types.BlockPos
import org.chorus_oss.protocol.types.ContainerType

class BeaconInventory(blockBeacon: BlockEntityBeacon) : BaseInventory(blockBeacon, InventoryType.BEACON, 1),
    BlockEntityInventoryNameable {
    override fun init() {
        val map = super.slotTypeMap()
        map[0] = ContainerSlotType.BEACON_PAYMENT

        val networkSlotMap = super.networkSlotMap()
        networkSlotMap[0] = 27
    }

    override fun onClose(who: Player) {
        super.onClose(who)

        val drops = who.inventory.addItem(this.getItem(0))
        for (drop in drops) {
            if (!who.dropItem(drop)) {
                holder.level!!.dropItem(holder.vector3.add(0.5, 0.5, 0.5), drop)
            }
        }

        this.clear(0)
    }

    override fun onOpen(who: Player) {
        super.onOpen(who)
        who.sendPacket(
            org.chorus_oss.protocol.packets.ContainerOpenPacket(
                containerID = who.getWindowId(this).toByte(),
                containerType = ContainerType(type),
                position = BlockPos(holder.vector3),
                targetActorID = who.getUniqueID()
            )
        )
        this.sendContents(who)
    }

    override var holder: InventoryHolder
        get() = super.holder as BlockEntityBeacon
        set(holder) {
            super.holder = holder
        }

    override val blockEntityInventoryHolder: BlockEntityNameable
        get() = holder as BlockEntityNameable
}
