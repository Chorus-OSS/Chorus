package org.chorus_oss.chorus.level.generator.`object`

import org.chorus_oss.chorus.block.*
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockVector3
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.ChorusRandom

class ObjectCherryTree : TreeGenerator() {
    protected var LOG_Y_AXIS: BlockState
    protected var LOG_X_AXIS: BlockState
    protected var LOG_Z_AXIS: BlockState
    protected var LEAVES: BlockState

    override fun generate(level: BlockManager, rand: ChorusRandom, pos: Vector3): Boolean {
        val x = pos.floorX
        val y = pos.floorY
        val z = pos.floorZ

        val isBigTree: Boolean = rand.nextBoolean()
        if (isBigTree) {
            val ok = generateBigTree(level, rand, x, y, z)
            if (ok) return true
        }
        return generateSmallTree(level, rand, x, y, z)
    }

    protected fun generateBigTree(level: BlockManager, rand: ChorusRandom, x: Int, y: Int, z: Int): Boolean {
        val mainTrunkHeight = (if (rand.nextBoolean()) 1 else 0) + 10

        if (!canPlaceObject(level, mainTrunkHeight, x, y, z)) return false

        var growOnXAxis: Boolean = rand.nextBoolean()
        var xMultiplier = if (growOnXAxis) 1 else 0
        var zMultiplier = if (growOnXAxis) 0 else 1

        val leftSideTrunkLength: Int = rand.nextInt(2, 4)
        val leftSideTrunkHeight: Int = rand.nextInt(3, 5)
        val leftSideTrunkStartY: Int = rand.nextInt(4, 5)

        if (!canPlaceObject(
                level, leftSideTrunkHeight, x - leftSideTrunkLength * xMultiplier,
                y + leftSideTrunkStartY, z - leftSideTrunkLength * zMultiplier
            )
        ) {
            growOnXAxis = !growOnXAxis
            xMultiplier = if (growOnXAxis) 1 else 0
            zMultiplier = if (growOnXAxis) 0 else 1
            if (!canPlaceObject(
                    level, leftSideTrunkHeight, x - leftSideTrunkLength * xMultiplier,
                    y + leftSideTrunkStartY, z - leftSideTrunkLength * zMultiplier
                )
            ) {
                return false
            }
        }

        val rightSideTrunkLength: Int = rand.nextInt(2, 4)
        val rightSideTrunkHeight: Int = rand.nextInt(3, 5)
        val rightSideTrunkStartY: Int = rand.nextInt(4, 5)

        if (!canPlaceObject(
                level, rightSideTrunkHeight, x + rightSideTrunkLength * xMultiplier,
                y + rightSideTrunkStartY, z + rightSideTrunkLength * zMultiplier
            )
        ) return false

        this.setDirtAt(level, BlockVector3(x, y - 1, z))

        // Generate main trunk
        for (yy in 0..<mainTrunkHeight) {
            level.setBlockStateAt(x, y + yy, z, LOG_Y_AXIS)
        }
        // generate side trunks
        val sideBlockState = if (growOnXAxis) LOG_X_AXIS else LOG_Z_AXIS
        // generate left-side trunk
        for (xx in 1..leftSideTrunkLength) {
            if (this.canGrowInto(
                    level.getBlockIdAt(
                        x - xx * xMultiplier,
                        y + leftSideTrunkStartY,
                        z - xx * zMultiplier
                    )
                )
            ) level.setBlockStateAt(x - xx * xMultiplier, y + leftSideTrunkStartY, z - xx * zMultiplier, sideBlockState)
        }
        for (yy in 1..<leftSideTrunkHeight) {
            if (this.canGrowInto(
                    level.getBlockIdAt(
                        x - leftSideTrunkLength * xMultiplier,
                        y + leftSideTrunkStartY + yy, z - leftSideTrunkLength * zMultiplier
                    )
                )
            ) level.setBlockStateAt(
                x - leftSideTrunkLength * xMultiplier, y + leftSideTrunkStartY + yy,
                z - leftSideTrunkLength * zMultiplier, LOG_Y_AXIS
            )
        }
        // We just generated this above
        //       |
        // |     |     |
        // └-----|-----┘
        //       |
        // However, when start y == 4, minecraft generate trunk like this:
        //       |
        // └-┐   |   ┌-┘
        //   └---|---┘
        //       |
        if (leftSideTrunkStartY == 4) {
            var tmpX = x - leftSideTrunkLength * xMultiplier
            var tmpY = y + leftSideTrunkStartY
            var tmpZ = z - leftSideTrunkLength * zMultiplier
            level.setBlockStateAt(tmpX, tmpY, tmpZ, BlockAir.properties.defaultState)
            tmpX += xMultiplier
            tmpY += 1
            tmpZ += zMultiplier
            if (this.canGrowInto(level.getBlockIdAt(tmpX, tmpY, tmpZ))) {
                level.setBlockStateAt(tmpX, tmpY, tmpZ, LOG_Y_AXIS)
            }
            tmpX -= xMultiplier
            tmpZ -= zMultiplier
            if (this.canGrowInto(level.getBlockIdAt(tmpX, tmpY, tmpZ))) {
                level.setBlockStateAt(tmpX, tmpY, tmpZ, sideBlockState)
            }
        }
        // generate right-side trunk
        for (xx in 1..rightSideTrunkLength) {
            if (this.canGrowInto(
                    level.getBlockIdAt(
                        x + xx * xMultiplier,
                        y + rightSideTrunkStartY,
                        z + xx * zMultiplier
                    )
                )
            ) level.setBlockStateAt(
                x + xx * xMultiplier,
                y + rightSideTrunkStartY,
                z + xx * zMultiplier,
                sideBlockState
            )
        }
        for (yy in 1..<rightSideTrunkHeight) {
            if (this.canGrowInto(
                    level.getBlockIdAt(
                        x + rightSideTrunkLength * xMultiplier,
                        y + rightSideTrunkStartY + yy, z + rightSideTrunkLength * zMultiplier
                    )
                )
            ) level.setBlockStateAt(
                x + rightSideTrunkLength * xMultiplier, y + rightSideTrunkStartY + yy,
                z + rightSideTrunkLength * zMultiplier, LOG_Y_AXIS
            )
        }
        if (rightSideTrunkStartY == 4) {
            var tmpX = x + rightSideTrunkLength * xMultiplier
            var tmpY = y + rightSideTrunkStartY
            var tmpZ = z + rightSideTrunkLength * zMultiplier
            level.setBlockStateAt(tmpX, tmpY, tmpZ, BlockAir.properties.defaultState)
            tmpX -= xMultiplier
            tmpY += 1
            tmpZ -= zMultiplier
            if (this.canGrowInto(level.getBlockIdAt(tmpX, tmpY, tmpZ))) {
                level.setBlockStateAt(tmpX, tmpY, tmpZ, LOG_Y_AXIS)
            }
            tmpX += xMultiplier
            tmpZ += zMultiplier
            if (this.canGrowInto(level.getBlockIdAt(tmpX, tmpY, tmpZ))) {
                level.setBlockStateAt(tmpX, tmpY, tmpZ, sideBlockState)
            }
        }
        // generate main trunk leaves
        generateLeaves(level, rand, x, y + mainTrunkHeight + 1, z)
        // generate left-side trunk leaves
        generateLeaves(
            level, rand, x - leftSideTrunkLength * xMultiplier,
            y + leftSideTrunkStartY + leftSideTrunkHeight + 1, z - leftSideTrunkLength * zMultiplier
        )
        // generate right-side trunk leaves
        generateLeaves(
            level, rand, x + rightSideTrunkLength * xMultiplier,
            y + rightSideTrunkStartY + rightSideTrunkHeight + 1, z + rightSideTrunkLength * zMultiplier
        )
        return true
    }

