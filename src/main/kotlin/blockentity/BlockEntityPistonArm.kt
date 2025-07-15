package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.block.BlockPistonArmCollision
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.entity.EntityMoveByPistonEvent
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockVector3
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.IntTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.nbt.tag.Tag
import org.chorus_oss.chorus.utils.Faceable
import org.chorus_oss.chorus.utils.RedstoneComponent
import org.chorus_oss.chorus.utils.Utils
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class BlockEntityPistonArm(chunk: IChunk, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt) {
    lateinit var facing: BlockFace
    var extending: Boolean = false
    var sticky: Boolean = false

    @JvmField
    var state: Byte = 0

    var newState: Byte = 1

    lateinit var attachedBlocks: MutableList<BlockVector3>

    @JvmField
    var powered: Boolean = false
    var progress: Float = 0f
    var lastProgress: Float = 1f


    var finished: Boolean = true

    protected fun moveCollidedEntities() {
        if (this.closed) {
            return
        }

        val pushDirection = if (this.extending) facing else facing.getOpposite()
        for (pos in attachedBlocks) {
            val blockEntity = level.getBlockEntity(pos.getSide(pushDirection))
            if (blockEntity is BlockEntityMovingBlock) blockEntity.moveCollidedEntities(
                this,
                pushDirection
            )
        }
        val bb = SimpleAxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0).getOffsetBoundingBox(
            position.x + (pushDirection.xOffset * progress),
            position.y + (pushDirection.yOffset * progress),
            position.z + (pushDirection.zOffset * progress) //带动站在移动方块上的实体
        ).addCoord(0.0, if (pushDirection.axis.isHorizontal) 0.25 else 0.0, 0.0)
        for (entity in level.getCollidingEntities(bb)) moveEntity(
            entity,
            pushDirection
        )
    }

    fun moveEntity(entity: Entity, moveDirection: BlockFace) {
        //不需要给予向下的力
        if (moveDirection == BlockFace.DOWN) return
        val diff = abs((this.progress - this.lastProgress).toDouble()).toFloat()
        //玩家客户端会自动处理移动
        if (diff == 0f || !entity.canBePushed() || entity is Player) return
        val event = EntityMoveByPistonEvent(entity, entity.locator.position)
        Server.instance.pluginManager.callEvent(event)
        if (event.cancelled) return
        entity.onPushByPiston(this)
        if (entity.closed) return
        //需要抵消重力
        entity.move(
            (diff * moveDirection.xOffset).toDouble(),
            (diff * moveDirection.yOffset * (if (moveDirection == BlockFace.UP) 2 else 1)).toDouble(),
            (diff * moveDirection.zOffset).toDouble()
        )
    }

    /**
     * Performs the preparatory operations before a move.
     * This method initializes the state prior to movement, including setting whether the structure is extending or contracting,
     * progress, state, and updates relevant moving data.
     *
     * @param extending      A boolean indicating whether is extending
     * @param attachedBlocks A list of BlockVector3 representing the blocks attached to the moving block.
     */
    fun preMove(extending: Boolean, attachedBlocks: MutableList<BlockVector3>) {
        this.finished = false // Initialize movement as unfinished
        this.extending = extending // Set the extending status
        this.progress = (if (extending) 0 else 1).toFloat()
        this.lastProgress = this.progress // Set progress: 0 for extending, 1 for contracting
        this.newState = (if (extending) 1 else 3).toByte()
        this.state = this.newState // Set current and new states: 1 for extending, 3 for contracting
        this.attachedBlocks = attachedBlocks // Set the attached blocks list
        this.isMovable = false // Set the structure as immovable
        // Update moving data immediately to ensure timeliness
        updateMovingData(true)
    }


    //需要先调用preMove
    fun move() {
        if (this.closed) {
            return
        }

        //开始推动
        this.lastProgress = (if (this.extending) 0 else 1).toFloat()
        this.moveCollidedEntities()
        this.scheduleUpdate()
    }

    /**
     * The piston extension process lasts 2gt.
     */
    override fun onUpdate(): Boolean {
        //此bool标记下一gt是否需要继续更新
        var hasUpdate = true
        //推动过程
        if (this.extending) {
            this.progress = min(1.0, (this.progress + MOVE_STEP).toDouble()).toFloat()
            this.lastProgress = min(1.0, (this.lastProgress + MOVE_STEP).toDouble()).toFloat()
        } else {
            this.progress = max(0.0, (this.progress - MOVE_STEP).toDouble()).toFloat()
            this.lastProgress = max(0.0, (this.lastProgress - MOVE_STEP).toDouble()).toFloat()
        }
        moveCollidedEntities()
        if (this.progress == this.lastProgress) {
            //结束推动
            this.newState = (if (extending) 2 else 0).toByte()
            this.state = this.newState
            val pushDirection = if (this.extending) facing else facing.getOpposite()
            val redstoneUpdateList = ArrayList<BlockVector3>()
            for (pos in attachedBlocks) {
                redstoneUpdateList.add(pos)
                redstoneUpdateList.add(pos.getSide(pushDirection))
                val movingBlock = level.getBlockEntity(pos.getSide(pushDirection))
                if (movingBlock is BlockEntityMovingBlock) {
                    movingBlock.close()
                    val moved = movingBlock.movingBlock
                    moved!!.position(movingBlock)
                    level.setBlock(movingBlock.position, 1, Block.get(BlockID.AIR), true, false)
                    //普通方块更新
                    level.setBlock(movingBlock.position, moved, true, true)
                    val movedBlockEntity = movingBlock.movingBlockEntityCompound
                    if (movedBlockEntity != null) {
                        movedBlockEntity.putInt("x", movingBlock.position.floorX)
                        movedBlockEntity.putInt("y", movingBlock.position.floorY)
                        movedBlockEntity.putInt("z", movingBlock.position.floorZ)
                        createBlockEntity(
                            movedBlockEntity.getString("id"),
                            level.getChunk(movingBlock.position.chunkX, movingBlock.position.chunkZ), movedBlockEntity
                        )
                    }
                    //活塞更新
                    moved.onUpdate(Level.BLOCK_UPDATE_MOVED)
                }
            }
            for (update in redstoneUpdateList) {
                //红石更新
                RedstoneComponent.updateAllAroundRedstone(
                    Locator(
                        update.x.toDouble(), update.y.toDouble(), update.z.toDouble(),
                        this.level
                    )
                )
            }
            val pos = getSide(facing)
            if (!extending) {
                //未伸出的活塞可以被推动
                this.isMovable = true
                if (level.getBlock(pos.position) is BlockPistonArmCollision) {
                    level.setBlock(pos.position, 1, Block.get(BlockID.AIR), true, false)
                    //方块更新
                    level.setBlock(pos.position, Block.get(BlockID.AIR), true)
                }
            }
            //对和活塞直接接触的观察者进行更新
            level.updateAroundObserver(this.position)
            //下一计划刻再自检一遍，防止出错
            level.scheduleUpdate(this.levelBlock, 1)
            attachedBlocks.clear()
            this.finished = true
            hasUpdate = false
            updateMovingData(false)
        }
        return super.onUpdate() || hasUpdate
    }

    override fun loadNBT() {
        super.loadNBT()
        this.state = namedTag.getByte("State")
        this.newState = namedTag.getByte("NewState")
        if (namedTag.contains("Progress")) this.progress = namedTag.getFloat("Progress")
        if (namedTag.contains("LastProgress")) this.lastProgress = namedTag.getFloat("LastProgress")
        this.sticky = namedTag.getBoolean("Sticky")
        this.extending = namedTag.getBoolean("Extending")
        this.powered = namedTag.getBoolean("powered")
        if (namedTag.contains("facing")) {
            this.facing = BlockFace.fromIndex(namedTag.getInt("facing"))
        } else {
            val block = this.levelBlock
            if (block is Faceable) this.facing = block.blockFace
            else this.facing = BlockFace.NORTH
        }
        attachedBlocks = mutableListOf<BlockVector3>()
        if (namedTag.contains("AttachedBlocks")) {
            val blocks = namedTag.getList("AttachedBlocks", IntTag::class.java)
            if (blocks.size() > 0) {
                var i = 0
                while (i < blocks.size()) {
                    attachedBlocks.add(
                        BlockVector3(
                            blocks[i].data,
                            blocks[i + 1].data,
                            blocks[i + 2].data
                        )
                    )
                    i += 3
                }
            }
        } else namedTag.putList("AttachedBlocks", ListTag<Tag<*>>())
    }

    override fun saveNBT() {
        super.saveNBT()
        namedTag.putByte("State", state.toInt())
        namedTag.putByte("NewState", newState.toInt())
        namedTag.putFloat("Progress", this.progress)
        namedTag.putFloat("LastProgress", this.lastProgress)
        namedTag.putBoolean("powered", this.powered)
        namedTag.putList("AttachedBlocks", getAttachedBlocks())
        namedTag.putInt("facing", facing.index)
        namedTag.putBoolean("Sticky", this.sticky)
        namedTag.putBoolean("Extending", this.extending)
    }

    override val isBlockEntityValid: Boolean
        get() {
            val blockId = block.id
            return blockId == BlockID.PISTON || blockId == BlockID.STICKY_PISTON
        }

    override val spawnCompound: CompoundTag
        get() = super.spawnCompound
            .putBoolean("isMovable", this.isMovable)
            .putFloat("Progress", this.progress)
            .putFloat("LastProgress", this.lastProgress)
            .putList("AttachedBlocks", getAttachedBlocks())
            .putList("BreakBlocks", ListTag<Tag<*>>())
            .putBoolean("Sticky", this.sticky)
            .putByte("State", state.toInt())
            .putByte("NewState", newState.toInt())

    protected fun getAttachedBlocks(): ListTag<IntTag> {
        val attachedBlocks = ListTag<IntTag>()
        for (block in this.attachedBlocks) {
            attachedBlocks.add(IntTag(block.x))
            attachedBlocks.add(IntTag(block.y))
            attachedBlocks.add(IntTag(block.z))
        }
        return attachedBlocks
    }

    fun updateMovingData(immediately: Boolean) {
        if (this.closed) {
            return
        }

        val packet = this.spawnPacket
        if (!immediately) {
            level.addChunkPacket(
                position.chunkX,
                position.chunkZ, packet
            )
        } else {
            Server.broadcastPacket(
                level.getChunkPlayers(
                    chunk.x,
                    chunk.z
                ).values, packet
            )
        }
    }

    companion object {
        val MOVE_STEP: Float = Utils.dynamic(0.25f)
    }
}
