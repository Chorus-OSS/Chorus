package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.block.BlockGrowEvent
import org.chorus_oss.chorus.event.block.BlockHarvestEvent
import org.chorus_oss.chorus.event.entity.EntityDamageByBlockEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemSweetBerries
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.particle.BoneMealParticle
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.tags.BlockTags
import java.util.concurrent.ThreadLocalRandom

class BlockSweetBerryBush @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockFlowable(blockstate) {
    override val name: String
        get() = "Sweet Berry Bush"

    override val burnChance: Int
        get() = 30

    override val burnAbility: Int
        get() = 60

    override fun canBeActivated(): Boolean {
        return true
    }

    override val hardness: Double
        get() = if (growth == 0) 0.0 else 0.25

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        val age: Int = growth.coerceIn(0, 3)

        if (age < 3 && item.isFertilizer) {
            val block = clone() as BlockSweetBerryBush
            block.setGrowth(block.growth + 1)
            if (block.growth > 3) {
                block.setGrowth(3)
            }
            val ev: BlockGrowEvent = BlockGrowEvent(this, block)
            Server.instance.pluginManager.callEvent(ev)

            if (ev.cancelled) {
                return false
            }

            level.setBlock(this.position, ev.newState, false, true)
            level.addParticle(BoneMealParticle(this.position))

            if (player != null && (player.gamemode and 0x01) == 0) {
                item.count--
            }

            return true
        }

        if (age < 2) {
            return true
        }

        var amount = 1 + ThreadLocalRandom.current().nextInt(2)
        if (age == 3) {
            amount++
        }

        val event: BlockHarvestEvent = BlockHarvestEvent(
            this,
            BlockSweetBerryBush().setGrowth(1),
            arrayOf<Item>(ItemSweetBerries(0, amount))
        )

        Server.instance.pluginManager.callEvent(event)
        if (!event.cancelled) {
            level.setBlock(this.position, event.newState, true, true)
            val dropPos = add(0.5, 0.5, 0.5)
            for (drop in event.drops) {
                level.dropItem(dropPos.position, drop)
            }
        }

        return true
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (!isSupportValid(down())) {
                level.useBreakOn(this.position)
                return Level.BLOCK_UPDATE_NORMAL
            }
        } else if (type == Level.BLOCK_UPDATE_RANDOM) {
            if (growth < 3 && ThreadLocalRandom.current().nextInt(5) == 0 && level.getFullLight(
                    position.add(0.0, 1.0, 0.0)
                ) >= BlockCrops.MIN_LIGHT_LEVEL
            ) {
                val event: BlockGrowEvent = BlockGrowEvent(
                    this, get(id).setPropertyValue<Int, IntPropertyType>(
                        CommonBlockProperties.GROWTH, growth + 1
                    )
                )
                if (!event.cancelled) {
                    level.setBlock(this.position, event.newState, true, true)
                }
            }
            return type
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
        if (target!!.id == BlockID.SWEET_BERRY_BUSH || !block.isAir) {
            return false
        }
        if (isSupportValid(block.down())) {
            level.setBlock(block.position, this, true)
            return true
        }
        return false
    }

    override fun hasEntityCollision(): Boolean {
        return growth > 0
    }

    override fun onEntityCollide(entity: Entity) {
        if (growth > 0) {
            if (entity.positionChanged && !entity.isSneaking() && ThreadLocalRandom.current().nextInt(20) == 0) {
                if (entity.attack(EntityDamageByBlockEvent(this, entity, EntityDamageEvent.DamageCause.CONTACT, 1f))) {
                    level.addSound(entity.position, Sound.BLOCK_SWEET_BERRY_BUSH_HURT)
                }
            }
        }
    }

    override val collisionBoundingBox: AxisAlignedBB?
        get() = if (growth > 0) this else null

    override fun getDrops(item: Item): Array<Item> {
        val age: Int = growth.coerceIn(0, 3)

        var amount = 1
        if (age > 1) {
            amount = 1 + ThreadLocalRandom.current().nextInt(2)
            if (age == 3) {
                amount++
            }
        }

        return arrayOf(ItemSweetBerries(0, amount))
    }

    val growth: Int
        get() = getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.GROWTH)

    fun setGrowth(growth: Int): Block {
        return setPropertyValue<Int, IntPropertyType>(CommonBlockProperties.GROWTH, growth)
    }

    override fun toItem(): Item {
        return ItemSweetBerries()
    }

    override val isFertilizable: Boolean
        get() = true

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.SWEET_BERRY_BUSH, CommonBlockProperties.GROWTH)


        fun isSupportValid(block: Block): Boolean {
            return block.`is`(BlockTags.DIRT)
        }
    }
}
