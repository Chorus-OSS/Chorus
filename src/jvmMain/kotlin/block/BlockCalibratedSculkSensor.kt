package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.CommonPropertyMap
import org.chorus_oss.chorus.blockentity.BlockEntityCalibratedSculkSensor
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromHorizontalIndex
import org.chorus_oss.chorus.utils.RedstoneComponent

class BlockCalibratedSculkSensor @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockFlowable(blockstate), BlockEntityHolder<BlockEntityCalibratedSculkSensor>, RedstoneComponent {
    override val name: String
        get() = "Calibrated Sculk Sensor"

    override fun getBlockEntityClass(): Class<out BlockEntityCalibratedSculkSensor> {
        return BlockEntityCalibratedSculkSensor::class.java
    }

    override val isPowerSource: Boolean
        get() = true

    override fun getBlockEntityType(): String {
        return BlockEntityID.CALIBRATED_SCULK_SENSOR
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
        blockFace =
            if (player != null) fromHorizontalIndex(player.getDirection().horizontalIndex) else BlockFace.SOUTH

        level.setBlock(block.position, this, direct = true, update = true)
        return true
    }

    var blockFace: BlockFace?
        get() = CommonPropertyMap.CARDINAL_BLOCKFACE[getPropertyValue(
            CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION
        )]
        set(face) {
            val horizontalIndex = face!!.horizontalIndex
            if (horizontalIndex > -1) {
                this.setPropertyValue(
                    CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION,
                    CommonPropertyMap.CARDINAL_BLOCKFACE.inverse()[fromHorizontalIndex(horizontalIndex)]!!
                )
            }
        }

    override fun getWeakPower(face: BlockFace): Int {
        val blockEntity = this.getOrCreateBlockEntity()
        return if (getSide(face.getOpposite()) is BlockRedstoneComparator) {
            blockEntity.comparatorPower
        } else {
            blockEntity.power
        }
    }

    override fun onUpdate(type: Int): Int {
        getOrCreateBlockEntity()
        if (type == Level.BLOCK_UPDATE_SCHEDULED) {
            if (Server.instance.settings.levelSettings.enableRedstone) {
                this.blockEntity!!.calPower()
                this.setPhase(0)
                updateAroundRedstone()
            }
            return type
        }
        return 0
    }

    fun setPhase(phase: Int) {
        if (phase == 1) level.addSound(position.add(0.5, 0.5, 0.5), Sound.POWER_ON_SCULK_SENSOR)
        else level.addSound(position.add(0.5, 0.5, 0.5), Sound.POWER_OFF_SCULK_SENSOR)
        this.setPropertyValue(CommonBlockProperties.SCULK_SENSOR_PHASE, phase)
        level.setBlock(this.position, this, direct = true, update = false)
    }

    override fun isSolid(side: BlockFace): Boolean {
        return false
    }

    override fun canPassThrough(): Boolean {
        return false
    }

    override fun breaksWhenMoved(): Boolean {
        return false
    }

    override fun canBeFlowedInto(): Boolean {
        return false
    }

    override fun recalculateBoundingBox(): AxisAlignedBB {
        return this
    }

    override val waterloggingLevel: Int
        get() = 1

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.CALIBRATED_SCULK_SENSOR,
            CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION,
            CommonBlockProperties.SCULK_SENSOR_PHASE
        )
    }
}