package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.CrackedState
import org.chorus_oss.chorus.block.property.enums.TurtleEggCount
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.Entity.Companion.createEntity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.EntityLiving
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.mob.EntityBat
import org.chorus_oss.chorus.entity.mob.animal.EntityChicken
import org.chorus_oss.chorus.entity.mob.animal.EntityTurtle
import org.chorus_oss.chorus.entity.mob.monster.EntityGhast
import org.chorus_oss.chorus.entity.mob.monster.EntityPhantom
import org.chorus_oss.chorus.event.Event
import org.chorus_oss.chorus.event.block.BlockGrowEvent
import org.chorus_oss.chorus.event.block.BlockPlaceEvent
import org.chorus_oss.chorus.event.block.TurtleEggHatchEvent
import org.chorus_oss.chorus.event.entity.CreatureSpawnEvent
import org.chorus_oss.chorus.event.entity.EntityInteractEvent
import org.chorus_oss.chorus.event.player.PlayerInteractEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.particle.BoneMealParticle
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.registry.Registries
import java.util.concurrent.ThreadLocalRandom

class BlockTurtleEgg @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockFlowable(blockstate) {
    override val name: String
        get() = "Turtle Egg"

    var cracks: CrackedState
        get() = getPropertyValue(CommonBlockProperties.CRACKED_STATE)
        set(cracks) {
            setPropertyValue(
                CommonBlockProperties.CRACKED_STATE,
                cracks
            )
        }

    override val hardness: Double
        get() = 0.5

    override val resistance: Double
        get() = 2.5

    var eggCount: TurtleEggCount
        get() = getPropertyValue(CommonBlockProperties.TURTLE_EGG_COUNT)
        set(eggCount) {
            setPropertyValue(
                CommonBlockProperties.TURTLE_EGG_COUNT,
                eggCount
            )
        }

    override fun canBeActivated(): Boolean {
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
        if (item.blockId == BlockID.TURTLE_EGG && (player == null || !player.isSneaking())) {
            val eggCount = eggCount
            if (eggCount == TurtleEggCount.FOUR_EGG) {
                return false
            }
            val newState = BlockTurtleEgg()
            newState.eggCount = eggCount.next()
            val placeEvent = BlockPlaceEvent(
                player!!,
                newState,
                this,
                down(),
                item
            )
            if (placeEvent.cancelled) {
                return false
            }
            if (!level.setBlock(this.position, placeEvent.block, true, true)) {
                return false
            }
            val placeBlock = placeEvent.block
            level.addLevelSoundEvent(
                this.position,
                org.chorus_oss.protocol.packets.LevelSoundEventPacket.Companion.SoundType.Place,
                placeBlock.runtimeId
            )
            item.setCount(item.getCount() - 1)

            if (down().id == BlockID.SAND) {
                level.addParticle(BoneMealParticle(this.position))
            }

            return true
        }

        return false
    }

    override fun hasEntityCollision(): Boolean {
        return true
    }

    override var minX: Double
        get() = position.x + (3.0 / 16)
        set(minX) {
            super.minX = minX
        }

    override var minZ: Double
        get() = position.z + (3.0 / 16)
        set(minZ) {
            super.minZ = minZ
        }

    override var maxX: Double
        get() = position.x + (12.0 / 16)
        set(maxX) {
            super.maxX = maxX
        }

    override var maxZ: Double
        get() = position.z + (12.0 / 16)
        set(maxZ) {
            super.maxZ = maxZ
        }

    override var maxY: Double
        get() = position.y + (7.0 / 16)
        set(maxY) {
            super.maxY = maxY
        }

