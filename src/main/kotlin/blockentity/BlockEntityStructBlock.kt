package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.inventory.Inventory
import org.chorus_oss.chorus.inventory.StructBlockInventory
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockVector3
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.protocol.packets.StructureBlockUpdatePacket
import org.chorus_oss.protocol.types.structure.*

class BlockEntityStructBlock(level: Level, nbt: CompoundTag) : BlockEntitySpawnable(level, nbt), IStructBlock,
    BlockEntityInventoryHolder {
    private var animationMode: StructureAnimationMode? = null
    private var animationSeconds = 0f
    private var data: StructureBlockType? = null
    private var dataField: String? = null
    private var ignoreEntities = false
    private var includePlayers = false
    private var integrity = 0f
    private var isPowered = false
    private var mirror: StructureMirror? = null
    private var redstoneSaveMode: StructureRedstoneSaveMode? = null
    private var removeBlocks = false
    private var rotation: StructureRotation? = null
    private var seed: Long = 0
    private var showBoundingBox = false
    private var structureName: String? = null
    private var size: BlockVector3? = null
    private var offset: BlockVector3? = null
    private val structBlockInventory = StructBlockInventory(this)

    override fun loadNBT() {
        super.loadNBT()
        if (namedTag.contains(IStructBlock.Companion.TAG_ANIMATION_MODE)) {
            this.animationMode =
                StructureAnimationMode.entries[namedTag.getByte(IStructBlock.Companion.TAG_ANIMATION_MODE).toInt()]
        } else {
            this.animationMode = StructureAnimationMode.entries[0]
        }
        if (namedTag.contains(IStructBlock.Companion.TAG_ANIMATION_SECONDS)) {
            this.animationSeconds = namedTag.getFloat(IStructBlock.Companion.TAG_ANIMATION_SECONDS)
        } else {
            this.animationSeconds = 0f
        }
        if (namedTag.contains(IStructBlock.Companion.TAG_DATA)) {
            this.data = StructureBlockType.entries[namedTag.getByte(IStructBlock.Companion.TAG_DATA).toInt()]
        } else {
            this.data = StructureBlockType.entries[1]
        }
        if (namedTag.contains(IStructBlock.Companion.TAG_DATA_FIELD)) {
            this.dataField = namedTag.getString(IStructBlock.Companion.TAG_DATA_FIELD)
        } else {
            this.dataField = ""
        }
        if (namedTag.contains(IStructBlock.Companion.TAG_IGNORE_ENTITIES)) {
            this.ignoreEntities = namedTag.getBoolean(IStructBlock.Companion.TAG_IGNORE_ENTITIES)
        } else {
            this.ignoreEntities = false
        }
        if (namedTag.contains(IStructBlock.Companion.TAG_INCLUDE_PLAYERS)) {
            this.includePlayers = namedTag.getBoolean(IStructBlock.Companion.TAG_INCLUDE_PLAYERS)
        } else {
            this.includePlayers = false
        }
        if (namedTag.contains(IStructBlock.Companion.TAG_INTEGRITY)) {
            this.integrity = namedTag.getFloat(IStructBlock.Companion.TAG_INTEGRITY)
        } else {
            this.integrity = 100f
        }
        if (namedTag.contains(IStructBlock.Companion.TAG_IS_POWERED)) {
            this.isPowered = namedTag.getBoolean(IStructBlock.Companion.TAG_IS_POWERED)
        } else {
            this.isPowered = false
        }
        if (namedTag.contains(IStructBlock.Companion.TAG_MIRROR)) {
            this.mirror = StructureMirror.entries[namedTag.getByte(IStructBlock.Companion.TAG_MIRROR).toInt()]
        } else {
            this.mirror = StructureMirror.entries[0]
        }
        if (namedTag.contains(IStructBlock.Companion.TAG_REDSTONE_SAVEMODE)) {
            this.redstoneSaveMode =
                StructureRedstoneSaveMode.entries[namedTag.getByte(IStructBlock.Companion.TAG_REDSTONE_SAVEMODE)
                    .toInt()]
        } else {
            this.redstoneSaveMode = StructureRedstoneSaveMode.entries[0]
        }
        if (namedTag.contains(IStructBlock.Companion.TAG_REMOVE_BLOCKS)) {
            this.removeBlocks = namedTag.getBoolean(IStructBlock.Companion.TAG_REMOVE_BLOCKS)
        } else {
            this.removeBlocks = false
        }
        if (namedTag.contains(IStructBlock.Companion.TAG_ROTATION)) {
            this.rotation = StructureRotation.entries[namedTag.getByte(IStructBlock.Companion.TAG_ROTATION).toInt()]
        } else {
            this.rotation = StructureRotation.entries[0]
        }
        if (namedTag.contains(IStructBlock.Companion.TAG_SEED)) {
            this.seed = namedTag.getLong(IStructBlock.Companion.TAG_SEED)
        } else {
            this.seed = 0L
        }
        if (namedTag.contains(IStructBlock.Companion.TAG_SHOW_BOUNDING_BOX)) {
            this.showBoundingBox = namedTag.getBoolean(IStructBlock.Companion.TAG_SHOW_BOUNDING_BOX)
        } else {
            this.showBoundingBox = true
        }
        if (namedTag.contains(IStructBlock.Companion.TAG_STRUCTURE_NAME)) {
            this.structureName = namedTag.getString(IStructBlock.Companion.TAG_STRUCTURE_NAME)
        } else {
            this.structureName = ""
        }
        if (namedTag.contains(IStructBlock.Companion.TAG_X_STRUCTURE_OFFSET) && namedTag.contains(IStructBlock.Companion.TAG_Y_STRUCTURE_OFFSET) && namedTag.contains(
                IStructBlock.Companion.TAG_Z_STRUCTURE_OFFSET
            )
        ) {
            this.offset = BlockVector3(
                namedTag.getInt(IStructBlock.Companion.TAG_X_STRUCTURE_OFFSET),
                namedTag.getInt(IStructBlock.Companion.TAG_Y_STRUCTURE_OFFSET),
                namedTag.getInt(IStructBlock.Companion.TAG_Z_STRUCTURE_OFFSET)
            )
        } else {
            this.offset = BlockVector3(0, -1, 0)
        }
        if (namedTag.contains(IStructBlock.Companion.TAG_X_STRUCTURE_SIZE) && namedTag.contains(IStructBlock.Companion.TAG_Y_STRUCTURE_SIZE) && namedTag.contains(
                IStructBlock.Companion.TAG_Z_STRUCTURE_SIZE
            )
        ) {
            this.size = BlockVector3(
                namedTag.getInt(IStructBlock.Companion.TAG_X_STRUCTURE_SIZE),
                namedTag.getInt(IStructBlock.Companion.TAG_Y_STRUCTURE_SIZE),
                namedTag.getInt(IStructBlock.Companion.TAG_Z_STRUCTURE_SIZE)
            )
        } else {
            this.size = BlockVector3(5, 5, 5)
        }
    }

    override val spawnCompound: CompoundTag
        get() = super.spawnCompound
            .putByte(IStructBlock.Companion.TAG_ANIMATION_MODE, animationMode!!.ordinal)
            .putFloat(IStructBlock.Companion.TAG_ANIMATION_SECONDS, this.animationSeconds)
            .putInt(IStructBlock.Companion.TAG_DATA, data!!.ordinal)
            .putString(IStructBlock.Companion.TAG_DATA_FIELD, this.dataField!!)
            .putBoolean(IStructBlock.Companion.TAG_IGNORE_ENTITIES, ignoreEntities)
            .putBoolean(IStructBlock.Companion.TAG_INCLUDE_PLAYERS, includePlayers)
            .putFloat(IStructBlock.Companion.TAG_INTEGRITY, integrity)
            .putBoolean(IStructBlock.Companion.TAG_IS_POWERED, isPowered)
            .putByte(IStructBlock.Companion.TAG_MIRROR, mirror!!.ordinal)
            .putByte(IStructBlock.Companion.TAG_REDSTONE_SAVEMODE, redstoneSaveMode!!.ordinal)
            .putBoolean(IStructBlock.Companion.TAG_REMOVE_BLOCKS, removeBlocks)
            .putByte(IStructBlock.Companion.TAG_ROTATION, rotation!!.ordinal)
            .putLong(IStructBlock.Companion.TAG_SEED, seed)
            .putBoolean(IStructBlock.Companion.TAG_SHOW_BOUNDING_BOX, showBoundingBox)
            .putString(IStructBlock.Companion.TAG_STRUCTURE_NAME, structureName!!)
            .putInt(IStructBlock.Companion.TAG_X_STRUCTURE_OFFSET, offset!!.x)
            .putInt(IStructBlock.Companion.TAG_Y_STRUCTURE_OFFSET, offset!!.y)
            .putInt(IStructBlock.Companion.TAG_Z_STRUCTURE_OFFSET, offset!!.z)
            .putInt(IStructBlock.Companion.TAG_X_STRUCTURE_SIZE, size!!.x)
            .putInt(IStructBlock.Companion.TAG_Y_STRUCTURE_SIZE, size!!.y)
            .putInt(IStructBlock.Companion.TAG_Z_STRUCTURE_SIZE, size!!.z)

    override fun saveNBT() {
        super.saveNBT()
        namedTag.putByte(
            IStructBlock.Companion.TAG_ANIMATION_MODE,
            animationMode!!.ordinal
        )
            .putFloat(IStructBlock.Companion.TAG_ANIMATION_SECONDS, this.animationSeconds)
            .putInt(IStructBlock.Companion.TAG_DATA, data!!.ordinal)
            .putString(IStructBlock.Companion.TAG_DATA_FIELD, dataField!!)
            .putBoolean(IStructBlock.Companion.TAG_IGNORE_ENTITIES, ignoreEntities)
            .putBoolean(IStructBlock.Companion.TAG_INCLUDE_PLAYERS, includePlayers)
            .putFloat(IStructBlock.Companion.TAG_INTEGRITY, integrity)
            .putBoolean(IStructBlock.Companion.TAG_IS_POWERED, isPowered)
            .putByte(IStructBlock.Companion.TAG_MIRROR, mirror!!.ordinal)
            .putByte(IStructBlock.Companion.TAG_REDSTONE_SAVEMODE, redstoneSaveMode!!.ordinal)
            .putBoolean(IStructBlock.Companion.TAG_REMOVE_BLOCKS, removeBlocks)
            .putByte(IStructBlock.Companion.TAG_ROTATION, rotation!!.ordinal)
            .putLong(IStructBlock.Companion.TAG_SEED, seed)
            .putBoolean(IStructBlock.Companion.TAG_SHOW_BOUNDING_BOX, showBoundingBox)
            .putString(IStructBlock.Companion.TAG_STRUCTURE_NAME, structureName!!)
            .putInt(IStructBlock.Companion.TAG_X_STRUCTURE_OFFSET, offset!!.x)
            .putInt(IStructBlock.Companion.TAG_Y_STRUCTURE_OFFSET, offset!!.y)
            .putInt(IStructBlock.Companion.TAG_Z_STRUCTURE_OFFSET, offset!!.z)
            .putInt(IStructBlock.Companion.TAG_X_STRUCTURE_SIZE, size!!.x)
            .putInt(IStructBlock.Companion.TAG_Y_STRUCTURE_SIZE, size!!.y)
            .putInt(IStructBlock.Companion.TAG_Z_STRUCTURE_SIZE, size!!.z)
    }

    override val isBlockEntityValid: Boolean
        get() {
            val blockId = this.levelBlock.id
            return blockId === BlockID.STRUCTURE_BLOCK
        }

    override var name: String
        get() = if (this.hasName()) namedTag.getString(IStructBlock.Companion.TAG_CUSTOM_NAME) else BlockEntityID.STRUCTURE_BLOCK
        set(name) {
            if (name.isEmpty()) {
                namedTag.remove(IStructBlock.Companion.TAG_CUSTOM_NAME)
            } else {
                namedTag.putString(IStructBlock.Companion.TAG_CUSTOM_NAME, name)
            }
        }

    override fun hasName(): Boolean {
        return namedTag.contains(IStructBlock.Companion.TAG_CUSTOM_NAME)
    }

    override val inventory
        get(): Inventory {
            return structBlockInventory
        }

    override fun close() {
        if (!closed) {
            for (player in HashSet(this.inventory.viewers)) {
                player.removeWindow(this.inventory)
            }
            super.close()
        }
    }

    fun updateSetting(packet: StructureBlockUpdatePacket) {
        this.animationMode = packet.settings.animationMode
        this.animationSeconds = packet.settings.animationDuration
        this.data = packet.structureBlockType
        this.dataField = packet.dataField
        this.ignoreEntities = packet.settings.ignoringEntities
        this.includePlayers = packet.includePlayers
        this.integrity = packet.settings.integrity
        this.isPowered = packet.shouldTrigger
        this.mirror = packet.settings.mirror
        this.redstoneSaveMode = packet.redstoneSaveMode
        this.removeBlocks = packet.settings.ignoringBlocks
        this.rotation = packet.settings.rotation
        this.seed = packet.settings.seed.toLong()
        this.showBoundingBox = packet.showBoundingBox
        this.structureName = packet.structureName
        this.offset = Vector3(packet.settings.offset).asBlockVector3()
        this.size = Vector3(packet.settings.size).asBlockVector3()
    }
}
