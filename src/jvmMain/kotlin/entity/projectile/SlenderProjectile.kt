package org.chorus_oss.chorus.entity.projectile

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.entity.ProjectileHitEvent
import org.chorus_oss.chorus.level.MovingObjectPosition
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BVector3
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import java.util.*
import java.util.function.ToDoubleFunction
import kotlin.math.abs

/**
 * 这个抽象类代表较为细长的投射物实体(例如弓箭,三叉戟),它通过重写[Entity.move]方法实现这些实体较为准确的碰撞箱计算。
 *
 *
 * This abstract class represents slender projectile entities (e.g.arrow, trident), and it realized a more accurate collision box calculation for these entities by overriding the [Entity.move] method.
 */
abstract class SlenderProjectile : EntityProjectile {
    private var lastHitBlock: MovingObjectPosition? = null

    constructor(chunk: IChunk?, nbt: CompoundTag?) : super(chunk, nbt)

    constructor(chunk: IChunk?, nbt: CompoundTag?, shootingEntity: Entity?) : super(chunk, nbt, shootingEntity)

    //对于SlenderProjectile你不应该把Width设置太大,如果没必要请使用默认值.
    override fun getWidth(): Float {
        return 0.1f
    }

    //对于SlenderProjectile你不应该把Height设置太大,如果没必要请使用默认值.
    override fun getHeight(): Float {
        return 0.1f
    }

