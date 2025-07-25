package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.Attachment
import org.chorus_oss.chorus.block.property.enums.WallConnectionType
import org.chorus_oss.chorus.block.property.type.BooleanPropertyType
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.math.VectorMath.calculateAxis
import org.chorus_oss.chorus.math.VectorMath.calculateFace
import org.chorus_oss.chorus.utils.Faceable
import org.chorus_oss.chorus.utils.Loggable
import java.util.*


abstract class BlockWallBase(blockstate: BlockState) : BlockTransparent(blockstate), BlockConnectable, Loggable {
    override val isSolid: Boolean
        get() = false

    override fun isSolid(side: BlockFace): Boolean {
        return false
    }

    override val hardness: Double
        get() = 2.0

    override val resistance: Double
        get() = 30.0

    override val waterloggingLevel: Int
        get() = 1

    private fun shouldBeTall(above: Block, face: BlockFace): Boolean {
        return when (above.id) {
            BlockID.AIR, BlockID.SKULL -> false
            BlockID.BELL -> {
                val bell = above as BlockBell
                bell.attachment == Attachment.STANDING
                        && bell.blockFace.axis != face.axis
            }

            else -> {
                when (above) {
                    is BlockWallBase -> {
                        above.getConnectionType(face) != WallConnectionType.NONE
                    }

                    is BlockConnectable -> {
                        (above as BlockConnectable).isConnected(face)
                    }

                    is BlockPressurePlateBase, is BlockStairs -> {
                        true
                    }

                    else -> above.isSolid && !above.isTransparent || shouldBeTallBasedOnBoundingBox(above, face)
                }
            }
        }
    }

    private fun shouldBeTallBasedOnBoundingBox(above: Block, face: BlockFace): Boolean {
        var boundingBox = above.boundingBox ?: return false
        boundingBox = boundingBox.getOffsetBoundingBox(-above.position.x, -above.position.y, -above.position.z)
        if (boundingBox.minY > 0) {
            return false
        }
        var offset = face.xOffset
        if (offset < 0) {
            return boundingBox.minX < MIN_POST_BB && boundingBox.minZ < MIN_POST_BB && MAX_POST_BB < boundingBox.maxZ
        } else if (offset > 0) {
            return MAX_POST_BB < boundingBox.maxX && MAX_POST_BB < boundingBox.maxZ && boundingBox.minZ < MAX_POST_BB
        } else {
            offset = face.zOffset
            if (offset < 0) {
                return boundingBox.minZ < MIN_POST_BB && boundingBox.minX < MIN_POST_BB && MIN_POST_BB < boundingBox.maxX
            } else if (offset > 0) {
                return MAX_POST_BB < boundingBox.maxZ && MAX_POST_BB < boundingBox.maxX && boundingBox.minX < MAX_POST_BB
            }
        }
        return false
    }

    fun autoConfigureState(): Boolean {
        val previousMeta = blockState.specialValue()

        isWallPost = true

        val above = up(1, 0)

        for (blockFace in BlockFace.Plane.HORIZONTAL_FACES) {
            val side = getSideAtLayer(0, blockFace)
            if (canConnect(side)) {
                try {
                    connect(blockFace, above, false)
                } catch (e: RuntimeException) {
                    log.error(
                        "Failed to connect the block {} at {} to {} which is {} at {}",
                        this, locator, blockFace, side, side.locator, e
                    )
                    throw e
                }
            } else {
                disconnect(blockFace)
            }
        }

        recheckPostConditions(above)
        return blockState.specialValue() != previousMeta
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (autoConfigureState()) {
                level.setBlock(this.position, this, true)
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
        autoConfigureState()
        return super.place(item, block, target, face, fx, fy, fz, player)
    }

    var isWallPost: Boolean
        get() = getPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.WALL_POST_BIT)
        set(wallPost) {
            setPropertyValue<Boolean, BooleanPropertyType>(
                CommonBlockProperties.WALL_POST_BIT,
                wallPost
            )
        }

    fun clearConnections() {
        setPropertyValue(CommonBlockProperties.WALL_CONNECTION_TYPE_EAST, WallConnectionType.NONE)
        setPropertyValue(CommonBlockProperties.WALL_CONNECTION_TYPE_WEST, WallConnectionType.NONE)
        setPropertyValue(CommonBlockProperties.WALL_CONNECTION_TYPE_NORTH, WallConnectionType.NONE)
        setPropertyValue(CommonBlockProperties.WALL_CONNECTION_TYPE_SOUTH, WallConnectionType.NONE)
    }

