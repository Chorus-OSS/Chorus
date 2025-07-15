package org.chorus_oss.chorus.level

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.block.BlockTNT
import org.chorus_oss.chorus.blockentity.BlockEntity
import org.chorus_oss.chorus.blockentity.BlockEntityShulkerBox
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityExplosive
import org.chorus_oss.chorus.entity.item.EntityItem
import org.chorus_oss.chorus.entity.item.EntityXpOrb
import org.chorus_oss.chorus.event.block.BlockExplodeEvent
import org.chorus_oss.chorus.event.block.BlockUpdateEvent
import org.chorus_oss.chorus.event.entity.EntityDamageByBlockEvent
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityExplodeEvent
import org.chorus_oss.chorus.inventory.InventoryHolder
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.network.protocol.LevelEventPacket
import org.chorus_oss.chorus.utils.Hash
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

class Explosion protected constructor(private val source: Locator, size: Double, private val what: Any) {
    private val RAYS = 16
    private val STEP_LEN = 0.3

    private val level = source.level
    private val size = size.coerceAtLeast(0.0)

    @JvmField
    var fireChance: Double = 0.0

    private var affectedBlocks: MutableSet<Block> = mutableSetOf()
    private var fireIgnitions: MutableSet<Block> = mutableSetOf()
    private var doesDamage = true

    constructor(center: Locator, size: Double, what: Entity) : this(center, size, what as Any)

    constructor(center: Locator, size: Double, what: Block) : this(center, size, what as Any)

    var isIncendiary: Boolean
        get() = fireChance > 0
        set(incendiary) {
            if (!incendiary) {
                fireChance = 0.0
            } else if (fireChance <= 0) {
                fireChance = 1.0 / 3.0
            }
        }

    /**
     * @return bool
     */
    fun explode(): Boolean {
        if (explodeA()) {
            return explodeB()
        }
        return false
    }

    /**
     * Calculates which blocks will be destroyed by this explosion. If [.explodeB] is called without calling this,
     * no blocks will be destroyed.
     *
     * @return `true` if success
     */
    fun explodeA(): Boolean {
        if (what is EntityExplosive) {
            val entity = what as Entity
            val blockLayer0 = level.getBlock(entity.position.floor())
            val blockLayer1 = level.getBlock(entity.position.floor(), 1)
            if (BlockID.FLOWING_WATER == blockLayer0.id
                || BlockID.WATER == blockLayer0.id
                || BlockID.FLOWING_WATER == blockLayer1.id
                || BlockID.WATER == blockLayer1.id
            ) {
                this.doesDamage = false
                return true
            }
        }

        if (this.size < 0.1) {
            return false
        }

        val incendiary = fireChance > 0

        val random = ThreadLocalRandom.current()

        val vector = Vector3(0.0, 0.0, 0.0)
        val vBlock = Vector3(0.0, 0.0, 0.0)

        val mRays = this.RAYS - 1
        for (i in 0..<this.RAYS) {
            for (j in 0..<this.RAYS) {
                for (k in 0..<this.RAYS) {
                    if (i == 0 || i == mRays || j == 0 || j == mRays || k == 0 || k == mRays) {
                        vector.setComponents(
                            i.toDouble() / mRays.toDouble() * 2.0 - 1,
                            j.toDouble() / mRays.toDouble() * 2.0 - 1,
                            k.toDouble() / mRays.toDouble() * 2.0 - 1
                        )
                        val len = vector.length()
                        vector.setComponents(
                            (vector.x / len) * this.STEP_LEN,
                            (vector.y / len) * this.STEP_LEN,
                            (vector.z / len) * this.STEP_LEN
                        )
                        var pointerX = source.position.x
                        var pointerY = source.position.y
                        var pointerZ = source.position.z

                        var blastForce = this.size * (random.nextInt(700, 1301)) / 1000.0
                        while (blastForce > 0) {
                            val x = pointerX.toInt()
                            val y = pointerY.toInt()
                            val z = pointerZ.toInt()
                            vBlock.x = (if (pointerX >= x) x else x - 1).toDouble()
                            vBlock.y = (if (pointerY >= y) y else y - 1).toDouble()
                            vBlock.z = (if (pointerZ >= z) z else z - 1).toDouble()
                            if (!level.isYInRange(vBlock.y.toInt())) {
                                break
                            }
                            val block = level.getBlock(vBlock)

                            if (!block.isAir) {
                                val layer1 = block.getLevelBlockAtLayer(1)
                                val resistance = block.resistance.coerceAtLeast(layer1.resistance)
                                blastForce -= (resistance / 5 + 0.3) * this.STEP_LEN
                                if (blastForce > 0) {
                                    if (affectedBlocks.add(block)) {
                                        if (incendiary && random.nextDouble() <= fireChance) {
                                            fireIgnitions.add(block)
                                        }
                                        if (!layer1.isAir) {
                                            affectedBlocks.add(layer1)
                                        }
                                    }
                                }
                            }
                            pointerX += vector.x
                            pointerY += vector.y
                            pointerZ += vector.z
                            blastForce -= this.STEP_LEN * 0.75
                        }
                    }
                }
            }
        }

        return true
    }

