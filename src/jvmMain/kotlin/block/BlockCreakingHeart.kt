package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.CreakingHeartState
import org.chorus_oss.chorus.blockentity.BlockEntityCreakingHeart
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.RedstoneComponent

class BlockCreakingHeart @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSolid(blockstate), RedstoneComponent, BlockEntityHolder<BlockEntityCreakingHeart> {
    override val hardness: Double
        get() = 10.0

    override val resistance: Double
        get() = 10.0

    override val toolType: Int
        get() = ItemTool.TYPE_AXE

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
        if (BlockEntityHolder.setBlockAndCreateEntity(
                this,
                direct = true,
                update = true
            ) != null
        ) {
            this.pillarAxis = face.axis
            testAxis()
            return true
        }
        return false
    }

    override fun onNeighborChange(side: BlockFace) {
        testAxis()
        super.onNeighborChange(side)
    }

    protected fun testAxis() {
        if (blockEntity?.linkedCreaking == null) {
            var state: CreakingHeartState = CreakingHeartState.DORMANT
            for (face in BlockFace.entries) {
                if (pillarAxis.test(face)) {
                    val block = getSide(face)
                    if (block is BlockPaleOakLog) {
                        if (block.pillarAxis != pillarAxis) state = CreakingHeartState.UPROOTED
                    } else state = CreakingHeartState.UPROOTED
                }
            }

            if (state != this.state) {
                setPropertyValue(
                    CommonBlockProperties.CREAKING_HEART_STATE,
                    state
                )
                level.setBlock(this.position, this)
            }
        }
    }

    val state: CreakingHeartState
        get() = getPropertyValue(
            CommonBlockProperties.CREAKING_HEART_STATE
        )

    val isActive: Boolean
        get() = getPropertyValue(
            CommonBlockProperties.CREAKING_HEART_STATE
        ) != CreakingHeartState.UPROOTED

    var pillarAxis: BlockFace.Axis
        get() = getPropertyValue(
            CommonBlockProperties.PILLAR_AXIS
        )
        set(axis) {
            setPropertyValue(
                CommonBlockProperties.PILLAR_AXIS,
                axis
            )
        }

    override val lightLevel: Int
        get() = if (isActive) 15 else 0

    override fun canBePushed(): Boolean {
        return false
    }

    override fun canBePulled(): Boolean {
        return false
    }

    override fun getBlockEntityClass(): Class<out BlockEntityCreakingHeart> {
        return BlockEntityCreakingHeart::class.java
    }

    override fun getBlockEntityType(): String {
        return BlockEntityID.CREAKING_HEART
    }

    override fun hasComparatorInputOverride(): Boolean {
        return true
    }

    override val comparatorInputOverride: Int
        get() {
            val entityCreakingHeart: BlockEntityCreakingHeart = getOrCreateBlockEntity()
            val creaking = entityCreakingHeart.linkedCreaking
            return if (creaking != null) {
                (15 - ((creaking.position.distance(this.position) / 32) * 15)).toInt()
            } else 0
        }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.CREAKING_HEART,
            CommonBlockProperties.NATURAL,
            CommonBlockProperties.CREAKING_HEART_STATE,
            CommonBlockProperties.PILLAR_AXIS
        )
    }
}