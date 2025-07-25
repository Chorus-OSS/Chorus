package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockBedrock
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.block.BlockObsidian
import org.chorus_oss.chorus.entity.Entity.Companion.createEntity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.FloatTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import java.util.*

class ItemEndCrystal @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.Companion.END_CRYSTAL, meta, count, "End Crystal") {
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
        if (target !is BlockBedrock && target !is BlockObsidian) return false
        val chunk = level.getChunk(block.x.toInt() shr 4, block.z.toInt() shr 4)
        val entities = level.getNearbyEntities(
            SimpleAxisAlignedBB(
                target.position.x,
                target.position.y,
                target.position.z,
                target.position.x + 1,
                target.position.y + 2,
                target.position.z + 1
            )
        )
        val up = target.up()

        if (chunk == null || (up.id != BlockID.AIR) || (up.up().id != BlockID.AIR) || entities.size != 0) {
            return false
        }

        val nbt = CompoundTag()
            .putList(
                "Pos", ListTag<FloatTag>()
                    .add(FloatTag(target.position.x + 0.5))
                    .add(FloatTag(up.position.y))
                    .add(FloatTag(target.position.z + 0.5))
            )
            .putList(
                "Motion", ListTag<FloatTag>()
                    .add(FloatTag(0f))
                    .add(FloatTag(0f))
                    .add(FloatTag(0f))
            )
            .putList(
                "Rotation", ListTag<FloatTag>()
                    .add(FloatTag(Random().nextFloat() * 360))
                    .add(FloatTag(0f))
            )

        if (this.hasCustomName()) {
            nbt.putString("CustomName", this.customName)
        }

        val entity = createEntity(EntityID.ENDER_CRYSTAL, chunk, nbt)
        if (entity != null) {
            if (player.isAdventure || player.isSurvival) {
                val item = player.inventory.itemInHand
                item.setCount(item.getCount() - 1)
                player.inventory.setItemInHand(item)
            }
            entity.spawnToAll()
            return true
        }
        return false
    }
}
