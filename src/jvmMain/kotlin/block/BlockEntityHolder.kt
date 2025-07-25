package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.blockentity.BlockEntity
import org.chorus_oss.chorus.blockentity.BlockEntity.Companion.createBlockEntity
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.BlockVector3
import org.chorus_oss.chorus.math.IVector3
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.utils.LevelException

interface BlockEntityHolder<E : BlockEntity> {
    val blockEntity: E?
        get() {
            val level = level
            val blockEntity = when (this) {
                is Vector3 -> level.getBlockEntity(this as Vector3)
                is IVector3 -> level.getBlockEntity((this as IVector3).vector3)
                else -> level.getBlockEntity(BlockVector3(floorX, floorY, floorZ))
            }

            val blockEntityClass = getBlockEntityClass()
            if (blockEntityClass.isInstance(blockEntity)) {
                return blockEntityClass.cast(blockEntity)
            }
            return null
        }

    fun createBlockEntity(): E {
        return createBlockEntity(null)
    }

    fun createBlockEntity(initialData: CompoundTag?, vararg args: Any?): E {
        var initialData1 = initialData
        val typeName = getBlockEntityType()
        val chunk = chunk ?: throw LevelException("Undefined Level or chunk reference")
        initialData1 = initialData1?.copy() ?: CompoundTag()
        val created = createBlockEntity(
            typeName, chunk,
            initialData1
                .putString("id", typeName)
                .putInt("x", floorX)
                .putInt("y", floorY)
                .putInt("z", floorZ),
            *args
        )

        val entityClass = getBlockEntityClass()

        if (!entityClass.isInstance(created)) {
            val error =
                "Failed to create the block entity " + typeName + " of class " + entityClass + " at " + locator + ", " +
                        "the created type is not an instance of the requested class. Created: " + created
            created?.close()
            throw IllegalStateException(error)
        }
        return entityClass.cast(created)
    }

    fun getOrCreateBlockEntity(): E {
        val blockEntity = blockEntity
        if (blockEntity != null) {
            return blockEntity
        }
        return createBlockEntity()
    }

    fun getBlockEntityClass(): Class<out E>

    fun getBlockEntityType(): String

    val chunk: IChunk?

    val floorX: Int

    val floorY: Int

    val floorZ: Int

    val locator: Locator

    val level: Level

    val block: Block?
        get() {
            return when (this) {
                is Block -> this
                is Locator -> locator.levelBlock
                is Vector3 -> level.getBlock(vector3)
                else -> level.getBlock(floorX, floorY, floorZ)
            }
        }

    companion object {
        fun <E : BlockEntity, H : BlockEntityHolder<E>> setBlockAndCreateEntity(holder: H): E? {
            return setBlockAndCreateEntity(holder, direct = true, update = true)
        }

        fun <E : BlockEntity, H : BlockEntityHolder<E>> setBlockAndCreateEntity(
            holder: H, direct: Boolean, update: Boolean
        ): E? {
            return setBlockAndCreateEntity(holder, direct, update, null)
        }

        fun <E : BlockEntity, H : BlockEntityHolder<E>> setBlockAndCreateEntity(
            holder: H, direct: Boolean, update: Boolean, initialData: CompoundTag?,
            vararg args: Any?
        ): E? {
            val block = holder.block
            val level = block!!.level
            val layer0 = level.getBlock(block.position, 0)
            val layer1 = level.getBlock(block.position, 1)
            if (level.setBlock(block.position, block, direct, update)) {
                try {
                    return holder.createBlockEntity(initialData, *args)
                } catch (e: Exception) {
                    Loggers.logBlocKEntityHolder.warn(
                        "Failed to create block entity {} at {} at ",
                        holder.getBlockEntityType(),
                        holder.locator, e
                    )
                    level.setBlock(layer0.position, 0, layer0, direct, update)
                    level.setBlock(layer1.position, 1, layer1, direct, update)
                    throw e
                }
            }

            return null
        }
    }
}