    val wallConnections: Map<BlockFace, WallConnectionType>
        get() {
            val connections =
                EnumMap<BlockFace, WallConnectionType>(
                    BlockFace::class.java
                )
            for (blockFace in BlockFace.Plane.HORIZONTAL_FACES) {
                val connectionType = getConnectionType(blockFace)
                if (connectionType != WallConnectionType.NONE) {
                    connections[blockFace] = connectionType
                }
            }
            return connections
        }

    fun getConnectionType(blockFace: BlockFace): WallConnectionType {
        return when (blockFace) {
            BlockFace.NORTH -> getPropertyValue(CommonBlockProperties.WALL_CONNECTION_TYPE_NORTH)
            BlockFace.SOUTH -> getPropertyValue(CommonBlockProperties.WALL_CONNECTION_TYPE_SOUTH)
            BlockFace.WEST -> getPropertyValue(CommonBlockProperties.WALL_CONNECTION_TYPE_WEST)
            BlockFace.EAST -> getPropertyValue(CommonBlockProperties.WALL_CONNECTION_TYPE_EAST)
            else -> WallConnectionType.NONE
        }
    }

    fun setConnection(blockFace: BlockFace, type: WallConnectionType): Boolean {
        return when (blockFace) {
            BlockFace.NORTH -> {
                setPropertyValue(CommonBlockProperties.WALL_CONNECTION_TYPE_NORTH, type)
                true
            }

            BlockFace.SOUTH -> {
                setPropertyValue(CommonBlockProperties.WALL_CONNECTION_TYPE_SOUTH, type)
                true
            }

            BlockFace.WEST -> {
                setPropertyValue(CommonBlockProperties.WALL_CONNECTION_TYPE_WEST, type)
                true
            }

            BlockFace.EAST -> {
                setPropertyValue(CommonBlockProperties.WALL_CONNECTION_TYPE_EAST, type)
                true
            }

            else -> false
        }
    }

    /**
     * @return true if it should be a post
     */
    fun autoUpdatePostFlag() {
        isWallPost = recheckPostConditions(up(1, 0))
    }

    fun hasConnections(): Boolean {
        return getPropertyValue(CommonBlockProperties.WALL_CONNECTION_TYPE_EAST) != WallConnectionType.NONE || getPropertyValue(
            CommonBlockProperties.WALL_CONNECTION_TYPE_WEST
        ) != WallConnectionType.NONE || getPropertyValue(
            CommonBlockProperties.WALL_CONNECTION_TYPE_NORTH
        ) != WallConnectionType.NONE || getPropertyValue(
            CommonBlockProperties.WALL_CONNECTION_TYPE_SOUTH
        ) != WallConnectionType.NONE
    }

    private fun recheckPostConditions(above: Block): Boolean {
        // If nothing is connected, it should be a post
        if (!hasConnections()) {
            return true
        }

        // If it's not straight, it should be a post
        val connections = wallConnections
        if (connections.size != 2) {
            return true
        }

        val iterator = connections.entries.iterator()
        val entryA = iterator.next()
        val entryB = iterator.next()
        if (entryA.value != entryB.value || entryA.key.getOpposite() != entryB.key) {
            return true
        }

        val axis = entryA.key.axis

        when (above.id) {
            BlockID.FLOWER_POT, BlockID.SKULL, BlockID.CONDUIT, BlockID.STANDING_BANNER, BlockID.TURTLE_EGG -> {
                return true
            }

            BlockID.END_ROD -> {
                if ((above as Faceable).blockFace == BlockFace.UP) {
                    return true
                }
            }

            BlockID.BELL -> {
                val bell = above as BlockBell
                if (bell.attachment == Attachment.STANDING
                    && bell.blockFace.axis == axis
                ) {
                    return true
                }
            }

            else -> {
                if (above is BlockWallBase) {
                    // If the wall above is a post, it should also be a post

                    if (above.isWallPost) {
                        return true
                    }
                } else if (above is BlockLantern) {
                    // Lanterns makes this become a post if they are not hanging

                    if (!above.isHanging) {
                        return true
                    }
                } else if (above.id == BlockID.LEVER || above is BlockTorch || above is BlockButton) {
                    // These blocks make this become a post if they are placed down (facing up)

                    if ((above as Faceable).blockFace == BlockFace.UP) {
                        return true
                    }
                } else if (above is BlockFenceGate) {
                    // If the gate don't follow the path, make it a post

                    if ((above as Faceable).blockFace.axis == axis) {
                        return true
                    }
                } else if (above is BlockConnectable) {
                    // If the connectable block above don't share 2 equal connections, then this should be a post

                    var shared = 0
                    for (connection in (above as BlockConnectable).connections) {
                        if (connections.containsKey(connection) && ++shared == 2) {
                            break
                        }
                    }

                    if (shared < 2) {
                        return true
                    }
                }
            }
        }

        // Sign posts always makes the wall become a post
        return above is BlockStandingSign
    }

