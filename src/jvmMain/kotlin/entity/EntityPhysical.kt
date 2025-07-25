package org.chorus_oss.chorus.entity

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockFlowingLava
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.block.BlockLiquid
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.event.player.EntityFreezeEvent
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Stream
import kotlin.math.abs
import kotlin.math.sqrt

abstract class EntityPhysical(chunk: IChunk?, nbt: CompoundTag?) : EntityCreature(chunk, nbt), EntityAsyncPrepare {
    /**
     * Time-wide broadcast delay is used to alleviate the situation of squeezing CPUs by a large number of tasks submitted at the same time.
     */
    val tickSpread: Int = globalCycleTickSpread.getAndIncrement() and 0xf

    protected val previousCollideMotion: Vector3 = Vector3()
    val previousCurrentMotion: Vector3 = Vector3()

    /**
     * timeForPhysicalFreeFallMovement
     */
    private var fallingTick: Int = 0
    var needsReCalcMovement: Boolean = true
    private var needsCollisionDamage: Boolean = false

    override fun asyncPrepare(currentTick: Int) {
        // 计算是否需要重新计算高开销实体运动
        this.needsReCalcMovement =
            level!!.tickRateOptDelay == 1 || ((currentTick + tickSpread) and (level!!.tickRateOptDelay - 1)) == 0
        // 重新计算绝对位置碰撞箱
        this.calculateOffsetBoundingBox()
        if (!this.isImmobile()) {
            // 处理重力
            handleGravity()
            if (needsReCalcMovement) {
                // 处理碰撞箱挤压运动
                handleCollideMovement(currentTick)
            }
            addTmpMoveMotionXZ(previousCollideMotion)
            handleFloatingMovement()
            handleGroundFrictionMovement()
            handlePassableBlockFrictionMovement()
        }
    }

    override fun onUpdate(currentTick: Int): Boolean {
        // 记录最大高度，用于计算坠落伤害
        if (!this.onGround && position.y > highestPosition) {
            this.highestPosition = position.y
        }
        // 添加挤压伤害
        if (needsCollisionDamage) {
            this.attack(EntityDamageEvent(this, DamageCause.COLLIDE, 3f))
        }
        return super.onUpdate(currentTick)
    }

    override fun entityBaseTick(): Boolean {
        return this.entityBaseTick(1)
    }

    override fun entityBaseTick(tickDiff: Int): Boolean {
        val hasUpdate: Boolean = super.entityBaseTick(tickDiff)
        //handle human entity freeze
        val collidedWithPowderSnow: Boolean =
            getTickCachedCollisionBlocks()!!.stream().anyMatch { block: Block -> block.id === BlockID.POWDER_SNOW }
        if (this.getFreezingTicks() < 140 && collidedWithPowderSnow) {
            this.addFreezingTicks(1)
            val event = EntityFreezeEvent(this)
            Server.instance.pluginManager.callEvent(event)
//            if (!event.isCancelled) {
//                //this.setMovementSpeed(); // todo 给物理实体添加freeze减速
//            }
        } else if (this.getFreezingTicks() > 0 && !collidedWithPowderSnow) {
            this.addFreezingTicks(-1)
            //this.setMovementSpeed();
        }
        if (this.getFreezingTicks() == 140 && level!!.tick % 40 == 0) {
            this.attack(EntityDamageEvent(this, DamageCause.FREEZING, getFrostbiteInjury().toFloat()))
        }
        return hasUpdate
    }

    override fun canBeMovedByCurrents(): Boolean {
        return true
    }

    override fun updateMovement() {
        // 检测自由落体时间
        if (isFalling()) {
            fallingTick++
        }
        super.updateMovement()
        this.move(motion.x, motion.y, motion.z)
    }

    fun isFalling(): Boolean {
        return !this.onGround && position.y < this.highestPosition
    }

    fun addTmpMoveMotion(tmpMotion: Vector3) {
        motion.x += tmpMotion.x
        motion.y += tmpMotion.y
        motion.z += tmpMotion.z
    }

    fun addTmpMoveMotionXZ(tmpMotion: Vector3) {
        motion.x += tmpMotion.x
        motion.z += tmpMotion.z
    }

    protected fun handleGravity() {
        //重力一直存在
        motion.y -= getGravity().toDouble()
        if (!this.onGround && this.hasWaterAt(getFootHeight())) {
            //落地水
            resetFallDistance()
        }
    }