    override fun recalculateCollisionBoundingBox(): AxisAlignedBB {
        return this
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_RANDOM) {
            if (down().id == BlockID.SAND) {
                val celestialAngle = level.calculateCelestialAngle(level.getTime(), 1f)
                val random = ThreadLocalRandom.current()
                if (0.70 > celestialAngle && celestialAngle > 0.65 || random.nextInt(500) == 0) {
                    val crackState = cracks
                    if (crackState != CrackedState.MAX_CRACKED) {
                        val newState = clone()
                        newState.cracks = crackState.next()
                        val event = BlockGrowEvent(this, newState)
                        Server.instance.pluginManager.callEvent(event)
                        if (!event.cancelled) {
                            level.addSound(
                                this.position,
                                Sound.BLOCK_TURTLE_EGG_CRACK,
                                0.7f,
                                0.9f + random.nextFloat() * 0.2f
                            )
                            level.setBlock(this.position, event.newState, true, true)
                        }
                    } else {
                        hatch()
                    }
                }
            }
            return type
        }
        return 0
    }

    @JvmOverloads
    fun hatch(eggs: TurtleEggCount = eggCount, newState: Block = BlockAir()) {
        val turtleEggHatchEvent = TurtleEggHatchEvent(this, eggs.ordinal + 1, newState)
        //TODO Cancelled by default because EntityTurtle doesn't have AI yet, remove it when AI is added
        turtleEggHatchEvent.cancelled = true
        Server.instance.pluginManager.callEvent(turtleEggHatchEvent)
        val eggsHatching = turtleEggHatchEvent.eggsHatching
        if (!turtleEggHatchEvent.cancelled) {
            level.addSound(this.position, Sound.BLOCK_TURTLE_EGG_CRACK)

            var hasFailure = false
            for (i in 0..<eggsHatching) {
                level.addSound(this.position, Sound.BLOCK_TURTLE_EGG_CRACK)

                val creatureSpawnEvent = CreatureSpawnEvent(
                    Registries.ENTITY.getEntityNetworkId(EntityID.TURTLE),
                    add(
                        0.3 + i * 0.2,
                        0.0,
                        0.3
                    ),
                    CreatureSpawnEvent.SpawnReason.TURTLE_EGG
                )
                Server.instance.pluginManager.callEvent(creatureSpawnEvent)

                if (!creatureSpawnEvent.cancelled) {
                    val turtle = createEntity(
                        creatureSpawnEvent.entityNetworkId,
                        creatureSpawnEvent.position
                    ) as EntityTurtle?
                    if (turtle != null) {
                        turtle.setBreedingAge(-24000)
                        turtle.setHomePos(position.clone())
                        turtle.setDataFlag(EntityFlag.BABY, true)
                        turtle.setScale(0.16f)
                        turtle.spawnToAll()
                        continue
                    }
                }

                if (turtleEggHatchEvent.isRecalculateOnFailure) {
                    turtleEggHatchEvent.eggsHatching = turtleEggHatchEvent.eggsHatching - 1
                    hasFailure = true
                }
            }

            if (hasFailure) {
                turtleEggHatchEvent.recalculateNewState()
            }

            level.setBlock(this.position, turtleEggHatchEvent.newState, true, true)
        }
    }

    override fun onEntityCollide(entity: Entity) {
        if (entity is EntityLiving
            && (entity !is EntityChicken) && (entity !is EntityBat) && (entity !is EntityGhast) && (entity !is EntityPhantom) && entity.getY() >= this.maxY
        ) {
            val ev: Event = if (entity is Player) {
                PlayerInteractEvent(
                    entity,
                    null,
                    position,
                    null,
                    PlayerInteractEvent.Action.PHYSICAL
                )
            } else {
                EntityInteractEvent(entity, this)
            }

            ev.cancelled = ThreadLocalRandom.current().nextInt(200) > 0
            Server.instance.pluginManager.callEvent(ev)
            if (!ev.cancelled) {
                level.useBreakOn(this.position, null, null, true)
            }
        }
    }

    override fun onBreak(item: Item?): Boolean {
        val eggCount = eggCount
        if (item!!.getEnchantment(Enchantment.ID_SILK_TOUCH) == null) {
            level.addSound(this.position, Sound.BLOCK_TURTLE_EGG_CRACK)
        }
        if (eggCount == TurtleEggCount.ONE_EGG) {
            return super.onBreak(item)
        } else {
            this.eggCount = eggCount.before()
            return level.setBlock(this.position, this, true, true)
        }
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
        if (!isValidSupport(block.down(1, 0))) {
            return false
        }

        if (level.setBlock(this.position, this, true, true)) {
            if (down().id == BlockID.SAND) {
                level.addParticle(BoneMealParticle(this.position))
            }
            return true
        } else {
            return false
        }
    }

    fun isValidSupport(support: Block): Boolean {
        return support.isSolid(BlockFace.UP) || support is BlockWallBase
    }

    override fun getDrops(item: Item): Array<Item> {
        return Item.EMPTY_ARRAY
    }

    override fun canSilkTouch(): Boolean {
        return true
    }

    override val waterloggingLevel: Int
        get() = 1

    override fun canPassThrough(): Boolean {
        return false
    }

    override fun canBeFlowedInto(): Boolean {
        return false
    }

    override fun clone(): BlockTurtleEgg {
        return super.clone() as BlockTurtleEgg
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.TURTLE_EGG,
            CommonBlockProperties.CRACKED_STATE,
            CommonBlockProperties.TURTLE_EGG_COUNT
        )
    }
}