    protected fun generateSmallTree(level: BlockManager, rand: ChorusRandom, x: Int, y: Int, z: Int): Boolean {
        val mainTrunkHeight = (if (rand.nextBoolean()) 1 else 0) + 4
        val sideTrunkHeight: Int = rand.nextInt(3, 5)

        if (!canPlaceObject(level, mainTrunkHeight + 1, x, y, z)) return false

        var growDirection: Int = rand.nextInt(0, 3)
        var xMultiplier = 0
        var zMultiplier = 0
        var canPlace = false
        for (i in 0..3) {
            growDirection = (growDirection + 1) % 4
            xMultiplier = when (growDirection) {
                0 -> -1
                1 -> 1
                else -> 0
            }
            zMultiplier = when (growDirection) {
                2 -> -1
                3 -> 1
                else -> 0
            }
            if (canPlaceObject(
                    level, sideTrunkHeight, x + xMultiplier * sideTrunkHeight, y,
                    z + zMultiplier * sideTrunkHeight
                )
            ) {
                canPlace = true
                break
            }
        }
        if (!canPlace) {
            return false
        }

        val sideBlockState = if (xMultiplier == 0) LOG_Z_AXIS else LOG_X_AXIS
        // Generate main trunk
        for (yy in 0..<mainTrunkHeight) {
            if (this.canGrowInto(level.getBlockIdAt(x, y + yy, z))) level.setBlockStateAt(x, y + yy, z, LOG_Y_AXIS)
        }
        // Generate side trunk
        // (└)-┐      <- if side trunk is 4 or more blocks high, do not place the last block
        //     └-┐    <- side trunk
        //       └-┐
        //         |  <- main trunk
        //         |
        for (yy in 1..sideTrunkHeight) {
            val tmpX = x + yy * xMultiplier
            var tmpY = y + mainTrunkHeight + yy - 2
            val tmpZ = z + yy * zMultiplier
            if (this.canGrowInto(level.getBlockIdAt(tmpX, tmpY, tmpZ))) {
                level.setBlockStateAt(tmpX, tmpY, tmpZ, sideBlockState)
            }
            // if side trunk is 4 or 5 blocks high, do not place the last block
            if (yy == sideTrunkHeight - 1 && sideTrunkHeight > 3) {
                continue
            }
            tmpY += 1
            if (this.canGrowInto(level.getBlockIdAt(tmpX, tmpY, tmpZ))) {
                level.setBlockStateAt(tmpX, tmpY, tmpZ, LOG_Y_AXIS)
            }
        }

        // generate leaves
        generateLeaves(
            level, rand, x + sideTrunkHeight * xMultiplier, y + mainTrunkHeight + sideTrunkHeight,
            z + sideTrunkHeight * zMultiplier
        )

        return true
    }

