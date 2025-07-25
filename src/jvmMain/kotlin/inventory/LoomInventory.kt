package org.chorus_oss.chorus.inventory

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.BlockLoom
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.network.protocol.types.itemstack.ContainerSlotType
import org.chorus_oss.protocol.types.BlockPos
import org.chorus_oss.protocol.types.ContainerType

class LoomInventory(blockLoom: BlockLoom) : BaseInventory(blockLoom, InventoryType.LOOM, 3) {
    override fun init() {
        val map = super.networkSlotMap()
        for (i in 0..<size) {
            map[i] = 9 + i
        }

        val map2 = super.slotTypeMap()
        map2[0] = ContainerSlotType.LOOM_INPUT
        map2[1] = ContainerSlotType.LOOM_DYE
        map2[2] = ContainerSlotType.LOOM_MATERIAL
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

    val banner: Item
        get() = getItem(0)

    val dye: Item
        get() = getItem(1)

    val pattern: Item
        get() = getItem(2)
}
