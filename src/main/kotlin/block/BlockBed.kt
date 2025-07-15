package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.BooleanPropertyType
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.blockentity.BlockEntityBed
import org.chorus_oss.chorus.blockentity.BlockEntityID
import org.chorus_oss.chorus.entity.effect.EffectType
import org.chorus_oss.chorus.event.block.BlockExplosionPrimeEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemBed
import org.chorus_oss.chorus.lang.TranslationContainer
import org.chorus_oss.chorus.level.Explosion
import org.chorus_oss.chorus.level.GameRule
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromHorizontalIndex
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.network.protocol.types.SpawnPointType
import org.chorus_oss.chorus.utils.DyeColor
import org.chorus_oss.chorus.utils.Faceable
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.chorus.utils.TextFormat


class BlockBed @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockTransparent(blockstate), Faceable, BlockEntityHolder<BlockEntityBed>, Loggable {
    override fun getBlockEntityClass(): Class<out BlockEntityBed> {
        return BlockEntityBed::class.java
    }

    override fun getBlockEntityType(): String {
        return BlockEntityID.BED
    }

    override fun canBeActivated(): Boolean {
        return true
    }

    override val resistance: Double
        get() = 1.0

    override val hardness: Double
        get() = 0.2

    override val name: String
        get() = dyeColor.colorName + " Bed Block"

    override var maxY: Double
        get() = position.y + 0.5625
        set(maxY) {
            super.maxY = maxY
        }

    override val waterloggingLevel: Int
        get() = 1


    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        if (isNotActivate(player)) return false
        val dir = blockFace

        val shouldExplode = level.dimension != Level.DIMENSION_OVERWORLD
        val willExplode = shouldExplode && level.gameRules.getBoolean(GameRule.TNT_EXPLODES)

        val head: Block?
        if (isHeadPiece) {
            head = this
        } else {
            head = getSide(dir)
            if (head.id !== id || !(head as BlockBed).isHeadPiece || (head.blockFace != dir)) {
                if (player != null && !willExplode) {
                    player.sendMessage(TranslationContainer(TextFormat.GRAY.toString() + "%tile.bed.notValid"))
                }

                if (!shouldExplode) {
                    return true
                }
            }
        }

        val footPart = dir.getOpposite()

        if (shouldExplode) {
            if (!willExplode) {
                return true
            }

            val event = BlockExplosionPrimeEvent(this, player, 5.0)
            event.isIncendiary = true
            if (event.cancelled) {
                return true
            }

            level.setBlock(this.position, get(BlockID.AIR), direct = false, update = false)
            onBreak(Item.AIR)
            level.updateAround(this.position)

            val explosion = Explosion(this, event.force, this)
            explosion.fireChance = event.fireChance
            if (event.isBlockBreaking) {
                explosion.explodeA()
            }
            explosion.explodeB()
            return true
        }

        if (player == null || !player.hasEffect(EffectType.CONDUIT_POWER) && getLevelBlockAtLayer(1) is BlockFlowingWater) {
            return true
        }

        val accessArea = SimpleAxisAlignedBB(
            head.position.x - 2,
            head.position.y - 5.5,
            head.position.z - 2,
            head.position.x + 3,
            head.position.y + 2.5,
            head.position.z + 3
        )
            .addCoord(footPart.xOffset.toDouble(), 0.0, footPart.zOffset.toDouble())

        if (!accessArea.isVectorInside(player.position)) {
            player.sendMessage(TranslationContainer(TextFormat.GRAY.toString() + "%tile.bed.tooFar"))
            return true
        }

        val spawn = fromObject(
            position.add(0.5, 0.5, 0.5),
            player.level!!
        )
        if (player.spawn.first != spawn) {
            player.setSpawn(this, SpawnPointType.BLOCK)
        }
        player.sendMessage(TranslationContainer(TextFormat.GRAY.toString() + "%tile.bed.respawnSet"))

        val time = level.getTime() % Level.TIME_FULL

        val isNight = (time >= Level.TIME_NIGHT && time < Level.TIME_SUNRISE)

        if (!isNight) {
            player.sendMessage(TranslationContainer(TextFormat.GRAY.toString() + "%tile.bed.noSleep"))
            return true
        }

        if (!player.isCreative) {
            val checkMonsterArea = SimpleAxisAlignedBB(
                head.position.x - 8,
                head.position.y - 6.5,
                head.position.z - 8,
                head.position.x + 9,
                head.position.y + 5.5,
                head.position.z + 9
            )
                .addCoord(footPart.xOffset.toDouble(), 0.0, footPart.zOffset.toDouble())

            for (entity in level.getCollidingEntities(checkMonsterArea)) {
                if (!entity.isClosed() && entity.isPreventingSleep(player)) {
                    player.sendTranslation(TextFormat.GRAY.toString() + "%tile.bed.notSafe")
                    return true
                }
                // TODO: Check Chicken Jockey, Spider Jockey
            }
        }

        if (!player.sleepOn(head.position)) {
            player.sendMessage(TranslationContainer(TextFormat.GRAY.toString() + "%tile.bed.occupied"))
        }

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
        val down = this.down()
        if (!(BlockLever.isSupportValid(down, BlockFace.UP) || down is BlockCauldron)) {
            return false
        }

