package org.chorus_oss.chorus.utils

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sqrt


class BlockIterator @JvmOverloads constructor(
    level: Level, start: Vector3, direction: Vector3, yOffset: Double = 0.0,
    private val maxDistance: Int = 0
) :
    Iterator<Block> {
    private var end = false

    private val blockQueue = arrayOfNulls<Block>(3)
    private var currentBlock = 0

    private var currentBlockObject: Block? = null
    private var currentDistance: Int
    private var maxDistanceInt = 0

    private var secondError: Int
    private var thirdError: Int

    private val secondStep: Int
    private val thirdStep: Int

    private var mainFace: BlockFace? = null
    private var secondFace: BlockFace? = null
    private var thirdFace: BlockFace? = null

    init {
        val startClone = Vector3(start.x, start.y, start.z)
        startClone.y += yOffset

        this.currentDistance = 0

        var mainDirection = 0.0
        var secondDirection = 0.0
        var thirdDirection = 0.0

        var mainPosition = 0.0
        var secondPosition = 0.0
        var thirdPosition = 0.0

        val pos = Vector3(startClone.x, startClone.y, startClone.z)
        val startBlock =
            level.getBlock(Vector3(floor(pos.x), floor(pos.y), floor(pos.z)))

        if (this.getXLength(direction) > mainDirection) {
            this.mainFace = this.getXFace(direction)
            mainDirection = this.getXLength(direction)
            mainPosition = this.getXPosition(direction, startClone, startBlock)

            this.secondFace = this.getYFace(direction)
            secondDirection = this.getYLength(direction)
            secondPosition = this.getYPosition(direction, startClone, startBlock)

            this.thirdFace = this.getZFace(direction)
            thirdDirection = this.getZLength(direction)
            thirdPosition = this.getZPosition(direction, startClone, startBlock)
        }
        if (this.getYLength(direction) > mainDirection) {
            this.mainFace = this.getYFace(direction)
            mainDirection = this.getYLength(direction)
            mainPosition = this.getYPosition(direction, startClone, startBlock)

            this.secondFace = this.getZFace(direction)
            secondDirection = this.getZLength(direction)
            secondPosition = this.getZPosition(direction, startClone, startBlock)

            this.thirdFace = this.getXFace(direction)
            thirdDirection = this.getXLength(direction)
            thirdPosition = this.getXPosition(direction, startClone, startBlock)
        }
        if (this.getZLength(direction) > mainDirection) {
            this.mainFace = this.getZFace(direction)
            mainDirection = this.getZLength(direction)
            mainPosition = this.getZPosition(direction, startClone, startBlock)

            this.secondFace = this.getXFace(direction)
            secondDirection = this.getXLength(direction)
            secondPosition = this.getXPosition(direction, startClone, startBlock)

            this.thirdFace = this.getYFace(direction)
            thirdDirection = this.getYLength(direction)
            thirdPosition = this.getYPosition(direction, startClone, startBlock)
        }

        val d = mainPosition / mainDirection
        val secondd = secondPosition - secondDirection * d
        val thirdd = thirdPosition - thirdDirection * d

        this.secondError = floor(secondd * GRID_SIZE).toInt()
        this.secondStep = Math.round(secondDirection / mainDirection * GRID_SIZE).toInt()
        this.thirdError = floor(thirdd * GRID_SIZE).toInt()
        this.thirdStep = Math.round(thirdDirection / mainDirection * GRID_SIZE).toInt()

        if (this.secondError + this.secondStep <= 0) {
            this.secondError = -this.secondStep + 1
        }

        if (this.thirdError + this.thirdStep <= 0) {
            this.thirdError = -this.thirdStep + 1
        }

        var lastBlock = startBlock.getSide(mainFace!!.getOpposite())

        if (this.secondError < 0) {
            this.secondError += GRID_SIZE
            lastBlock = lastBlock.getSide(secondFace!!.getOpposite())
        }

        if (this.thirdError < 0) {
            this.thirdError += GRID_SIZE
            lastBlock = lastBlock.getSide(thirdFace!!.getOpposite())
        }

        this.secondError -= GRID_SIZE
        this.thirdError -= GRID_SIZE

        blockQueue[0] = lastBlock

        this.currentBlock = -1

        this.scan()

        var startBlockFound = false

        for (cnt in this.currentBlock downTo 0) {
            if (this.blockEquals(blockQueue[cnt]!!, startBlock)) {
                this.currentBlock = cnt
                startBlockFound = true
                break
            }
        }

        check(startBlockFound) { "Start block missed in BlockIterator" }

        this.maxDistanceInt =
            Math.round(maxDistance / (sqrt(mainDirection * mainDirection + secondDirection * secondDirection + thirdDirection * thirdDirection) / mainDirection))
                .toInt()
    }

    private fun blockEquals(a: Block, b: Block): Boolean {
        return a.position.x == b.position.x && a.position.y == b.position.y && a.position.z == b.position.z
    }

    private fun getXFace(direction: Vector3): BlockFace {
        return if ((direction.x) > 0) BlockFace.EAST else BlockFace.WEST
    }

    private fun getYFace(direction: Vector3): BlockFace {
        return if ((direction.y) > 0) BlockFace.UP else BlockFace.DOWN
    }

    private fun getZFace(direction: Vector3): BlockFace {
        return if ((direction.z) > 0) BlockFace.SOUTH else BlockFace.NORTH
    }

    private fun getXLength(direction: Vector3): Double {
        return abs(direction.x)
    }

    private fun getYLength(direction: Vector3): Double {
        return abs(direction.y)
    }

    private fun getZLength(direction: Vector3): Double {
        return abs(direction.z)
    }

    private fun getPosition(direction: Double, position: Double, blockPosition: Double): Double {
        return if (direction > 0) (position - blockPosition) else (blockPosition + 1 - position)
    }

    private fun getXPosition(direction: Vector3, position: Vector3, block: Block): Double {
        return this.getPosition(direction.x, position.x, block.position.x)
    }

    private fun getYPosition(direction: Vector3, position: Vector3, block: Block): Double {
        return this.getPosition(direction.y, position.y, block.position.y)
    }

    private fun getZPosition(direction: Vector3, position: Vector3, block: Block): Double {
        return this.getPosition(direction.z, position.z, block.position.z)
    }

    override fun next(): Block {
        this.scan()

        if (this.currentBlock <= -1) {
            throw IndexOutOfBoundsException()
        } else {
            this.currentBlockObject = blockQueue[currentBlock--]
        }
        return this.currentBlockObject!!
    }

    override fun hasNext(): Boolean {
        this.scan()
        return this.currentBlock != -1
    }

    private fun scan() {
        if (this.currentBlock >= 0) {
            return
        }

        if (this.maxDistance != 0 && this.currentDistance > this.maxDistanceInt) {
            this.end = true
            return
        }

        if (this.end) {
            return
        }

        ++this.currentDistance

        this.secondError += this.secondStep
        this.thirdError += this.thirdStep

        if (this.secondError > 0 && this.thirdError > 0) {
            blockQueue[2] = blockQueue[0]!!.getSide(mainFace!!)

            if ((this.secondStep * this.thirdError) < (this.thirdStep * this.secondError)) {
                blockQueue[1] = blockQueue[2]!!.getSide(secondFace!!)
                blockQueue[0] = blockQueue[1]!!.getSide(thirdFace!!)
            } else {
                blockQueue[1] = blockQueue[2]!!.getSide(thirdFace!!)
                blockQueue[0] = blockQueue[1]!!.getSide(secondFace!!)
            }

            this.thirdError -= GRID_SIZE
            this.secondError -= GRID_SIZE
            this.currentBlock = 2
        } else if (this.secondError > 0) {
            blockQueue[1] = blockQueue[0]!!.getSide(mainFace!!)
            blockQueue[0] = blockQueue[1]!!.getSide(secondFace!!)
            this.secondError -= GRID_SIZE
            this.currentBlock = 1
        } else if (this.thirdError > 0) {
            blockQueue[1] = blockQueue[0]!!.getSide(mainFace!!)
            blockQueue[0] = blockQueue[1]!!.getSide(thirdFace!!)
            this.thirdError -= GRID_SIZE
            this.currentBlock = 1
        } else {
            blockQueue[0] = blockQueue[0]!!.getSide(mainFace!!)
            this.currentBlock = 0
        }
    }

    companion object {
        private const val GRID_SIZE = 1 shl 24
    }
}