    /**
     * 计算地面摩擦力
     */
    protected fun handleGroundFrictionMovement() {
        //未在地面就没有地面阻力
        if (!this.onGround) return
        //小于精度
        if (abs(motion.z) < PRECISION && abs(
                motion.x
            ) < PRECISION
        ) return
        // 减少移动向量（计算摩擦系数，在冰上滑得更远）
        val factor: Double = getGroundFrictionFactor()
        motion.x *= factor
        motion.z *= factor
        if (abs(motion.x) < PRECISION) motion.x = 0.0
        if (abs(motion.z) < PRECISION) motion.z = 0.0
    }

    /**
     * 计算流体阻力（空气/液体）
     */
    protected fun handlePassableBlockFrictionMovement() {
        //小于精度
        if (abs(motion.z) < PRECISION && abs(
                motion.x
            ) < PRECISION && abs(motion.y) < PRECISION
        ) return
        val factor: Double = getPassableBlockFrictionFactor()
        motion.x *= factor
        motion.y *= factor
        motion.z *= factor
        if (abs(motion.x) < PRECISION) motion.x = 0.0
        if (abs(motion.y) < PRECISION) motion.y = 0.0
        if (abs(motion.z) < PRECISION) motion.z = 0.0
    }

    /**
     * 计算当前位置的地面摩擦因子
     *
     * @return 当前位置的地面摩擦因子
     */
    fun getGroundFrictionFactor(): Double {
        if (!this.onGround) return 1.0
        return level!!.getTickCachedBlock(position.add(0.0, -1.0, 0.0).floor()).frictionFactor
    }

    /**
     * 计算当前位置的流体阻力因子（空气/水）
     *
     * @return 当前位置的流体阻力因子
     */
    fun getPassableBlockFrictionFactor(): Double {
        val block: Block = locator.tickCachedLevelBlock
        if (block.collidesWithBB(this.getBoundingBox(), true)) return block.passableBlockFrictionFactor
        return Block.DEFAULT_AIR_FLUID_FRICTION
    }

    /**
     * 默认使用nk内置实现，这只是个后备算法
     */
    protected fun handleLiquidMovement() {
        val tmp: Vector3 = Vector3()
        var blockLiquid: BlockLiquid? = null
        for (each: Block? in level!!.getCollisionBlocks(
            offsetBoundingBox,
            targetFirst = false, ignoreCollidesCheck = true
        )
        { block: Block? -> block is BlockLiquid }) {
            blockLiquid = each as BlockLiquid?
            val flowVector: Vector3 = blockLiquid!!.getSafeFlowVector()
            tmp.x += flowVector.x
            tmp.y += flowVector.y
            tmp.z += flowVector.z
        }
        if (blockLiquid != null) {
            val len: Double = tmp.length()
            val speed: Float = getLiquidMovementSpeed(blockLiquid) * 0.3f
            if (len > 0) {
                motion.x += tmp.x / len * speed
                motion.y += tmp.y / len * speed
                motion.z += tmp.z / len * speed
            }
        }
    }

    fun addPreviousLiquidMovement() {
        addTmpMoveMotion(previousCurrentMotion)
    }

    protected fun handleFloatingMovement() {
        if (this.hasWaterAt(0f)) {
            motion.y += this.getGravity() * getFloatingForceFactor()
        }
    }

    /**
     * 浮力系数<br></br>
     * 示例:
     * <pre>
     * if (hasWaterAt(this.getFloatingHeight())) {//实体指定高度进入水中后实体上浮
     * return 1.3;//因为浮力系数>1,该值越大上浮越快
     * }
     * return 0.7;//实体指定高度没进入水中，实体存在浮力会抵抗部分重力，但是不会上浮。
     * //因为浮力系数<1,该值最好和上值相加等于2，例 1.3+0.7=2
    </pre> *
     *
     * @return the floating force factor
     */
    open fun getFloatingForceFactor(): Double {
        if (hasWaterAt(this.getFloatingHeight())) {
            return 1.3
        }
        return 0.7
    }

    /**
     * 获得浮动到的实体高度 , 0为实体底部 [Entity.getCurrentHeight]为实体顶部<br></br>
     * 例：<br></br>值为0时，实体的脚接触水平面<br></br>值为getCurrentHeight/2时，实体的中间部分接触水平面<br></br>值为getCurrentHeight时，实体的头部接触水平面
     *
     * @return the float
     */
    open fun getFloatingHeight(): Float {
        return this.getEyeHeight()
    }

