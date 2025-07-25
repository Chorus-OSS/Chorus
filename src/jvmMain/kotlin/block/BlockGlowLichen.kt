package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.math.BlockFace

class BlockGlowLichen(blockState: BlockState = properties.defaultState) : BlockLichen(blockState) {
    override val name: String
        get() = "Glow Lichen"

    override fun onActivate(
        item: Item,
        player: Player?,
        blockFace: BlockFace,
        fx: Float,
        fy: Float,
        fz: Float
    ): Boolean {
        if (!item.isFertilizer) {
            return false
        }

        val candidates = candidates

        item.decrement(1)

        if (candidates.isEmpty()) {
            return true
        }

        val keySet = candidates.keys
        val keyList: List<Block?> = ArrayList(keySet)

        val rand = RANDOM.nextInt(0, candidates.size - 1)

        val random = keyList[rand]

        val newLichen = if (random!!.id == BlockID.GLOW_LICHEN) {
            random
        } else {
            get(BlockID.GLOW_LICHEN)
        }

        newLichen.setPropertyValue<Int, IntPropertyType>(
            CommonBlockProperties.MULTI_FACE_DIRECTION_BITS,
            newLichen.getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.MULTI_FACE_DIRECTION_BITS) or (1 shl candidates[random]!!.indexDUSWNE)
        )

        level.setBlock(random.position, newLichen, true, true)

        return true
    }

    private val candidates: Map<Block?, BlockFace>
        get() {
            val candidates: MutableMap<Block?, BlockFace> =
                HashMap()
            for (side in BlockFace.entries) {
                val support = this.getSide(side)

                if (isGrowthToSide(side)) {
                    val supportSides = side.edges.toTypedArray<BlockFace>()

                    for (supportSide in supportSides) {
                        val supportNeighbor = support.getSide(supportSide)

                        if (!isSupportNeighborAdded(candidates, supportSide.getOpposite(), supportNeighbor)) {
                            continue
                        }

                        val supportNeighborOppositeSide =
                            supportNeighbor.getSide(side.getOpposite())
                        if (shouldAddSupportNeighborOppositeSide(side, supportNeighborOppositeSide)) {
                            candidates[supportNeighborOppositeSide] = side
                        }
                    }
                } else {
                    if (support.isSolid) {
                        candidates[this] = side
                    }
                }
            }
            return candidates
        }

    override fun canBeActivated(): Boolean {
        return true
    }

    override val lightLevel: Int
        get() = 7

    private fun isSupportNeighborAdded(
        candidates: MutableMap<Block?, BlockFace>,
        side: BlockFace,
        supportNeighbor: Block
    ): Boolean {
        // Air is a valid candidate!
        if (supportNeighbor.id == BlockID.AIR) {
            candidates[supportNeighbor] = side
        }

        // Other non-solid blocks isn't a valid candidates
        return supportNeighbor.isSolid(side)
    }

    private fun shouldAddSupportNeighborOppositeSide(side: BlockFace, supportNeighborOppositeSide: Block): Boolean {
        if (supportNeighborOppositeSide.id == BlockID.AIR || supportNeighborOppositeSide.id == BlockID.GLOW_LICHEN) {
            return supportNeighborOppositeSide.id != BlockID.GLOW_LICHEN ||
                    (!(supportNeighborOppositeSide as BlockGlowLichen).isGrowthToSide(side) && supportNeighborOppositeSide.getSide(
                        side
                    ).id != BlockID.AIR)
        }
        return false
    }

    override val isFertilizable: Boolean
        get() = true

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.GLOW_LICHEN, CommonBlockProperties.MULTI_FACE_DIRECTION_BITS)
    }
}
