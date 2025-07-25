package org.chorus_oss.chorus.blockentity


import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.BlockBeehive
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.block.BlockLiquid
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.mob.animal.EntityBee
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.FloatTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.nbt.tag.Tag
import org.chorus_oss.chorus.utils.Identifier
import org.chorus_oss.chorus.utils.Loggable
import java.util.*
import kotlin.math.atan


class BlockEntityBeehive(chunk: IChunk, nbt: CompoundTag) : BlockEntity(chunk, nbt) {
    private var occupants: MutableList<Occupant> = mutableListOf()

    var interactingEntity: Entity? = null

    override fun initBlockEntity() {
        super.initBlockEntity()
        this.occupants = mutableListOf()
        if (!isEmpty) {
            scheduleUpdate()
        }
    }

    override fun loadNBT() {
        super.loadNBT()
        if (!namedTag.contains("ShouldSpawnBees")) {
            namedTag.putByte("ShouldSpawnBees", 0)
        }

        if (!namedTag.contains("Occupants")) {
            namedTag.putList("Occupants", ListTag<Tag<*>>())
        } else {
            val occupantsTag = namedTag.getList("Occupants", CompoundTag::class.java)
            for (i in 0..<occupantsTag.size()) {
                occupants.add(Occupant(occupantsTag[i]))
            }
        }

        // Backward compatibility
        if (namedTag.contains("HoneyLevel")) {
            val block = block
            if (block is BlockBeehive) {
                val honeyLevel = namedTag.getByte("HoneyLevel").toInt()
                block.blockFace = block.blockFace
                block.honeyLevel = honeyLevel
                block.level.setBlock(block.position, block, true, true)
            }
            namedTag.remove("HoneyLevel")
        }
    }

    override fun saveNBT() {
        super.saveNBT()
        val occupantsTag = ListTag<CompoundTag>()
        for (occupant in occupants) {
            occupantsTag.add(occupant.saveNBT())
        }
        namedTag.putList("Occupants", occupantsTag)

        // Backward compatibility
        if (namedTag.contains("HoneyLevel")) {
            val block = block
            if (block is BlockBeehive) {
                val honeyLevel = namedTag.getByte("HoneyLevel").toInt()
                block.blockFace = block.blockFace
                block.honeyLevel = honeyLevel
                block.level.setBlock(block.position, block, true, true)
            }
            namedTag.remove("HoneyLevel")
        }
    }

    var honeyLevel: Int
        get() {
            val block = block
            return if (block is BlockBeehive) {
                block.honeyLevel
            } else {
                0
            }
        }
        set(honeyLevel) {
            val block = block
            if (block is BlockBeehive) {
                block.honeyLevel = honeyLevel
                block.level.setBlock(block.position, block, true, true)
            }
        }

    fun addOccupant(occupant: Occupant): Boolean {
        occupants.add(occupant)
        val occupants = namedTag.getList("Occupants", CompoundTag::class.java)
        occupants.add(occupant.saveNBT())
        namedTag.putList("Occupants", occupants)
        scheduleUpdate()
        return true
    }

    fun addOccupant(entity: Entity): Occupant? {
        if (entity is EntityBee) {
            val bee = entity
            val hasNectar = bee.hasNectar()
            return addOccupant(bee, if (hasNectar) 2400 else 600, hasNectar, true)
        } else {
            return addOccupant(entity, 600, false, true)
        }
    }

    fun addOccupant(entity: Entity, ticksLeftToStay: Int): Occupant? {
        return addOccupant(entity, ticksLeftToStay, false, true)
    }

    fun addOccupant(entity: Entity, ticksLeftToStay: Int, hasNectar: Boolean): Occupant? {
        return addOccupant(entity, ticksLeftToStay, hasNectar, true)
    }

    fun addOccupant(entity: Entity, ticksLeftToStay: Int, hasNectar: Boolean, playSound: Boolean): Occupant? {
        entity.saveNBT()
        val occupant = Occupant(ticksLeftToStay, entity.getEntityIdentifier(), hasNectar, entity.namedTag!!.copy())
        if (!addOccupant(occupant)) {
            return null
        }

        entity.close()
        if (playSound) {
            entity.level!!.addSound(this.position, Sound.BLOCK_BEEHIVE_ENTER)
            if (entity.level != null && (entity.level !== level || entity.position.distanceSquared(this.position) >= 4)) {
                entity.level!!.addSound(entity.position, Sound.BLOCK_BEEHIVE_ENTER)
            }
        }
        return occupant
    }

    fun getOccupants(): Array<Occupant> {
        return occupants.toTypedArray()
    }

    fun removeOccupant(occupant: Occupant): Boolean {
        return occupants.remove(occupant)
    }

