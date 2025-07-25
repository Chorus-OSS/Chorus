package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.DirtType
import org.chorus_oss.chorus.event.block.BlockSpreadEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.ChorusRandom

class BlockMycelium(blockState: BlockState = properties.defaultState) : BlockDirt(blockState) {
    override val name: String
        get() = "Mycelium"

    override val toolType: Int
        get() = ItemTool.TYPE_SHOVEL

    override val hardness: Double
        get() = 0.6

    override val resistance: Double
        get() = 2.5

    override fun getDrops(item: Item): Array<Item> {
        return arrayOf(
            super.toItem()
        )
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_RANDOM) {
            if (level.getFullLight(add(0.0, 1.0, 0.0).position) >= BlockCrops.MIN_LIGHT_LEVEL) {
                //TODO: light levels
                val random = ChorusRandom()
                val x: Int = random.nextInt(position.floorX - 1, position.floorX + 1)
                val y: Int = random.nextInt(position.floorY - 1, position.floorY + 1)
                val z: Int = random.nextInt(position.floorZ - 1, position.floorZ + 1)
                val block = level.getBlock(Vector3(x.toDouble(), y.toDouble(), z.toDouble()))
                if (block.id == BlockID.DIRT && block.getPropertyValue(
                        CommonBlockProperties.DIRT_TYPE
                    ) == DirtType.NORMAL
                ) {
                    if (block.up().isTransparent) {
                        val ev = BlockSpreadEvent(block, this, get(BlockID.MYCELIUM))
                        Server.instance.pluginManager.callEvent(ev)
                        if (!ev.cancelled) {
                            level.setBlock(block.position, ev.newState)
                        }
                    }
                }
            }
        }
        return 0
    }

    override fun canSilkTouch(): Boolean {
        return true
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
        if (!up().canBeReplaced()) {
            return false
        }

        if (item.isShovel) {
            item.useOn(this)
            level.setBlock(this.position, get(BlockID.GRASS_PATH))
            if (player != null) {
                player.level!!.addSound(player.position, Sound.USE_GRASS)
            }
            return true
        }
        return false
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.MYCELIUM)
    }
}
