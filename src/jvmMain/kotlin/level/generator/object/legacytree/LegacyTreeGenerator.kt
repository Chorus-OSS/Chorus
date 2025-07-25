package org.chorus_oss.chorus.level.generator.`object`.legacytree

import org.chorus_oss.chorus.block.*
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.WoodType
import org.chorus_oss.chorus.level.generator.`object`.BlockManager
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.utils.ChorusRandom
import kotlin.math.abs

abstract class LegacyTreeGenerator {
    open var treeHeight: Int = 7
        protected set

    protected fun overridable(b: Block): Boolean {
        if (b is BlockWood) return true
        return when (b.id) {
            BlockID.AIR, BlockID.ACACIA_LEAVES, BlockID.AZALEA_LEAVES, BlockID.BIRCH_LEAVES, BlockID.AZALEA_LEAVES_FLOWERED, BlockID.CHERRY_LEAVES, BlockID.DARK_OAK_LEAVES, BlockID.JUNGLE_LEAVES, BlockID.MANGROVE_LEAVES, BlockID.OAK_LEAVES, BlockID.SPRUCE_LEAVES, BlockID.SNOW_LAYER, BlockID.ACACIA_SAPLING, BlockID.CHERRY_SAPLING, BlockID.SPRUCE_SAPLING, BlockID.BAMBOO_SAPLING, BlockID.OAK_SAPLING, BlockID.JUNGLE_SAPLING, BlockID.DARK_OAK_SAPLING, BlockID.BIRCH_SAPLING -> true
            else -> false
        }
    }

    open val type: WoodType
        get() = WoodType.OAK

    fun canPlaceObject(level: BlockManager, x: Int, y: Int, z: Int): Boolean {
        var radiusToCheck = 0
        for (yy in 0..<this.treeHeight + 3) {
            if (yy == 1 || yy == this.treeHeight) {
                ++radiusToCheck
            }
            for (xx in -radiusToCheck..<(radiusToCheck + 1)) {
                for (zz in -radiusToCheck..<(radiusToCheck + 1)) {
                    if (!this.overridable(level.getBlockAt(x + xx, y + yy, z + zz) ?: BlockAir())) {
                        return false
                    }
                }
            }
        }

        return true
    }

    open fun placeObject(level: BlockManager, x: Int, y: Int, z: Int, random: ChorusRandom) {
        this.placeTrunk(level, x, y, z, random, this.treeHeight - 1)

        for (yy in y - 3 + this.treeHeight..y + this.treeHeight) {
            val yOff = (yy - (y + this.treeHeight)).toDouble()
            val mid = (1 - yOff / 2).toInt()
            for (xx in x - mid..x + mid) {
                val xOff = abs(xx - x)
                for (zz in z - mid..z + mid) {
                    val zOff = abs(zz - z)
                    if (xOff == mid && zOff == mid && (yOff == 0.0 || random.nextInt(2) == 0)) {
                        continue
                    }
                    val blockAt: Block = level.getBlockAt(xx, yy, zz) ?: BlockAir()
                    if (!blockAt.isSolid) {
                        level.setBlockStateAt(xx, yy, zz, leafBlockState)
                    }
                }
            }
        }
    }

    protected open fun placeTrunk(
        level: BlockManager,
        x: Int,
        y: Int,
        z: Int,
        random: ChorusRandom?,
        trunkHeight: Int
    ) {
        // The base dirt block
        level.setBlockStateAt(x, y - 1, z, BlockID.DIRT)

        for (yy in 0..<trunkHeight) {
            val b: Block = level.getBlockAt(x, y + yy, z) ?: BlockAir()
            if (this.overridable(b)) {
                level.setBlockStateAt(x, y + yy, z, trunkBlockState)
            }
        }
    }

    protected open val trunkBlockState: BlockState
        get() {
            val pillarAxisValue = CommonBlockProperties.PILLAR_AXIS.createValue(BlockFace.Axis.Y)
            return when (type) {
                WoodType.JUNGLE -> BlockJungleLog.properties.getBlockState(pillarAxisValue)
                WoodType.DARK_OAK -> BlockDarkOakLog.properties.getBlockState(pillarAxisValue)
                WoodType.SPRUCE -> BlockSpruceLog.properties.getBlockState(pillarAxisValue)
                WoodType.ACACIA -> BlockAcaciaLog.properties.getBlockState(pillarAxisValue)
                WoodType.BIRCH -> BlockBirchLog.properties.getBlockState(pillarAxisValue)
                WoodType.OAK -> BlockOakLog.properties.getBlockState(pillarAxisValue)
                WoodType.CHERRY -> BlockCherryLog.properties.getBlockState(pillarAxisValue)
                WoodType.PALE_OAK -> BlockPaleOakLog.properties.getBlockState(pillarAxisValue)
                WoodType.MANGROVE -> BlockMangroveLog.properties.getBlockState(pillarAxisValue)
            }
        }

    protected open val leafBlockState: BlockState
        get() {
            return when (type) {
                WoodType.OAK -> BlockOakLeaves.properties.defaultState
                WoodType.BIRCH -> BlockBirchLeaves.properties.defaultState
                WoodType.ACACIA -> BlockAcaciaLeaves.properties.defaultState
                WoodType.JUNGLE -> BlockJungleLeaves.properties.defaultState
                WoodType.SPRUCE -> BlockSpruceLeaves.properties.defaultState
                WoodType.DARK_OAK -> BlockDarkOakLeaves.properties.defaultState
                WoodType.CHERRY -> BlockCherryLeaves.properties.defaultState
                WoodType.PALE_OAK -> BlockPaleOakLeaves.properties.defaultState
                WoodType.MANGROVE -> BlockMangroveLeaves.properties.defaultState
            }
        }

    companion object {
        fun growTree(
            level: BlockManager,
            x: Int,
            y: Int,
            z: Int,
            random: ChorusRandom,
            type: WoodType,
            tall: Boolean
        ) {
            val tree = when (type) {
                WoodType.SPRUCE -> LegacySpruceTree()
                WoodType.BIRCH -> {
                    if (tall) {
                        LegacyTallBirchTree()
                    } else {
                        LegacyBirchTree()
                    }
                }

                WoodType.DARK_OAK -> LegacyDarkOakTree(6f, 3)
                WoodType.JUNGLE -> LegacyJungleTree()
                else -> LegacyOakTree()
            }

            if (tree.canPlaceObject(level, x, y, z)) {
                tree.placeObject(level, x, y, z, random)
            }
        }
    }
}
