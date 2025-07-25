package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.blockentity.BlockEntityTarget
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.projectile.EntitySmallFireball
import org.chorus_oss.chorus.entity.projectile.abstract_arrow.EntityArrow
import org.chorus_oss.chorus.entity.projectile.abstract_arrow.EntityThrownTrident
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.RedstoneComponent
import java.util.*
import kotlin.math.ceil

class BlockTarget @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockTransparent(blockState), RedstoneComponent, BlockEntityHolder<BlockEntityTarget> {
    override val name: String
        get() = "Target"

    override fun getBlockEntityClass() = BlockEntityTarget::class.java

    override fun getBlockEntityType(): String {
        return BlockEntityID.TARGET
    }

    override val isPowerSource: Boolean
        get() = true

    override fun getWeakPower(face: BlockFace): Int {
        val target = blockEntity
        return target?.activePower ?: 0
    }

    @JvmOverloads
    fun activatePower(power: Int, ticks: Int = 4 * 2): Boolean {
        var level = level
        if (power <= 0 || ticks <= 0) {
            return deactivatePower()
        }

        if (!Server.instance.settings.levelSettings.enableRedstone) {
            return false
        }

        val target = getOrCreateBlockEntity()
        val previous = target.activePower
        level.cancelSheduledUpdate(this.position, this)
        level.scheduleUpdate(this, ticks)
        target.activePower = power
        if (previous != power) {
            updateAroundRedstone()
        }
        return true
    }

    fun deactivatePower(): Boolean {
        val target = blockEntity
        if (target != null) {
            val currentPower = target.activePower
            target.activePower = 0
            target.close()
            if (currentPower != 0 && Server.instance.settings.levelSettings.enableRedstone) {
                updateAroundRedstone()
            }
            return true
        }
        return false
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_SCHEDULED) {
            deactivatePower()
            return type
        }
        return 0
    }

    override fun onProjectileHit(projectile: Entity, locator: Locator, motion: Vector3): Boolean {
        var ticks = 8
        if (projectile is EntityArrow || projectile is EntityThrownTrident || projectile is EntitySmallFireball) {
            ticks = 20
        }

        val intercept = calculateIntercept(
            locator.position,
            locator.position.add(motion.multiply(2.0))
        )
        if (intercept == null) {
            return false
        }

        val faceHit: BlockFace = intercept.faceHit ?: return false

        val hitVector = intercept.hitVector!!.subtract(
            position.x * 2,
            position.y * 2, position.z * 2
        )
        val axes: MutableList<BlockFace.Axis?> = ArrayList(Arrays.asList(*BlockFace.Axis.entries.toTypedArray()))
        axes.remove(faceHit.axis)

        val coords = doubleArrayOf(hitVector.getAxis(axes[0]!!), hitVector.getAxis(axes[1]!!))

        for (i in 0..1) {
            if (coords[i] == 0.5) {
                coords[i] = 1.0
            } else if (coords[i] <= 0 || coords[i] >= 1) {
                coords[i] = 0.0
            } else if (coords[i] < 0.5) {
                coords[i] *= 2.0
            } else {
                coords[i] = (coords[i] / (-0.5)) + 2
            }
        }

        val scale = (coords[0] + coords[1]) / 2
        activatePower(ceil(16 * scale).toInt(), ticks)
        return true
    }

    override val toolType: Int
        get() = ItemTool.TYPE_HOE

    override val hardness: Double
        get() = 0.5

    override val resistance: Double
        get() = 0.5

    override val burnAbility: Int
        get() = 15

    override val burnChance: Int
        get() = 0

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.TARGET)
    }
}
