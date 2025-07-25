package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.BooleanPropertyType
import org.chorus_oss.chorus.entity.item.EntityMinecartAbstract
import org.chorus_oss.chorus.inventory.ContainerInventory.Companion.calculateRedstone
import org.chorus_oss.chorus.inventory.InventoryHolder
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.utils.RedstoneComponent
import org.chorus_oss.chorus.utils.RedstoneComponent.Companion.updateAroundRedstone

class BlockDetectorRail @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockRail(blockstate), RedstoneComponent {
    override val name: String
        get() = "Detector Rail"

    override val isPowerSource: Boolean
        get() = true

    override fun hasComparatorInputOverride(): Boolean {
        return true
    }

    override val comparatorInputOverride: Int
        get() {
            val mc = findMinecart()
            return if (mc is InventoryHolder) calculateRedstone(
                mc.inventory
            ) else 0
        }

    override fun getWeakPower(face: BlockFace): Int {
        return if (isActive()) 15 else 0
    }

    override fun getStrongPower(side: BlockFace): Int {
        return if (isActive()) (if (side == BlockFace.UP) 15 else 0) else 0
    }

    fun findMinecart(): EntityMinecartAbstract? {
        for (entity in level.getNearbyEntities(
            SimpleAxisAlignedBB(
                floorX + 0.2,
                floorY.toDouble(),
                floorZ + 0.2,
                floorX + 0.8,
                floorY + 0.8,
                floorZ + 0.8
            )
        )) {
            if (entity is EntityMinecartAbstract) return entity
        }
        return null
    }

    fun updateState(powered: Boolean) {
        val wasPowered = isActive()
        if (powered != wasPowered) {
            this.setIsActive(powered)
            updateAroundRedstone()
            updateAroundRedstone(getSide(BlockFace.DOWN))
        }
        if (powered) {
            //每20gt检查一遍
            level.scheduleUpdate(this, 20)
            //更新比较器输出
            level.updateComparatorOutputLevel(this.position)
        }
    }

    override fun isActive(): Boolean {
        return getPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.RAIL_DATA_BIT)
    }

    override fun setRailActive(active: Boolean) {
        setPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.RAIL_DATA_BIT, active)
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(
                BlockID.DETECTOR_RAIL,
                CommonBlockProperties.RAIL_DATA_BIT,
                CommonBlockProperties.RAIL_DIRECTION_6
            )
    }
}