    /*
     * 经过测试这个算法在大多数情况下效果不错。
     */
    override fun move(dx: Double, dy: Double, dz: Double): Boolean {
        var dx: Double = dx
        var dy: Double = dy
        var dz: Double = dz
        if (dx == 0.0 && dz == 0.0 && dy == 0.0) {
            return true
        }

        this.ySize *= 0.4.toFloat()

        val movX: Double = dx
        val movY: Double = dy
        val movZ: Double = dz

        val projectile: SlenderProjectile = this
        shootingEntity!!
        ticksLived

        val currentAABB: AxisAlignedBB = boundingBox.clone()
        val dirVector: Vector3 = Vector3(dx, dy, dz).multiply(1 / SPLIT_NUMBER.toDouble())

        var collisionEntity: Entity? = null
        var collisionBlock: Block? = null
        for (i in 0..<SPLIT_NUMBER) {
            val collisionBlocks: Array<Block> =
                level!!.getCollisionBlocks(currentAABB.offset(dirVector.x, dirVector.y, dirVector.z))
            val collisionEntities = level!!.getCollidingEntities(currentAABB, this)
            if (collisionBlocks.size != 0) {
                currentAABB.offset(-dirVector.x, -dirVector.y, -dirVector.z)
                collisionBlock = Arrays.stream(collisionBlocks).min(
                    Comparator.comparingDouble(
                        ToDoubleFunction { block: Block -> projectile.position.distanceSquared(block.position) })
                ).get()
                break
            }
            collisionEntity = collisionEntities.stream()
                .filter { entity: Entity? -> this.collideEntityFilter(entity!!) }
                .min(Comparator.comparingDouble(ToDoubleFunction { o: Entity? -> o!!.position.distanceSquared(projectile.position) }))
                .orElse(null)
            if (collisionEntity != null) {
                break
            }
        }
        val centerPoint1: Vector3 = Vector3(
            (currentAABB.minX + currentAABB.maxX) / 2,
            (currentAABB.minY + currentAABB.maxY) / 2,
            (currentAABB.minZ + currentAABB.maxZ) / 2
        )
        //collide with entity
        if (collisionEntity != null) {
            onCollideWithEntity(collisionEntity)
            return true
        }

        val centerPoint2: Vector3 = Vector3(
            (boundingBox.minX + boundingBox.maxX) / 2,
            (boundingBox.minY + boundingBox.maxY) / 2,
            (boundingBox.minZ + boundingBox.maxZ) / 2
        )
        val diff: Vector3 = centerPoint1.subtract(centerPoint2)
        if (dy > 0) {
            if (diff.y + 0.001 < dy) {
                dy = diff.y
            }
        }
        if (dy < 0) {
            if (diff.y - 0.001 > dy) {
                dy = diff.y
            }
        }
        if (dx > 0) {
            if (diff.x + 0.001 < dx) {
                dx = diff.x
            }
        }
        if (dx < 0) {
            if (diff.x - 0.001 > dx) {
                dx = diff.x
            }
        }
        if (dz > 0) {
            if (diff.z + 0.001 < dz) {
                dz = diff.z
            }
        }
        if (dz < 0) {
            if (diff.z - 0.001 > dz) {
                dz = diff.z
            }
        }
        boundingBox.offset(0.0, dy, 0.0)
        boundingBox.offset(dx, 0.0, 0.0)
        boundingBox.offset(0.0, 0.0, dz)
        position.x = (boundingBox.minX + boundingBox.maxX) / 2
        position.y = boundingBox.minY - this.ySize
        position.z = (boundingBox.minZ + boundingBox.maxZ) / 2

        this.checkChunks()

        this.checkGroundState(movX, movY, movZ, dx, dy, dz)
        this.updateFallState(this.onGround)

        if (movX != dx) {
            motion.x = 0.0
        }
        if (movY != dy) {
            motion.y = 0.0
        }
        if (movZ != dz) {
            motion.z = 0.0
        }

        //collide with block
        if (this.isCollided && !this.hadCollision) {
            this.hadCollision = true
            motion.x = 0.0
            motion.y = 0.0
            motion.z = 0.0
            val bVector3: BVector3 = BVector3.fromPos(Vector3(dx, dy, dz))
            var blockFace = BlockFace.fromHorizontalAngle(bVector3.yaw)
            var block: Block = level!!.getBlock(
                position.floorX,
                position.floorY, position.floorZ
            ).getSide(blockFace)
            if (block.isAir) {
                blockFace = BlockFace.DOWN
                block = level!!.getBlock(
                    position.floorX,
                    position.floorY, position.floorZ
                ).down()
            }
            if (block.isAir) {
                blockFace = BlockFace.UP
                block = level!!.getBlock(
                    position.floorX,
                    position.floorY, position.floorZ
                ).up()
            }
            if (block.isAir && collisionBlock != null) {
                block = collisionBlock
            }
            Server.instance.pluginManager.callEvent(
                ProjectileHitEvent(
                    this, MovingObjectPosition.fromBlock(
                        block.position.floorX, block.position.floorY, block.position.floorZ, blockFace,
                        this.position
                    ).also { lastHitBlock = it })
            )
            onCollideWithBlock(locator, getMotion())
            addHitEffect()
        }
        return true
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

        if (this.isCollided && this.hadCollision) {
            if (lastHitBlock != null && lastHitBlock!!.typeOfHit == 0 && level!!.getBlock(
                    lastHitBlock!!.blockX,
                    lastHitBlock!!.blockY,
                    lastHitBlock!!.blockZ
                ).isAir
            ) {
                motion.y -= getGravity().toDouble()
                updateRotation()
                this.move(motion.x, motion.y, motion.z)
                this.updateMovement()
            }
            return this.entityBaseTick(tickDiff)
        }

        var hasUpdate: Boolean = this.entityBaseTick(tickDiff)

        if (this.isAlive()) {
            if (!this.isCollided) {
                updateMotion()
            }
            if (!this.hadCollision || abs(motion.x) > 0.00001 || abs(
                    motion.y
                ) > 0.00001 || abs(motion.z) > 0.00001
            ) {
                updateRotation()
                hasUpdate = true
            }
            this.move(motion.x, motion.y, motion.z)
            this.updateMovement()
        }
        return hasUpdate
    }

    companion object {
        private const val SPLIT_NUMBER: Int = 10
    }
}
