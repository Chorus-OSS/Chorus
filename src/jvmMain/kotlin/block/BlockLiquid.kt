package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.block.BlockFromToEvent
import org.chorus_oss.chorus.event.block.LiquidFlowEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.Level.Companion.blockHash
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.particle.SmokeParticle
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.protocol.packets.LevelEventPacket
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.min

abstract class BlockLiquid(state: BlockState) : BlockTransparent(state) {
    var adjacentSources: Int = 0
    protected var flowVector: Vector3? = null
    private val flowCostVisited: MutableMap<Long, Byte> = mutableMapOf()

    override fun canBeFlowedInto(): Boolean {
        return true
    }

    override fun recalculateBoundingBox(): AxisAlignedBB? {
        return null
    }

    override fun getDrops(item: Item): Array<Item> {
        return Item.EMPTY_ARRAY
    }

    override fun hasEntityCollision(): Boolean {
        return true
    }

    override fun isBreakable(vector: Vector3, layer: Int, face: BlockFace?, item: Item?, player: Player?): Boolean {
        return false
    }

    override fun canBeReplaced(): Boolean {
        return true
    }

    override val isSolid: Boolean
        get() = false

    override fun isSolid(side: BlockFace): Boolean {
        return false
    }

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    override val boundingBox: AxisAlignedBB?
        get() = null

    override var maxY: Double
        get() = position.y + 1 - this.fluidHeightPercent
        set(maxY) {
            super.maxY = maxY
        }

    override fun recalculateCollisionBoundingBox(): AxisAlignedBB? {
        return this
    }

    /**
     * Whether this fluid can place the second layer (aquifer)
     */
    open fun usesWaterLogging(): Boolean {
        return false
    }

    val fluidHeightPercent: Float
        get() {
            var d = liquidDepth.toFloat()
            if (d >= 8) {
                d = 0f
            }

            return (d + 1) / 9f
        }

    protected fun getFlowDecay(block: Block): Int {
        if (block is BlockLiquid) {
            return block.liquidDepth
        } else {
            val layer1 = block.getLevelBlockAtLayer(1)
            return if (layer1 is BlockLiquid) {
                layer1.liquidDepth
            } else {
                -1
            }
        }
    }

    protected fun getEffectiveFlowDecay(block: Block): Int {
        val l = if (block is BlockLiquid) {
            block
        } else {
            val layer1 = block.getLevelBlockAtLayer(1)
            if (layer1 is BlockLiquid) {
                layer1
            } else {
                return -1
            }
        }
        var decay = l.liquidDepth
        if (decay >= 8) {
            decay = 0
        }
        return decay
    }

    fun clearCaches() {
        this.flowVector = null
        flowCostVisited.clear()
    }

