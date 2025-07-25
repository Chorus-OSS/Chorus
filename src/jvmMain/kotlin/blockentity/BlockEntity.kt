package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.registry.Registries
import org.chorus_oss.chorus.scheduler.Task
import org.chorus_oss.chorus.utils.Loggable

abstract class BlockEntity(chunk: IChunk, nbt: CompoundTag) : Locator(chunk.provider.level),
    BlockEntityID {

    open var name: String = ""

    @JvmField
    var id: Long
    var isMovable: Boolean

    @JvmField
    var closed: Boolean = false

    @JvmField
    var namedTag: CompoundTag

    init {
        this.setLevel(chunk.provider.level)
        this.namedTag = nbt
        this.id = count++
        position.x = namedTag.getInt("x").toDouble()
        position.y = namedTag.getInt("y").toDouble()
        position.z = namedTag.getInt("z").toDouble()
        this.isMovable = namedTag.getBoolean("isMovable")


        this.initBlockEntity()

        check(!closed) { "Could not create the entity " + javaClass.name + ", the initializer closed it on construction." }

        this.chunk.addBlockEntity(this)
        level.addBlockEntity(this)
    }

    protected open fun initBlockEntity() {
        loadNBT()
    }

    /**
     * 从方块实体的namedtag中读取数据
     */
    open fun loadNBT() {
    }

    /**
     * 存储方块实体数据到namedtag
     */
    open fun saveNBT() {
        namedTag.putString(
            TAG_ID,
            saveId
        )
        namedTag.putInt(TAG_X, this.x.toInt())
        namedTag.putInt(TAG_Y, this.y.toInt())
        namedTag.putInt(TAG_Z, this.z.toInt())
        namedTag.putBoolean(TAG_IS_MOVABLE, this.isMovable)
    }

    val saveId: String
        get() = Registries.BLOCKENTITY.getSaveId(javaClass)!!

    open val cleanedNBT: CompoundTag?
        get() {
            this.saveNBT()
            val tag = namedTag.copy()
            tag.remove("x").remove("y").remove("z").remove("id")
            return if (tag.tags.isNotEmpty()) {
                tag
            } else {
                null
            }
        }

    val block: Block
        get() = this.levelBlock

    abstract val isBlockEntityValid: Boolean

    open fun onUpdate(): Boolean {
        return false
    }

    fun scheduleUpdate() {
        level.scheduleBlockEntityUpdate(this)
    }

    open fun close() {
        if (!this.closed) {
            this.closed = true
            chunk.removeBlockEntity(this)
            level.removeBlockEntity(this)
        }
    }

    open fun onBreak(isSilkTouch: Boolean) {
    }

    open fun setDirty() {
        chunk.setChanged()

        if (!this.levelBlock.isAir) {
            level.scheduler.scheduleTask(object : Task() {
                override fun onRun(currentTick: Int) {
                    if (isBlockEntityValid) {
                        level.updateComparatorOutputLevelSelective(
                            this@BlockEntity.position,
                            isObservable
                        )
                    }
                }
            })
        }
    }

    open val isObservable: Boolean
        /**
         * Indicates if an observer blocks that are looking at this block should blink when [.setDirty] is called.
         */
        get() = true

    override val levelBlockEntity: BlockEntity?
        get() = super.levelBlockEntity

    companion object : Loggable {
        const val TAG_CUSTOM_NAME: String = "CustomName"
        const val TAG_ID: String = "id"
        const val TAG_IS_MOVABLE: String = "isMovable"
        const val TAG_X: String = "x"
        const val TAG_Y: String = "y"
        const val TAG_Z: String = "z"

        var count: Long = 1
        fun createBlockEntity(type: String, locator: Locator, vararg args: Any?): BlockEntity {
            return createBlockEntity(type, locator, getDefaultCompound(locator.position, type), *args)!!
        }


        fun createBlockEntity(type: String, pos: Locator, nbt: CompoundTag?, vararg args: Any?): BlockEntity? {
            return createBlockEntity(
                type,
                pos.level.getChunk(pos.position.floorX shr 4, pos.position.floorZ shr 4),
                nbt,
                *args
            )
        }

        @JvmStatic
        fun createBlockEntity(type: String, chunk: IChunk?, nbt: CompoundTag?, vararg args: Any?): BlockEntity? {
            var blockEntity: BlockEntity? = null

            val clazz = Registries.BLOCKENTITY[type]
            if (clazz != null) {
                var exceptions: MutableList<Exception?>? = null

                for (constructor in clazz.constructors) {
                    if (blockEntity != null) {
                        break
                    }

                    if (constructor.parameterCount != (args.size + 2)) {
                        continue
                    }

                    try {
                        if (args.isEmpty()) {
                            blockEntity = constructor.newInstance(chunk, nbt) as BlockEntity
                        } else {
                            val objects = arrayOfNulls<Any>(args.size + 2)

                            objects[0] = chunk
                            objects[1] = nbt
                            System.arraycopy(args, 0, objects, 2, args.size)
                            blockEntity = constructor.newInstance(*objects) as BlockEntity
                        }
                    } catch (e: Exception) {
                        if (exceptions == null) {
                            exceptions = ArrayList()
                        }
                        exceptions.add(e)
                    }
                }
                if (blockEntity == null) {
                    val cause: Exception = IllegalArgumentException(
                        "Could not create a block entity of type $type",
                        if (exceptions != null && exceptions.size > 0) exceptions[0] else null
                    )
                    if (exceptions != null && exceptions.size > 1) {
                        for (i in 1..<exceptions.size) {
                            cause.addSuppressed(exceptions[i])
                        }
                    }
                    log.error(
                        "Could not create a block entity of type {} with {} args", type,
                        args.size, cause
                    )
                }
            } else {
                log.debug("Block entity type {} is unknown", type)
            }


            return blockEntity
        }

        @JvmStatic
        fun getDefaultCompound(pos: Vector3, id: String): CompoundTag {
            return CompoundTag()
                .putString("id", id)
                .putInt("x", pos.floorX)
                .putInt("y", pos.floorY)
                .putInt("z", pos.floorZ)
        }
    }
}
