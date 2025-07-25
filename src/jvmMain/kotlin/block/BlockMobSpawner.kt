package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.blockentity.BlockEntity
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.blockentity.BlockEntityMobSpawner
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemSpawnEgg
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.registry.Registries

class BlockMobSpawner @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockSolid(blockState) {
    override val name: String
        get() = "Monster Spawner"

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    override val hardness: Double
        get() = 5.0

    override val resistance: Double
        get() = 25.0

    override fun getDrops(item: Item): Array<Item> {
        return Item.EMPTY_ARRAY
    }

    override fun canBePushed(): Boolean {
        return false
    }

    override val waterloggingLevel: Int
        get() = 1

    override fun canBePulled(): Boolean {
        return false
    }

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override fun canBeActivated(): Boolean {
        return true
    }

    fun setType(networkId: Int): Boolean {
        val identifier = Registries.ENTITY.getEntityIdentifier(networkId)

        val blockEntity = level.getBlockEntity(this.position)
        if (blockEntity is BlockEntityMobSpawner) {
            blockEntity.spawnEntityType = identifier
        } else {
            blockEntity?.close()
            val nbt: CompoundTag = CompoundTag()
                .putString(BlockEntity.TAG_ID, BlockEntityID.MOB_SPAWNER)
                .putString(BlockEntityMobSpawner.TAG_ENTITY_IDENTIFIER, identifier)
                .putInt(BlockEntity.TAG_X, position.floorX)
                .putInt(BlockEntity.TAG_Y, position.floorY)
                .putInt(BlockEntity.TAG_Z, position.floorZ)

            val entitySpawner: BlockEntityMobSpawner = BlockEntityMobSpawner(
                level.getChunk(
                    position.chunkX,
                    position.chunkZ
                ), nbt
            )
            entitySpawner.spawnToAll()
        }
        return true
    }

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        if (item !is ItemSpawnEgg) return false
        if (player == null) return false
        if (player.isAdventure) return false
        if (setType(item.getNetId())) {
            if (!player.isCreative) {
                player.inventory.decreaseCount(player.inventory.heldItemIndex)
            }
            return true
        }
        return false
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.MOB_SPAWNER)
    }
}