    protected fun handleCollideMovement(currentTick: Int) {
        val selfAABB: AxisAlignedBB = offsetBoundingBox.getOffsetBoundingBox(
            motion.x,
            motion.y, motion.z
        )
        val collidingEntities = level!!.getCollidingEntities(selfAABB, this).toMutableList()
        collidingEntities.removeIf { entity -> !(entity.canCollide() && (entity is Player || entity is EntityPhysical)) }
        val size: Int = collidingEntities.size
        if (size == 0) {
            previousCollideMotion.setX(0.0)
            previousCollideMotion.setZ(0.0)
            return
        } else {
            if (!onCollide(currentTick, collidingEntities)) {
                return
            }
        }
        val dxPositives = ArrayList<Double>(size)
        val dxNegatives = ArrayList<Double>(size)
        val dzPositives = ArrayList<Double>(size)
        val dzNegatives = ArrayList<Double>(size)

        var stream: Stream<Entity> = collidingEntities.stream()
        if (size > 4) {
            stream = stream.parallel()
        }
        stream.forEach { each ->
            val targetAABB: AxisAlignedBB = when (each) {
                is Player -> each.reCalcOffsetBoundingBox()
                is EntityPhysical -> each.offsetBoundingBox
                else -> return@forEach
            }
            // 计算碰撞箱
            val centerXWidth: Double =
                (targetAABB.maxX + targetAABB.minX - selfAABB.maxX - selfAABB.minX) * 0.5
            val centerZWidth: Double =
                (targetAABB.maxZ + targetAABB.minZ - selfAABB.maxZ - selfAABB.minZ) * 0.5
            if (centerXWidth > 0) {
                dxPositives.add((targetAABB.maxX - targetAABB.minX) + (selfAABB.maxX - selfAABB.minX) * 0.5 - centerXWidth)
            } else {
                dxNegatives.add((targetAABB.maxX - targetAABB.minX) + (selfAABB.maxX - selfAABB.minX) * 0.5 + centerXWidth)
            }
            if (centerZWidth > 0) {
                dzPositives.add((targetAABB.maxZ - targetAABB.minZ) + (selfAABB.maxZ - selfAABB.minZ) * 0.5 - centerZWidth)
            } else {
                dzNegatives.add((targetAABB.maxZ - targetAABB.minZ) + (selfAABB.maxZ - selfAABB.minZ) * 0.5 + centerZWidth)
            }
        }
        val resultX: Double =
            (if (size > 4) dxPositives.parallelStream() else dxPositives.stream()).mapToDouble { it }.max()
                .orElse(0.0) - (if (size > 4) dxNegatives.parallelStream() else dxNegatives.stream()).mapToDouble { it }
                .max()
                .orElse(0.0)
        val resultZ: Double =
            (if (size > 4) dzPositives.parallelStream() else dzPositives.stream()).mapToDouble { it }.max()
                .orElse(0.0) - (if (size > 4) dzNegatives.parallelStream() else dzNegatives.stream()).mapToDouble { it }
                .max()
                .orElse(0.0)
        val len: Double = sqrt(resultX * resultX + resultZ * resultZ)
        previousCollideMotion.setX(-(resultX / len * 0.2 * 0.32))
        previousCollideMotion.setZ(-(resultZ / len * 0.2 * 0.32))
    }

    /**
     * @param collidingEntities 碰撞的实体
     * @return false以拦截实体碰撞运动计算
     */
    protected open fun onCollide(currentTick: Int, collidingEntities: List<Entity>): Boolean {
        if (currentTick % 10 == 0) {
            if (collidingEntities.size > 24) {
                this.needsCollisionDamage = true
            }
        }
        return true
    }

    protected fun getLiquidMovementSpeed(liquid: BlockLiquid?): Float {
        if (liquid is BlockFlowingLava) {
            return 0.02f
        }
        return 0.05f
    }

    open fun getFootHeight(): Float {
        return getCurrentHeight() / 2 - 0.1f
    }

    protected fun calculateOffsetBoundingBox() {
        //由于是asyncPrepare,this.offsetBoundingBox有几率为null，需要判空
        val dx: Double = this.getWidth() * 0.5
        val dz: Double = this.getHeight() * 0.5
        offsetBoundingBox.minX = (position.x - dx)
        offsetBoundingBox.maxX = (position.x + dz)
        offsetBoundingBox.minY = (position.y)
        offsetBoundingBox.maxY = (position.y + this.getHeight())
        offsetBoundingBox.minZ = (position.z - dz)
        offsetBoundingBox.maxZ = (position.z + dz)
    }

    override fun resetFallDistance() {
        this.fallingTick = 0
        super.resetFallDistance()
    }

    fun getFallingTick(): Int {
        return this.fallingTick
    }

    companion object {
        /**
         * 移动精度阈值，绝对值小于此阈值的移动被视为没有移动
         */
        const val PRECISION: Float = 0.00001f

        val globalCycleTickSpread: AtomicInteger = AtomicInteger()
    }
}
