package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.Attachment
import org.chorus_oss.chorus.blockentity.BlockEntityBell
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.item.EntityItem
import org.chorus_oss.chorus.entity.projectile.abstract_arrow.EntityArrow
import org.chorus_oss.chorus.event.block.BellRingEvent
import org.chorus_oss.chorus.event.block.BellRingEvent.RingCause
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromHorizontalIndex
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.Faceable
import org.chorus_oss.chorus.utils.RedstoneComponent
import kotlin.math.abs
import kotlin.math.max

class BlockBell @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockTransparent(blockState), RedstoneComponent, Faceable, BlockEntityHolder<BlockEntityBell> {
    override val name: String
        get() = "Bell"

    override fun getBlockEntityClass(): Class<out BlockEntityBell> {
        return BlockEntityBell::class.java
    }

    override fun getBlockEntityType(): String {
        return BlockEntityID.BELL
    }

    private fun isConnectedTo(connectedFace: BlockFace, attachmentType: Attachment, blockFace: BlockFace): Boolean {
        val faceAxis = connectedFace.axis
        when (attachmentType) {
            Attachment.STANDING -> {
                return if (faceAxis == BlockFace.Axis.Y) {
                    connectedFace == BlockFace.DOWN
                } else {
                    blockFace.axis != faceAxis
                }
            }

            Attachment.HANGING -> {
                return connectedFace == BlockFace.UP
            }

            Attachment.SIDE -> {
                return connectedFace == blockFace.getOpposite()
            }

            Attachment.MULTIPLE -> {
                return connectedFace == blockFace || connectedFace == blockFace.getOpposite()
            }
        }
    }

    override fun recalculateBoundingBox(): AxisAlignedBB {
        val attachmentType = attachment
        val blockFace = blockFace
        val north = this.isConnectedTo(BlockFace.NORTH, attachmentType, blockFace)
        val south = this.isConnectedTo(BlockFace.SOUTH, attachmentType, blockFace)
        val west = this.isConnectedTo(BlockFace.WEST, attachmentType, blockFace)
        val east = this.isConnectedTo(BlockFace.EAST, attachmentType, blockFace)
        val up = this.isConnectedTo(BlockFace.UP, attachmentType, blockFace)
        val down = this.isConnectedTo(BlockFace.DOWN, attachmentType, blockFace)

        val n = if (north) 0.0 else 0.25
        val s = if (south) 1.0 else 0.75
        val w = if (west) 0.0 else 0.25
        val e = if (east) 1.0 else 0.75
        val d = if (down) 0.0 else 0.25
        val u = if (up) 1.0 else 0.75

        return SimpleAxisAlignedBB(
            position.x + w,
            position.y + d,
            position.z + n,
            position.x + e,
            position.y + u,
            position.z + s
        )
    }

    override fun onEntityCollide(entity: Entity) {
        if (entity.positionChanged) {
            val boundingBox = entity.getBoundingBox()
            val blockBoundingBox = this.collisionBoundingBox
            if (boundingBox.intersectsWith(blockBoundingBox!!)) {
                val entityCenter = Vector3(
                    (boundingBox.maxX - boundingBox.minX) / 2,
                    (boundingBox.maxY - boundingBox.minY) / 2,
                    (boundingBox.maxZ - boundingBox.minZ) / 2
                )

                val blockCenter = Vector3(
                    (blockBoundingBox.maxX - blockBoundingBox.minX) / 2,
                    (blockBoundingBox.maxY - blockBoundingBox.minY) / 2,
                    (blockBoundingBox.maxZ - blockBoundingBox.minZ) / 2
                )
                val entityPos = entity.position.add(entityCenter)
                val blockPos = position.add(
                    blockBoundingBox.minX - position.x + blockCenter.x,
                    blockBoundingBox.minY - position.y + blockCenter.y,
                    blockBoundingBox.minZ - position.z + blockCenter.z
                )


                if (ring(entity, RingCause.DROPPED_ITEM)) {
                    if (entity is EntityItem) {
                        var entityVector = entityPos.subtract(blockPos)
                        entityVector = entityVector.normalize().multiply(0.4)
                        entityVector.y = max(0.15, entityVector.y)
                        entity.setMotion(entityVector)
                    }
                }
            }
        }
    }

