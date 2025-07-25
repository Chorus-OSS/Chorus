package org.chorus_oss.chorus.block

import com.google.common.collect.Sets
import org.chorus_oss.chorus.AdventureSettings
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.CommonPropertyMap
import org.chorus_oss.chorus.block.property.type.BooleanPropertyType
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.event.block.BlockRedstoneEvent
import org.chorus_oss.chorus.event.block.DoorToggleEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationType
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.AxisDirection
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.utils.Faceable
import org.chorus_oss.chorus.utils.RedstoneComponent

open class BlockTrapdoor @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockTransparent(blockState), RedstoneComponent, Faceable {
    override val name: String
        get() = "Oak Trapdoor"

    override val hardness: Double
        get() = 3.0

    override val resistance: Double
        get() = 15.0

    override fun canBeActivated(): Boolean {
        return true
    }

    override val toolType: Int
        get() = ItemTool.TYPE_AXE

    override val waterloggingLevel: Int
        get() = 1

    private val relativeBoundingBox: AxisAlignedBB?
        get() = boundingBox2SpecialV[blockState.specialValue().toInt()]

    override var minX: Double
        get() = position.x + relativeBoundingBox!!.minX
        set(minX) {
            super.minX = minX
        }

    override var maxX: Double
        get() = position.x + relativeBoundingBox!!.maxX
        set(maxX) {
            super.maxX = maxX
        }

    override var minY: Double
        get() = position.y + relativeBoundingBox!!.minY
        set(minY) {
            super.minY = minY
        }

    override var maxY: Double
        get() = position.y + relativeBoundingBox!!.maxY
        set(maxY) {
            super.maxY = maxY
        }

    override var minZ: Double
        get() = position.z + relativeBoundingBox!!.minZ
        set(minZ) {
            super.minZ = minZ
        }

    override var maxZ: Double
        get() = position.z + relativeBoundingBox!!.maxZ
        set(maxZ) {
            super.maxZ = maxZ
        }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_REDSTONE && Server.instance.settings.levelSettings.enableRedstone) {
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
            return type
        }

        return 0
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
        blockFace = if (player == null) face else player.getDirection().getOpposite()
        isTop = if (face.axis.isHorizontal) fy > 0.5 else face != BlockFace.UP

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
        return this.setOpen(player!!, !this.isOpen)
    }

    fun setOpen(player: Player?, open: Boolean): Boolean {
        var player = player
        if (open == this.isOpen) {
            return false
        }

        val event = DoorToggleEvent(this, player)
        Server.instance.pluginManager.callEvent(event)

        if (event.cancelled) {
            return false
        }

        player = event.player

        setPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.OPEN_BIT, open)
        if (!level.setBlock(this.position, this, true, true)) return false

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
        if (isOpen) {
            playOpenSound()
        } else {
            playCloseSound()
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

    var isTop: Boolean
        get() = getPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.UPSIDE_DOWN_BIT)
        set(top) {
            setPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.UPSIDE_DOWN_BIT, top)
        }

    override var blockFace: BlockFace
        get() = CommonPropertyMap.EWSN_DIRECTION.inverse()[getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.DIRECTION)]!!
        set(face) {
            setPropertyValue<Int, IntPropertyType>(
                CommonBlockProperties.DIRECTION,
                CommonPropertyMap.EWSN_DIRECTION[face]!!
            )
        }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.TRAPDOOR,
            CommonBlockProperties.DIRECTION,
            CommonBlockProperties.OPEN_BIT,
            CommonBlockProperties.UPSIDE_DOWN_BIT
        )


        private const val THICKNESS = 0.1875

        // Contains a list of positions of trap doors, which have been opened by hand (by a player).
        // It is used to detect on redstone update, if the door should be closed if redstone is off on the update,
        // previously the door always closed, when placing an unpowered redstone at the door, this fixes it
        // and gives the vanilla behavior; no idea how to make this better :d
        private val manualOverrides: MutableSet<Locator> = Sets.newConcurrentHashSet()

        private val boundingBox2SpecialV = arrayOfNulls<AxisAlignedBB>(0x1 shl properties.specialValueBits.toInt())

        //<editor-fold desc="pre-computing the bounding boxes" defaultstate="collapsed">
        init {
            for (specialValue in boundingBox2SpecialV.indices) {
                val bb: AxisAlignedBB
                if (properties.getPropertyValue(
                        specialValue,
                        CommonBlockProperties.OPEN_BIT
                    )
                ) {
                    var face =
                        CommonPropertyMap.EWSN_DIRECTION.inverse()[properties.getPropertyValue<Int, IntPropertyType>(
                            specialValue,
                            CommonBlockProperties.DIRECTION
                        )]
                    face = face!!.getOpposite()
                    bb = if (face.axisDirection == AxisDirection.NEGATIVE) {
                        SimpleAxisAlignedBB(
                            0.0,
                            0.0,
                            0.0,
                            1 + face.xOffset - (THICKNESS * face.xOffset),
                            1.0,
                            1 + face.zOffset - (THICKNESS * face.zOffset)
                        )
                    } else {
                        SimpleAxisAlignedBB(
                            face.xOffset - (THICKNESS * face.xOffset),
                            0.0,
                            face.zOffset - (THICKNESS * face.zOffset),
                            1.0,
                            1.0,
                            1.0
                        )
                    }
                } else if (properties.getPropertyValue<Boolean, BooleanPropertyType>(
                        specialValue,
                        CommonBlockProperties.UPSIDE_DOWN_BIT
                    )
                ) {
                    bb = SimpleAxisAlignedBB(
                        0.0,
                        1 - THICKNESS,
                        0.0,
                        1.0,
                        1.0,
                        1.0
                    )
                } else {
                    bb = SimpleAxisAlignedBB(
                        0.0,
                        0.0,
                        0.0,
                        1.0,
                        0 + THICKNESS,
                        1.0
                    )
                }
                boundingBox2SpecialV[specialValue] = bb
            }
        }
    }
}
