package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.blockentity.BlockEntityConduit
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class BlockConduit(blockState: BlockState = properties.defaultState) : BlockTransparent(blockState),
    BlockEntityHolder<BlockEntityConduit> {
    override val name: String
        get() = "Conduit"

    override fun getBlockEntityClass(): Class<out BlockEntityConduit> {
        return BlockEntityConduit::class.java
    }

    override fun getBlockEntityType(): String {
        return BlockEntityID.CONDUIT
    }

    override val waterloggingLevel: Int
        get() = 2

    override val hardness: Double
        get() = 3.0

    override val resistance: Double
        get() = 15.0

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
        if (item!!.isBlock() && item.blockId == BlockID.CONDUIT && target!!.id == BlockID.CONDUIT) {
            return false
        }

        val conduit = BlockEntityHolder.setBlockAndCreateEntity(
            this, direct = true, update = true,
            initialData = CompoundTag().putBoolean("IsMovable", true)
        )
        if (conduit != null) {
            conduit.scheduleUpdate()
            return true
        }

        return false
    }

    override val lightLevel: Int
        get() = 15

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override var minX: Double
        get() = position.x + (5.0 / 16)
        set(minX) {
            super.minX = minX
        }

    override var minY: Double
        get() = position.y + (5.0 / 16)
        set(minY) {
            super.minY = minY
        }

    override var minZ: Double
        get() = position.z + (5.0 / 16)
        set(minZ) {
            super.minZ = minZ
        }

    override var maxX: Double
        get() = position.x + (11.0 / 16)
        set(maxX) {
            super.maxX = maxX
        }

    override var maxY: Double
        get() = position.y + (11.0 / 16)
        set(maxY) {
            super.maxY = maxY
        }

    override var maxZ: Double
        get() = position.z + (11.0 / 16)
        set(maxZ) {
            super.maxZ = maxZ
        }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.CONDUIT)
    }
}
