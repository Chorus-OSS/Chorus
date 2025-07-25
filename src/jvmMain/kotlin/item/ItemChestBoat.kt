package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockFlowingWater
import org.chorus_oss.chorus.entity.Entity.Companion.createEntity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.item.EntityChestBoat
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.FloatTag
import org.chorus_oss.chorus.nbt.tag.ListTag

open class ItemChestBoat : Item {
    //legacy chest boat , have aux
    @JvmOverloads
    constructor(meta: Int = 0, count: Int = 1) : super(ItemID.Companion.CHEST_BOAT, meta, count) {
        adjustName()
    }

    //chest boat item after split aux
    constructor(id: String) : super(id)

    override var damage: Int
        get() = super.damage
        set(meta) {
            super.damage = (meta)
            adjustName()
        }

    private fun adjustName() {
        name = when (damage) {
            0 -> "Oak Chest Boat"
            1 -> "Spruce Chest Boat"
            2 -> "Birch Chest Boat"
            3 -> "Jungle Chest Boat"
            4 -> "Acacia Chest Boat"
            5 -> "Dark Oak Chest Boat"
            6 -> "Mangrove Chest Boat"
            7 -> "Bamboo Chest Raft"
            8 -> "Cherry Chest Boat"
            else -> "Chest Boat"
        }
    }

    override fun canBeActivated(): Boolean {
        return true
    }

    open val boatId: Int
        get() = this.meta

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
        if (face != BlockFace.UP || block is BlockFlowingWater) return false
        val boat = createEntity(
            EntityID.CHEST_BOAT,
            level.getChunk(block.position.floorX shr 4, block.position.floorZ shr 4), CompoundTag()
                .putList(
                    "Pos", ListTag<FloatTag>()
                        .add(FloatTag(block.x + 0.5))
                        .add(FloatTag(block.y - (if (target is BlockFlowingWater) 0.375 else 0.0)))
                        .add(FloatTag(block.z + 0.5))
                )
                .putList(
                    "Motion", ListTag<FloatTag>()
                        .add(FloatTag(0f))
                        .add(FloatTag(0f))
                        .add(FloatTag(0f))
                )
                .putList(
                    "Rotation", ListTag<FloatTag>()
                        .add(FloatTag(((player.rotation.yaw + 90f) % 360).toFloat()))
                        .add(FloatTag(0f))
                )
                .putInt("Variant", boatId)
        ) as EntityChestBoat?

        if (boat == null) {
            return false
        }

        if (player.isSurvival || player.isAdventure) {
            val item = player.inventory.itemInHand
            item.setCount(item.getCount() - 1)
            player.inventory.setItemInHand(item)
        }

        boat.spawnToAll()
        return true
    }

    override val maxStackSize: Int
        get() = 1
}
