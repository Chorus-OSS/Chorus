package org.chorus_oss.chorus.utils

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.block.BlockState
import org.chorus_oss.chorus.level.DimensionData
import org.chorus_oss.chorus.level.DimensionEnum
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.math.*

import java.util.function.BiPredicate


object PortalHelper : BlockID {
    fun spawnPortal(pos: Locator) {
        val lvl = pos.level //TODO: This will generate part of the time, seems to be only when the chunk is populated
        val x = pos.position.floorX
        var y = pos.position.floorY
        var z = pos.position.floorZ

        val air = Block.get(BlockID.AIR)
        val obsidian = Block.get(BlockID.OBSIDIAN)
        val netherPortal = Block.get(BlockID.PORTAL)
        for (xx in -1..3) {
            for (yy in 1..3) {
                for (zz in -1..2) {
                    lvl.setBlock(x + xx, y + yy, z + zz, air, direct = false, update = true)
                }
            }
        }

        lvl.setBlock(x + 1, y, z, obsidian, direct = false, update = true)
        lvl.setBlock(x + 2, y, z, obsidian, direct = false, update = true)

        z++
        lvl.setBlock(x, y, z, obsidian, direct = false, update = true)
        lvl.setBlock(x + 1, y, z, obsidian, direct = false, update = true)
        lvl.setBlock(x + 2, y, z, obsidian, direct = false, update = true)
        lvl.setBlock(x + 3, y, z, obsidian, direct = false, update = true)

        z++
        lvl.setBlock(x + 1, y, z, obsidian, direct = false, update = true)
        lvl.setBlock(x + 2, y, z, obsidian, direct = false, update = true)
        z--

        for (i in 0..2) {
            y++
            lvl.setBlock(x, y, z, obsidian, direct = false, update = true)
            lvl.setBlock(x + 1, y, z, netherPortal, direct = false, update = true)
            lvl.setBlock(x + 2, y, z, netherPortal, direct = false, update = true)
            lvl.setBlock(x + 3, y, z, obsidian, direct = false, update = true)
        }

        y++
        lvl.setBlock(x, y, z, obsidian, direct = false, update = true)
        lvl.setBlock(x + 1, y, z, obsidian, direct = false, update = true)
        lvl.setBlock(x + 2, y, z, obsidian, direct = false, update = true)
        lvl.setBlock(x + 3, y, z, obsidian, direct = false, update = true)
    }

    fun getNearestValidPortal(currentPos: Locator): Locator? {
        val axisAlignedBB: AxisAlignedBB = SimpleAxisAlignedBB(
            Vector3(
                currentPos.position.floorX - 128.0,
                currentPos.level.dimensionData.minHeight.toDouble(),
                currentPos.position.floorZ - 128.0
            ),
            Vector3(
                currentPos.position.floorX + 128.0,
                currentPos.level.dimensionData.maxHeight.toDouble(),
                currentPos.position.floorZ + 128.0
            )
        )
        val condition = BiPredicate { _: BlockVector3?, state: BlockState? -> state!!.identifier == BlockID.PORTAL }
        val blocks = currentPos.level.scanBlocks(axisAlignedBB, condition)

        if (blocks.isEmpty()) {
            return null
        }

        val currentPosV2 = Vector2(currentPos.position.floorX.toDouble(), currentPos.position.floorZ.toDouble())
        val by = currentPos.position.floorY.toDouble()
        val euclideanDistance = Comparator.comparingDouble { block: Block? ->
            currentPosV2.distanceSquared(
                block!!.position.floorX.toDouble(), block.position.floorZ.toDouble()
            )
        }
        val heightDistance = Comparator.comparingDouble { block: Block? ->
            val ey = by - block!!.position.y
            ey * ey
        }

        return blocks.stream()
            .filter { block: Block? -> block!!.down().id != BlockID.PORTAL }
            .min(euclideanDistance.thenComparing(heightDistance))
            .orElse(null)
    }

    fun convertPosBetweenNetherAndOverworld(current: Locator): Locator? {
        val defaultNetherLevel = Server.instance.defaultNetherLevel ?: return null
        val dimensionData: DimensionData
        when (current.level.dimension) {
            Level.DIMENSION_OVERWORLD -> {
                dimensionData = DimensionEnum.NETHER.dimensionData
                return Locator(
                    (current.position.floorX shr 3).toDouble(),
                    current.position.floorY.coerceIn(dimensionData.minHeight, dimensionData.maxHeight)
                        .toDouble(),
                    (current.position.floorZ shr 3).toDouble(),
                    defaultNetherLevel
                )
            }

            Level.DIMENSION_NETHER -> {
                dimensionData = DimensionEnum.OVERWORLD.dimensionData
                return Locator(
                    (current.position.floorX shl 3).toDouble(),
                    current.position.floorY.coerceIn(dimensionData.minHeight, dimensionData.maxHeight)
                        .toDouble(),
                    (current.position.floorZ shl 3).toDouble(),
                    Server.instance.defaultLevel!!
                )
            }

            else -> {
                throw IllegalArgumentException("Neither overworld nor nether given!")
            }
        }
    }

    @JvmStatic
    fun moveToTheEnd(current: Locator): Locator? {
        val defaultEndLevel = Server.instance.defaultEndLevel ?: return null
        return when (current.level.dimension) {
            Level.DIMENSION_OVERWORLD -> {
                Locator(100.0, 49.0, 0.0, defaultEndLevel)
            }

            Level.DIMENSION_THE_END -> {
                Server.instance.defaultLevel!!.spawnLocation
            }

            else -> {
                throw IllegalArgumentException("Neither overworld nor the end given!")
            }
        }
    }
}
