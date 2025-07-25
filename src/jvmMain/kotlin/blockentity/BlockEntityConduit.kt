package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.effect.Effect
import org.chorus_oss.chorus.entity.effect.EffectType
import org.chorus_oss.chorus.entity.mob.monster.EntityMonster
import org.chorus_oss.chorus.event.block.ConduitActivateEvent
import org.chorus_oss.chorus.event.block.ConduitDeactivateEvent
import org.chorus_oss.chorus.event.entity.EntityDamageByBlockEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.math.Vector2
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.tags.BiomeTags
import org.chorus_oss.chorus.tags.BlockTags
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs

class BlockEntityConduit(chunk: IChunk, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt) {
    var targetEntity: Entity? = null
    private var target: Long = 0

    // Client validates the structure, so if we set it to an invalid state it would cause a visual desync
    /*public void setActive(boolean active) {
           this.active = active;
       }*/
    var isActive: Boolean = false
        private set
    var validBlocks: Int = 0
        private set


    override fun initBlockEntity() {
        super.initBlockEntity()
        this.scheduleUpdate()
    }

    override fun loadNBT() {
        super.loadNBT()
        validBlocks = -1
        if (!namedTag.contains("Target")) {
            namedTag.putLong("Target", -1)
            target = -1
            targetEntity = null
        } else {
            target = namedTag.getLong("Target")
        }

        isActive = namedTag.getBoolean("Active")
    }

    override fun saveNBT() {
        super.saveNBT()
        val targetEntity = this.targetEntity
        namedTag.putLong("Target", targetEntity?.uniqueId ?: -1)
        namedTag.putBoolean("Active", isActive)
    }

    override var name: String
        get() = "Conduit"
        set(name) {
            super.name = name
        }

    override fun onUpdate(): Boolean {
        if (closed) {
            return false
        }

        val activeBeforeUpdate = isActive
        val targetBeforeUpdate = targetEntity
        if (validBlocks == -1) {
            isActive = scanStructure()
        }

        if (level.currentTick % 20 == 0L) {
            isActive = scanStructure()
        }

        if (target != -1L) {
            targetEntity = level.getEntity(target)
            target = -1
        }

        if (activeBeforeUpdate != isActive || targetBeforeUpdate !== targetEntity) {
            this.spawnToAll()
            if (activeBeforeUpdate && !isActive) {
                level.addSound(position.add(0.0, 0.5, 0.0), Sound.CONDUIT_DEACTIVATE)
                Server.instance.pluginManager.callEvent(ConduitDeactivateEvent(block))
            } else if (!activeBeforeUpdate && isActive) {
                level.addSound(position.add(0.0, 0.5, 0.0), Sound.CONDUIT_ACTIVATE)
                Server.instance.pluginManager.callEvent(ConduitActivateEvent(block))
            }
        }

        if (!isActive) {
            targetEntity = null
            target = -1
        } else if (level.currentTick % 40 == 0L) {
            attackMob()
            addEffectToPlayers()
        }

        return true
    }

    override val isBlockEntityValid: Boolean
        get() = block.id === BlockID.CONDUIT


    fun addEffectToPlayers() {
        val radius = playerRadius
        if (radius <= 0) {
            return
        }
        val radiusSquared = radius * radius

        val conduitPos = Vector2(position.x, position.z)

        level.players.values.stream()
            .filter { target: Player -> this.canAffect(target) }
            .filter { p: Player -> conduitPos.distanceSquared(p.position.x, p.position.z) <= radiusSquared }
            .forEach { p: Player ->
                p.addEffect(
                    Effect.get(EffectType.CONDUIT_POWER)
                        .setDuration(260)
                        .setVisible(true)
                        .setAmplifier(0)
                        .setAmbient(true)
                )
            }
    }

    fun attackMob() {
        val radius = attackRadius
        if (radius <= 0) {
            return
        }

        var updated = false

        var target = this.targetEntity
        if (target != null && !canAttack(target)) {
            target = null
            updated = true
            this.targetEntity = null
            this.target = -1
        }

        if (target == null) {
            val mobs = level.getCollidingEntities(
                SimpleAxisAlignedBB(
                    position.x - radius,
                    position.y - radius,
                    position.z - radius,
                    position.x + 1 + radius, position.y + 1 + radius, position.z + 1 + radius
                )
            ).filter { this.canAttack(it) }.toList().toTypedArray()

            if (mobs.isEmpty()) {
                if (updated) {
                    spawnToAll()
                }
                return
            }

            target = mobs[ThreadLocalRandom.current().nextInt(mobs.size)]
            this.targetEntity = target
            updated = true
        }

        if (!target.attack(EntityDamageByBlockEvent(block, target, EntityDamageEvent.DamageCause.MAGIC, 4f))) {
            this.targetEntity = null
            updated = true
        }

        if (updated) {
            spawnToAll()
        }
    }

