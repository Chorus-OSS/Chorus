package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.BambooLeafSize
import org.chorus_oss.chorus.block.property.type.BooleanPropertyType
import org.chorus_oss.chorus.event.block.BlockGrowEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemBlock
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.particle.BoneMealParticle
import org.chorus_oss.chorus.math.BlockFace
import java.util.concurrent.ThreadLocalRandom

class BlockBambooSapling @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockFlowable(blockstate) {

    override val name: String
        get() = "Bamboo Sapling"

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (isSupportInvalid) {
                level.useBreakOn(this.position, null, null, true)
            } else {
                val up = up()
                if (up.id == BlockID.BAMBOO) {
                    val upperBamboo = up as BlockBamboo
                    val newState = BlockBamboo()
                    newState.isThick = upperBamboo.isThick
                    level.setBlock(this.position, newState, direct = true, update = true)
                }
            }
            return type
        } else if (type == Level.BLOCK_UPDATE_RANDOM) {
            val up = up()
            if (!isAge && up.isAir && level.getFullLight(up.position) >= BlockCrops.MIN_LIGHT_LEVEL && ThreadLocalRandom.current()
                    .nextInt(3) == 0
            ) {
                val newState = BlockBamboo()
                newState.bambooLeafSize = BambooLeafSize.SMALL_LEAVES
                val blockGrowEvent = BlockGrowEvent(up, newState)
                Server.instance.pluginManager.callEvent(blockGrowEvent)
                if (!blockGrowEvent.cancelled) {
                    val newState1 = blockGrowEvent.newState
                    newState1.position.y = up.position.y
                    newState1.position.x = position.x
                    newState1.position.z = position.z
                    newState1.level = level
                    newState1.place(toItem(), up, this, BlockFace.DOWN, 0.5, 0.5, 0.5, null)
                }
            }
            return type
        }
        return 0
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
        if (isSupportInvalid) {
            return false
        }

        if (levelBlock is BlockLiquid || getLevelBlockAtLayer(1) is BlockLiquid) {
            return false
        }

        level.setBlock(this.position, this, direct = true, update = true)
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
            var success = false
            val block = this.up()
            if (block.isAir) {
                success = grow(block)
            }

            if (success) {
                if (player != null && (player.gamemode and 0x01) == 0) {
                    item.count--
                }

                level.addParticle(BoneMealParticle(this.position))
            }

            return true
        }
        return false
    }

    fun grow(up: Block?): Boolean {
        val bamboo = BlockBamboo()
        bamboo.position.x = position.x
        bamboo.position.y = position.y
        bamboo.position.z = position.z
        bamboo.level = level
        return bamboo.grow(up!!)
    }

    private val isSupportInvalid: Boolean
        get() = when (down().id) {
            BlockID.BAMBOO, BlockID.DIRT, BlockID.GRASS_BLOCK, BlockID.SAND, BlockID.GRAVEL, BlockID.PODZOL, BlockID.BAMBOO_SAPLING, BlockID.MOSS_BLOCK -> false
            else -> true
        }

    override val resistance: Double
        get() = 5.0

    var isAge: Boolean
        /**
         * Alias age == 0 | age == false | !age
         */
        get() = getPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.AGE_BIT)
        set(isAge) {
            setPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.AGE_BIT, isAge)
        }

    override fun toItem(): Item {
        return ItemBlock(BlockBamboo.properties.defaultState, name)
    }

    override var minX: Double
        get() = position.x + 0.125
        set(minX) {
            super.minX = minX
        }

    override var maxX: Double
        get() = position.x + 0.875
        set(maxX) {
            super.maxX = maxX
        }

    override var minZ: Double
        get() = position.z + 0.125
        set(minZ) {
            super.minZ = minZ
        }

    override var maxZ: Double
        get() = position.z + 0.875
        set(maxZ) {
            super.maxZ = maxZ
        }

    override var maxY: Double
        get() = position.y + 0.875
        set(maxY) {
            super.maxY = maxY
        }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.BAMBOO_SAPLING, CommonBlockProperties.AGE_BIT)
    }
}