    /**
     * Executes the explosion's effects on the world. This includes destroying blocks (if any),
     * harming and knocking back entities, and creating sounds and particles.
     *
     * @return `false` if explosion was canceled, otherwise `true`
     */
    fun explodeB(): Boolean {
        val updateBlocks: MutableSet<Long> = mutableSetOf()
        val send: MutableList<Vector3> = mutableListOf()

        val source = (Vector3(
            source.position.x,
            source.position.y, source.position.z
        )).floor()
        var yield = (1.0 / this.size) * 100.0

        if (what is Entity) {
            val affectedBlocksList: List<Block> = this.affectedBlocks.toList()
            val ev: EntityExplodeEvent = EntityExplodeEvent(
                what,
                this.source, affectedBlocksList, yield
            )
            ev.ignitions = (fireIgnitions)
            Server.instance.pluginManager.callEvent(ev)
            if (ev.cancelled) {
                return false
            } else {
                yield = ev.yield
                affectedBlocks.clear()
                affectedBlocks.addAll(ev.blockList)
                fireIgnitions = ev.ignitions.toMutableSet()
            }
        } else if (what is Block) {
            val ev: BlockExplodeEvent = BlockExplodeEvent(
                what, this.source, this.affectedBlocks,
                fireIgnitions, yield, this.fireChance
            )
            Server.instance.pluginManager.callEvent(ev)
            if (ev.cancelled) {
                return false
            } else {
                yield = ev.yield
                affectedBlocks = ev.affectedBlocks.toMutableSet()
                fireIgnitions = ev.ignitions.toMutableSet()
            }
        }

        val explosionSize = this.size * 2.0
        val minX = floor(this.source.position.x - explosionSize - 1)
        val maxX = ceil(this.source.position.x + explosionSize + 1)
        val minY = floor(this.source.position.y - explosionSize - 1)
        val maxY = ceil(this.source.position.y + explosionSize + 1)
        val minZ = floor(this.source.position.z - explosionSize - 1)
        val maxZ = ceil(this.source.position.z + explosionSize + 1)

        val explosionBB: AxisAlignedBB = SimpleAxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ)
        val list = level.getNearbyEntities(explosionBB, if (what is Entity) what else null)
        for (entity in list) {
            val distance = entity.position.distance(this.source.position) / explosionSize

            if (distance <= 1) {
                val motion = entity.position.subtract(this.source.position).normalize()

                val blockDensity = level.getBlockDensity(this.source.position, entity.boundingBox)
                val force = this.size * 2.0f
                val d = entity.position.distance(source) / force
                val impact = (1.0 - d) * blockDensity
                val entityDamageAmount = ((impact * impact + impact).toFloat() / 2.0 * 7.0 * force + 1.0).toFloat()
                val damage = if (this.doesDamage) entityDamageAmount else 0f

                when (what) {
                    is Entity -> {
                        entity.attack(
                            EntityDamageByEntityEvent(
                                what,
                                entity,
                                EntityDamageEvent.DamageCause.ENTITY_EXPLOSION,
                                damage
                            )
                        )
                    }

                    is Block -> {
                        entity.attack(
                            EntityDamageByBlockEvent(
                                what,
                                entity,
                                EntityDamageEvent.DamageCause.BLOCK_EXPLOSION,
                                damage
                            )
                        )
                    }

                    else -> {
                        entity.attack(EntityDamageEvent(entity, EntityDamageEvent.DamageCause.BLOCK_EXPLOSION, damage))
                    }
                }

                if (!(entity is EntityItem || entity is EntityXpOrb)) {
                    val multipliedMotion = motion.multiply(impact)
                    entity.motion.x += multipliedMotion.x
                    entity.motion.y += multipliedMotion.y
                    entity.motion.z += multipliedMotion.z
                }
            }
        }

