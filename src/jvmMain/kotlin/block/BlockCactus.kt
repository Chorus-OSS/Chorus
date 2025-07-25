package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server.Companion.instance
import org.chorus_oss.chorus.block.BlockFlowerPot.FlowerPotBlock
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.block.BlockGrowEvent
import org.chorus_oss.chorus.event.entity.EntityDamageByBlockEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.Item.Companion.get
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromIndex
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.tags.ItemTags
import org.chorus_oss.chorus.tags.ItemTags.getItemSet

class BlockCactus @JvmOverloads constructor(state: BlockState = properties.defaultState) :
    BlockTransparent(state), FlowerPotBlock, Natural {
    override val hardness: Double
        get() = 0.4

    override val resistance: Double
        get() = 2.0

    override val waterloggingLevel: Int
        get() = 1

    override fun hasEntityCollision(): Boolean {
        return true
    }

    override var minX: Double
        get() = position.x + 0.0625
        set(minX) {
            super.minX = minX
        }

    override var minY: Double
        get() = position.y
        set(minY) {
            super.minY = minY
        }

    override var minZ: Double
        get() = position.z + 0.0625
        set(minZ) {
            super.minZ = minZ
        }

    override var maxX: Double
        get() = position.x + 0.9375
        set(maxX) {
            super.maxX = maxX
        }

    override var maxY: Double
        get() = position.y + 0.9375
        set(maxY) {
            super.maxY = maxY
        }

    override var maxZ: Double
        get() = position.z + 0.9375
        set(maxZ) {
            super.maxZ = maxZ
        }

    override fun recalculateCollisionBoundingBox(): AxisAlignedBB {
        return SimpleAxisAlignedBB(
            position.x,
            position.y,
            position.z, position.x + 1, position.y + 1, position.z + 1
        )
    }

    override fun onEntityCollide(entity: Entity) {
        entity.attack(EntityDamageByBlockEvent(this, entity, DamageCause.CONTACT, 1f))
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            val down = down()
            if (!getItemSet(ItemTags.SAND).contains(down.id)
                && down !is BlockCactus
            ) {
                level.useBreakOn(this.position)
                return 0
            }
            for (side in 2..5) {
                val block = getSide(fromIndex(side))
                if (!block.canBeFlowedInto()) {
                    level.useBreakOn(this.position)
                }
            }
            return 0
        }
        if (type == Level.BLOCK_UPDATE_RANDOM) {
            if (down() is BlockCactus) {
                return 0
            }
            if (this.age < maxAge) {
                this.age = this.age + 1
                level.setBlock(this.position, this)
                return 0
            }
            for (y in 1..2) {
                val b = level.getBlock(
                    Vector3(
                        position.x,
                        position.y + y, position.z
                    )
                )
                if (!b.isAir) {
                    continue
                }
                val event = BlockGrowEvent(b, get(BlockID.CACTUS))
                instance.pluginManager.callEvent(event)
                if (!event.cancelled) {
                    level.setBlock(b.position, event.newState, true)
                }
                break
            }
            this.age = minAge
        }

        return 0
    }

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
        val down = this.down()
        if (!getItemSet(ItemTags.SAND.toString()).contains(down.id)
            && down !is BlockCactus
        ) {
            return false
        }
        val block0 = north()
        val block1 = south()
        val block2 = west()
        val block3 = east()
        if (block0.canBeFlowedInto() && block1.canBeFlowedInto() && block2.canBeFlowedInto() && block3.canBeFlowedInto()) {
            level.setBlock(this.position, this, true)
            return true
        }
        return false
    }

    override val name: String
        get() = "Cactus"

    override fun getDrops(item: Item): Array<Item> {
        return arrayOf(
            get(BlockID.CACTUS, 0, 1)
        )
    }

    override fun breaksWhenMoved(): Boolean {
        return true
    }

    override fun sticksToPiston(): Boolean {
        return false
    }

    var age: Int
        get() = this.getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.AGE_16)
        set(age) {
            this.setPropertyValue<Int, IntPropertyType>(
                CommonBlockProperties.AGE_16,
                age
            )
        }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.CACTUS,
            CommonBlockProperties.AGE_16
        )

        val maxAge: Int
            get() = CommonBlockProperties.AGE_16.max

        val minAge: Int
            get() = CommonBlockProperties.AGE_16.min
    }
}
