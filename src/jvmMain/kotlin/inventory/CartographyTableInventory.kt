package org.chorus_oss.chorus.inventory

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.BlockCartographyTable
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.network.protocol.types.itemstack.ContainerSlotType
import org.chorus_oss.protocol.types.BlockPos
import org.chorus_oss.protocol.types.ContainerType

class CartographyTableInventory(blockCartographyTable: BlockCartographyTable) :
    BaseInventory(blockCartographyTable, InventoryType.CARTOGRAPHY, 2) {
    override fun init() {
        val map = super.networkSlotMap()
        for (i in 0..<size) {
            map[i] = 12 + i
        }

        val map2 = super.slotTypeMap()
        map2[0] = ContainerSlotType.CARTOGRAPHY_INPUT
        map2[1] = ContainerSlotType.CARTOGRAPHY_ADDITIONAL
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

    override fun onClose(who: Player) {
        super.onClose(who)

        var drops = arrayOf(
            input,
            additional
        )
        drops = who.inventory.addItem(*drops)
        for (drop in drops) {
            if (!who.dropItem(drop)) {
                holder.level!!.dropItem(holder.vector3.add(0.5, 0.5, 0.5), drop)
            }
        }

        clear(0)
        clear(1)
    }

    val input: Item
        get() = this.getItem(0)

    val additional: Item
        get() = this.getItem(1)
}