        val air = Item.AIR
        var container: BlockEntity?
        val smokePositions: MutableList<Vector3> = mutableListOf()
        val random = ThreadLocalRandom.current()

        for (block in affectedBlocks) {
            if (block is BlockTNT) {
                block.prime(random.nextInt(10, 31), if (what is Entity) what else null)
            } else if ((block.level.getBlockEntity(block.position).also { container = it }) is InventoryHolder) {
                if (container is BlockEntityShulkerBox) {
                    level.dropItem(block.position.add(0.5, 0.5, 0.5), block.toItem())
                    (container as InventoryHolder).inventory.clearAll()
                } else {
                    for (drop in (container as InventoryHolder).inventory.contents.values) {
                        level.dropItem(block.position.add(0.5, 0.5, 0.5), drop)
                    }
                    (container as InventoryHolder).inventory.clearAll()
                }
            } else if (random.nextDouble() * 100 < yield) {
                for (drop in block.getDrops(air)) {
                    level.dropItem(block.position.add(0.5, 0.5, 0.5), drop)
                }
            }

            if (random.nextInt(8) == 0) {
                smokePositions.add(block.position)
            }

            level.setBlock(
                Vector3(
                    block.position.x.toInt().toDouble(),
                    block.position.y.toInt().toDouble(),
                    block.position.z.toInt().toDouble()
                ),
                block.layer, Block.get(BlockID.AIR)
            )

            if (block.layer != 0) {
                continue
            }

            val pos = Vector3(block.position.x, block.position.y, block.position.z)

            for (side in BlockFace.entries) {
                val sideBlock = pos.getSide(side)
                val index = Hash.hashBlock(sideBlock.x.toInt(), sideBlock.y.toInt(), sideBlock.z.toInt())
                if (!affectedBlocks.contains(level.getBlock(sideBlock)) && !updateBlocks.contains(index)) {
                    var ev: BlockUpdateEvent = BlockUpdateEvent(level.getBlock(sideBlock))
                    Server.instance.pluginManager.callEvent(ev)
                    if (!ev.cancelled) {
                        ev.block.onUpdate(Level.BLOCK_UPDATE_NORMAL)
                    }
                    val layer1 = level.getBlock(sideBlock, 1)
                    if (!layer1.isAir) {
                        ev = BlockUpdateEvent(layer1)
                        Server.instance.pluginManager.callEvent(ev)
                        if (!ev.cancelled) {
                            ev.block.onUpdate(Level.Companion.BLOCK_UPDATE_NORMAL)
                        }
                    }
                    updateBlocks.add(index)
                }
            }
            send.add(Vector3(block.position.x - source.x, block.position.y - source.y, block.position.z - source.z))
        }

        for (remainingPos in fireIgnitions) {
            val toIgnite = level.getBlock(remainingPos.position)
            if (toIgnite.isAir && toIgnite.down().isSolid(BlockFace.UP)) {
                level.setBlock(toIgnite.position, Block.get(BlockID.FIRE))
            }
        }

        val count: Int = smokePositions.size
        val data = CompoundTag()
            .putFloat("originX", this.source.position.x.toFloat())
            .putFloat("originY", this.source.position.y.toFloat())
            .putFloat("originZ", this.source.position.z.toFloat())
            .putFloat("radius", size.toFloat())
            .putInt("size", count)
        for (i in 0..<count) {
            val pos = smokePositions[i]
            val prefix = "pos$i"
            data.putFloat(prefix + "x", pos.x.toFloat())
            data.putFloat(prefix + "y", pos.y.toFloat())
            data.putFloat(prefix + "z", pos.z.toFloat())
        }
        level.addSound(this.source.position, Sound.RANDOM_EXPLODE)
        level.addLevelEvent(
            this.source.position, LevelEventPacket.EVENT_PARTICLE_EXPLOSION, size.toFloat().roundToInt()
        )
        level.addLevelEvent(this.source.position, LevelEventPacket.EVENT_PARTICLE_BLOCK_EXPLOSION, data)

        return true
    }
}
