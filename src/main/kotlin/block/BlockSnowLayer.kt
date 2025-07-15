package org.chorus_oss.chorus.block

import com.google.common.base.Preconditions
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.BooleanPropertyType
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityLiving
import org.chorus_oss.chorus.event.block.BlockFadeEvent
import org.chorus_oss.chorus.experimental.network.protocol.utils.FLAG_ALL_PRIORITY
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.Item.Companion.get
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.plugin.InternalPlugin
import org.chorus_oss.chorus.registry.Registries
import org.chorus_oss.chorus.tags.BiomeTags
import java.util.stream.Stream
import kotlin.math.min

class BlockSnowLayer @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockFallable(blockstate) {
    override val name: String
        get() = "Top Snow"

    var snowHeight: Int
        get() = getPropertyValue(CommonBlockProperties.HEIGHT)
        set(snowHeight) {
            setPropertyValue(CommonBlockProperties.HEIGHT, snowHeight)
        }

    var isCovered: Boolean
        get() = getPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.COVERED_BIT)
        set(covered) {
            setPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.COVERED_BIT, covered)
        }

    override var maxY: Double
        get() = position.y + (min(16.0, (snowHeight + 1).toDouble()) * 2) / 16.0
        set(maxY) {
            super.maxY = maxY
        }

    override fun recalculateBoundingBox(): AxisAlignedBB? {
        val snowHeight = snowHeight
        if (snowHeight < 3) {
            return null
        }
        if (snowHeight == 3 || snowHeight == CommonBlockProperties.HEIGHT.max) {
            return this
        }
        return SimpleAxisAlignedBB(
            position.x,
            position.y,
            position.z, position.x + 1, position.y + 8 / 16.0, position.z + 1
        )
    }

    override fun recalculateCollisionBoundingBox(): AxisAlignedBB {
        return this
    }

    override val hardness: Double
        get() = 0.2

    override val resistance: Double
        get() = 0.1

    override val toolType: Int
        get() = ItemTool.TYPE_SHOVEL

    override fun canBeReplaced(): Boolean {
        return snowHeight < CommonBlockProperties.HEIGHT.max
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
        val increment = Stream.of<Block>(target, block)
            .filter { b: Block -> b.id == BlockID.SNOW_LAYER }
            .map<BlockSnowLayer> { obj: Block? -> BlockSnowLayer::class.java.cast(obj) }
            .filter { b: BlockSnowLayer -> b.snowHeight < CommonBlockProperties.HEIGHT.max }
            .findFirst()

        if (increment.isPresent) {
            val other = increment.get()
            if (
                level.getCollidingEntities(
                    SimpleAxisAlignedBB(
                        other.position.x, other.position.y, other.position.z,
                        other.position.x + 1, other.position.y + 1, other.position.z + 1
                    )
                ).stream()
                    .anyMatch { e: Entity? -> e is EntityLiving }
            ) {
                return false
            }
            other.snowHeight = other.snowHeight + 1
            return level.setBlock(other.position, other, true)
        }

        val down = down()
        if (!down.isSolid) {
            return false
        }

        when (down.id) {
            BlockID.BARRIER, BlockID.STRUCTURE_VOID -> {
                return false
            }

            BlockID.GRASS_BLOCK -> isCovered = true
            BlockID.TALL_GRASS -> {
                if (!level.setBlock(this.position, 0, this, true)) {
                    return false
                }
                level.setBlock(block.position, 1, block, true, false)
                return true
            }

            else -> Unit
        }

        return level.setBlock(block.position, this, true)
    }

    override fun onBreak(item: Item?): Boolean {
        if (layer != 0) {
            return super.onBreak(item)
        }
        return level.setBlock(this.position, 0, getLevelBlockAtLayer(1), true, true)
    }

    override fun afterRemoval(newBlock: Block, update: Boolean) {
        if (layer != 0 || newBlock.id == id) {
            return
        }

        val layer1 = getLevelBlockAtLayer(1)
        if (layer1.id != BlockID.TALL_GRASS) {
            return
        }

        // Clear the layer1 block and do a small hack as workaround a vanilla client rendering bug
        var level = level
        level.setBlock(this.position, 0, layer1, true, false)
        level.setBlock(this.position, 1, get(BlockID.AIR), true, false)
        level.setBlock(this.position, 0, newBlock, true, false)
        level.scheduler.scheduleDelayedTask(InternalPlugin.INSTANCE, {
            val target =
                level.getChunkPlayers(position.chunkX, position.chunkZ).values.toTypedArray()
            val blocks = arrayOf<Vector3?>(this.position)
            level.sendBlocks(
                target,
                blocks,
                org.chorus_oss.protocol.packets.UpdateBlockPacket.FLAG_ALL_PRIORITY.toInt(),
                0,
                false
            )
            level.sendBlocks(
                target,
                blocks,
                org.chorus_oss.protocol.packets.UpdateBlockPacket.FLAG_ALL_PRIORITY.toInt(),
                1,
                false
            )
        }, 10)

        val target = level.getChunkPlayers(position.chunkX, position.chunkZ).values.toTypedArray()
        val blocks = arrayOf<Vector3?>(this.position)
        level.sendBlocks(
            target,
            blocks,
            org.chorus_oss.protocol.packets.UpdateBlockPacket.FLAG_ALL_PRIORITY.toInt(),
            0,
            false
        )
        level.sendBlocks(
            target,
            blocks,
            org.chorus_oss.protocol.packets.UpdateBlockPacket.FLAG_ALL_PRIORITY.toInt(),
            1,
            false
        )
    }

    override fun onUpdate(type: Int): Int {
        super.onUpdate(type)
        if (type == Level.BLOCK_UPDATE_RANDOM) {
            val biomeDefinition = Registries.BIOME.get(
                level.getBiomeId(
                    floorX,
                    position.floorY, floorZ
                )
            )!!
            if (biomeDefinition.tags.contains(BiomeTags.WARM) || level.getBlockLightAt(floorX, floorY, floorZ) >= 10) {
                melt()
                return Level.BLOCK_UPDATE_RANDOM
            }
        } else if (type == Level.BLOCK_UPDATE_NORMAL) {
            val covered = down().id == BlockID.GRASS_BLOCK
            if (isCovered != covered) {
                isCovered = covered
                level.setBlock(this.position, this, true)
                return type
            }
        }
        return 0
    }

    @JvmOverloads
    fun melt(layers: Int = 2): Boolean {
        Preconditions.checkArgument(layers > 0, "Layers must be positive, got {}", layers)
        var toMelt: Block = this
        while (toMelt.getPropertyValue(CommonBlockProperties.HEIGHT) === CommonBlockProperties.HEIGHT.max) {
            val up = toMelt.up()
            if (up.id != BlockID.SNOW_LAYER) {
                break
            }

            toMelt = up
        }

        val snowHeight = toMelt.getPropertyValue(CommonBlockProperties.HEIGHT) - layers
        val newState = if (snowHeight < 0) get(BlockID.AIR) else get(
            blockState.setPropertyValue(Companion.properties, CommonBlockProperties.HEIGHT, snowHeight)
        )
        val event = BlockFadeEvent(toMelt, newState)
        Server.instance.pluginManager.callEvent(event)
        if (event.cancelled) {
            return false
        }

        return level.setBlock(toMelt.position, event.newState, true)
    }

    override fun getDrops(item: Item): Array<Item> {
        if (!item.isShovel || item.tier < ItemTool.TIER_WOODEN) {
            return Item.EMPTY_ARRAY
        }

        val amount = when (snowHeight) {
            0, 1, 2 -> 1
            3, 4 -> 2
            5, 6 -> 3
            else -> 4
        }
        return arrayOf(get(ItemID.SNOWBALL, 0, amount))
    }

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val isTransparent: Boolean
        get() = true

    override fun canBeFlowedInto(): Boolean {
        return true
    }

    override fun breaksWhenMoved(): Boolean {
        return true
    }

    override fun sticksToPiston(): Boolean {
        return false
    }

    override fun canPassThrough(): Boolean {
        return snowHeight < 3
    }

    override fun isSolid(side: BlockFace): Boolean {
        return side == BlockFace.UP && snowHeight == CommonBlockProperties.HEIGHT.max
    }

    override fun toFallingItem(): Item {
        return Item.get(ItemID.SNOWBALL)
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.SNOW_LAYER, CommonBlockProperties.COVERED_BIT, CommonBlockProperties.HEIGHT)
    }
}