    val isSameHeightStraight: Boolean
        get() {
            val connections = wallConnections
            if (connections.size != 2) {
                return false
            }

            val iterator =
                connections.entries.iterator()
            val a = iterator.next()
            val b = iterator.next()
            return a.value == b.value && a.key.getOpposite() == b.key
        }

    @JvmOverloads
    fun connect(blockFace: BlockFace, recheckPost: Boolean = true): Boolean {
        if (blockFace.horizontalIndex < 0) {
            return false
        }

        val above = getSideAtLayer(0, BlockFace.UP)
        return connect(blockFace, above, recheckPost)
    }

    private fun connect(blockFace: BlockFace, above: Block, recheckPost: Boolean): Boolean {
        val type = if (shouldBeTall(above, blockFace)) WallConnectionType.TALL else WallConnectionType.SHORT
        if (setConnection(blockFace, type)) {
            if (recheckPost) {
                recheckPostConditions(above)
            }
            return true
        }
        return false
    }

    fun disconnect(blockFace: BlockFace): Boolean {
        if (blockFace.horizontalIndex < 0) {
            return false
        }

        if (setConnection(blockFace, WallConnectionType.NONE)) {
            autoUpdatePostFlag()
            return true
        }
        return false
    }

    override fun recalculateBoundingBox(): AxisAlignedBB? {
        val north = this.canConnect(this.getSide(BlockFace.NORTH))
        val south = this.canConnect(this.getSide(BlockFace.SOUTH))
        val west = this.canConnect(this.getSide(BlockFace.WEST))
        val east = this.canConnect(this.getSide(BlockFace.EAST))

        var n = if (north) 0.0 else 0.25
        var s = if (south) 1.0 else 0.75
        var w = if (west) 0.0 else 0.25
        var e = if (east) 1.0 else 0.75

        if (north && south && !west && !east) {
            w = 0.3125
            e = 0.6875
        } else if (!north && !south && west && east) {
            n = 0.3125
            s = 0.6875
        }

        return SimpleAxisAlignedBB(
            position.x + w,
            position.y,
            position.z + n,
            position.x + e,
            position.y + 1.5,
            position.z + s
        )
    }

    override fun canConnect(block: Block?): Boolean {
        if (block != null) {
            when (block.id) {
                BlockID.GLASS_PANE, BlockID.IRON_BARS, BlockID.GLASS -> {
                    return true
                }

                else -> {
                    if (block is BlockGlassStained || block is BlockGlassPaneStained || block is BlockWallBase) {
                        return true
                    }
                    if (block is BlockFenceGate) {
                        return block.blockFace.axis != calculateAxis(this.position, block.position)
                    }
                    if (block is BlockStairs) {
                        return block.blockFace.getOpposite() == calculateFace(this.position, block.position)
                    }
                    if (block is BlockTrapdoor) {
                        return block.isOpen && block.blockFace == calculateFace(this.position, block.position)
                    }
                    return block.isSolid && !block.isTransparent
                }
            }
        }
        return false
    }

    override fun isConnected(face: BlockFace): Boolean {
        return getConnectionType(face) != WallConnectionType.NONE
    }

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override fun canHarvestWithHand(): Boolean {
        return false
    }

    companion object {
        private const val MIN_POST_BB = 5.0 / 16
        private const val MAX_POST_BB = 11.0 / 16
    }
}