        val direction = if (player == null) BlockFace.NORTH else player.getDirection()
        val next = this.getSide(direction)
        val downNext = next.down()

        if (!next.canBeReplaced() || !(BlockLever.isSupportValid(
                downNext,
                BlockFace.UP
            ) || downNext is BlockCauldron)
        ) {
            return false
        }

        val thisLayer0 = level.getBlock(this.position, 0)
        val thisLayer1 = level.getBlock(this.position, 1)
        val nextLayer0 = level.getBlock(next.position, 0)
        val nextLayer1 = level.getBlock(next.position, 1)

        blockFace = direction

        level.setBlock(block.position, this, direct = true, update = true)
        if (next is BlockLiquid && next.usesWaterLogging()) {
            level.setBlock(next.position, 1, next, direct = true, update = false)
        }

        val head = clone() as BlockBed
        head.isHeadPiece = true
        level.setBlock(next.position, head, direct = true, update = true)

        var thisBed: BlockEntityBed? = null
        try {
            thisBed = createBlockEntity(CompoundTag().putByte("color", item!!.damage))
            val nextBlock = next.levelBlock as BlockEntityHolder<*>?
            nextBlock!!.createBlockEntity(CompoundTag().putByte("color", item.damage))
        } catch (e: Exception) {
            log.warn(
                "Failed to create the block entity {} at {} and {}",
                getBlockEntityType(), locator, next.locator, e
            )
            thisBed?.close()
            level.setBlock(thisLayer0.position, 0, thisLayer0, true)
            level.setBlock(thisLayer1.position, 1, thisLayer1, true)
            level.setBlock(nextLayer0.position, 0, nextLayer0, true)
            level.setBlock(nextLayer1.position, 1, nextLayer1, true)
            return false
        }
        return true
    }

    override fun onBreak(item: Item?): Boolean {
        val direction = blockFace
        if (isHeadPiece) { //This is the Top part of bed
            val other = getSide(direction.getOpposite())
            if (other.id == id && !(other as BlockBed).isHeadPiece && direction == other.blockFace) {
                level.setBlock(other.position, get(BlockID.AIR), direct = true, update = true)
            }
        } else { //Bottom Part of Bed
            val other = getSide(direction)
            if (other.id == id && (other as BlockBed).isHeadPiece && direction == other.blockFace) {
                level.setBlock(other.position, get(BlockID.AIR), direct = true, update = true)
            }
        }

        level.setBlock(
            this.position,
            get(BlockID.AIR),
            direct = true,
            update = false
        ) // Do not update both parts to prevent duplication bug if there is two fallable blocks top of the bed

        return true
    }

    override fun toItem(): Item {
        return ItemBed(dyeColor.woolData)
    }

    val dyeColor: DyeColor
        get() {
            val blockEntity = blockEntity
            if (blockEntity != null) {
                return blockEntity.dyeColor
            }

            return DyeColor.WHITE
        }

    override var blockFace: BlockFace
        get() = fromHorizontalIndex(getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.DIRECTION))
        set(face) {
            setPropertyValue<Int, IntPropertyType>(
                CommonBlockProperties.DIRECTION,
                face.horizontalIndex
            )
        }

    var isHeadPiece: Boolean
        get() = getPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.HEAD_PIECE_BIT)
        set(headPiece) {
            setPropertyValue<Boolean, BooleanPropertyType>(
                CommonBlockProperties.HEAD_PIECE_BIT,
                headPiece
            )
        }

    var isOccupied: Boolean
        get() = getPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.OCCUPIED_BIT)
        set(occupied) {
            setPropertyValue<Boolean, BooleanPropertyType>(
                CommonBlockProperties.OCCUPIED_BIT,
                occupied
            )
        }

    override fun breaksWhenMoved(): Boolean {
        return true
    }

    override fun sticksToPiston(): Boolean {
        return false
    }

    val isBedValid: Boolean
        get() {
            val dir = blockFace
            val head: Block?
            val foot: Block?
            if (isHeadPiece) {
                head = this
                foot = getSide(dir.getOpposite())
            } else {
                head = getSide(dir)
                foot = this
            }

            return head.id == foot.id
                    && (head as BlockBed).isHeadPiece && head.blockFace == dir
                    && !(foot as BlockBed).isHeadPiece && foot.blockFace == dir
        }

    val headPart: BlockBed?
        get() {
            if (isHeadPiece) {
                return this
            }
            val dir = blockFace
            val head = getSide(dir)
            if (head.id == id && (head as BlockBed).isHeadPiece && head.blockFace == dir) {
                return head
            }
            return null
        }

    val footPart: BlockBed?
        get() {
            if (!isHeadPiece) {
                return this
            }

            val dir = blockFace
            val foot = getSide(dir.getOpposite())
            if (foot.id == id && !(foot as BlockBed).isHeadPiece && foot.blockFace == dir) {
                return foot
            }
            return null
        }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.BED,
            CommonBlockProperties.DIRECTION,
            CommonBlockProperties.HEAD_PIECE_BIT,
            CommonBlockProperties.OCCUPIED_BIT
        )

    }
}
