package org.chorus_oss.chorus.entity

import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.nbt.tag.CompoundTag


abstract class EntityHanging(chunk: IChunk?, nbt: CompoundTag?) : Entity(chunk, nbt) {
    protected var direction: Int = 0

    override fun initEntity() {
        super.initEntity()

        this.setMaxHealth(1)
        this.setHealthSafe(1f)

        if (namedTag!!.contains("Direction")) {
            this.direction = namedTag!!.getByte("Direction").toInt()
        } else if (namedTag!!.contains("Dir")) {
            val d: Int = namedTag!!.getByte("Dir").toInt()
            if (d == 2) {
                this.direction = 0
            } else if (d == 0) {
                this.direction = 2
            }
        }
    }

    override fun saveNBT() {
        super.saveNBT()

        namedTag!!.putByte("Direction", getDirection().horizontalIndex)
        namedTag!!.putInt("TileX", position.x.toInt())
        namedTag!!.putInt("TileY", position.y.toInt())
        namedTag!!.putInt("TileZ", position.z.toInt())
    }

    override fun getDirection(): BlockFace {
        return BlockFace.fromIndex(this.direction)
    }

    override fun onUpdate(currentTick: Int): Boolean {
        if (this.closed) {
            return false
        }

        if (!this.isPlayer) {
            this.blocksAround = null
            this.collisionBlocks = null
        }

        if (!this.isAlive()) {
            this.despawnFromAll()
            if (!this.isPlayer) {
                this.close()
            }
            return true
        }

        this.checkBlockCollision()

        if (prevRotation.yaw != rotation.yaw || prevPosition.x != position.x || prevPosition.y != position.y || prevPosition.z != position.z) {
            this.despawnFromAll()
            this.direction = (rotation.yaw / 90).toInt()
            prevRotation.yaw = rotation.yaw
            prevPosition.x = position.x
            prevPosition.y = position.y
            prevPosition.z = position.z
            this.spawnToAll()
            return true
        }

        return false
    }

    protected fun isSurfaceValid(): Boolean {
        return true
    }
}