    init {
        val logY: BlockCherryLog = BlockCherryLog()
        logY.pillarAxis = (BlockFace.Axis.Y)
        this.LOG_Y_AXIS = logY.blockState
        val logX: BlockCherryLog = BlockCherryLog()
        logX.pillarAxis = (BlockFace.Axis.X)
        this.LOG_X_AXIS = logX.blockState
        val logZ: BlockCherryLog = BlockCherryLog()
        logZ.pillarAxis = (BlockFace.Axis.Z)
        this.LOG_Z_AXIS = logZ.blockState
        this.LEAVES = BlockCherryLeaves().blockState
    }

    fun generateLeaves(level: BlockManager, rand: ChorusRandom, x: Int, y: Int, z: Int) {
        for (dy in -2..2) {
            for (dx in -LEAVES_RADIUS..LEAVES_RADIUS) {
                for (dz in -LEAVES_RADIUS..LEAVES_RADIUS) {
                    val currentRadius = LEAVES_RADIUS - (Math.max(1, Math.abs(dy)))
                    if (dx * dx + dz * dz > currentRadius * currentRadius) continue
                    var block = level.getBlockAt(x + dx, y + dy, z + dz)
                    var blockId = block!!.id
                    if (blockId == BlockID.AIR ||
                        block is BlockLeaves ||
                        blockId == BlockID.AZALEA_LEAVES_FLOWERED
                    ) {
                        level.setBlockStateAt(x + dx, y + dy, z + dz, LEAVES)
                    }
                    if (dy == -2 && rand.nextInt(0, 2) == 0) {
                        block = level.getBlockAt(x + dx, y + dy - 1, z + dz)
                        blockId = block!!.id
                        if (blockId == BlockID.AIR ||
                            block is BlockLeaves ||
                            blockId == BlockID.AZALEA_LEAVES_FLOWERED
                        ) {
                            level.setBlockStateAt(x + dx, y + dy - 1, z + dz, LEAVES)
                        }
                    }
                }
            }
        }
    }

    fun canPlaceObject(level: BlockManager, treeHeight: Int, x: Int, y: Int, z: Int): Boolean {
        var radiusToCheck = 0
        for (yy in 0..<treeHeight + 3) {
            if (yy == 1 || yy == treeHeight) {
                ++radiusToCheck
            }
            for (xx in -radiusToCheck..<(radiusToCheck + 1)) {
                for (zz in -radiusToCheck..<(radiusToCheck + 1)) {
                    if (!this.canGrowInto(level.getBlockIdAt(x + xx, y + yy, z + zz))) {
                        return false
                    }
                }
            }
        }

        return true
    }

    companion object {
        const val LEAVES_RADIUS: Int = 4
    }
}

