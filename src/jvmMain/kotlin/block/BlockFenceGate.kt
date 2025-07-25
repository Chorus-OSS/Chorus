package org.chorus_oss.chorus.block

import com.google.common.collect.Sets
import org.chorus_oss.chorus.AdventureSettings
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.MinecraftCardinalDirection
import org.chorus_oss.chorus.block.property.type.BooleanPropertyType
import org.chorus_oss.chorus.event.block.BlockRedstoneEvent
import org.chorus_oss.chorus.event.block.DoorToggleEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationType
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromHorizontalIndex
import org.chorus_oss.chorus.utils.Faceable
import org.chorus_oss.chorus.utils.RedstoneComponent

open class BlockFenceGate @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockTransparent(blockState), RedstoneComponent, Faceable {
    override val name: String
        get() = "Oak Fence Gate"

    override val hardness: Double
        get() = 2.0

    override val resistance: Double
        get() = 15.0

    override val waterloggingLevel: Int
        get() = 1

    override fun canBeActivated(): Boolean {
        return true
    }

    override val toolType: Int
        get() = ItemTool.TYPE_AXE

    private val offsetIndex: Int
        get() = when (blockFace) {
            BlockFace.SOUTH, BlockFace.NORTH -> 0
            else -> 1
        }

    override var minX: Double
        get() = position.x + offMinX[offsetIndex]
        set(minX) {
            super.minX = minX
        }

    override var minZ: Double
        get() = position.z + offMinZ[offsetIndex]
        set(minZ) {
            super.minZ = minZ
        }

    override var maxX: Double
        get() = position.x + offMaxX[offsetIndex]
        set(maxX) {
            super.maxX = maxX
        }

