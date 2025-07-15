package org.chorus_oss.chorus.inventory

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.blockentity.BlockEntityEnderChest
import org.chorus_oss.chorus.blockentity.BlockEntityNameable
import org.chorus_oss.chorus.entity.IHuman
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.network.protocol.types.itemstack.ContainerSlotType
import org.chorus_oss.protocol.types.BlockPos
import org.chorus_oss.protocol.types.ContainerType


class HumanEnderChestInventory(human: IHuman) : BaseInventory(human, InventoryType.CONTAINER, 27),
    BlockEntityInventoryNameable {
    private var enderChest: BlockEntityEnderChest? = null

    override fun init() {
        val map = super.slotTypeMap()
        for (i in 0..<size) {
            map[i] = ContainerSlotType.LEVEL_ENTITY
        }
    }

    override var holder: InventoryHolder
        get() = super.holder as IHuman
        set(holder) {
            super.holder = holder
        }

    fun setBlockEntityEnderChest(player: Player, blockEntityEnderChest: BlockEntityEnderChest?) {
        if (blockEntityEnderChest == null) {
            enderChest = null
            player.enderChestOpen = false
        } else {
            enderChest = blockEntityEnderChest
            player.enderChestOpen = true
        }
    }

    override fun onOpen(who: Player) {
        if (who !== this.holder) {
            return
        }
        if (enderChest == null) {
            return
        }
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

        val blockEventPacket = org.chorus_oss.protocol.packets.BlockEventPacket(
            blockPosition = BlockPos(enderChest!!.position),
            eventType = 1,
            eventValue = 2,
        )

        val level = who.level
        if (level != null) {
            level.addSound(who.vector3.add(0.5, 0.5, 0.5), Sound.RANDOM_ENDERCHESTOPEN)
            level.addChunkPacket(
                who.position.chunkX,
                who.position.chunkZ,
                blockEventPacket
            )
        }
    }

    override fun slotTypeMap(): MutableMap<Int?, ContainerSlotType?> {
        val map = super.slotTypeMap()
        map[SpecialWindowId.CONTAINER_ID_REGISTRY.id] = ContainerSlotType.ANVIL_INPUT
        return map
    }

    override fun onClose(who: Player) {
        if (who !== this.holder) {
            return
        }
        if (enderChest == null) {
            return
        }

        val containerId = who.getWindowId(this)
        who.sendPacket(
            org.chorus_oss.protocol.packets.ContainerClosePacket(
                containerID = containerId.toByte(),
                containerType = ContainerType(type),
                serverInitiatedClose = who.closingWindowId != containerId
            )
        )

        val blockEventPacket = org.chorus_oss.protocol.packets.BlockEventPacket(
            blockPosition = BlockPos(enderChest!!.position),
            eventType = 1,
            eventValue = 0,
        )

        val level = who.level
        if (level != null) {
            level.addSound(who.vector3.add(0.5, 0.5, 0.5), Sound.RANDOM_ENDERCHESTCLOSED)
            level.addChunkPacket(
                who.position.chunkX,
                who.position.chunkZ,
                blockEventPacket
            )
        }
        setBlockEntityEnderChest(who, null)
        super.onClose(who)
    }

    override val blockEntityInventoryHolder: BlockEntityNameable?
        get() = enderChest

    override var inventoryTitle: String
        get() {
            return enderChest?.name ?: "Unknown"
        }
        set(name) {
            if (enderChest != null) {
                enderChest!!.name = name
            }
        }
}
