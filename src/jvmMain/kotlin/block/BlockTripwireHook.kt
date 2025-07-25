package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.BooleanPropertyType
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.event.block.BlockRedstoneEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemBlock
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationType
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromHorizontalIndex
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.RedstoneComponent
import org.chorus_oss.chorus.utils.RedstoneComponent.Companion.updateAroundRedstone


class BlockTripwireHook @JvmOverloads constructor(state: BlockState = properties.defaultState) :
    BlockTransparent(state), RedstoneComponent {
    override val name: String
        get() = "Tripwire Hook"

    override fun onUpdate(type: Int): Int {
        when (type) {
            Level.BLOCK_UPDATE_NORMAL -> {
                val supportBlock = this.getSide(facing.getOpposite())
                if (!supportBlock.isNormalBlock && supportBlock !is BlockGlass) {
                    level.useBreakOn(this.position)
                }
                return type
            }

            Level.BLOCK_UPDATE_SCHEDULED -> {
                this.updateLine(false, true)
                return type
            }
        }
        return 0
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
        val supportBlock = this.getSide(face.getOpposite())
        if (face == BlockFace.DOWN || face == BlockFace.UP ||
            (!supportBlock.isNormalBlock && supportBlock !is BlockGlass)
        ) {
            return false
        }

        if (face.axis.isHorizontal) {
            this.setFace(face)
        }

        level.setBlock(this.position, this)

        if (player != null) {
            this.updateLine(false, false)
        }
        return true
    }

    override fun onBreak(item: Item?): Boolean {
        super.onBreak(item)
        val attached = isAttached
        val powered = isPowered

        if (attached || powered) {
            this.updateLine(true, false)
        }

        if (powered) {
            updateAroundRedstone()
            updateAroundRedstone(getSide(facing.getOpposite()))
        }

        return true
    }

    @JvmOverloads
    fun updateLine(
        isHookBroken: Boolean, doUpdateAroundHook: Boolean,
        eventDistance: Int = -1, eventBlock: BlockTripWire? = null
    ) {
        if (!Server.instance.settings.levelSettings.enableRedstone) {
            return
        }

        val facing = this.facing
        val locator = this.locator
        val wasConnected = this.isAttached
        val wasPowered = this.isPowered

        var isConnected = !isHookBroken
        var isPowered = false

        var pairedHookDistance = -1
        val line = arrayOfNulls<BlockTripWire>(MAX_TRIPWIRE_CIRCUIT_LENGTH)
        //Skip the starting hook in potential circuit
        for (steps in 1..<MAX_TRIPWIRE_CIRCUIT_LENGTH) {
            var b = level.getBlock(locator.getSide(facing, steps).position)

            if (b is BlockTripwireHook) {
                if (b.facing == facing.getOpposite()) {
                    pairedHookDistance = steps
                }
                break
            }

            if (steps == eventDistance && eventBlock != null) {
                b = eventBlock
            }

            if (b !is BlockTripWire) {
                line[steps] = null
                isConnected = false
                continue
            }

            val notDisarmed = !b.isDisarmed
            isPowered = isPowered or (notDisarmed && b.isPowered)

            if (steps == eventDistance) {
                level.scheduleUpdate(this, 10)
                isConnected = isConnected and notDisarmed
            }
            line[steps] = b
        }

        val foundPairedHook = pairedHookDistance > 1
        isConnected = isConnected and foundPairedHook
        isPowered = isPowered and isConnected

        val updatedHook = get(BlockID.TRIPWIRE_HOOK) as BlockTripwireHook
        updatedHook.setLevel(this.level)
        updatedHook.isAttached = isConnected
        updatedHook.isPowered = isPowered

        if (foundPairedHook) {
            val pairedPos = locator.getSide(facing, pairedHookDistance)
            val pairedFace = facing.getOpposite()
            updatedHook.setFace(pairedFace)
            level.setBlock(pairedPos.position, updatedHook, true, true)
            updateAroundRedstone(pairedPos)
            updateAroundRedstone(pairedPos.getSide(pairedFace.getOpposite()))
            this.addSound(pairedPos.position, isConnected, isPowered, wasConnected, wasPowered)
        }

        this.addSound(locator.position, isConnected, isPowered, wasConnected, wasPowered)

        if (!isHookBroken) {
            updatedHook.setFace(facing)
            level.setBlock(locator.position, updatedHook, true, true)

            if (doUpdateAroundHook) {
                updateAroundRedstone()
                updateAroundRedstone(locator.getSide(facing.getOpposite()))
            }
        }

        if (wasConnected == isConnected) {
            return
        }
        for (steps in 1..<pairedHookDistance) {
            val wire = line[steps] ?: continue
            val vc = locator.position.getSide(facing, steps)
            wire.isAttached = isConnected
            level.setBlock(vc, wire, true, true)
        }
    }

    private fun addSound(pos: Vector3, canConnect: Boolean, nextPowered: Boolean, attached: Boolean, powered: Boolean) {
        if (nextPowered && !powered) {
            level.addLevelSoundEvent(
                pos,
                org.chorus_oss.protocol.packets.LevelSoundEventPacket.Companion.SoundType.PowerOn
            )
            Server.instance.pluginManager.callEvent(BlockRedstoneEvent(this, 0, 15))
        } else if (!nextPowered && powered) {
            level.addLevelSoundEvent(
                pos,
                org.chorus_oss.protocol.packets.LevelSoundEventPacket.Companion.SoundType.PowerOff
            )
            Server.instance.pluginManager.callEvent(BlockRedstoneEvent(this, 15, 0))
        } else if (canConnect && !attached) {
            level.addLevelSoundEvent(
                pos,
                org.chorus_oss.protocol.packets.LevelSoundEventPacket.Companion.SoundType.Attach
            )
        } else if (!canConnect && attached) {
            level.addLevelSoundEvent(
                pos,
                org.chorus_oss.protocol.packets.LevelSoundEventPacket.Companion.SoundType.Detach
            )
        }
    }

    val facing: BlockFace
        get() = fromHorizontalIndex(this.direction)

    val direction: Int
        get() = this.getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.DIRECTION)

    var isAttached: Boolean = false
        get() = this.getPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.ATTACHED_BIT)
        set(isAttached) {
            if (field == isAttached) {
                return
            }
            this.setPropertyValue<Boolean, BooleanPropertyType>(
                CommonBlockProperties.ATTACHED_BIT,
                isAttached
            )
            val pos = this.add(0.5, 0.5, 0.5)
            val vibrationType =
                if (isAttached) VibrationType.BLOCK_ATTACH else VibrationType.BLOCK_DETACH
            level.vibrationManager.callVibrationEvent(
                VibrationEvent(
                    this,
                    pos.position,
                    vibrationType
                )
            )
        }

    var isPowered: Boolean = false
        get() = this.getPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.POWERED_BIT)
        set(isPowered) {
            if (field == isPowered) {
                return
            }
            this.setPropertyValue<Boolean, BooleanPropertyType>(
                CommonBlockProperties.POWERED_BIT,
                isPowered
            )
            val pos = this.add(0.5, 0.5, 0.5)
            val vibrationType =
                if (isPowered) VibrationType.BLOCK_ACTIVATE else VibrationType.BLOCK_DEACTIVATE
            level.vibrationManager.callVibrationEvent(
                VibrationEvent(
                    this,
                    pos.position,
                    vibrationType
                )
            )
        }

    fun setFace(face: BlockFace) {
        val direction = face.horizontalIndex
        if (this.direction == direction) {
            return
        }
        this.setPropertyValue<Int, IntPropertyType>(CommonBlockProperties.DIRECTION, direction)
    }

    override val isPowerSource: Boolean
        get() = true

    override fun getWeakPower(face: BlockFace): Int {
        return if (isPowered) 15 else 0
    }

    override fun getStrongPower(side: BlockFace): Int {
        return if (!isPowered) 0 else if (facing == side) 15 else 0
    }

    override val waterloggingLevel: Int
        get() = 2

    override fun canBeFlowedInto(): Boolean {
        return false
    }

    override fun toItem(): Item {
        return ItemBlock(this.blockState, name, 0)
    }

    override val isSolid: Boolean
        get() = false

    override fun isSolid(side: BlockFace): Boolean {
        return false
    }

    override fun canPassThrough(): Boolean {
        return false
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        /** Includes 40 tripwire and both tripwire hooks  */
        const val MAX_TRIPWIRE_CIRCUIT_LENGTH: Int = 42

        val properties: BlockProperties = BlockProperties(
            BlockID.TRIPWIRE_HOOK,
            CommonBlockProperties.DIRECTION, CommonBlockProperties.ATTACHED_BIT, CommonBlockProperties.POWERED_BIT
        )
    }
}
