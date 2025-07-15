package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.AdventureSettings
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.event.block.BlockRedstoneEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationType
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromIndex
import org.chorus_oss.chorus.network.protocol.LevelSoundEventPacket
import org.chorus_oss.chorus.utils.Faceable
import org.chorus_oss.chorus.utils.RedstoneComponent
import org.chorus_oss.chorus.utils.RedstoneComponent.Companion.updateAroundRedstone

abstract class BlockButton(meta: BlockState) : BlockFlowable(meta), RedstoneComponent, Faceable {
    override val resistance: Double
        get() = 2.5

    override val hardness: Double
        get() = 0.5

    override val waterloggingLevel: Int
        get() = 1

    override fun canBeFlowedInto(): Boolean {
        return false
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
        if (!BlockLever.isSupportValid(target!!, face)) {
            return false
        }

        blockFace = face
        level.setBlock(block.position, this, direct = true, update = true)
        return true
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
        if (player != null) {
            if (!player.adventureSettings.get(AdventureSettings.Type.DOORS_AND_SWITCHED)) return false
            val itemInHand = player.inventory.itemInHand
            if (player.isSneaking() && !(itemInHand.isTool || itemInHand.isNothing)) return false
        }
        if (this.isActivated) {
            return false
        }

        level.scheduleUpdate(this, 30)

        setActivated(true, player)
        level.setBlock(this.position, this, direct = true, update = false)
        level.addLevelSoundEvent(
            position.add(0.5, 0.5, 0.5), LevelSoundEventPacket.SOUND_POWER_ON,
            blockState.blockStateHash()
        )
        if (Server.instance.settings.levelSettings.enableRedstone) {
            Server.instance.pluginManager.callEvent(BlockRedstoneEvent(this, 0, 15))

            updateAroundRedstone()
            updateAroundRedstone(getSide(facing.getOpposite()), facing)
        }

        return true
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            val thisFace = facing
            val touchingFace = thisFace.getOpposite()
            val side = this.getSide(touchingFace)
            if (!BlockLever.isSupportValid(side, thisFace)) {
                level.useBreakOn(this.position, Item.get(ItemID.WOODEN_PICKAXE))
                return Level.BLOCK_UPDATE_NORMAL
            }
        } else if (type == Level.BLOCK_UPDATE_SCHEDULED) {
            if (this.isActivated) {
                isActivated = false
                level.setBlock(this.position, this, direct = true, update = false)
                level.addLevelSoundEvent(
                    position.add(0.5, 0.5, 0.5), LevelSoundEventPacket.SOUND_POWER_OFF,
                    blockState.blockStateHash()
                )

                if (Server.instance.settings.levelSettings.enableRedstone) {
                    Server.instance.pluginManager.callEvent(BlockRedstoneEvent(this, 15, 0))

                    updateAroundRedstone()
                    updateAroundRedstone(getSide(facing.getOpposite()), facing)
                }
            }
            return Level.BLOCK_UPDATE_SCHEDULED
        }

        return 0
    }

    var isActivated: Boolean
        get() = getPropertyValue(CommonBlockProperties.BUTTON_PRESSED_BIT)
        set(activated) {
            setActivated(activated, null)
        }

    fun setActivated(activated: Boolean, player: Player?) {
        setPropertyValue(CommonBlockProperties.BUTTON_PRESSED_BIT, activated)
        val pos = this.add(0.5, 0.5, 0.5)
        if (activated) {
            level.vibrationManager.callVibrationEvent(
                VibrationEvent(
                    player
                        ?: this, pos.position, VibrationType.BLOCK_ACTIVATE
                )
            )
        } else {
            level.vibrationManager.callVibrationEvent(
                VibrationEvent(
                    player
                        ?: this, pos.position, VibrationType.BLOCK_DEACTIVATE
                )
            )
        }
    }

    override val isPowerSource: Boolean
        get() = true

    override fun getWeakPower(face: BlockFace): Int {
        return if (isActivated) 15 else 0
    }

    override fun getStrongPower(side: BlockFace): Int {
        return if (!isActivated) 0 else (if (facing == side) 15 else 0)
    }

    val facing: BlockFace
        get() = fromIndex(getPropertyValue(CommonBlockProperties.FACING_DIRECTION))

    override fun onBreak(item: Item?): Boolean {
        if (isActivated) {
            Server.instance.pluginManager.callEvent(BlockRedstoneEvent(this, 15, 0))
        }

        return super.onBreak(item)
    }

    override var blockFace: BlockFace
        get() = facing
        set(face) {
            setPropertyValue(CommonBlockProperties.FACING_DIRECTION, face.index)
        }
}