    fun canAttack(target: Entity?): Boolean {
        return target is EntityMonster && canAffect(target)
    }

    fun canAffect(target: Entity): Boolean {
        return target.isTouchingWater()
                || target.level!!.isRaining && target.level!!.canBlockSeeSky(target.position)
                && !BiomeTags.containTag(
            target.level!!.getBiomeId(
                target.position.floorX,
                target.position.floorY,
                target.position.floorZ
            ), BiomeTags.FROZEN
        )
    }

    private fun scanWater(): Boolean {
        val x = floorX
        val y = floorY
        val z = floorZ
        for (ix in -1..1) {
            for (iz in -1..1) {
                for (iy in -1..1) {
                    var block = level.getBlock(x + ix, y + iy, z + iz, 0)
                    if (!block.`is`(BlockTags.WATER)) {
                        block = level.getBlock(x + ix, y + iy, z + iz, 1)
                        if (!block.`is`(BlockTags.WATER)) {
                            return false
                        }
                    }
                }
            }
        }

        return true
    }

    private fun scanFrame(): Int {
        var validBlocks = 0
        val x = floorX
        val y = floorY
        val z = floorZ
        for (iy in -2..2) {
            if (iy == 0) {
                for (ix in -2..2) {
                    for (iz in -2..2) {
                        if (abs(iz.toDouble()) != 2.0 && abs(ix.toDouble()) != 2.0) {
                            continue
                        }

                        val blockId = level.getBlockIdAt(x + ix, y, z + iz)
                        //validBlocks++;
                        //level.setBlock(x + ix, y, z + iz, new BlockPlanks(), true, true);
                        if (VALID_STRUCTURE_BLOCKS.contains(blockId)) {
                            validBlocks++
                        }
                    }
                }
            } else {
                val absIY = abs(iy.toDouble()).toInt()
                for (ix in -2..2) {
                    if (absIY != 2 && ix == 0) {
                        continue
                    }

                    if (absIY == 2 || abs(ix.toDouble()) == 2.0) {
                        val blockId = level.getBlockIdAt(x + ix, y + iy, z)
                        //validBlocks++;
                        //level.setBlock(x + ix, y + iy, z, new BlockWood(), true, true);
                        if (VALID_STRUCTURE_BLOCKS.contains(blockId)) {
                            validBlocks++
                        }
                    }
                }

                for (iz in -2..2) {
                    if (absIY != 2 && iz == 0) {
                        continue
                    }

                    if (absIY == 2 && iz != 0 || abs(iz.toDouble()) == 2.0) {
                        val blockId = level.getBlockIdAt(x, y + iy, z + iz)
                        //validBlocks++;
                        //level.setBlock(x, y + iy, z + iz, new BlockWood(), true, true);
                        if (VALID_STRUCTURE_BLOCKS.contains(blockId)) {
                            validBlocks++
                        }
                    }
                }
            }
        }

        return validBlocks
    }

    fun scanEdgeBlock(): List<Block> {
        val validBlocks: MutableList<Block> = ArrayList()
        val x = floorX
        val y = floorY
        val z = floorZ
        for (iy in -2..2) {
            if (iy != 0) {
                for (ix in -2..2) {
                    for (iz in -2..2) {
                        if (abs(iy.toDouble()) != 2.0 && abs(iz.toDouble()) < 2 && abs(ix.toDouble()) < 2) {
                            continue
                        }

                        if (ix == 0 || iz == 0) {
                            continue
                        }

                        val block = level.getBlock(x + ix, y + iy, z + iz)
                        //validBlocks++;
                        //level.setBlock(x + ix, y + iy, z + iz, new BlockDiamond(), true, true);
                        if (VALID_STRUCTURE_BLOCKS.contains(block.id)) {
                            validBlocks.add(block)
                        }
                    }
                }
            }
        }

        return validBlocks
    }

    fun scanStructure(): Boolean {
        if (!scanWater()) {
            this.validBlocks = 0
            return false
        }

        val validBlocks = scanFrame()
        if (validBlocks < 16) {
            this.validBlocks = 0
            return false
        }

        this.validBlocks = validBlocks

        return true
    }

    val playerRadius: Int
        get() {
            val radius = validBlocks / 7
            return radius * 16
        }

    val attackRadius: Int
        get() {
            return if (validBlocks >= 42) {
                8
            } else {
                0
            }
        }

    override val spawnCompound: CompoundTag
        get() {
            val tag = super.spawnCompound
                .putBoolean("isMovable", this.isMovable)
                .putBoolean("Active", this.isActive)
            val targetEntity = this.targetEntity
            tag.putLong("Target", targetEntity?.uniqueId ?: -1)
            return tag
        }

    companion object {
        private val VALID_STRUCTURE_BLOCKS = HashSet(listOf(BlockID.PRISMARINE, BlockID.SEA_LANTERN))
    }
}
