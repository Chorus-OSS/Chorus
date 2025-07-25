package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.blockentity.BlockEntity
import org.chorus_oss.chorus.blockentity.BlockEntity.Companion.getDefaultCompound
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.blockentity.BlockEntityMovingBlock
import org.chorus_oss.chorus.blockentity.BlockEntityPistonArm
import org.chorus_oss.chorus.event.block.BlockPistonEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool.Companion.getBestTool
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationType
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromIndex
import org.chorus_oss.chorus.math.BlockVector3
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.utils.Faceable
import org.chorus_oss.chorus.utils.RedstoneComponent
import java.util.concurrent.CopyOnWriteArrayList
import java.util.stream.Collectors
import kotlin.math.abs

abstract class BlockPistonBase(blockstate: BlockState) : BlockTransparent(blockstate), Faceable,
    RedstoneComponent, BlockEntityHolder<BlockEntityPistonArm> {
    @JvmField
    var sticky: Boolean = false

    override fun getBlockEntityClass() = BlockEntityPistonArm::class.java

    override fun getBlockEntityType(): String {
        return BlockEntityID.PISTON_ARM
    }

    override val resistance: Double
        get() = 1.5

    override val hardness: Double
        get() = 1.5

    override val waterloggingLevel: Int
        get() = 1

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
        if (player != null) {
            if (abs(player.position.floorX - position.x) <= 1 && abs(player.position.floorZ - position.z) <= 1) {
                val y = player.position.y + player.getEyeHeight()

                if (y - position.y > 2) {
                    this.setPropertyValue<Int, IntPropertyType>(
                        CommonBlockProperties.FACING_DIRECTION,
                        BlockFace.UP.index
                    )
                } else if (position.y - y > 0) {
                    this.setPropertyValue<Int, IntPropertyType>(
                        CommonBlockProperties.FACING_DIRECTION,
                        BlockFace.DOWN.index
                    )
                } else {
                    this.setPropertyValue<Int, IntPropertyType>(
                        CommonBlockProperties.FACING_DIRECTION,
                        player.getHorizontalFacing().index
                    )
                }
            } else {
                this.setPropertyValue<Int, IntPropertyType>(
                    CommonBlockProperties.FACING_DIRECTION,
                    player.getHorizontalFacing().index
                )
            }
        }
        level.setBlock(block.position, this, true, true)
        val nbt = getDefaultCompound(this.position, BlockEntityID.PISTON_ARM)
            .putInt("facing", blockFace.index)
            .putBoolean("Sticky", this.sticky)
            .putBoolean("powered", isGettingPower)
        val piston = BlockEntity.createBlockEntity(
            BlockEntityID.PISTON_ARM,
            level.getChunk(position.chunkX, position.chunkZ), nbt
        ) as BlockEntityPistonArm?
        piston!!.powered = isGettingPower
        this.checkState(piston.powered)
        return true
    }

    override fun onBreak(item: Item?): Boolean {
        level.setBlock(this.position, get(BlockID.AIR), true, true)
        val block = this.getSide(blockFace)
        if (block is BlockPistonArmCollision && block.blockFace == this.blockFace) block.onBreak(item)
        return true
    }

    val isExtended: Boolean
        get() {
            val face = blockFace
            val block = getSide(face)
            return block is BlockPistonArmCollision && block.blockFace == face
        }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_REDSTONE || type == Level.BLOCK_UPDATE_MOVED || type == Level.BLOCK_UPDATE_NORMAL) {
            if (!Server.instance.settings.levelSettings.enableRedstone) return 0
            level.scheduleUpdate(this, 2)
            return type
        }
        if (type == Level.BLOCK_UPDATE_SCHEDULED) {
            if (!Server.instance.settings.levelSettings.enableRedstone) return 0
            // We can't use getOrCreateBlockEntity(), because the update method is called on block place,
            // before the "real" BlockEntity is set. That means, if we'd use the other method here,
            // it would create two BlockEntities.
            val arm = this.blockEntity ?: return 0
            val powered = this.isGettingPower
            this.updateAroundRedstoneTorches(powered)
            if (arm.state % 2 == 0 && arm.powered != powered && checkState(powered)) {
                arm.powered = powered
                arm.chunk.setChanged()
                if (powered && !isExtended)  //推出未成功,下一个计划刻再次自检
                //TODO: 这里可以记录阻挡的方块并在阻挡因素移除后同步更新到活塞，而不是使用计划刻
                    level.scheduleUpdate(this, 2)
                return type
            }
            //上一次推出未成功
            if (type == Level.BLOCK_UPDATE_SCHEDULED && powered && !isExtended && !checkState(true))  //依然不成功，下一个计划刻继续自检
                level.scheduleUpdate(this, 2)
            return type
        }
        return 0
    }

    override val isGettingPower: Boolean
        get() {
            val face = blockFace
            for (side in BlockFace.entries) {
                if (side == face) continue
                val b = this.getSide(side)
                if (b.id == BlockID.REDSTONE_WIRE && b.getPropertyValue<Int, IntPropertyType>(
                        CommonBlockProperties.REDSTONE_SIGNAL
                    ) > 0
                ) return true
                if (level.isSidePowered(b.position, side)) return true
            }
            return false
        }

    protected fun updateAroundRedstoneTorches(powered: Boolean) {
        for (side in BlockFace.entries) {
            if ((getSide(side) is BlockRedstoneTorch && powered)
                || (getSide(side) is BlockUnlitRedstoneTorch && !powered)
            ) {
                val torch = getSide(side) as BlockTorch?

                val torchAttachment = torch!!.torchAttachment
                val support = torch.getSide(torchAttachment.attachedFace)

                if (support.locator == this.locator) {
                    torch.onUpdate(Level.BLOCK_UPDATE_REDSTONE)
                }
            }
        }
    }

    protected fun checkState(isPowered: Boolean?): Boolean {
        var isPowered = isPowered
        if (!Server.instance.settings.levelSettings.enableRedstone) {
            return false
        }

        if (isPowered == null) {
            isPowered = this.isGettingPower
        }

        val face = blockFace
        val block = getSide(face)

        val isExtended: Boolean
        if (block is BlockPistonArmCollision) {
            if (block.blockFace != face) {
                return false
            }
            isExtended = true
        } else {
            isExtended = false
        }

        if (isPowered && !isExtended) {
            return this.doMove(true)
        } else if (!isPowered && isExtended) {
            return this.doMove(false)
        }
        return false
    }

    protected fun doMove(extending: Boolean): Boolean {
        val pistonFace = blockFace
        val calculator = BlocksCalculator(extending)
        val canMove = calculator.canMove()

        if (!canMove && extending) {
            return false
        }

        var toMoveBlockVec: List<BlockVector3> = ArrayList()
        val event = BlockPistonEvent(
            this, pistonFace,
            calculator.blocksToMove,
            calculator.blocksToDestroy, extending
        )
        Server.instance.pluginManager.callEvent(event)
        if (event.cancelled) {
            return false
        }

        val oldPosList = ArrayList<Vector3>()
        val blockEntityHolderList = ArrayList<BlockEntityHolder<*>>()
        val nbtList = ArrayList<CompoundTag?>()
        if (canMove && (this.sticky || extending)) {
            val destroyBlocks = calculator.blocksToDestroy
            //破坏需要破坏的方块
            for (i in destroyBlocks.indices.reversed()) {
                val block = destroyBlocks[i]
                val item = getBestTool(block.toolType)
                //清除位置上所含的水等
                level.setBlock(block.position, 1, get(BlockID.AIR), true, false)
                level.useBreakOn(block.position, item)
            }
            val blocksToMove = calculator.blocksToMove
            toMoveBlockVec =
                blocksToMove.stream().map { b: Block -> b.position.asBlockVector3() }.collect(Collectors.toList())
            val moveDirection = if (extending) pistonFace else pistonFace.getOpposite()
            for (blockToMove in blocksToMove) {
                val oldPos = Vector3(blockToMove.position.x, blockToMove.position.y, blockToMove.position.z)
                val newPos = blockToMove.getSidePos(moveDirection)
                //清除位置上所含的水等
                level.setBlock(newPos.position, 1, get(BlockID.AIR), true, false)
                val nbt = getDefaultCompound(newPos.position, BlockEntityID.MOVING_BLOCK)
                    .putBoolean("expanding", extending)
                    .putInt("pistonPosX", position.floorX)
                    .putInt("pistonPosY", position.floorY)
                    .putInt("pistonPosZ", position.floorZ)
                    .putCompound("movingBlock", blockToMove.blockState.blockStateTag)
                    .putCompound(
                        "movingBlockExtra",
                        level.getBlock(blockToMove.position, 1).blockState.blockStateTag
                    )
                    .putBoolean("isMovable", true)
                val blockEntity = level.getBlockEntity(oldPos)
                //移动方块实体
                if (blockEntity != null && blockEntity !is BlockEntityMovingBlock) {
                    blockEntity.saveNBT()
                    nbt.putCompound("movingEntity", CompoundTag(blockEntity.namedTag.tags))
                    blockEntity.close()
                }
                oldPosList.add(oldPos)

                blockEntityHolderList.add(
                    get(
                        BlockID.MOVING_BLOCK, fromObject(
                            newPos.position,
                            this.level
                        )
                    ) as BlockEntityHolder<*>
                )
                nbtList.add(nbt)
            }
        }
        val blockEntity = this.blockEntity ?: return false
        val finalToMoveBlockVec = toMoveBlockVec
        blockEntity.preMove(extending, finalToMoveBlockVec.toMutableList())
        //生成moving_block
        if (!oldPosList.isEmpty()) {
            for (i in oldPosList.indices) {
                val oldPos = oldPosList[i]
                val blockEntityHolder = blockEntityHolderList[i]
                val nbt = nbtList[i]
                BlockEntityHolder.setBlockAndCreateEntity(blockEntityHolder, true, true, nbt)
                if (level.getBlock(oldPos).id != BlockID.MOVING_BLOCK) {
                    level.setBlock(oldPos, get(BlockID.AIR))
                }
            }
        }
        //创建活塞臂方块
        if (extending) {
            val pistonArmPos = this.getSide(pistonFace)
            //清除位置上所含的水等
            level.setBlock(pistonArmPos.position, 1, get(BlockID.AIR), true, false)
            val blockFace = blockFace
            if (blockFace.axis == BlockFace.Axis.Y) {
                level.setBlock(pistonArmPos.position, createHead(blockFace), true, false)
            } else {
                level.setBlock(pistonArmPos.position, createHead(blockFace.getOpposite()), true, false)
            }
        }

        //开始移动
        blockEntity.move()
        if (extending) {
            level.addSound(this.position, Sound.TILE_PISTON_OUT)
            level.vibrationManager.callVibrationEvent(
                VibrationEvent(
                    this,
                    position.add(0.5, 0.5, 0.5), VibrationType.PISTON_EXTEND
                )
            )
        } else {
            level.addSound(this.position, Sound.TILE_PISTON_IN)
            level.vibrationManager.callVibrationEvent(
                VibrationEvent(
                    this,
                    position.add(0.5, 0.5, 0.5), VibrationType.PISTON_CONTRACT
                )
            )
        }
        return true
    }

    protected abstract fun createHead(blockFace: BlockFace): Block

    override var blockFace: BlockFace
        get() {
            val face =
                fromIndex(getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.FACING_DIRECTION))
            return if (face.horizontalIndex >= 0) face.getOpposite() else face
        }
        set(face) {
            setPropertyValue<Int, IntPropertyType>(CommonBlockProperties.FACING_DIRECTION, face.index)
        }

    override val isSolid: Boolean
        get() = false

    inner class BlocksCalculator(private val extending: Boolean) {
        private val pistonPos = position
        private var blockToMove: Block? = null
        private var moveDirection: BlockFace? = null
        private val toMove: MutableList<Block> = object : CopyOnWriteArrayList<Block>() {
            override fun indexOf(element: Block?): Int {
                if (element == null) {
                    for (i in 0..<size) if (get(i) == null) return i
                } else {
                    for (i in 0..<size) {
                        if (element == get(i)) return i
                    }
                }
                return -1
            }

            //以防万一
            override fun contains(element: Block?): Boolean {
                return indexOf(element) >= 0
            }
        }

        private val toDestroy: MutableList<Block> = ArrayList()
        private var armPos: Vector3? = null

        init {
            val face = blockFace
            if (!extending) {
                this.armPos = pistonPos.getSideVec(face)
            }

            if (extending) {
                this.moveDirection = face
                this.blockToMove = getSide(face)
            } else {
                this.moveDirection = face.getOpposite()
                if (sticky) {
                    this.blockToMove = getSide(face, 2)
                } else {
                    this.blockToMove = null
                }
            }
        }

        fun canMove(): Boolean {
            if (!sticky && !extending) {
                return true
            }

            toMove.clear()
            toDestroy.clear()
            val block = this.blockToMove
            if (!canPush(block!!, this.moveDirection, true, extending)) {
                return false
            }

            if (block.breaksWhenMoved()) {
                if (extending || block.sticksToPiston()) toDestroy.add(this.blockToMove!!)
                return true
            }

            if (!this.addBlockLine(
                    blockToMove!!,
                    blockToMove!!.getSide(moveDirection!!), true
                )
            ) {
                return false
            }

            for (b in this.toMove) {
                if (b.canSticksBlock() && !this.addBranchingBlocks(b)) {
                    return false
                }
            }
            return true
        }

        protected fun addBlockLine(origin: Block, from: Block, mainBlockLine: Boolean): Boolean {
            var block: Block? = origin.clone()
            if (block!!.isAir) {
                return true
            }

            if (!mainBlockLine && block.canSticksBlock() && from.canSticksBlock() && (block.id != from.id)) {
                return true
            }

            if (!canPush(origin, this.moveDirection, false, extending)) {
                return true
            }

            if (origin == this.pistonPos) {
                return true
            }

            if (toMove.contains(origin)) {
                return true
            }

            if (toMove.size >= MOVE_BLOCK_LIMIT) {
                return false
            }

            toMove.add(block)

            var count = 1
            val beStuck: MutableList<Block> = ArrayList()
            while (block!!.canSticksBlock()) {
                val oldBlock = block.clone()
                block = origin.getSide(moveDirection!!.getOpposite(), count)
                if ((!extending || !mainBlockLine) && block.canSticksBlock() && oldBlock.canSticksBlock() && (block.id != oldBlock.id)) {
                    break
                }

                if (block.isAir || !canPush(
                        block,
                        this.moveDirection, false, extending
                    ) || block == this.pistonPos
                ) {
                    break
                }

                if (block.breaksWhenMoved() && block.sticksToPiston()) {
                    toDestroy.add(block)
                    break
                }

                if (count + toMove.size > MOVE_BLOCK_LIMIT) {
                    return false
                }

                count++
                beStuck.add(block)
            }

            var beStuckCount = beStuck.size
            if (beStuckCount > 0) {
                toMove.addAll(beStuck.reversed())
            }

            var step = 1
            while (true) {
                val nextBlock = origin.getSide(moveDirection!!, step)
                val index = toMove.indexOf(nextBlock)
                if (index > -1) {
                    this.reorderListAtCollision(beStuckCount, index)
                    for (i in 0..index + beStuckCount) {
                        val b = toMove[i]
                        if ((b.canSticksBlock()) && !this.addBranchingBlocks(b)) {
                            return false
                        }
                    }
                    return true
                }

                if (nextBlock.isAir || nextBlock == armPos) {
                    return true
                }

                if (!canPush(
                        nextBlock,
                        this.moveDirection, true, extending
                    ) || nextBlock == this.pistonPos
                ) {
                    return false
                }

                if (nextBlock.breaksWhenMoved()) {
                    toDestroy.add(nextBlock)
                    return true
                }

                if (toMove.size >= MOVE_BLOCK_LIMIT) {
                    return false
                }

                toMove.add(nextBlock)
                ++beStuckCount
                ++step
            }
        }

        private fun reorderListAtCollision(count: Int, index: Int) {
            val list: List<Block> = ArrayList(toMove.subList(0, index))
            val list1: List<Block> = ArrayList(
                toMove.subList(
                    toMove.size - count, toMove.size
                )
            )
            val list2: List<Block> = ArrayList(
                toMove.subList(
                    index,
                    toMove.size - count
                )
            )
            toMove.clear()
            toMove.addAll(list)
            toMove.addAll(list1)
            toMove.addAll(list2)
        }

        protected fun addBranchingBlocks(block: Block): Boolean {
            for (face in BlockFace.entries) {
                if (face.axis != moveDirection!!.axis && !this.addBlockLine(
                        block.getSide(face),
                        block,
                        false
                    )
                ) return false
            }
            return true
        }

        val blocksToMove: List<Block>
            get() = this.toMove

        val blocksToDestroy: List<Block>
            get() = this.toDestroy
    }

    companion object {
        /**
         * @return 指定方块是否能向指定方向推动<br></br>Whether the specified square can be pushed in the specified direction
         */
        fun canPush(block: Block, face: BlockFace?, destroyBlocks: Boolean, extending: Boolean): Boolean {
            val min = block.level.minHeight
            val max = block.level.maxHeight - 1
            if (block.y >= min && (face != BlockFace.DOWN || block.y != min.toDouble()) && block.y <= max && (face != BlockFace.UP || block.y != max.toDouble())
            ) {
                if (extending && !block.canBePushed() || !extending && !block.canBePulled()) return false
                if (block.breaksWhenMoved()) return destroyBlocks || block.sticksToPiston()
                val blockEntity = block.levelBlockEntity
                return blockEntity == null || blockEntity.isMovable
            }

            return false
        }

        private var MOVE_BLOCK_LIMIT = 12

        var moveBlockLimit: Int
            get() = MOVE_BLOCK_LIMIT
            set(moveBlockLimit) {
                require(moveBlockLimit >= 0) { "The move block limit must be greater than or equal to 0" }
                MOVE_BLOCK_LIMIT = moveBlockLimit
            }
    }
}
