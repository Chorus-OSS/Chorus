package org.chorus_oss.chorus.entity.weather

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.*
import org.chorus_oss.chorus.block.property.enums.OxidizationLevel
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.event.block.BlockFadeEvent
import org.chorus_oss.chorus.event.block.BlockIgniteEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.level.GameRule
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.level.particle.ElectricSparkParticle
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationType
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import java.util.concurrent.ThreadLocalRandom
import java.util.function.IntConsumer
import java.util.function.Supplier
import kotlin.math.max

class EntityLightningBolt(chunk: IChunk?, nbt: CompoundTag?) : Entity(chunk, nbt),
    EntityLightningStrike {
    override fun getEntityIdentifier(): String {
        return EntityID.LIGHTNING_BOLT
    }

    var state: Int = 0
    var liveTime: Int = 0
    var isEffect: Boolean = true

    override fun initEntity() {
        super.initEntity()

        this.setHealthSafe(4f)
        this.setMaxHealth(4)

        this.state = 2
        this.liveTime = ThreadLocalRandom.current().nextInt(3) + 1

        if (isEffect && level!!.gameRules.getBoolean(GameRule.DO_FIRE_TICK) && (Server.instance.getDifficulty() >= 2)) {
            val block: Block = locator.levelBlock
            if (block.isAir || block.id == BlockID.TALL_GRASS) {
                val fire: BlockFire = Block.get(BlockID.FIRE) as BlockFire
                fire.position = block.position.clone()
                fire.level = level!!
                //                this.getLevel().setBlock(fire, fire, true); WTF???
                if (fire.isBlockTopFacingSurfaceSolid(fire.down()) || fire.canNeighborBurn()) {
                    val e: BlockIgniteEvent =
                        BlockIgniteEvent(block, null, this, BlockIgniteEvent.BlockIgniteCause.LIGHTNING)
                    Server.instance.pluginManager.callEvent(e)

                    if (!e.cancelled) {
                        level!!.setBlock(fire.position, fire, true)
                        level!!.scheduleUpdate(fire, fire.tickRate() + ThreadLocalRandom.current().nextInt(10))
                    }
                }
            }
        }
    }

    override fun attack(source: EntityDamageEvent): Boolean {
        //false?
        source.damage = (0f)
        return super.attack(source)
    }

    override fun onUpdate(currentTick: Int): Boolean {
        if (this.closed) {
            return false
        }

        val tickDiff: Int = currentTick - this.lastUpdate

        if (tickDiff <= 0 && !this.justCreated) {
            return true
        }

        this.lastUpdate = currentTick

        this.entityBaseTick(tickDiff)

        if (this.state == 2) {
            level!!.addSound(this.position, Sound.AMBIENT_WEATHER_THUNDER)
            level!!.addSound(this.position, Sound.RANDOM_EXPLODE)

            val down: Block = level!!.getBlock(position.down())
            if (isVulnerableOxidizable(down)) {
                val changes: MutableMap<Locator, OxidizationLevel> = LinkedHashMap()
                changes.put(Locator(down.vector3, level!!), OxidizationLevel.UNAFFECTED)

                val random: ThreadLocalRandom = ThreadLocalRandom.current()
                val scans: Int = random.nextInt(3) + 3

                val directionPos: Vector3 = Vector3()
                val randomPos: Vector3 = Vector3()
                val cleanOxidizationAround: Supplier<Vector3?> =
                    Supplier<Vector3?> {
                        for (attempt in 0..9) {
                            randomPos.x = directionPos.x + (random.nextInt(3) - 1)
                            randomPos.y = directionPos.y + (random.nextInt(3) - 1)
                            randomPos.z = directionPos.z + (random.nextInt(3) - 1)
                            val possibility: Block = level!!.getBlock(randomPos)
                            if (isVulnerableOxidizable(possibility)) {
                                val nextPos: Vector3 = randomPos.clone()
                                changes.compute(Locator(nextPos, level!!)) { k: Locator?, v: OxidizationLevel? ->
                                    val nextLevel: Int =
                                        if (v == null) (possibility as Oxidizable).oxidizationLevel.ordinal - 1 else v.ordinal - 1
                                    OxidizationLevel.entries.get(max(0.0, nextLevel.toDouble()).toInt())
                                }
                                return@Supplier nextPos
                            }
                        }
                        null
                    }

                val cleanOxidizationAroundLoop: IntConsumer = IntConsumer { count: Int ->
                    directionPos.setComponents(down.position)
                    for (i in 0..<count) {
                        val next: Vector3? = cleanOxidizationAround.get()
                        if (next != null) {
                            directionPos.setComponents(next)
                        } else {
                            break
                        }
                    }
                }

                for (scan in 0..<scans) {
                    val count: Int = random.nextInt(8) + 1
                    cleanOxidizationAroundLoop.accept(count)
                }

                for (entry: Map.Entry<Locator, OxidizationLevel> in changes.entries) {
                    val current: Block = level!!.getBlock(entry.key.position)
                    val next: Block = (current as Oxidizable).getBlockWithOxidizationLevel(entry.value)
                    val event: BlockFadeEvent = BlockFadeEvent(current, next)
                    Server.instance.pluginManager.callEvent(event)
                    if (event.cancelled) {
                        break
                    }
                    level!!.setBlock(entry.key.position, event.newState)
                    level!!.addParticle(ElectricSparkParticle(entry.key.position))
                }
            }
        }

        state--

        if (this.state < 0) {
            if (this.liveTime == 0) {
                this.close()
                return false
            } else if (this.state < -ThreadLocalRandom.current().nextInt(10)) {
                liveTime--
                this.state = 1

                if (this.isEffect && level!!.gameRules.getBoolean(GameRule.DO_FIRE_TICK)) {
                    val block: Block = locator.levelBlock

                    if (block.isAir || block.id == BlockID.TALL_GRASS) {
                        val event: BlockIgniteEvent =
                            BlockIgniteEvent(block, null, this, BlockIgniteEvent.BlockIgniteCause.LIGHTNING)
                        Server.instance.pluginManager.callEvent(event)

                        if (!event.cancelled) {
                            val fire: Block = Block.get(BlockID.FIRE)
                            level!!.setBlock(block.position, fire)
                            level!!.scheduleUpdate(fire, fire.tickRate())
                        }
                    }
                }
            }
        }

        if (this.state >= 0) {
            if (this.isEffect) {
                val bb: AxisAlignedBB = getBoundingBox().grow(3.0, 3.0, 3.0)
                bb.maxX = (bb.maxX + 6)

                for (entity: Entity in level!!.getCollidingEntities(bb, this)) {
                    entity.onStruckByLightning(this)
                }
            }
        }

        return true
    }

    override fun getOriginalName(): String {
        return "Lightning Bolt"
    }

    override fun spawnToAll() {
        level!!.vibrationManager.callVibrationEvent(
            VibrationEvent(
                this,
                this.vector3, VibrationType.LIGHTNING_STRIKE
            )
        )
        super.spawnToAll()
    }

    companion object {
        private fun isVulnerableOxidizable(block: Block): Boolean {
            return block is Oxidizable && (block !is Waxable || !(block as Waxable).isWaxed)
        }
    }
}
