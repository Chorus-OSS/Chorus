package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.BlockFlowerPot.FlowerPotBlock
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemStick
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.tags.BlockTags
import java.util.*

class BlockDeadbush @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockFlowable(blockState), FlowerPotBlock {
    override val name: String
        get() = "Dead Bush"

    override val waterloggingLevel: Int
        get() = 1

    override fun canBeReplaced(): Boolean {
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
        if (isSupportValid) {
            level.setBlock(block.position, this, true, true)
            return true
        }
        return false
    }

    private val isSupportValid: Boolean
        get() {
            val down = down()
            if (down is BlockHardenedClay) return true
            return down.`is`(BlockTags.DIRT) || down.`is`(BlockTags.SAND)
        }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (!isSupportValid) {
                level.useBreakOn(this.position)

                return Level.BLOCK_UPDATE_NORMAL
            }
        }
        return 0
    }

    override fun getDrops(item: Item): Array<Item> {
        return if (item.isShears) {
            arrayOf(
                toItem()
            )
        } else {
            arrayOf(
                ItemStick(0, Random().nextInt(3))
            )
        }
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.DEADBUSH)
    }
}