    val isHoneyEmpty: Boolean
        get() = honeyLevel == CommonBlockProperties.HONEY_LEVEL.min

    val isHoneyFull: Boolean
        get() = honeyLevel == CommonBlockProperties.HONEY_LEVEL.max

    val isEmpty: Boolean
        get() = occupants.isEmpty()

    val occupantsCount: Int
        get() = occupants.size

    fun isSpawnFaceValid(face: BlockFace): Boolean {
        val side = getSide(face).levelBlock
        return side.canPassThrough() && side !is BlockLiquid
    }

    fun scanValidSpawnFaces(): List<BlockFace> {
        return scanValidSpawnFaces(false)
    }

    fun scanValidSpawnFaces(preferFront: Boolean): List<BlockFace> {
        if (preferFront) {
            val block = block
            if (block is BlockBeehive) {
                val beehiveFace = block.blockFace
                if (isSpawnFaceValid(beehiveFace)) {
                    return listOf(beehiveFace)
                }
            }
        }

        val validFaces: MutableList<BlockFace> = ArrayList(4)
        for (faceIndex in 0..3) {
            val face = BlockFace.fromHorizontalIndex(faceIndex)
            if (isSpawnFaceValid(face)) {
                validFaces.add(face)
            }
        }

        return validFaces
    }

    fun spawnOccupant(occupant: Occupant): Entity? {
        return this.spawnOccupant(occupant, listOf(BlockFace.UP))
    }

    override fun onBreak(isSilkTouch: Boolean) {
        if (!isSilkTouch) {
            val interactingEntity = this.interactingEntity
            for (occupant in getOccupants()) {
                val spawnOccupant = spawnOccupant(occupant)
                if (spawnOccupant is EntityBee && interactingEntity != null) {
                    if (interactingEntity is Player) {
                        if (interactingEntity.isSurvival || interactingEntity.isAdventure) {
                            spawnOccupant.setAngry(interactingEntity)
                        }
                    } else spawnOccupant.setAngry(interactingEntity)
                }
            }
        }
        super.onBreak(isSilkTouch)
    }

    fun spawnOccupant(occupant: Occupant, validFaces: List<BlockFace>?): Entity? {
        if (validFaces != null && validFaces.isEmpty()) {
            return null
        }
        if (!Identifier.isValid(occupant.actorIdentifier)) {
            log.warn("Invalid beehive occupant identifier: {}", occupant.actorIdentifier)
            occupant.actorIdentifier = "minecraft:bee"
        }

        val saveData = occupant.saveData.copy()

        val lookAt: Locator
        val spawnLocator: Locator
        if (validFaces != null) {
            val face = validFaces[RANDOM.nextInt(validFaces.size)]
            spawnLocator = add(
                face.xOffset * 0.25 - face.zOffset * 0.5,
                face.yOffset + (if (face.yOffset < 0) -0.4 else 0.2),
                face.zOffset * 0.25 - face.xOffset * 0.5
            )

            saveData.putList(
                "Pos", ListTag<FloatTag>()
                    .add(FloatTag(spawnLocator.position.x))
                    .add(FloatTag(spawnLocator.position.y))
                    .add(FloatTag(spawnLocator.position.z))
            )

            saveData.putList(
                "Motion", ListTag<FloatTag>()
                    .add(FloatTag(0f))
                    .add(FloatTag(0f))
                    .add(FloatTag(0f))
            )

            lookAt = getSide(face, 2)
        } else {
            spawnLocator = add(RANDOM.nextDouble(), 0.2, RANDOM.nextDouble())
            lookAt = spawnLocator.add(RANDOM.nextDouble(), 0.0, RANDOM.nextDouble())
        }

        val dx = lookAt.x - spawnLocator.x
        val dz = lookAt.z - spawnLocator.z
        var yaw = 0f

        if (dx != 0.0) {
            yaw = if (dx < 0) {
                (1.5 * Math.PI).toFloat()
            } else {
                (0.5 * Math.PI).toFloat()
            }
            yaw = yaw - atan(dz / dx).toFloat()
        } else if (dz < 0) {
            yaw = Math.PI.toFloat()
        }

        yaw = -yaw * 180f / Math.PI.toFloat()

        saveData.putList(
            "Rotation", ListTag<FloatTag>()
                .add(FloatTag(yaw))
                .add(FloatTag(0f))
        )

        val entity = Entity.createEntity(occupant.actorIdentifier, spawnLocator.chunk, saveData)
        if (entity != null) {
            removeOccupant(occupant)
            level.addSound(this.position, Sound.BLOCK_BEEHIVE_EXIT)
        }

        val bee = if (entity is EntityBee) entity else null

        if (occupant.hasNectar && occupant.ticksLeftToStay <= 0) {
            if (!isHoneyFull) {
                honeyLevel = honeyLevel + 1
            }
            bee?.nectarDelivered(this)
        } else {
            bee?.leftBeehive(this)
        }

        entity?.spawnToAll()

        return entity
    }


