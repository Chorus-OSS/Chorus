package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.blockentity.BlockEntityMusic
import org.chorus_oss.chorus.event.player.PlayerInteractEvent
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationType
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.network.protocol.LevelSoundEventPacket
import org.chorus_oss.chorus.utils.RedstoneComponent
import org.chorus_oss.protocol.types.BlockPos

class BlockNoteblock @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSolid(blockstate), RedstoneComponent, BlockEntityHolder<BlockEntityMusic> {
    override val name: String
        get() = "Note Block"

    override val toolType: Int
        get() = ItemTool.TYPE_AXE

    override fun getBlockEntityClass() = BlockEntityMusic::class.java

    override fun getBlockEntityType(): String {
        return BlockEntityID.MUSIC
    }

    override val hardness: Double
        get() = 0.8

    override val resistance: Double
        get() = 0.8

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
        if (player != null && player.isSneaking() || (up().isAir && blockFace == BlockFace.UP && item.isBlock() && item.getSafeBlockState()
                .toBlock() is BlockHead)
        ) {
            return false
        }
        this.increaseStrength()
        this.emitSound(player)
        return true
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
        return BlockEntityHolder.setBlockAndCreateEntity(this) != null
    }

    override fun onTouch(
        vector: Vector3,
        item: Item,
        face: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float,
        player: Player?,
        action: PlayerInteractEvent.Action
    ) {
        onUpdate(Level.BLOCK_UPDATE_TOUCH)
        if (player != null && action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK && player.isSurvival) {
            this.emitSound(player)
        }
    }

    val strength: Int
        get() = this.blockEntity?.pitch ?: 0

    fun increaseStrength() {
        getOrCreateBlockEntity().changePitch()
    }

    val instrument: Instrument
        get() = when (this.down()) {
            is BlockWool -> Instrument.GUITAR

            is BlockConcretePowder,
            is BlockSand,
            is BlockGravel,
            is BlockGlass,
            is BlockSeaLantern,
            is BlockBeacon -> Instrument.SNARE_DRUM

            is BlockStone,
            is BlockBlackstone,
            is BlockNetherrack,
            is BlockNylium,
            is BlockObsidian,
            is BlockQuartzBlock,
            is BlockSandstone,
            is BlockOre,
            is BlockBrickBlock,
            is BlockCoral,
            is BlockRespawnAnchor,
            is BlockBedrock,
            is BlockConcrete,
            is BlockStonecutter,
            is BlockFurnace,
            is BlockObserver,
            is BlockHardenedClay,
            is BlockPrismarine -> Instrument.BASS_DRUM

            is BlockGoldBlock -> Instrument.BELLS

            is BlockClay,
            is BlockHoneycombBlock,
            is BlockInfestedChiseledStoneBricks,
            is BlockInfestedMossyStoneBricks,
            is BlockInfestedStone,
            is BlockInfestedDeepslate,
            is BlockInfestedCrackedStoneBricks,
            is BlockInfestedStoneBricks -> Instrument.FLUTE

            is BlockPackedIce -> Instrument.CHIMES

            is BlockBoneBlock -> Instrument.XYLOPHONE

            is BlockIronBlock -> Instrument.IRON_XYLOPHONE

            is BlockSoulSand -> Instrument.COW_BELL

            is BlockPumpkin -> Instrument.DIDGERIDOO

            is BlockEmeraldBlock -> Instrument.BIT

            is BlockHayBlock -> Instrument.BANJO

            is BlockGlowstone -> Instrument.PLING

            else -> {
                val skull = up()
                if (skull is BlockHead) {
                    var meta = 0
                    if (skull.blockEntity != null) {
                        meta = skull.blockEntity!!.namedTag.getByte("SkullType").toInt()
                    }
                    when (meta) {
                        0, 3 -> Instrument.SKELETON
                        1 -> Instrument.WITHER_SKELETON
                        2 -> Instrument.ZOMBIE
                        4 -> Instrument.CREEPER
                        5 -> Instrument.ENDER_DRAGON
                        else -> Instrument.PIGLIN
                    }
                } else {
                    Instrument.HARP
                }
            }
        }

    @JvmOverloads
    fun emitSound(player: Player? = null) {
        level.vibrationManager.callVibrationEvent(
            VibrationEvent(
                player
                    ?: this, add(0.5, 0.5, 0.5).position, VibrationType.BLOCK_CHANGE
            )
        )

        val instrument = this.instrument

        level.addLevelSoundEvent(
            this.position,
            LevelSoundEventPacket.SOUND_NOTE,
            instrument.ordinal shl 8 or this.strength
        )

        val pk = org.chorus_oss.protocol.packets.BlockEventPacket(
            blockPosition = BlockPos(position),
            eventType = instrument.ordinal,
            eventValue = this.strength,
        )
        level.addChunkPacket(position.floorX shr 4, position.floorZ shr 4, pk)
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_REDSTONE) {
            // We can't use getOrCreateBlockEntity(), because the update method is called on block place,
            // before the "real" BlockEntity is set. That means, if we'd use the other method here,
            // it would create two BlockEntities.
            val music: BlockEntityMusic = blockEntity ?: return 0

            if (this.isGettingPower) {
                if (!music.isPowered) {
                    this.emitSound()
                }
                music.isPowered = true
            } else {
                music.isPowered = false
            }
        }
        return super.onUpdate(type)
    }

    enum class Instrument(sound: Sound) {
        HARP(Sound.NOTE_HARP),
        BASS_DRUM(Sound.NOTE_BD),
        SNARE_DRUM(Sound.NOTE_SNARE),
        CLICKS_AND_STICKS(Sound.NOTE_HAT),
        BASS(Sound.NOTE_BASS),
        BELLS(Sound.NOTE_BELL),
        FLUTE(Sound.NOTE_FLUTE),
        CHIMES(Sound.NOTE_CHIME),
        GUITAR(Sound.NOTE_GUITAR),
        XYLOPHONE(Sound.NOTE_XYLOPHONE),
        IRON_XYLOPHONE(Sound.NOTE_IRON_XYLOPHONE),
        COW_BELL(Sound.NOTE_COW_BELL),
        DIDGERIDOO(Sound.NOTE_DIDGERIDOO),
        BIT(Sound.NOTE_BIT),
        BANJO(Sound.NOTE_BANJO),
        PLING(Sound.NOTE_PLING),
        SKELETON(Sound.NOTE_SKELETON),
        WITHER_SKELETON(Sound.NOTE_WITHERSKELETON),
        ZOMBIE(Sound.NOTE_ZOMBIE),
        CREEPER(Sound.NOTE_CREEPER),
        ENDER_DRAGON(Sound.NOTE_ENDERDRAGON),
        PIGLIN(Sound.NOTE_PIGLIN);

        private val sound: Sound = sound

        fun getSound(): Sound {
            return sound
        }
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.NOTEBLOCK)
    }
}
