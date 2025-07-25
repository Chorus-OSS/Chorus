package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server.Companion.instance
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.CommonPropertyMap
import org.chorus_oss.chorus.block.property.enums.BigDripleafTilt
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.projectile.EntityProjectile
import org.chorus_oss.chorus.event.block.BigDripleafTiltChangeEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.particle.BoneMealParticle
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.Faceable
import kotlin.math.ceil

class BlockBigDripleaf @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockFlowable(blockState), Faceable {
    override val name: String
        get() = "Big Dripleaf"

    override var blockFace: BlockFace
        get() = CommonPropertyMap.CARDINAL_BLOCKFACE[getPropertyValue(
            CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION
        )]!!
        set(face) {
            setPropertyValue(
                CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION,
                CommonPropertyMap.CARDINAL_BLOCKFACE.inverse()[face]!!
            )
        }

    var isHead: Boolean
        get() = this.getPropertyValue(CommonBlockProperties.BIG_DRIPLEAF_HEAD)
        set(isHead) {
            this.setPropertyValue(
                CommonBlockProperties.BIG_DRIPLEAF_HEAD,
                isHead
            )
        }

    val tilt: BigDripleafTilt
        get() = this.getPropertyValue(
            CommonBlockProperties.BIG_DRIPLEAF_TILT
        )

    fun setTilt(tilt: BigDripleafTilt): Boolean {
        val event = BigDripleafTiltChangeEvent(
            this,
            this.tilt, tilt
        )
        instance.pluginManager.callEvent(event)
        if (event.cancelled) return false
        this.setPropertyValue(CommonBlockProperties.BIG_DRIPLEAF_TILT, tilt)
        return true
    }

    override val waterloggingLevel: Int
        get() = 2

    override val toolType: Int
        get() = ItemTool.TYPE_NONE

    override fun canBeActivated(): Boolean {
        return true
    }

    override val hardness: Double
        get() = 0.0

    override val resistance: Double
        get() = 0.0

    override val burnChance: Int
        get() = 15

    override val burnAbility: Int
        get() = 100

    override fun place(
        item: Item?,
        block: Block,
        target: Block?,
        face: BlockFace,
        fx: Double,
        fy: Double,
        fz: Double,
        player: Player?
    ): Boolean {
        val below = block.down()
        val id = below.id
        if (!isValidSupportBlock(id)) return false

        if (id == BlockID.BIG_DRIPLEAF) {
            val b = BlockBigDripleaf()
            val bf = (below as BlockBigDripleaf).blockFace
            b.blockFace = bf
            b.isHead = false
            level.setBlock(below.position, b, direct = true, update = false)
            blockFace = bf
        } else {
            blockFace = player?.getHorizontalFacing()?.getOpposite() ?: BlockFace.SOUTH
        }
        isHead = true

        if (block is BlockFlowingWater) level.setBlock(this.position, 1, block, direct = true, update = false)
        return super.place(item, block, target, face, fx, fy, fz, player)
    }

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        if (item.isFertilizer) {
            var head: Block = this
            var up: Block?
            while ((head.up().also { up = it }).id === BlockID.BIG_DRIPLEAF) head = up!!
            if (head.position.floorY + 1 > level.maxHeight) return false
            val above = head.up()
            if (!above.isAir && above !is BlockFlowingWater) return false
            if (player != null && !player.isCreative) item.count--
            level.addParticle(BoneMealParticle(this.position))
            val aboveDownBlock = BlockBigDripleaf()
            aboveDownBlock.blockFace = this.blockFace
            level.setBlock(above.position.getSideVec(BlockFace.DOWN), aboveDownBlock, direct = true, update = false)
            if (above is BlockFlowingWater) level.setBlock(above.position, 1, above, direct = true, update = false)
            val aboveBock = BlockBigDripleaf()
            aboveBock.blockFace = this.blockFace
            aboveBock.isHead = true
            level.setBlock(above.position, aboveBock, true)
            return true
        }

