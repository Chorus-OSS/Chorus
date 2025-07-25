package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.Entity.Companion.createEntity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.item.EntityFallingBlock
import org.chorus_oss.chorus.event.block.BlockFallEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.FloatTag
import org.chorus_oss.chorus.nbt.tag.ListTag

abstract class BlockFallable(blockstate: BlockState) : BlockSolid(blockstate) {
    override fun onUpdate(type: Int): Int {
        val down = this.down()
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if ((down.isAir || down is BlockFire || down is BlockLiquid ||
                        (down is BlockBubbleColumn && down.getLevelBlockAtLayer(1) is BlockLiquid))
            ) {
                val event = BlockFallEvent(this)
                Server.instance.pluginManager.callEvent(event)
                if (event.cancelled) {
                    return type
                }

                drop(CompoundTag())
            }
            return type
        }
        return 0
    }

    fun drop(customNbt: CompoundTag) {
        level.setBlock(this.position, get(BlockID.AIR), direct = true, update = true)
        val fall = createFallingEntity(customNbt)

        fall!!.spawnToAll()
    }

    protected open fun createFallingEntity(customNbt: CompoundTag): EntityFallingBlock? {
        val nbt = CompoundTag()
            .putList(
                "Pos", ListTag<FloatTag>()
                    .add(FloatTag(position.x + 0.5))
                    .add(FloatTag(position.y))
                    .add(FloatTag(position.z + 0.5))
            )
            .putList(
                "Motion", ListTag<FloatTag>()
                    .add(FloatTag(0f))
                    .add(FloatTag(0f))
                    .add(FloatTag(0f))
            )
            .putList(
                "Rotation", ListTag<FloatTag>()
                    .add(FloatTag(0f))
                    .add(FloatTag(0f))
            )
            .putCompound("Block", blockState.blockStateTag.copy())

        for ((key, value) in customNbt.entrySet) {
            nbt.put(key, value.copy())
        }

        val fall = createEntity(
            EntityID.FALLING_BLOCK,
            level.getChunk(position.x.toInt() shr 4, position.z.toInt() shr 4), nbt
        ) as EntityFallingBlock?

        fall?.spawnToAll()

        return fall
    }

    open fun toFallingItem(): Item {
        return this.toItem()
    }
}
