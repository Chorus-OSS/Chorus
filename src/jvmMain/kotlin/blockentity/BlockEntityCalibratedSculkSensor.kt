package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.BlockCalibratedSculkSensor
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationListener
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import kotlin.math.floor
import kotlin.math.max

class BlockEntityCalibratedSculkSensor(chunk: IChunk, nbt: CompoundTag) : BlockEntity(chunk, nbt),
    VibrationListener {
    var lastActiveTime: Int = level.tick
        protected set
    var lastVibrationEvent: VibrationEvent? = null
        protected set

    var power: Int = 0
        protected set

    var comparatorPower: Int = 0
        protected set

    protected var waitForVibration: Boolean = false


    override fun initBlockEntity() {
        level.vibrationManager.addListener(this)
    }

    override fun onBreak(isSilkTouch: Boolean) {
        if (!isSilkTouch) {
            level.vibrationManager.removeListener(this)
        } else {
            calPower()
        }
    }

    override fun close() {
        level.vibrationManager.removeListener(this)
        super.close()
    }

    override val isBlockEntityValid: Boolean
        get() = levelBlock.id === BlockID.CALIBRATED_SCULK_SENSOR

    override val listenerVector = clone().position.floor().add(0.5, 0.5, 0.5)

    override fun onVibrationOccur(event: VibrationEvent): Boolean {
        if (this.isBlockEntityValid && Server.instance.settings.levelSettings.enableRedstone && (level.getBlock(event.source) !is BlockCalibratedSculkSensor)
        ) {
            val canBeActive = (level.tick - lastActiveTime) > 40 && !waitForVibration
            if (canBeActive) waitForVibration = true
            return canBeActive
        } else {
            return false
        }
    }

    override fun onVibrationArrive(event: VibrationEvent) {
        if (this.isBlockEntityValid && Server.instance.settings.levelSettings.enableRedstone
        ) {
            this.lastVibrationEvent = event
            this.updateLastActiveTime()
            waitForVibration = false

            calPower()

            val block = this.block as BlockCalibratedSculkSensor
            block.setPhase(1)
            block.updateAroundRedstone()
            level.scheduleUpdate(block, 41)
        }
    }

    override val listenRange = 8.0

    protected fun updateLastActiveTime() {
        this.lastActiveTime = level.tick
    }

    fun calPower() {
        val event = this.lastVibrationEvent
        if ((level.tick - this.lastActiveTime) >= 40 || event == null) {
            power = 0
            comparatorPower = 0
            return
        }
        comparatorPower = event.type.frequency
        power = max(
            1.0,
            (15 - floor(event.source.distance(position.add(0.5, 0.5, 0.5)) * 1.875).toInt()).toDouble()
        ).toInt()
    }
}