    fun angerBees(player: Player?) {
        if (!isEmpty) {
            val validFaces = scanValidSpawnFaces().toMutableList()
            if (isSpawnFaceValid(BlockFace.UP)) {
                validFaces.add(BlockFace.UP)
            }
            if (isSpawnFaceValid(BlockFace.DOWN)) {
                validFaces.add(BlockFace.DOWN)
            }
            for (occupant in getOccupants()) {
                val entity = spawnOccupant(occupant, validFaces)
                if (entity is EntityBee) {
                    if (player != null) {
                        entity.setAngry(player)
                    } else {
                        entity.setAngry(true)
                    }
                }
            }
        }
    }

    override fun onUpdate(): Boolean {
        if (this.closed || this.isEmpty) {
            return false
        }

        var validSpawnFaces: List<BlockFace>? = null

        // getOccupants will avoid ConcurrentModificationException if plugins changes the contents while iterating
        for (occupant in getOccupants()) {
            if (--occupant.ticksLeftToStay <= 0 && !(level.isRaining || !level.isDay)) {
                if (validSpawnFaces == null) {
                    validSpawnFaces = scanValidSpawnFaces(true)
                }

                if (spawnOccupant(occupant, validSpawnFaces) == null) {
                    occupant.ticksLeftToStay = 600
                }
            } else if (!occupant.isMuted && RANDOM.nextDouble() < 0.005) {
                level.addSound(position.add(0.5, 0.0, 0.5), occupant.workSound, 1f, occupant.workSoundPitch)
            }
        }

        return true
    }

    override val isBlockEntityValid: Boolean
        get() {
            val id = this.block.id
            return id === BlockID.BEEHIVE || id === BlockID.BEE_NEST
        }

    class Occupant : Cloneable {
        var ticksLeftToStay: Int
        var actorIdentifier: String
        var saveData: CompoundTag
            get() {
                return field.copy()
            }
        var workSound: Sound = Sound.BLOCK_BEEHIVE_WORK
        var workSoundPitch: Float = 1f
        var hasNectar: Boolean
        var isMuted: Boolean = false


        constructor(ticksLeftToStay: Int, actorIdentifier: String, hasNectar: Boolean, saveData: CompoundTag) {
            this.ticksLeftToStay = ticksLeftToStay
            this.actorIdentifier = actorIdentifier
            this.hasNectar = true
            this.saveData = saveData
        }

        constructor(saved: CompoundTag) {
            this.ticksLeftToStay = saved.getInt("TicksLeftToStay")
            this.actorIdentifier = saved.getString("ActorIdentifier")
            this.saveData = saved.getCompound("SaveData").copy()
            if (saved.contains("WorkSound")) {
                try {
                    this.workSound = Sound.valueOf(saved.getString("WorkSound"))
                } catch (ignored: IllegalArgumentException) {
                }
            }
            if (saved.contains("WorkSoundPitch")) {
                this.workSoundPitch = saved.getFloat("WorkSoundPitch")
            }
            this.hasNectar = saved.getBoolean("HasNectar")
            this.isMuted = saved.getBoolean("Muted")
        }

        fun saveNBT(): CompoundTag {
            val compoundTag = CompoundTag()
            compoundTag.putString("ActorIdentifier", actorIdentifier)
                .putInt("TicksLeftToStay", ticksLeftToStay)
                .putCompound("SaveData", saveData)
                .putString("WorkSound", workSound.name)
                .putFloat("WorkSoundPitch", workSoundPitch)
                .putBoolean("HasNectar", hasNectar)
                .putBoolean("Muted", isMuted)
            return compoundTag
        }

        override fun toString(): String {
            return "Occupant{" +
                    "ticksLeftToStay=" + ticksLeftToStay +
                    ", actorIdentifier='" + actorIdentifier + '\'' +
                    '}'
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val occupant = other as Occupant
            return ticksLeftToStay == occupant.ticksLeftToStay &&
                    actorIdentifier == occupant.actorIdentifier &&
                    saveData == occupant.saveData
        }

        override fun hashCode(): Int {
            return Objects.hash(ticksLeftToStay, actorIdentifier, saveData)
        }

        override fun clone(): Occupant {
            try {
                val occupant = super.clone() as Occupant
                occupant.saveData = saveData.copy()
                return occupant
            } catch (e: CloneNotSupportedException) {
                throw InternalError("Unexpected exception", e)
            }
        }

        companion object {
            val EMPTY_ARRAY: Array<Occupant?> = arrayOfNulls(0)
        }
    }

    companion object : Loggable {
        private val RANDOM = Random()
    }
}
