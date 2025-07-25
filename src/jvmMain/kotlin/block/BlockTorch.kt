package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.BlockLever.Companion.isSupportValid
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.TorchFacingDirection
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.utils.Faceable

open class BlockTorch @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockFlowable(blockstate), Faceable {
    override val name: String
        get() = "Torch"

    override val lightLevel: Int
        get() = 14

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            val torchAttachment = torchAttachment

            val support = this.getSide(torchAttachment.attachedFace)
            if (!isSupportValid(support, torchAttachment.torchDirection!!)) {
                level.useBreakOn(this.position)

                return Level.BLOCK_UPDATE_NORMAL
            }
        }

        return 0
    }

    private fun findValidSupport(): BlockFace? {
        for (horizontalFace in BlockFace.Plane.HORIZONTAL_FACES) {
            if (isSupportValid(getSide(horizontalFace.getOpposite()), horizontalFace)) {
                return horizontalFace
            }
        }
        if (isSupportValid(down(), BlockFace.UP)) {
            return BlockFace.UP
        }
        return null
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
        var target = target!!
        var face = face
        if (target.canBeReplaced()) {
            target = target.down()
            face = BlockFace.UP
        }

        if (face == BlockFace.DOWN || !isSupportValid(target, face)) {
            val valid = findValidSupport() ?: return false
            face = valid
        }

        this.blockFace = face
        level.setBlock(block.position, this, true, true)
        return true
    }

    override var blockFace: BlockFace
        get() = torchAttachment.torchDirection!!
        /**
         * Sets the direction that the flame is pointing.
         */
        set(face) {
            val torchAttachment = TorchFacingDirection.getByTorchDirection(face)
            this.torchAttachment = torchAttachment
        }

    var torchAttachment: TorchFacingDirection
        get() = getPropertyValue(CommonBlockProperties.TORCH_FACING_DIRECTION)
        set(face) {
            setPropertyValue(
                CommonBlockProperties.TORCH_FACING_DIRECTION,
                face
            )
        }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.TORCH, CommonBlockProperties.TORCH_FACING_DIRECTION)
    }
}