    override fun hasEntityCollision(): Boolean {
        return true
    }

    override fun recalculateCollisionBoundingBox(): AxisAlignedBB {
        return recalculateBoundingBox().expand(0.000001, 0.000001, 0.000001)
    }

    override fun canBeActivated(): Boolean {
        return true
    }

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        if (isNotActivate(player)) return false
        return ring(player, if (player != null) RingCause.HUMAN_INTERACTION else RingCause.UNKNOWN)
    }

    @JvmOverloads
    fun ring(causeEntity: Entity?, cause: RingCause, hitFace: BlockFace? = null): Boolean {
        var hitFace1 = hitFace
        val bell = getOrCreateBlockEntity()
        var addException = true
        val blockFace = blockFace
        if (hitFace1 == null) {
            if (causeEntity != null) {
                if (causeEntity is EntityItem) {
                    val blockMid = add(0.5, 0.5, 0.5)
                    val vector = causeEntity.position.subtract(blockMid.position).normalize()
                    var x = if (vector.x < 0) -1 else if (vector.x > 0) 1 else 0
                    var z = if (vector.z < 0) -1 else if (vector.z > 0) 1 else 0
                    if (x != 0 && z != 0) {
                        if (abs(vector.x) < abs(vector.z)) {
                            x = 0
                        } else {
                            z = 0
                        }
                    }
                    hitFace1 = blockFace
                    for (face in BlockFace.entries) {
                        if (face.xOffset == x && face.zOffset == z) {
                            hitFace1 = face
                            break
                        }
                    }
                } else {
                    hitFace1 = causeEntity.getDirection()
                }
            } else {
                hitFace1 = blockFace
            }
        }
        when (attachment) {
            Attachment.STANDING -> {
                if (hitFace1!!.axis != blockFace.axis) {
                    return false
                }
            }

            Attachment.MULTIPLE -> {
                if (hitFace1!!.axis == blockFace.axis) {
                    return false
                }
            }

            Attachment.SIDE -> {
                if (hitFace1!!.axis == blockFace.axis) {
                    addException = false
                }
            }

            else -> Unit
        }

        val event = BellRingEvent(this, cause, causeEntity!!)
        Server.instance.pluginManager.callEvent(event)
        if (event.cancelled) {
            return false
        }

        bell.direction = hitFace1!!.getOpposite().horizontalIndex
        bell.ticks = 0
        bell.setRinging(true)
        if (addException && causeEntity is Player) {
            bell.spawnExceptions.add(causeEntity)
        }
        return true
    }

    private fun checkSupport(): Boolean {
        when (attachment) {
            Attachment.STANDING -> if (checkSupport(down(), BlockFace.UP)) {
                return true
            }

            Attachment.HANGING -> if (checkSupport(up(), BlockFace.DOWN)) {
                return true
            }

            Attachment.MULTIPLE -> {
                val blockFace = blockFace
                if (checkSupport(getSide(blockFace), blockFace.getOpposite()) &&
                    checkSupport(getSide(blockFace.getOpposite()), blockFace)
                ) {
                    return true
                }
            }

            Attachment.SIDE -> {
                blockFace = blockFace
                if (checkSupport(getSide(blockFace.getOpposite()), blockFace)) {
                    return true
                }
            }

            else -> Unit
        }
        return false
    }

    private fun checkSupport(support: Block, attachmentFace: BlockFace?): Boolean {
        if (attachmentFace == null) return false

        if (BlockLever.isSupportValid(support, attachmentFace)) {
            return true
        }

        if (attachmentFace == BlockFace.DOWN) {
            return when (support.id) {
                BlockID.CHAIN, BlockID.HOPPER, BlockID.IRON_BARS -> true
                else -> support is BlockFence || support is BlockWallBase
            }
        }

        if (support is BlockCauldron) {
            return attachmentFace == BlockFace.UP
        }

        return false
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (!checkSupport()) {
                level.useBreakOn(this.position)
            }
            return type
        } else if (type == Level.BLOCK_UPDATE_REDSTONE && Server.instance.settings.levelSettings.enableRedstone) {
            if (this.isGettingPower) {
                if (!isToggled) {
                    isToggled = true
                    level.setBlock(this.position, this, direct = true, update = true)
                    ring(null, RingCause.REDSTONE)
                }
            } else if (isToggled) {
                isToggled = false
                level.setBlock(this.position, this, direct = true, update = true)
            }
            return type
        }
        return 0
    }

    override val isGettingPower: Boolean
        get() {
            for (side in BlockFace.entries) {
                val b = this.getSide(side)

                if (b.id == BlockID.REDSTONE_WIRE && b.blockState.getPropertyValue(
                        CommonBlockProperties.REDSTONE_SIGNAL
                    ) > 0 && b.position.y >= this.y
                ) {
                    return true
                }

                if (level.isSidePowered(b.position, side)) {
                    return true
                }
            }

            return level.isBlockPowered(this.position)
        }

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
        var face1 = face
        if (block.canBeReplaced() && block.isAir && (block.id != BlockID.BUBBLE_COLUMN) && (block !is BlockLiquid)) {
            face1 = BlockFace.UP
        }
        val playerDirection = if (player != null) player.getDirection() else BlockFace.EAST
        when (face1) {
            BlockFace.UP -> {
                attachment = Attachment.STANDING
                blockFace = playerDirection.getOpposite()
            }

            BlockFace.DOWN -> {
                attachment = Attachment.HANGING
                blockFace = playerDirection.getOpposite()
            }

            else -> {
                blockFace = face1
                attachment = if (checkSupport(block.getSide(face1), face1.getOpposite())) {
                    Attachment.MULTIPLE
                } else {
                    Attachment.SIDE
                }
            }
        }

        if (!checkSupport()) {
            return false
        }

        return BlockEntityHolder.setBlockAndCreateEntity(this) != null
    }

    override fun onProjectileHit(projectile: Entity, locator: Locator, motion: Vector3): Boolean {
        ring(projectile, RingCause.PROJECTILE)
        if (projectile.isOnFire() && projectile is EntityArrow && level.getBlock(projectile.position).isAir) {
            level.setBlock(projectile.position, get(BlockID.FIRE), true)
        }
        return true
    }

    override var blockFace: BlockFace
        get() = fromHorizontalIndex(getPropertyValue(CommonBlockProperties.DIRECTION))
        set(face) {
            setPropertyValue(
                CommonBlockProperties.DIRECTION,
                face.horizontalIndex
            )
        }

    var attachment: Attachment
        get() = getPropertyValue(
            CommonBlockProperties.ATTACHMENT
        )
        set(attachmentType) {
            setPropertyValue(
                CommonBlockProperties.ATTACHMENT,
                attachmentType
            )
        }

    var isToggled: Boolean
        get() = getPropertyValue(CommonBlockProperties.TOGGLE_BIT)
        set(toggled) {
            setPropertyValue(CommonBlockProperties.TOGGLE_BIT, toggled)
        }

    override val waterloggingLevel: Int
        get() = 1

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val hardness: Double
        get() = 1.0

    override val resistance: Double
        get() = 25.0

    override val toolTier: Int
        get() = ItemTool.TIER_WOODEN

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.BELL,
            CommonBlockProperties.ATTACHMENT,
            CommonBlockProperties.DIRECTION,
            CommonBlockProperties.TOGGLE_BIT
        )
    }
}
