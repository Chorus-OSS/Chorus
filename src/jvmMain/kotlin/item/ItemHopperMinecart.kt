package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockRail
import org.chorus_oss.chorus.entity.Entity.Companion.createEntity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.item.EntityHopperMinecart
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.FloatTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.utils.Rail

class ItemHopperMinecart @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.Companion.HOPPER_MINECART, meta, count, "Minecart with Hopper") {
    override fun canBeActivated(): Boolean {
        return true
    }

    override fun onActivate(
        level: Level,
        player: Player,
        block: Block,
        target: Block,
        face: BlockFace,
        fx: Double,
        fy: Double,
        fz: Double
    ): Boolean {
        if (Rail.isRailBlock(target)) {
            val type = (target as BlockRail).getOrientation()
            var adjacent = 0.0
            if (type != null) {
                if (type.isAscending) {
                    adjacent = 0.5
                }
            }
            val minecart = createEntity(
                EntityID.HOPPER_MINECART,
                level.getChunk(target.position.floorX shr 4, target.position.floorZ shr 4), CompoundTag()
                    .putList(
                        "Pos", ListTag<FloatTag>()
                            .add(FloatTag(target.x + 0.5))
                            .add(FloatTag(target.y + 0.0625 + adjacent))
                            .add(FloatTag(target.z + 0.5))
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
            ) as EntityHopperMinecart?

            if (minecart == null) {
                return false
            }

            if (player.isAdventure || player.isSurvival) {
                val item = player.inventory.itemInHand
                item.setCount(item.getCount() - 1)
                player.inventory.setItemInHand(item)
            }

            minecart.spawnToAll()
            return true
        }
        return false
    }

    override val maxStackSize: Int
        get() = 1
}