    fun getSafeFlowVector(): Vector3 {
        if (this.flowVector != null) {
            return flowVector!!
        }
        var vector: Vector3? = Vector3(0.0, 0.0, 0.0)
        val decay = this.getEffectiveFlowDecay(this)
        for (j in 0..3) {
            var x = position.x.toInt()
            val y = position.y.toInt()
            var z = position.z.toInt()
            when (j) {
                0 -> --x
                1 -> x++
                2 -> z--
                else -> z++
            }
            val sideBlock = level.getBlock(x, y, z)
            var blockDecay = this.getEffectiveFlowDecay(sideBlock)
            if (blockDecay < 0) {
                if (!sideBlock.canBeFlowedInto()) {
                    continue
                }
                blockDecay = this.getEffectiveFlowDecay(level.getBlock(x, y - 1, z))
                if (blockDecay >= 0) {
                    val realDecay = blockDecay - (decay - 8)
                    vector!!.x += (sideBlock.position.x - position.x) * realDecay
                    vector!!.y += (sideBlock.position.y - position.y) * realDecay
                    vector!!.z += (sideBlock.position.z - position.z) * realDecay
                }
            } else {
                val realDecay = blockDecay - decay
                vector!!.x += (sideBlock.position.x - position.x) * realDecay
                vector!!.y += (sideBlock.position.y - position.y) * realDecay
                vector!!.z += (sideBlock.position.z - position.z) * realDecay
            }
        }
        if (liquidDepth >= 8) {
            if (!this.canFlowInto(
                    level.getBlock(
                        position.x.toInt(),
                        position.y.toInt(), position.z.toInt() - 1
                    )
                ) || !this.canFlowInto(
                    level.getBlock(
                        position.x.toInt(),
                        position.y.toInt(), position.z.toInt() + 1
                    )
                ) || !this.canFlowInto(
                    level.getBlock(
                        position.x.toInt() - 1,
                        position.y.toInt(), position.z.toInt()
                    )
                ) || !this.canFlowInto(
                    level.getBlock(
                        position.x.toInt() + 1,
                        position.y.toInt(), position.z.toInt()
                    )
                ) || !this.canFlowInto(
                    level.getBlock(
                        position.x.toInt(),
                        position.y.toInt() + 1, position.z.toInt() - 1
                    )
                ) || !this.canFlowInto(
                    level.getBlock(
                        position.x.toInt(),
                        position.y.toInt() + 1, position.z.toInt() + 1
                    )
                ) || !this.canFlowInto(
                    level.getBlock(
                        position.x.toInt() - 1,
                        position.y.toInt() + 1, position.z.toInt()
                    )
                ) || !this.canFlowInto(
                    level.getBlock(
                        position.x.toInt() + 1,
                        position.y.toInt() + 1, position.z.toInt()
                    )
                )
            ) {
                vector = vector!!.normalize().add(0.0, -6.0, 0.0)
            }
        }
        return vector!!.normalize().also { this.flowVector = it }
    }

    override fun addVelocityToEntity(entity: Entity?, vector: Vector3?) {
        if (entity!!.canBeMovedByCurrents()) {
            val flow = this.getSafeFlowVector()
            vector!!.x += flow.x
            vector!!.y += flow.y
            vector!!.z += flow.z
        }
    }

