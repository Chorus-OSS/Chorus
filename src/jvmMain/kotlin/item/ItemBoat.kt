package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockFlowingWater
import org.chorus_oss.chorus.entity.Entity.Companion.createEntity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.item.EntityBoat
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.FloatTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.utils.Identifier

open class ItemBoat : Item {
    @JvmOverloads
    constructor(meta: Int = 0, count: Int = 1) : super(ItemID.Companion.BOAT, meta, count)

    constructor(id: String) : super(id)

    override fun internalAdjust() {
        when (damage) {
            0 -> {
                name = "Oak Boat"
                this.id = ItemID.Companion.OAK_BOAT
                this.identifier = Identifier(ItemID.Companion.OAK_BOAT)
                return
            }

            1 -> {
                name = "Spruce Boat"
                this.id = ItemID.Companion.SPRUCE_BOAT
                this.identifier = Identifier(ItemID.Companion.SPRUCE_BOAT)
                return
            }

            2 -> {
                name = "Birch Boat"
                this.id = ItemID.Companion.BIRCH_BOAT
                this.identifier = Identifier(ItemID.Companion.BIRCH_BOAT)
                return
            }

            3 -> {
                name = "Jungle Boat"
                this.id = ItemID.Companion.JUNGLE_BOAT
                this.identifier = Identifier(ItemID.Companion.JUNGLE_BOAT)
                return
            }

            4 -> {
                name = "Acacia Boat"
                this.id = ItemID.Companion.ACACIA_BOAT
                this.identifier = Identifier(ItemID.Companion.ACACIA_BOAT)
                return
            }

            5 -> {
                name = "Dark Oak Boat"
                this.id = ItemID.Companion.DARK_OAK_BOAT
                this.identifier = Identifier(ItemID.Companion.DARK_OAK_BOAT)
                return
            }

            6 -> {
                name = "Mangrove Boat"
                this.id = ItemID.Companion.MANGROVE_BOAT
                this.identifier = Identifier(ItemID.Companion.MANGROVE_BOAT)
                return
            }

            7 -> {
                name = "Bamboo Raft"
                this.id = ItemID.Companion.BAMBOO_RAFT
                this.identifier = Identifier(ItemID.Companion.BAMBOO_RAFT)
                return
            }

            8 -> {
                name = "Cherry Boat"
                this.id = ItemID.Companion.CHERRY_BOAT
                this.identifier = Identifier(ItemID.Companion.CHERRY_BOAT)
            }
        }
        this.meta = 0
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
            EntityID.BOAT,
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
        ) as EntityBoat?

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
