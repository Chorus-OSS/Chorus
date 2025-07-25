package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server.Companion.instance
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.event.block.BlockGrowEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemBlock
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockFace
import java.util.*

class BlockNetherWart @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockFlowable(blockstate) {
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
        if (down.id == BlockID.SOUL_SAND) {
            level.setBlock(block.position, this, direct = true, update = true)
            return true
        }
        return false
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (down().id != BlockID.SOUL_SAND) {
                level.useBreakOn(this.position)
                return Level.BLOCK_UPDATE_NORMAL
            }
        } else if (type == Level.BLOCK_UPDATE_RANDOM) {
            if (Random().nextInt(10) == 1) {
                if (this.age < 0x03) {
                    val block = clone() as BlockNetherWart
                    block.age += 1
                    val ev = BlockGrowEvent(this, block)
                    instance.pluginManager.callEvent(ev)

                    if (!ev.cancelled) {
                        level.setBlock(this.position, ev.newState, direct = true, update = true)
                    } else {
                        return Level.BLOCK_UPDATE_RANDOM
                    }
                }
            } else {
                return Level.BLOCK_UPDATE_RANDOM
            }
        }

        return 0
    }

    override val name: String
        get() = "Nether Wart Block"

    override fun getDrops(item: Item): Array<Item> {
        if (this.age == 0x03) {
            this.age = 0
            return arrayOf(
                ItemBlock(this.blockState, name, 0, 2 + (Math.random() * ((4 - 2) + 1)).toInt())
            )
        } else {
            return arrayOf(
                toItem()
            )
        }
    }

    var age: Int
        get() = getPropertyValue(CommonBlockProperties.AGE_4)
        set(age) {
            setPropertyValue(CommonBlockProperties.AGE_4, age)
        }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.NETHER_WART, CommonBlockProperties.AGE_4)
    }
}


