package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.effect.EffectType
import org.chorus_oss.chorus.entity.projectile.abstract_arrow.EntityArrow
import org.chorus_oss.chorus.event.block.BlockBurnEvent
import org.chorus_oss.chorus.event.block.BlockFadeEvent
import org.chorus_oss.chorus.event.block.BlockIgniteEvent
import org.chorus_oss.chorus.event.entity.EntityCombustByBlockEvent
import org.chorus_oss.chorus.event.entity.EntityDamageByBlockEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.GameRule
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.max
import kotlin.math.min


open class BlockFire @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockFlowable(blockstate) {
    var age: Int
        get() = getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.AGE_16)
        set(age) {
            setPropertyValue<Int, IntPropertyType>(CommonBlockProperties.AGE_16, age)
        }

    override fun hasEntityCollision(): Boolean {
        return true
    }

    override val name: String
        get() = "Fire Block"

    override val lightLevel: Int
        get() = 15

    override fun isBreakable(vector: Vector3, layer: Int, face: BlockFace?, item: Item?, player: Player?): Boolean {
        return false
    }

    override fun canBeReplaced(): Boolean {
        return true
    }

    override fun onEntityCollide(entity: Entity) {
        if (!entity.hasEffect(EffectType.FIRE_RESISTANCE)) {
            entity.attack(EntityDamageByBlockEvent(this, entity, DamageCause.FIRE, 1f))
        }

        val ev = EntityCombustByBlockEvent(this, entity, 8)
        if (entity is EntityArrow) {
            ev.cancelled = true
        }
        Server.instance.pluginManager.callEvent(ev)
        if (!ev.cancelled && entity.isAlive() && entity.noDamageTicks == 0) {
            entity.setOnFire(ev.duration)
        }
    }

    override fun getDrops(item: Item): Array<Item> {
        return Item.EMPTY_ARRAY
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL || type == Level.BLOCK_UPDATE_RANDOM) {
            val down = down()

            if (type == Level.BLOCK_UPDATE_NORMAL) {
                val downId = down.id
                if (downId == BlockID.SOUL_SAND || downId == BlockID.SOUL_SOIL) {
                    level.setBlock(
                        this.position, get(BlockID.SOUL_FIRE).setPropertyValue<Int, IntPropertyType>(
                            CommonBlockProperties.AGE_16,
                            age
                        )
                    )
                    return type
                }
            }

            if (!this.isBlockTopFacingSurfaceSolid(down) && !this.canNeighborBurn()) {
                val event = BlockFadeEvent(this, get(BlockID.AIR))
                Server.instance.pluginManager.callEvent(event)
                if (!event.cancelled) {
                    level.setBlock(this.position, event.newState, true)
                }
            } else if (level.gameRules.getBoolean(GameRule.DO_FIRE_TICK) && !level.isUpdateScheduled(
                    this.position,
                    this
                )
            ) {
                level.scheduleUpdate(this, tickRate())
            }

            //在第一次放置时就检查下雨
            checkRain()

            return Level.BLOCK_UPDATE_NORMAL
        } else if (type == Level.BLOCK_UPDATE_SCHEDULED && level.gameRules.getBoolean(GameRule.DO_FIRE_TICK)) {
            val down = down()
            val downId = down.id

            val random = ThreadLocalRandom.current()

            //TODO: END
            if (!this.isBlockTopFacingSurfaceSolid(down) && !this.canNeighborBurn()) {
                val event = BlockFadeEvent(this, get(BlockID.AIR))
                Server.instance.pluginManager.callEvent(event)
                if (!event.cancelled) {
                    level.setBlock(this.position, event.newState, true)
                }
            }

            val forever = downId == BlockID.NETHERRACK ||
                    downId == BlockID.MAGMA || downId == BlockID.BEDROCK && (down as BlockBedrock).burnIndefinitely

            if (!checkRain()) {
                val meta = age

                if (meta < 15) {
                    val newMeta = meta + random.nextInt(3)
                    this.age = min(newMeta.toDouble(), 15.0).toInt()
                    level.setBlock(this.position, this, true)
                }

                level.scheduleUpdate(this, this.tickRate() + random.nextInt(10))

                if (!forever && !this.canNeighborBurn()) {
                    if (!this.isBlockTopFacingSurfaceSolid(down) || meta > 3) {
                        val event = BlockFadeEvent(this, get(BlockID.AIR))
                        Server.instance.pluginManager.callEvent(event)
                        if (!event.cancelled) {
                            level.setBlock(this.position, event.newState, true)
                        }
                    }
                } else if (!forever && (down.burnAbility <= 0) && meta == 15 && random.nextInt(4) == 0) {
                    val event = BlockFadeEvent(this, get(BlockID.AIR))
                    Server.instance.pluginManager.callEvent(event)
                    if (!event.cancelled) {
                        level.setBlock(this.position, event.newState, true)
                    }
                } else {
                    val o = 0

                    //TODO: decrease the o if the rainfall values are high
                    this.tryToCatchBlockOnFire(east(), 300 + o, meta)
                    this.tryToCatchBlockOnFire(west(), 300 + o, meta)
                    this.tryToCatchBlockOnFire(down, 250 + o, meta)
                    this.tryToCatchBlockOnFire(up(), 250 + o, meta)
                    this.tryToCatchBlockOnFire(south(), 300 + o, meta)
                    this.tryToCatchBlockOnFire(north(), 300 + o, meta)

                    for (x in (position.x - 1).toInt()..(position.x + 1).toInt()) {
                        for (z in (position.z - 1).toInt()..(position.z + 1).toInt()) {
                            for (y in (position.y - 1).toInt()..(position.y + 4).toInt()) {
                                if (x != position.x.toInt() || y != position.y.toInt() || z != position.z.toInt()) {
                                    var k = 100

                                    if (y > position.y + 1) {
                                        k += ((y - (position.y + 1)) * 100).toInt()
                                    }

                                    val block =
                                        level.getBlock(Vector3(x.toDouble(), y.toDouble(), z.toDouble()))
                                    val chance = this.getChanceOfNeighborsEncouragingFire(
                                        block
                                    )

                                    if (chance > 0) {
                                        val t = (chance + 40 + Server.instance.getDifficulty() * 7) / (meta + 30)

                                        //TODO: decrease the t if the rainfall values are high
                                        if (t > 0 && random.nextInt(k) <= t) {
                                            var damage = meta + random.nextInt(5) / 4

                                            if (damage > 15) {
                                                damage = 15
                                            }

                                            val e = BlockIgniteEvent(
                                                block,
                                                this,
                                                null,
                                                BlockIgniteEvent.BlockIgniteCause.SPREAD
                                            )
                                            Server.instance.pluginManager.callEvent(e)

                                            if (!e.cancelled) {
                                                level.setBlock(
                                                    block.position, get(
                                                        blockState.setPropertyValue(
                                                            Companion.properties,
                                                            CommonBlockProperties.AGE_16.createValue(
                                                                age
                                                            )
                                                        )
                                                    ), true
                                                )
                                                level.scheduleUpdate(block, this.tickRate())
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return 0
    }

    private fun tryToCatchBlockOnFire(block: Block, bound: Int, damage: Int) {
        val burnAbility = block.burnAbility

        val random: Random = ThreadLocalRandom.current()

        if (random.nextInt(bound) < burnAbility) {
            if (random.nextInt(damage + 10) < 5) {
                var meta = damage + random.nextInt(5) / 4

                if (meta > 15) {
                    meta = 15
                }

                val e = BlockIgniteEvent(block, this, null, BlockIgniteEvent.BlockIgniteCause.SPREAD)
                Server.instance.pluginManager.callEvent(e)

                if (!e.cancelled) {
                    level.setBlock(
                        block.position, get(
                            blockState.setPropertyValue(
                                Companion.properties, CommonBlockProperties.AGE_16.createValue(
                                    age
                                )
                            )
                        ), true
                    )
                    level.scheduleUpdate(block, this.tickRate())
                }
            } else {
                val ev = BlockBurnEvent(block)
                Server.instance.pluginManager.callEvent(ev)

                if (!ev.cancelled) {
                    level.setBlock(block.position, get(BlockID.AIR), true)
                }
            }

            if (block is BlockTNT) {
                block.prime()
            }
        }
    }

    private fun getChanceOfNeighborsEncouragingFire(block: Block): Int {
        if (!block.isAir) {
            return 0
        } else {
            var chance = 0
            chance = max(chance.toDouble(), block.east().burnChance.toDouble()).toInt()
            chance = max(chance.toDouble(), block.west().burnChance.toDouble()).toInt()
            chance = max(chance.toDouble(), block.down().burnChance.toDouble()).toInt()
            chance = max(chance.toDouble(), block.up().burnChance.toDouble()).toInt()
            chance = max(chance.toDouble(), block.south().burnChance.toDouble()).toInt()
            chance = max(chance.toDouble(), block.north().burnChance.toDouble()).toInt()
            return chance
        }
    }

    fun canNeighborBurn(): Boolean {
        for (face in BlockFace.entries) {
            if (getSide(face).burnChance > 0) {
                return true
            }
        }

        return false
    }

    fun isBlockTopFacingSurfaceSolid(block: Block?): Boolean {
        if (block != null) {
            return if (block is BlockStairs) {
                false
            } else if (block is BlockSlab && !block.isOnTop) {
                false
            } else if (block is BlockSnowLayer) {
                false
            } else if (block is BlockFenceGate) {
                false
            } else if (block is BlockTrapdoor) {
                false
            } else if (block is BlockMossCarpet) {
                false
            } else if (block is BlockAzalea) {
                false
            } else block.isSolid
        }
        return false
    }

    override fun tickRate(): Int {
        return 30
    }

    override fun recalculateCollisionBoundingBox(): AxisAlignedBB? {
        return this
    }

    override fun toItem(): Item {
        return Item.AIR
    }

    /**
     * 检查火焰是否应被雨水浇灭
     *
     * @return 是否应被雨水浇灭
     */
    protected fun checkRain(): Boolean {
        val down = down()
        val downId = down.id
        val forever = downId == BlockID.NETHERRACK || downId == BlockID.MAGMA
                || downId == BlockID.BEDROCK && (down as BlockBedrock).burnIndefinitely

        if (!forever && level.isRaining &&
            (level.canBlockSeeSky(this.position) ||
                    level.canBlockSeeSky(east().position) ||
                    level.canBlockSeeSky(west().position) ||
                    level.canBlockSeeSky(south().position) ||
                    level.canBlockSeeSky(north().position))
        ) {
            val event = BlockFadeEvent(this, get(BlockID.AIR))
            Server.instance.pluginManager.callEvent(event)
            if (!event.cancelled) {
                level.setBlock(this.position, event.newState, true)
            }
            return true
        }
        return false
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.FIRE, CommonBlockProperties.AGE_16)
    }
}