    open val flowDecayPerBlock: Int
        /**
         * define the depth at which the fluid flows one block of decay
         */
        get() = 1

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) { //for normal update tick
            this.checkForMixing()
            if (usesWaterLogging() && layer > 0) {
                val layer0 = level.getBlock(this.position, 0)
                if (layer0.isAir) {
                    level.setBlock(this.position, 1, get(BlockID.AIR), false, false)
                    level.setBlock(this.position, 0, this, false, false)
                } else if (layer0.waterloggingLevel <= 0 || layer0.waterloggingLevel == 1 && liquidDepth > 0) {
                    level.setBlock(this.position, 1, get(BlockID.AIR), true, true)
                }
            }
            level.scheduleUpdate(this, this.tickRate())
            return 0
        } else if (type == Level.BLOCK_UPDATE_SCHEDULED) {
            var decay = this.getFlowDecay(this)
            val multiplier = this.flowDecayPerBlock
            if (decay > 0) {
                var smallestFlowDecay = -100
                this.adjacentSources = 0
                smallestFlowDecay = this.getSmallestFlowDecay(
                    level.getBlock(
                        position.x.toInt(), position.y.toInt(), position.z.toInt() - 1
                    ), smallestFlowDecay
                )
                smallestFlowDecay = this.getSmallestFlowDecay(
                    level.getBlock(
                        position.x.toInt(), position.y.toInt(), position.z.toInt() + 1
                    ), smallestFlowDecay
                )
                smallestFlowDecay = this.getSmallestFlowDecay(
                    level.getBlock(
                        position.x.toInt() - 1, position.y.toInt(), position.z.toInt()
                    ), smallestFlowDecay
                )
                smallestFlowDecay = this.getSmallestFlowDecay(
                    level.getBlock(
                        position.x.toInt() + 1, position.y.toInt(), position.z.toInt()
                    ), smallestFlowDecay
                )
                var newDecay = smallestFlowDecay + multiplier
                if (newDecay >= 8 || smallestFlowDecay < 0) {
                    newDecay = -1
                }
                val topFlowDecay = this.getFlowDecay(
                    level.getBlock(
                        position.x.toInt(),
                        position.y.toInt() + 1, position.z.toInt()
                    )
                )
                if (topFlowDecay >= 0) {
                    newDecay = topFlowDecay or 0x08
                }
                if (this.adjacentSources >= 2 && this is BlockFlowingWater) {
                    var bottomBlock = level.getBlock(
                        position.x.toInt(),
                        position.y.toInt() - 1, position.z.toInt()
                    )
                    if (bottomBlock.isSolid) {
                        newDecay = 0
                    } else if (bottomBlock is BlockFlowingWater && bottomBlock.liquidDepth == 0) {
                        newDecay = 0
                    } else {
                        bottomBlock = bottomBlock.getLevelBlockAtLayer(1)
                        if (bottomBlock is BlockFlowingWater && bottomBlock.liquidDepth == 0) {
                            newDecay = 0
                        }
                    }
                }
                if (newDecay != decay) {
                    decay = newDecay
                    val decayed = decay < 0
                    val to = if (decayed) {
                        get(BlockID.AIR)
                    } else {
                        getLiquidWithNewDepth(decay)
                    }
                    val event = BlockFromToEvent(this, to)
                    Server.instance.pluginManager.callEvent(event)
                    if (!event.cancelled) {
                        level.setBlock(this.position, layer, event.to, true, true)
                        if (!decayed) {
                            level.scheduleUpdate(this, this.tickRate())
                        }
                    }
                }
            }
            if (decay >= 0) {
                val bottomBlock = level.getBlock(
                    position.x.toInt(),
                    position.y.toInt() - 1, position.z.toInt()
                )
                this.flowIntoBlock(bottomBlock, decay or 0x08)
                if (decay == 0 || !(if (usesWaterLogging()) bottomBlock.canWaterloggingFlowInto() else bottomBlock.canBeFlowedInto())) {
                    val adjacentDecay = if (decay >= 8) {
                        1
                    } else {
                        decay + multiplier
                    }
                    if (adjacentDecay < 8) {
                        val flags = this.optimalFlowDirections
                        if (flags[0]) {
                            this.flowIntoBlock(
                                level.getBlock(
                                    position.x.toInt() - 1,
                                    position.y.toInt(), position.z.toInt()
                                ), adjacentDecay
                            )
                        }
                        if (flags[1]) {
                            this.flowIntoBlock(
                                level.getBlock(
                                    position.x.toInt() + 1,
                                    position.y.toInt(), position.z.toInt()
                                ), adjacentDecay
                            )
                        }
                        if (flags[2]) {
                            this.flowIntoBlock(
                                level.getBlock(
                                    position.x.toInt(),
                                    position.y.toInt(), position.z.toInt() - 1
                                ), adjacentDecay
                            )
                        }
                        if (flags[3]) {
                            this.flowIntoBlock(
                                level.getBlock(
                                    position.x.toInt(),
                                    position.y.toInt(), position.z.toInt() + 1
                                ), adjacentDecay
                            )
                        }
                    }
                }
                this.checkForMixing()
            }
        }
        return 0
    }

    protected open fun flowIntoBlock(block: Block, newFlowDecay: Int) {
        var block = block
        if (this.canFlowInto(block) && block !is BlockLiquid) {
            if (usesWaterLogging()) {
                val layer1 = block.getLevelBlockAtLayer(1)
                if (layer1 is BlockLiquid) {
                    return
                }

                if (block.waterloggingLevel > 1) {
                    block = layer1
                }
            }

            val event = LiquidFlowEvent(block, this, newFlowDecay)
            Server.instance.pluginManager.callEvent(event)
            if (!event.cancelled) {
                if (block.layer == 0 && !block.isAir) {
                    level.useBreakOn(block.position, if (block is BlockWeb) Item.get(ItemID.WOODEN_SWORD) else null)
                }
                level.setBlock(block.position, block.layer, getLiquidWithNewDepth(newFlowDecay), true, true)
                level.scheduleUpdate(block, this.tickRate())
            }
        }
    }

    private fun calculateFlowCost(
        blockX: Int,
        blockY: Int,
        blockZ: Int,
        accumulatedCost: Int,
        maxCost: Int,
        originOpposite: Int,
        lastOpposite: Int
    ): Int {
        var cost = 1000
        for (j in 0..3) {
            if (j == originOpposite || j == lastOpposite) {
                continue
            }
            var x = blockX
            val y = blockY
            var z = blockZ
            if (j == 0) {
                --x
            } else if (j == 1) {
                ++x
            } else if (j == 2) {
                --z
            } else {
                ++z
            }
            val hash = blockHash(x, y, z, this.level)
            if (!flowCostVisited.containsKey(hash)) {
                val blockSide = level.getBlock(x, y, z)
                if (!this.canFlowInto(blockSide)) {
                    flowCostVisited.put(hash, BLOCKED)
                } else if (if (usesWaterLogging()) level.getBlock(x, y - 1, z)
                        .canWaterloggingFlowInto() else level.getBlock(
                        x,
                        y - 1,
                        z
                    )
                        .canBeFlowedInto()
                ) {
                    flowCostVisited.put(hash, CAN_FLOW_DOWN)
                } else {
                    flowCostVisited.put(hash, CAN_FLOW)
                }
            }
            val status = flowCostVisited[hash]
            if (status == BLOCKED) {
                continue
            } else if (status == CAN_FLOW_DOWN) {
                return accumulatedCost
            }
            if (accumulatedCost >= maxCost) {
                continue
            }
            val realCost = this.calculateFlowCost(x, y, z, accumulatedCost + 1, maxCost, originOpposite, j xor 0x01)
            if (realCost < cost) {
                cost = realCost
            }
        }
        return cost
    }

    override val hardness: Double
        get() = 100.0

    override val resistance: Double
        get() = 500.0

    private val optimalFlowDirections: BooleanArray
        get() {
            val flowCost = intArrayOf(
                1000,
                1000,
                1000,
                1000
            )
            var maxCost = 4 / this.flowDecayPerBlock
            for (j in 0..3) {
                var x = position.x.toInt()
                val y = position.y.toInt()
                var z = position.z.toInt()
                if (j == 0) {
                    --x
                } else if (j == 1) {
                    ++x
                } else if (j == 2) {
                    --z
                } else {
                    ++z
                }
                val block = level.getBlock(x, y, z)
                if (!this.canFlowInto(block)) {
                    flowCostVisited.put(
                        blockHash(
                            x,
                            y,
                            z,
                            level
                        ), BLOCKED
                    )
                } else if (if (usesWaterLogging()) level.getBlock(x, y - 1, z)
                        .canWaterloggingFlowInto() else level.getBlock(x, y - 1, z)
                        .canBeFlowedInto()
                ) {
                    flowCostVisited.put(
                        blockHash(
                            x,
                            y,
                            z,
                            level
                        ), CAN_FLOW_DOWN
                    )
                    maxCost = 0
                    flowCost[j] = maxCost
                } else if (maxCost > 0) {
                    flowCostVisited.put(
                        blockHash(
                            x,
                            y,
                            z,
                            level
                        ), CAN_FLOW
                    )
                    flowCost[j] = this.calculateFlowCost(x, y, z, 1, maxCost, j xor 0x01, j xor 0x01)
                    maxCost = min(maxCost.toDouble(), flowCost[j].toDouble()).toInt()
                }
            }
            flowCostVisited.clear()
            var minCost = Double.MAX_VALUE
            for (i in 0..3) {
                val d = flowCost[i].toDouble()
                if (d < minCost) {
                    minCost = d
                }
            }
            val isOptimalFlowDirection = BooleanArray(4)
            for (i in 0..3) {
                isOptimalFlowDirection[i] = (flowCost[i].toDouble() == minCost)
            }
            return isOptimalFlowDirection
        }

    private fun getSmallestFlowDecay(block: Block, decay: Int): Int {
        var blockDecay = this.getFlowDecay(block)
        if (blockDecay < 0) {
            return decay
        } else if (blockDecay == 0) {
            ++this.adjacentSources
        } else if (blockDecay >= 8) {
            blockDecay = 0
        }
        return if (decay >= 0 && blockDecay >= decay) decay else blockDecay
    }

    /**
     * Handle for mixing function between fluids,
     * which is currently used to handle with the mixing of lava with water
     */
    protected open fun checkForMixing() {
    }

    protected fun triggerLavaMixEffects(pos: Vector3) {
        val random: Random = ThreadLocalRandom.current()
        level.addLevelEvent(
            pos.add(0.5, 0.5, 0.5),
            LevelEventPacket.SOUND_FIZZ,
            ((random.nextFloat() - random.nextFloat()) * 800).toInt() + 2600
        )

        for (i in 0..7) {
            level.addParticle(SmokeParticle(pos.add(Math.random(), 1.2, Math.random())))
        }
    }

    /**
     * Gets a liquid block instance with a new depth.
     *
     * @param depth the new depth
     */
    abstract fun getLiquidWithNewDepth(depth: Int): BlockLiquid

    override fun canPassThrough(): Boolean {
        return true
    }

    override fun onEntityCollide(entity: Entity) {
        entity.resetFallDistance()
    }


    fun liquidCollide(cause: Block?, result: Block): Boolean {
        val event = BlockFromToEvent(this, result)
        Server.instance.pluginManager.callEvent(event)
        if (event.cancelled) {
            return false
        }
        level.setBlock(this.position, event.to, true, true)
        level.setBlock(this.position, 1, get(BlockID.AIR), true, true)
        level.addSound(position.add(0.5, 0.5, 0.5), Sound.RANDOM_FIZZ)
        return true
    }

    protected fun canFlowInto(block: Block): Boolean {
        if (usesWaterLogging()) {
            if (block.canWaterloggingFlowInto()) {
                val blockLayer1 = block.getLevelBlockAtLayer(1)
                return !(block is BlockLiquid && block.liquidDepth == 0) && !(blockLayer1 is BlockLiquid && blockLayer1.liquidDepth == 0)
            }
        }
        return block.canBeFlowedInto() && !(block is BlockLiquid && block.liquidDepth == 0)
    }

    override fun toItem(): Item {
        return Item.AIR
    }

    override fun breaksWhenMoved(): Boolean {
        return true
    }

    override fun sticksToPiston(): Boolean {
        return false
    }

    var liquidDepth: Int
        /**
         * If bit 0x8 is set, this fluid is "falling" and spreads only downward. At this level, the lower bits are essentially ignored, since this block is then at its highest fluid level. This level is equal to the falling water above, equal to 8 plus the level of the non-falling lava above it.
         *
         *
         * The lower three bits are the fluid block's level. 0 is the highest fluid level (not necessarily filling the block - this depends on the neighboring fluid blocks above each upper corner of the block). Data values increase as the fluid level of the block drops: 1 is the next highest, 2 lower, on through 7, the lowest fluid level. Along a line on a flat plane, water drops one level per meter.
         */
        get() = getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.LIQUID_DEPTH)
        set(liquidDepth) {
            setPropertyValue<Int, IntPropertyType>(CommonBlockProperties.LIQUID_DEPTH, liquidDepth)
        }

    val isSource: Boolean
        get() = liquidDepth == 0

    val isFlowingDown: Boolean
        get() = liquidDepth >= 8

    val isSourceOrFlowingDown: Boolean
        get() {
            val liquidDepth = liquidDepth
            return liquidDepth == 0 || liquidDepth == 8
        }

    override val lightFilter: Int
        get() = 2

    override val walkThroughExtraCost: Int
        get() = 20

    companion object {
        private const val CAN_FLOW_DOWN: Byte = 1
        private const val CAN_FLOW: Byte = 0
        private const val BLOCKED: Byte = -1
    }
}
