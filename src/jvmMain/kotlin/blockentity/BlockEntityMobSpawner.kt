package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.BlockFlowable
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.mob.animal.EntityAnimal
import org.chorus_oss.chorus.entity.mob.monster.EntityMonster
import org.chorus_oss.chorus.event.entity.CreatureSpawnEvent
import org.chorus_oss.chorus.level.GameRule
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.utils.Utils

class BlockEntityMobSpawner(chunk: IChunk, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt) {
    private var delay: Short = 0
    private var displayEntityHeight = 0f
    private var displayEntityScale = 0f
    private var displayEntityWidth = 0f
    private var entityIdentifier: String? = null
    private var maxNearbyEntities: Short = 0
    private var maxSpawnDelay: Short = 0
    private var minSpawnDelay: Short = 0
    private var requiredPlayerRange: Short = 0
    private var spawnCount: Short = 0
    private var spawnData: CompoundTag? = null
    private var spawnPotentials: ListTag<CompoundTag>? = null
    private var spawnRange: Short = 0

    override fun initBlockEntity() {
        this.delay = if (namedTag.containsShort(TAG_DELAY)) namedTag.getShort(TAG_DELAY) else DELAY
        this.displayEntityHeight = if (namedTag.containsFloat(TAG_DISPLAY_ENTITY_HEIGHT)) namedTag.getFloat(
            TAG_DISPLAY_ENTITY_HEIGHT
        ) else DISPLAY_ENTITY_HEIGHT
        this.displayEntityScale = if (namedTag.containsFloat(TAG_DISPLAY_ENTITY_SCALE)) namedTag.getFloat(
            TAG_DISPLAY_ENTITY_SCALE
        ) else DISPLAY_ENTITY_SCALE
        this.displayEntityWidth = if (namedTag.containsFloat(TAG_DISPLAY_ENTITY_WIDTH)) namedTag.getFloat(
            TAG_DISPLAY_ENTITY_WIDTH
        ) else DISPLAY_ENTITY_WIDTH
        this.entityIdentifier = if (namedTag.containsString(TAG_ENTITY_IDENTIFIER)) namedTag.getString(
            TAG_ENTITY_IDENTIFIER
        ) else ENTITY_IDENTIFIER
        this.maxNearbyEntities = if (namedTag.containsShort(TAG_MAX_NEARBY_ENTITIES)) namedTag.getShort(
            TAG_MAX_NEARBY_ENTITIES
        ) else MAX_NEARBY_ENTITIES
        this.maxSpawnDelay =
            if (namedTag.containsShort(TAG_MAX_SPAWN_DELAY)) namedTag.getShort(TAG_MAX_SPAWN_DELAY) else MAX_SPAWN_DELAY
        this.minSpawnDelay =
            if (namedTag.containsShort(TAG_MIN_SPAWN_DELAY)) namedTag.getShort(TAG_MIN_SPAWN_DELAY) else MIN_SPAWN_DELAY
        this.requiredPlayerRange = if (namedTag.containsShort(TAG_REQUIRED_PLAYER_RANGE)) namedTag.getShort(
            TAG_REQUIRED_PLAYER_RANGE
        ) else REQUIRED_PLAYER_RANGE
        this.spawnCount =
            if (namedTag.containsShort(TAG_SPAWN_COUNT)) namedTag.getShort(TAG_SPAWN_COUNT) else SPAWN_COUNT
        this.spawnData =
            if (namedTag.containsCompound(TAG_SPAWN_DATA)) namedTag.getCompound(TAG_SPAWN_DATA) else SPAWN_DATA
        this.spawnPotentials = if (namedTag.containsList(TAG_SPAWN_POTENTIALS)) namedTag.getList(
            TAG_SPAWN_POTENTIALS,
            CompoundTag::class.java
        ) else SPAWN_POTENTIALS
        this.spawnRange =
            if (namedTag.containsShort(TAG_SPAWN_RANGE)) namedTag.getShort(TAG_SPAWN_RANGE) else SPAWN_RANGE

        this.saveNBT()

        this.scheduleUpdate()
        super.initBlockEntity()
    }

    override fun onUpdate(): Boolean {
        if (!isBlockEntityValid) this.close()
        if (this.closed) {
            return false
        }

        if (!level.gameRules.getBoolean(GameRule.DO_MOB_SPAWNING)) return true

        if (delay++ >= Utils.rand(
                minSpawnDelay.toInt(),
                maxSpawnDelay.toInt()
            )
        ) {
            this.delay = 0
            var nearbyEntities = 0
            var playerInRange = false
            for (entity in level.entities.values) {
                if (!playerInRange && entity is Player && !entity.isSpectator) {
                    if (entity.position.distance(this.position) <= this.requiredPlayerRange) {
                        playerInRange = true
                    }
                } else if (entity is EntityAnimal || entity is EntityMonster) {
                    if (entity.position.distance(this.position) <= this.requiredPlayerRange) {
                        nearbyEntities++
                    }
                }
            }

            for (i in 0..<this.spawnCount) {
                if (playerInRange && nearbyEntities <= this.maxNearbyEntities) {
                    val pos = Locator(
                        position.x + Utils.rand(
                            -this.spawnRange,
                            spawnRange.toInt()
                        ),
                        this.y,
                        position.z + Utils.rand(
                            -this.spawnRange,
                            spawnRange.toInt()
                        ),
                        this.level
                    )
                    val block = level.getBlock(pos.position)
                    //Mobs shouldn't spawn in walls and they shouldn't retry to
                    if ((block.id != BlockID.AIR) && (block !is BlockFlowable) && (block.id != BlockID.FLOWING_WATER) && (block.id != BlockID.WATER) && (block.id != BlockID.LAVA) && (block.id != BlockID.FLOWING_LAVA)
                    ) {
                        continue
                    }
                    if (!block.subtract(0.0, 1.0, 0.0).levelBlock.isSolid) {
                        continue
                    }

                    val ent = Entity.createEntity(entityIdentifier!!, pos)
                    if (ent != null) {
                        val ev = CreatureSpawnEvent(
                            ent.getNetworkID(),
                            pos,
                            CompoundTag(),
                            CreatureSpawnEvent.SpawnReason.SPAWNER
                        )
                        Server.instance.pluginManager.callEvent(ev)

                        if (ev.cancelled) {
                            continue
                        }
                        ent.spawnToAll()
                    }
                }
            }
        }
        return true
    }

