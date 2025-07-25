package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.CommonPropertyMap
import org.chorus_oss.chorus.block.property.type.BooleanPropertyType
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.ParticleEffect
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.utils.ChorusRandom
import org.chorus_oss.chorus.utils.Faceable

class BlockSmallDripleafBlock @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockFlowable(blockstate), Faceable {
    override val name: String
        get() = "Small Dripleaf"

    override var blockFace: BlockFace
        get() = CommonPropertyMap.CARDINAL_BLOCKFACE[getPropertyValue(
            CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION
        )]!!
        set(face) {
            setPropertyValue(
                CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION,
                CommonPropertyMap.CARDINAL_BLOCKFACE.inverse()[face]!!
            )
        }

    var isUpperBlock: Boolean
        get() = this.getPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.UPPER_BLOCK_BIT)
        set(isUpperBlock) {
            this.setPropertyValue<Boolean, BooleanPropertyType>(
                CommonBlockProperties.UPPER_BLOCK_BIT,
                isUpperBlock
            )
        }

    override val waterloggingLevel: Int
        get() = 2

    override val toolType: Int
        get() = ItemTool.TYPE_SHEARS

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
        val dripleaf = BlockSmallDripleafBlock()
        val dripleafTop = BlockSmallDripleafBlock()
        dripleafTop.isUpperBlock = true
        val direction = if (player != null) player.getDirection().getOpposite() else BlockFace.SOUTH
        dripleaf.blockFace = direction
        dripleafTop.blockFace = direction
        if (canKeepAlive(block)) {
            level.setBlock(block.position, dripleaf, true, true)
            level.setBlock(block.getSide(BlockFace.UP).position, dripleafTop, true, true)
            return true
        }
        return false
    }

    override fun getDrops(item: Item): Array<Item> {
        return if (item.isShears) {
            arrayOf(toItem())
        } else {
            Item.EMPTY_ARRAY
        }
    }

    override fun onBreak(item: Item?): Boolean {
        level.setBlock(this.position, BlockAir(), true, true)
        if (item!!.isShears) level.dropItem(this.position, toItem())
        if (getSide(BlockFace.UP).id == BlockID.SMALL_DRIPLEAF_BLOCK) {
            level.getBlock(getSide(BlockFace.UP).position).onBreak(null)
        }
        if (getSide(BlockFace.DOWN).id == BlockID.SMALL_DRIPLEAF_BLOCK) {
            level.getBlock(getSide(BlockFace.DOWN).position).onBreak(null)
        }
        return true
    }

    override fun onUpdate(type: Int): Int {
        if (!canKeepAlive(this)) {
            level.setBlock(this.position, BlockAir(), true, true)
            level.dropItem(this.position, toItem())
        }
        return super.onUpdate(type)
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
        if (item.isFertilizer) {
            val random = ChorusRandom()
            val height: Int = random.nextInt(4) + 2

            val blockBigDripleafDown = BlockBigDripleaf()
            val blockBigDripleafHead = BlockBigDripleaf()
            val direction = if (player != null) player.getDirection().getOpposite() else BlockFace.SOUTH
            blockBigDripleafDown.blockFace = direction
            blockBigDripleafHead.blockFace = direction
            blockBigDripleafHead.isHead = true

            var buttom: Block? = this.clone()
            while (buttom!!.getSide(BlockFace.DOWN).id == BlockID.SMALL_DRIPLEAF_BLOCK) {
                buttom = buttom.getSide(BlockFace.DOWN)
            }

            for (i in 0..<height) {
                level.setBlock(buttom.position.add(0.0, i.toDouble(), 0.0), blockBigDripleafDown, true, true)
            }
            level.setBlock(buttom.position.add(0.0, height.toDouble(), 0.0), blockBigDripleafHead, true, true)

            level.addParticleEffect(position.add(0.5, 0.5, 0.5), ParticleEffect.CROP_GROWTH)
            item.count--
            return true
        }
        return false
    }

    fun canKeepAlive(pos: Locator): Boolean {
        val blockDown = level.getBlock(pos.getSide(BlockFace.DOWN).position)
        val blockHere = level.getBlock(pos.position, 1)
        val blockUp = level.getBlock(pos.getSide(BlockFace.UP).position)
        if (level.getBlock(blockDown.position) is BlockClay) {
            return true
        }
        if (level.getBlock(blockDown.position) is BlockSmallDripleafBlock && !(level.getBlock(
                blockDown.position
            ) as BlockSmallDripleafBlock).isUpperBlock
        ) {
            return true
        }
        if (blockHere is BlockFlowingWater && (blockUp is BlockAir || blockUp is BlockSmallDripleafBlock) && (blockDown is BlockGrassBlock || blockDown is BlockDirt || blockDown is BlockDirtWithRoots || blockDown is BlockMossBlock)) {
            return true
        }
        return false
    }

    override val isFertilizable: Boolean
        get() = true

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(
            BlockID.SMALL_DRIPLEAF_BLOCK,
            CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION,
            CommonBlockProperties.UPPER_BLOCK_BIT
        )
    }
}