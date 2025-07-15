package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.blockentity.BlockEntity
import org.chorus_oss.chorus.blockentity.BlockEntityChest
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromHorizontalIndex
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.nbt.tag.Tag
import kotlin.math.min

class BlockTrappedChest @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockChest(blockstate) {
    override val name: String
        get() = "Trapped Chest"

    override fun place(
        item: Item?,
        block: Block,
        target: Block?,
        face: BlockFace,
        fx: Double,
        fy: Double,
        fz: Double,
        player: Player?
    ): Boolean {
        blockFace = if (player != null) fromHorizontalIndex(
            player.getDirection().getOpposite().horizontalIndex
        ) else BlockFace.SOUTH

        var chest: BlockEntityChest? = null

        for (side in BlockFace.Plane.HORIZONTAL_FACES) {
            if ((blockFace == BlockFace.WEST || blockFace == BlockFace.EAST) && (side == BlockFace.WEST || side == BlockFace.EAST)) {
                continue
            } else if ((blockFace == BlockFace.NORTH || blockFace == BlockFace.SOUTH) && (side == BlockFace.NORTH || side == BlockFace.SOUTH)) {
                continue
            }
            val c = this.getSide(side)
            if (c is BlockTrappedChest && c.blockFace == blockFace) {
                val blockEntity = level.getBlockEntity(c.position)
                if (blockEntity is BlockEntityChest && !blockEntity.isPaired) {
                    chest = blockEntity
                    break
                }
            }
        }

        level.setBlock(block.position, this, true, true)
        val nbt = CompoundTag()
            .putList("Items", ListTag<Tag<*>>())
            .putString("id", BlockEntityID.CHEST)
            .putInt("x", position.x.toInt())
            .putInt("y", position.y.toInt())
            .putInt("z", position.z.toInt())

        if (item!!.hasCustomName()) {
            nbt.putString("CustomName", item.customName)
        }

        if (item.hasCustomBlockData()) {
            val customData = item.customBlockData!!.tags
            for ((key, value) in customData) {
                nbt.put(key, value)
            }
        }

        val blockEntity = BlockEntity.createBlockEntity(
            BlockEntityID.CHEST,
            level.getChunk(
                position.x.toInt() shr 4,
                position.z.toInt() shr 4
            ),
            nbt
        ) as BlockEntityChest?
            ?: return false

        if (chest != null) {
            chest.pairWith(blockEntity)
            blockEntity.pairWith(chest)
        }

        return true
    }

    override fun getWeakPower(face: BlockFace): Int {
        var playerCount = 0

        val blockEntity = level.getBlockEntity(this.position)

        if (blockEntity is BlockEntityChest) {
            playerCount = blockEntity.inventory.viewers.size
        }

        return min(playerCount.toDouble(), 15.0).toInt()
    }

    override fun getStrongPower(side: BlockFace): Int {
        return if (side == BlockFace.UP) this.getWeakPower(side) else 0
    }

    override val isPowerSource: Boolean
        get() = true

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.TRAPPED_CHEST, CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION)
    }
}