    override fun saveNBT() {
        super.saveNBT()

        namedTag.putShort(TAG_DELAY, delay.toInt())
        namedTag.putFloat(TAG_DISPLAY_ENTITY_HEIGHT, this.displayEntityHeight)
        namedTag.putFloat(TAG_DISPLAY_ENTITY_SCALE, this.displayEntityScale)
        namedTag.putFloat(TAG_DISPLAY_ENTITY_WIDTH, this.displayEntityWidth)
        namedTag.putString(
            TAG_ENTITY_IDENTIFIER,
            entityIdentifier!!
        )
        namedTag.putShort(
            TAG_MAX_SPAWN_DELAY,
            maxSpawnDelay.toInt()
        )
        namedTag.putShort(
            TAG_MIN_SPAWN_DELAY,
            minSpawnDelay.toInt()
        )
        namedTag.putShort(
            TAG_REQUIRED_PLAYER_RANGE,
            requiredPlayerRange.toInt()
        )
        namedTag.putShort(TAG_SPAWN_COUNT, spawnCount.toInt())
        if (this.spawnData != null) {
            namedTag.putCompound(TAG_SPAWN_DATA, this.spawnData!!)
        }
        if (this.spawnPotentials != null) {
            namedTag.putList(TAG_SPAWN_POTENTIALS, this.spawnPotentials!!)
        }
        namedTag.putShort(TAG_SPAWN_RANGE, spawnRange.toInt())
    }

    override val isBlockEntityValid: Boolean
        get() = level.getBlockIdAt(
            position.floorX,
            position.floorY,
            position.floorZ
        ) == BlockID.MOB_SPAWNER

    override val spawnCompound: CompoundTag
        get() = super.spawnCompound
            .putString(TAG_ENTITY_IDENTIFIER, this.entityIdentifier!!)

    var spawnEntityType: String?
        get() = this.entityIdentifier
        set(entityIdentifier) {
            this.entityIdentifier = entityIdentifier
            this.spawnToAll()
        }

    fun setMinSpawnDelay(minDelay: Short) {
        if (minDelay > this.maxSpawnDelay) {
            return
        }

        this.minSpawnDelay = minDelay
    }

    fun setMaxSpawnDelay(maxDelay: Short) {
        if (this.minSpawnDelay > maxDelay) {
            return
        }

        this.maxSpawnDelay = maxDelay
    }

    fun setSpawnDelay(minDelay: Short, maxDelay: Short) {
        if (minDelay > maxDelay) {
            return
        }

        this.minSpawnDelay = minDelay
        this.maxSpawnDelay = maxDelay
    }

    fun setRequiredPlayerRange(range: Short) {
        this.requiredPlayerRange = range
    }

    fun setMaxNearbyEntities(count: Short) {
        this.maxNearbyEntities = count
    }

    companion object {
        const val TAG_DELAY: String = "Delay"
        const val TAG_DISPLAY_ENTITY_HEIGHT: String = "DisplayEntityHeight"
        const val TAG_DISPLAY_ENTITY_SCALE: String = "DisplayEntityScale"
        const val TAG_DISPLAY_ENTITY_WIDTH: String = "DisplayEntityWidth"
        const val TAG_ENTITY_IDENTIFIER: String = "EntityIdentifier"
        const val TAG_MAX_NEARBY_ENTITIES: String = "MaxNearbyEntities"
        const val TAG_MAX_SPAWN_DELAY: String = "MaxSpawnDelay"
        const val TAG_MIN_SPAWN_DELAY: String = "MinSpawnDelay"
        const val TAG_REQUIRED_PLAYER_RANGE: String = "RequiredPlayerRange"
        const val TAG_SPAWN_COUNT: String = "SpawnCount"
        const val TAG_SPAWN_DATA: String = "SpawnData"
        const val TAG_SPAWN_POTENTIALS: String = "SpawnPotentials"
        const val TAG_SPAWN_RANGE: String = "SpawnRange"

        private const val DELAY: Short = 0
        private const val DISPLAY_ENTITY_HEIGHT = 1.8f
        private const val DISPLAY_ENTITY_SCALE = 1.0f
        private const val DISPLAY_ENTITY_WIDTH = 0.8f
        private val ENTITY_IDENTIFIER: String? = null
        private const val MAX_NEARBY_ENTITIES: Short = 6
        private const val MAX_SPAWN_DELAY: Short = 800
        private const val MIN_SPAWN_DELAY: Short = 200
        private const val REQUIRED_PLAYER_RANGE: Short = 16
        private const val SPAWN_COUNT: Short = 4
        private val SPAWN_DATA: CompoundTag? = null
        private val SPAWN_POTENTIALS: ListTag<CompoundTag>? = null
        private const val SPAWN_RANGE: Short = 4
    }
}
