package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.blockentity.BlockEntity.Companion.getDefaultCompound
import org.chorus_oss.chorus.blockentity.BlockEntityBanner
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.CompassRoseDirection
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.IntTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.utils.DyeColor
import org.chorus_oss.chorus.utils.Faceable
import org.chorus_oss.chorus.utils.Loggable
import kotlin.math.floor


open class BlockStandingBanner @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockTransparent(blockstate), Faceable, BlockEntityHolder<BlockEntityBanner> {
    override fun getBlockEntityType(): String {
        return BlockEntityID.BANNER
    }

    override fun getBlockEntityClass() = BlockEntityBanner::class.java

    override val hardness: Double
        get() = 1.0

    override val resistance: Double
        get() = 5.0

    override val toolType: Int
        get() = ItemTool.TYPE_AXE

    override val name: String
        get() = "Banner"

    override fun recalculateBoundingBox(): AxisAlignedBB? {
        return null
    }

    override fun canPassThrough(): Boolean {
        return true
    }

    override val waterloggingLevel: Int
        get() = 1

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
        if (face == BlockFace.DOWN) {
            return false
        }

        val layer0 = level.getBlock(this.position, 0)
        val layer1 = level.getBlock(this.position, 1)

        if (face == BlockFace.UP) {
            val direction: CompassRoseDirection = CompassRoseDirection.from(
                floor((((player?.rotation?.yaw ?: 0.0) + 180) * 16 / 360) + 0.5).toInt() and 0x0f
            )
            this.direction = direction
            if (!level.setBlock(block.position, this, true)) {
                return false
            }
        } else {
            val wall = get(BlockID.WALL_BANNER) as BlockStandingBanner
            wall.blockFace = face
            if (!level.setBlock(block.position, wall, true)) {
                return false
            }
        }

        val nbt: CompoundTag = getDefaultCompound(this.position, BlockEntityID.BANNER)
            .putInt("Base", item!!.damage and 0xf)

        val type = item.getNamedTagEntry("Type")
        if (type is IntTag) {
            nbt.put("Type", type)
        }
        val patterns = item.getNamedTagEntry("Patterns")
        if (patterns is ListTag<*>) {
            nbt.put("Patterns", patterns)
        }

        try {
            createBlockEntity(nbt)
            return true
        } catch (e: Exception) {
            BlockStandingBanner.log.error(
                "Failed to create the block entity {} at {}",
                getBlockEntityType(),
                locator,
                e
            )
            level.setBlock(layer0.position, 0, layer0, true)
            level.setBlock(layer0.position, 1, layer1, true)
            return false
        }
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (down().id === BlockID.AIR) {
                level.useBreakOn(this.position)

                return Level.BLOCK_UPDATE_NORMAL
            }
        }

        return 0
    }

    override val itemId
        get() = ItemID.BANNER

    override fun toItem(): Item {
        val banner: BlockEntityBanner? = blockEntity
        val item = Item.get(ItemID.BANNER)
        if (banner != null) {
            item.damage = banner.baseColor and 0xf
            val type: Int = banner.namedTag.getInt("Type")
            if (type > 0) {
                item.setNamedTag(
                    (if (item.hasCompoundTag()) item.namedTag else CompoundTag())!!.putInt("Type", type)
                )
            }
            val patterns: ListTag<CompoundTag> = banner.namedTag.getList("Patterns", CompoundTag::class.java)
            if (patterns.size() > 0) {
                item.setNamedTag(
                    (if (item.hasCompoundTag()) item.namedTag else CompoundTag())!!.putList("Patterns", patterns)
                )
            }
        }
        return item
    }

    open var direction: CompassRoseDirection
        get() =
            CompassRoseDirection.from(getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.GROUND_SIGN_DIRECTION))
        set(direction) {
            setPropertyValue<Int, IntPropertyType>(
                CommonBlockProperties.GROUND_SIGN_DIRECTION,
                direction.index
            )
        }

    override var blockFace: BlockFace
        get() = direction.closestBlockFace
        set(face) {
            direction = face.compassRoseDirection!!
        }

    override fun breaksWhenMoved(): Boolean {
        return true
    }

    val dyeColor: DyeColor
        get() {
            if (this.level != null) {
                val blockEntity: BlockEntityBanner? = blockEntity

                if (blockEntity != null) {
                    return blockEntity.dyeColor
                }
            }

            return DyeColor.WHITE
        }

    override val isSolid: Boolean
        get() = false

    override val properties: BlockProperties
        get() = Companion.properties

    companion object : Loggable {
        val properties: BlockProperties =
            BlockProperties(BlockID.STANDING_BANNER, CommonBlockProperties.GROUND_SIGN_DIRECTION)
    }
}