    override var maxZ: Double
        get() = position.z + offMaxZ[offsetIndex]
        set(maxZ) {
            super.maxZ = maxZ
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
        val direction = player!!.getDirection()
        blockFace = direction

        if (getSide(direction.rotateY()) is BlockWallBase
            || getSide(direction.rotateYCCW()) is BlockWallBase
        ) {
            isInWall = true
        }

        if (!level.setBlock(block.position, this, true, true)) {
            return false
        }

        if (Server.instance.settings.levelSettings.enableRedstone && !this.isOpen && this.isGettingPower) {
            this.setOpen(null, true)
        }

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
            val itemInHand = player.inventory.itemInHand
            if (player.isSneaking() && !(itemInHand.isTool || itemInHand.isNothing)) return false
        }
        return toggle(player)
    }

    fun toggle(player: Player?): Boolean {
        if (player != null) {
            if (!player.adventureSettings.get(AdventureSettings.Type.DOORS_AND_SWITCHED)) return false
        }
        return this.setOpen(player, !this.isOpen)
    }

    fun setOpen(player: Player?, open: Boolean): Boolean {
        var player = player
        if (open == this.isOpen) {
            return false
        }

        val event = DoorToggleEvent(this, player!!)
        Server.instance.pluginManager.callEvent(event)

        if (event.cancelled) {
            return false
        }

        player = event.player

        val direction: BlockFace

        val originDirection = blockFace

        if (player != null) {
            val yaw = player.rotation.yaw
            var rotation = (yaw - 90) % 360

            if (rotation < 0) {
                rotation += 360.0
            }

            direction = if (originDirection.axis == BlockFace.Axis.Z) {
                if (rotation >= 0 && rotation < 180) {
                    BlockFace.NORTH
                } else {
                    BlockFace.SOUTH
                }
            } else {
                if (rotation >= 90 && rotation < 270) {
                    BlockFace.EAST
                } else {
                    BlockFace.WEST
                }
            }
        } else {
            direction = if (originDirection.axis == BlockFace.Axis.Z) {
                BlockFace.SOUTH
            } else {
                BlockFace.WEST
            }
        }

        blockFace = direction
        setPropertyValue<Boolean, BooleanPropertyType>(
            CommonBlockProperties.OPEN_BIT, !getPropertyValue<Boolean, BooleanPropertyType>(
                CommonBlockProperties.OPEN_BIT
            )
        )
        level.setBlock(this.position, this, false, false)

        if (player != null) {
            this.manualOverride = this.isGettingPower || this.isOpen
        }

        playOpenCloseSound()

        val source = vector3.add(0.5, 0.5, 0.5)
        val vibrationEvent = if (open) VibrationEvent(
            player,
            source, VibrationType.BLOCK_OPEN
        ) else VibrationEvent(
            player,
            source, VibrationType.BLOCK_CLOSE
        )
        level.vibrationManager.callVibrationEvent(vibrationEvent)
        return true
    }

    fun playOpenCloseSound() {
        if (this.isOpen) {
            this.playOpenSound()
        } else {
            this.playCloseSound()
        }
    }

    open fun playOpenSound() {
        level.addSound(this.position, Sound.RANDOM_DOOR_OPEN)
    }

    open fun playCloseSound() {
        level.addSound(this.position, Sound.RANDOM_DOOR_CLOSE)
    }

    var isOpen: Boolean
        get() = getPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.OPEN_BIT)
        set(open) {
            setPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.OPEN_BIT, open)
        }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            val face = blockFace
            val touchingWall = getSide(face.rotateY()) is BlockWallBase || getSide(face.rotateYCCW()) is BlockWallBase
            if (touchingWall != isInWall) {
                this.isInWall = touchingWall
                level.setBlock(this.position, this, true)
                return type
            }
        } else if (type == Level.BLOCK_UPDATE_REDSTONE && Server.instance.settings.levelSettings.enableRedstone) {
            this.onRedstoneUpdate()
            return type
        }

        return 0
    }

    private fun onRedstoneUpdate() {
        if ((this.isOpen != this.isGettingPower) && !this.manualOverride) {
            if (this.isOpen != this.isGettingPower) {
                Server.instance.pluginManager.callEvent(
                    BlockRedstoneEvent(
                        this,
                        if (this.isOpen) 15 else 0,
                        if (this.isOpen) 0 else 15
                    )
                )

                this.setOpen(null, this.isGettingPower)
            }
        } else if (this.manualOverride && (this.isGettingPower == this.isOpen)) {
            this.manualOverride = false
        }
    }

    var manualOverride: Boolean
        get() = manualOverrides.contains(this.locator)
        set(value) {
            if (value) {
                manualOverrides.add(this.locator)
            } else {
                manualOverrides.remove(this.locator)
            }
        }

    override fun onBreak(item: Item?): Boolean {
        this.manualOverride = false
        return super.onBreak(item)
    }

    var isInWall: Boolean
        get() = getPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.IN_WALL_BIT)
        set(inWall) {
            setPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.IN_WALL_BIT, inWall)
        }

    override var blockFace: BlockFace
        get() = fromHorizontalIndex(
            getPropertyValue(
                CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION
            ).ordinal
        )
        set(face) {
            setPropertyValue(
                CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION,
                MinecraftCardinalDirection.VALUES[face.horizontalIndex]
            )
        }

    override val burnChance: Int
        get() = 5

    override val burnAbility: Int
        get() = 20

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.FENCE_GATE,
            CommonBlockProperties.IN_WALL_BIT,
            CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION,
            CommonBlockProperties.OPEN_BIT
        )


        // Contains a list of positions of fence gates, which have been opened by hand (by a player).
        // It is used to detect on redstone update, if the gate should be closed if redstone is off on the update,
        // previously the gate always closed, when placing an unpowered redstone at the gate, this fixes it
        // and gives the vanilla behavior; no idea how to make this better :d
        private val manualOverrides: MutableSet<Locator> = Sets.newConcurrentHashSet()

        private val offMinX = DoubleArray(2)
        private val offMinZ = DoubleArray(2)
        private val offMaxX = DoubleArray(2)
        private val offMaxZ = DoubleArray(2)

        init {
            offMinX[0] = 0.0
            offMinZ[0] = 0.375
            offMaxX[0] = 1.0
            offMaxZ[0] = 0.625

            offMinX[1] = 0.375
            offMinZ[1] = 0.0
            offMaxX[1] = 0.625
            offMaxZ[1] = 1.0
        }
    }
}