        return false
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            level.scheduleUpdate(this, 1)
            return Level.BLOCK_UPDATE_NORMAL
        }

        if (type == Level.BLOCK_UPDATE_SCHEDULED) {
            if (!canSurvive()) {
                level.useBreakOn(this.position)
                return Level.BLOCK_UPDATE_NORMAL
            }

            if (!isHead) {
                if (up().id == BlockID.BIG_DRIPLEAF) {
                    return 0
                }

                level.useBreakOn(this.position)
                return Level.BLOCK_UPDATE_NORMAL
            }

            val tilt = tilt
            if (tilt == BigDripleafTilt.NONE) {
                return 0
            }

            if (level.isBlockPowered(this.position)) {
                setTilt(BigDripleafTilt.NONE)
                level.setBlock(this.position, this, direct = true, update = false)
                return Level.BLOCK_UPDATE_SCHEDULED
            }

            when (tilt) {
                BigDripleafTilt.UNSTABLE -> setTiltAndScheduleTick(BigDripleafTilt.PARTIAL_TILT)
                BigDripleafTilt.PARTIAL_TILT -> setTiltAndScheduleTick(BigDripleafTilt.FULL_TILT)
                BigDripleafTilt.FULL_TILT -> {
                    level.addSound(this.position, Sound.TILT_UP_BIG_DRIPLEAF)
                    setTilt(BigDripleafTilt.NONE)
                    level.setBlock(this.position, this, direct = true, update = false)
                }

                else -> Unit
            }
            return Level.BLOCK_UPDATE_SCHEDULED
        }

        if (type == Level.BLOCK_UPDATE_REDSTONE) {
            if (!isHead) return 0
            val tilt = tilt
            if (tilt == BigDripleafTilt.NONE) return 0
            if (!level.isBlockPowered(this.position)) return 0
            if (tilt != BigDripleafTilt.UNSTABLE) level.addSound(this.position, Sound.TILT_UP_BIG_DRIPLEAF)
            setTilt(BigDripleafTilt.NONE)
            level.setBlock(this.position, this, direct = true, update = false)

            level.cancelSheduledUpdate(this.position, this)
            return Level.BLOCK_UPDATE_SCHEDULED
        }

        return 0
    }

    override fun onEntityCollide(entity: Entity) {
        if (!isHead || tilt != BigDripleafTilt.NONE || entity is EntityProjectile) return
        setTiltAndScheduleTick(BigDripleafTilt.UNSTABLE)
    }

    override fun onProjectileHit(projectile: Entity, locator: Locator, motion: Vector3): Boolean {
        setTiltAndScheduleTick(BigDripleafTilt.FULL_TILT)
        return true
    }

    override fun canPassThrough(): Boolean {
        return !isHead || tilt == BigDripleafTilt.FULL_TILT
    }

    override fun recalculateBoundingBox(): AxisAlignedBB? {
        return if (!isHead) {
            null
        } else {
            SimpleAxisAlignedBB(
                position.x,
                position.y + 0.6875,
                position.z,
                position.x + 1,
                position.y + (if (tilt == BigDripleafTilt.PARTIAL_TILT) 0.8125 else 0.9375),
                position.z + 1
            )
        }
    }

    override fun recalculateCollisionBoundingBox(): AxisAlignedBB? {
        val bb = (boundingBox ?: return null).clone()
        //使方块碰撞检测箱的maxY向上取整，使当实体站在方块上面的时候可以触发碰撞
        if (isHead) bb.maxY = ceil(bb.maxY)
        return bb
    }

    override fun isSolid(side: BlockFace): Boolean {
        return false
    }

    private fun canSurvive(): Boolean {
        return isValidSupportBlock(down().id)
    }

    private fun isValidSupportBlock(id: String): Boolean {
        return id == BlockID.BIG_DRIPLEAF ||
                id == BlockID.GRASS_BLOCK ||
                id == BlockID.DIRT ||
                id == BlockID.MYCELIUM ||
                id == BlockID.PODZOL ||
                id == BlockID.FARMLAND ||
                id == BlockID.DIRT_WITH_ROOTS ||
                id == BlockID.MOSS_BLOCK ||
                id == BlockID.CLAY
    }

    private fun setTiltAndScheduleTick(tilt: BigDripleafTilt): Boolean {
        if (!setTilt(tilt)) return false
        level.setBlock(this.position, this, direct = true, update = false)

        when (tilt) {
            BigDripleafTilt.NONE -> level.scheduleUpdate(this, 1)
            BigDripleafTilt.UNSTABLE -> {
                level.scheduleUpdate(this, 15)
                return true
            }

            BigDripleafTilt.PARTIAL_TILT -> level.scheduleUpdate(this, 15)
            BigDripleafTilt.FULL_TILT -> level.scheduleUpdate(this, 100)
        }

        level.addSound(this.position, Sound.TILT_DOWN_BIG_DRIPLEAF)
        return true
    }

    override val isFertilizable: Boolean
        get() = true

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.BIG_DRIPLEAF,
            CommonBlockProperties.BIG_DRIPLEAF_HEAD,
            CommonBlockProperties.BIG_DRIPLEAF_TILT,
            CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION
        )
    }
}
