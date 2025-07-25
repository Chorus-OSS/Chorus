package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.event.block.FarmLandDecayEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemBlock
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3

class BlockFarmland @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockTransparent(blockstate) {
    override val name: String
        get() = "Farmland"

    override val resistance: Double
        get() = 3.0

    override val hardness: Double
        get() = 0.6

    override val toolType: Int
        get() = ItemTool.TYPE_SHOVEL

    override var maxY: Double
        get() = position.y + 1
        set(maxY) {
            super.maxY = maxY
        }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (up().isSolid) {
                val farmEvent = FarmLandDecayEvent(null, this)
                Server.instance.pluginManager.callEvent(farmEvent)
                if (farmEvent.cancelled) return 0

                level.setBlock(this.position, get(BlockID.DIRT), direct = false, update = true)

                return type
            }
        } else if (type == Level.BLOCK_UPDATE_RANDOM) {
            val v = Vector3()
            if (level.getBlock(
                    v.setComponents(
                        position.x,
                        position.y + 1, position.z
                    )
                ) is BlockCrops
            ) {
                return 0
            }

            var found = false

            if (level.isRaining) {
                found = true
            } else {
                var x = position.x.toInt() - 4
                end@ while (x <= position.x + 4) {
                    var z = position.z.toInt() - 4
                    while (z <= position.z + 4) {
                        var y = position.y.toInt()
                        while (y <= position.y + 1) {
                            if (z.toDouble() == position.z && x.toDouble() == position.x && y.toDouble() == position.y) {
                                y++
                                continue
                            }

                            v.setComponents(x.toDouble(), y.toDouble(), z.toDouble())
                            var block = level.getBlockIdAt(v.floorX, v.floorY, v.floorZ)

                            if (block == BlockID.FLOWING_WATER || block == BlockID.WATER || block == BlockID.FROSTED_ICE) {
                                found = true
                                break@end
                            } else {
                                block = level.getBlockIdAt(v.floorX, v.floorY, v.floorZ, 1)
                                if (block == BlockID.FLOWING_WATER || block == BlockID.WATER || block == BlockID.FROSTED_ICE) {
                                    found = true
                                    break@end
                                }
                            }
                            y++
                        }
                        z++
                    }
                    x++
                }
            }

            val block = level.getBlock(
                v.setComponents(
                    position.x,
                    position.y - 1, position.z
                )
            )
            if (found || block is BlockFlowingWater || block is BlockFrostedIce) {
                if (moistureAmount < 7) {
                    moistureAmount = 7
                    level.setBlock(this.position, this, false, moistureAmount == 0)
                }
                return Level.BLOCK_UPDATE_RANDOM
            }

            if (moistureAmount > 0) {
                this.moistureAmount -= 1
                level.setBlock(this.position, this, false, moistureAmount == 1)
            } else {
                val farmEvent = FarmLandDecayEvent(null, this)
                Server.instance.pluginManager.callEvent(farmEvent)
                if (farmEvent.cancelled) return 0
                level.setBlock(this.position, get(BlockID.DIRT), direct = false, update = true)
            }

            return Level.BLOCK_UPDATE_RANDOM
        }

        return 0
    }

    override fun toItem(): Item {
        return ItemBlock(BlockDirt.properties.defaultState, "")
    }

    override fun isSolid(side: BlockFace): Boolean {
        return true
    }

    override val isTransparent: Boolean
        get() = true

    var moistureAmount: Int
        get() = getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.MOISTURIZED_AMOUNT)
        set(value) {
            setPropertyValue<Int, IntPropertyType>(CommonBlockProperties.MOISTURIZED_AMOUNT, value)
        }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.FARMLAND, CommonBlockProperties.MOISTURIZED_AMOUNT)
    }
}
