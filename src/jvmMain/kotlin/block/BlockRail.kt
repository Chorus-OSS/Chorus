package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.BooleanPropertyType
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemBlock
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.fromHorizontalIndex
import org.chorus_oss.chorus.utils.Faceable
import org.chorus_oss.chorus.utils.Rail
import org.chorus_oss.chorus.utils.Rail.Orientation.Companion.ascending
import org.chorus_oss.chorus.utils.Rail.Orientation.Companion.curved
import org.chorus_oss.chorus.utils.Rail.Orientation.Companion.straight
import org.chorus_oss.chorus.utils.Rail.Orientation.Companion.straightOrCurved
import java.util.function.Consumer
import java.util.stream.Stream

open class BlockRail @JvmOverloads constructor(blockState: BlockState = properties.defaultState) :
    BlockFlowable(blockState), Faceable {
    // 0x8: Set the block active
    // 0x7: Reset the block to normal
    // If the rail can be powered. So it's a complex rail!
    protected var canBePowered: Boolean = false

    override val name: String
        get() = "Rail"

    override val hardness: Double
        get() = 0.7

    override val resistance: Double
        get() = 3.5

    override fun canPassThrough(): Boolean {
        return true
    }

    override val toolType: Int
        get() = ItemTool.TYPE_PICKAXE

    override fun sticksToPiston(): Boolean {
        return true
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            val ascendingDirection = getOrientation()!!.ascendingDirection()
            if (!checkCanBePlace(this.down()) || (ascendingDirection.isPresent && !checkCanBePlace(
                    this.getSide(
                        ascendingDirection.get()
                    )
                ))
            ) {
                level.useBreakOn(this.position)
                return Level.BLOCK_UPDATE_NORMAL
            }
        }
        if (type == Level.BLOCK_UPDATE_REDSTONE && getRailDirection().isCurved) {
            val connect = checkRailsConnected().values
            val railFace: MutableList<BlockFace?> = ArrayList()
            for (face in connect) {
                if (getSide(face.getOpposite()) is BlockRail) {
                    railFace.add(face.getOpposite())
                } else {
                    railFace.add(face)
                }
            }
            val orient = if (railFace.contains(BlockFace.SOUTH)) {
                if (railFace.contains(BlockFace.EAST)) {
                    Rail.Orientation.CURVED_SOUTH_EAST
                } else Rail.Orientation.CURVED_SOUTH_WEST
            } else {
                if (railFace.contains(BlockFace.EAST)) {
                    Rail.Orientation.CURVED_NORTH_EAST
                } else Rail.Orientation.CURVED_NORTH_WEST
            }
            setOrientation(orient)
        }
        return 0
    }

    override var maxY: Double
        get() = position.y + 0.125
        set(maxY) {
            super.maxY = maxY
        }

    public override fun recalculateBoundingBox(): AxisAlignedBB? {
        return this
    }

    //Information from http://minecraft.wiki/w/Rail
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
        val down = this.down()
        if (!checkCanBePlace(down)) {
            return false
        }
        val railsAround = this.checkRailsAroundAffected()
        val rails: List<BlockRail> = ArrayList(railsAround.keys)
        val faces: List<BlockFace?> = ArrayList(railsAround.values)
        if (railsAround.size == 1) {
            val other = rails[0]
            this.setRailDirection(this.connect(other, railsAround[other]!!))
        } else if (railsAround.size == 4) {
            if (this.isAbstract) {
                this.setRailDirection(
                    this.connect(
                        rails[faces.indexOf(BlockFace.SOUTH)],
                        BlockFace.SOUTH,
                        rails[faces.indexOf(BlockFace.EAST)],
                        BlockFace.EAST
                    )
                )
            } else {
                this.setRailDirection(
                    this.connect(
                        rails[faces.indexOf(BlockFace.EAST)],
                        BlockFace.EAST,
                        rails[faces.indexOf(BlockFace.WEST)],
                        BlockFace.WEST
                    )
                )
            }
        } else if (railsAround.isNotEmpty()) {
            if (this.isAbstract) {
                if (railsAround.size == 2) {
                    val rail1 = rails[0]
                    val rail2 = rails[1]
                    this.setRailDirection(this.connect(rail1, railsAround[rail1]!!, rail2, railsAround[rail2]!!))
                } else {
                    val cd = Stream.of(
                        Rail.Orientation.CURVED_SOUTH_EAST,
                        Rail.Orientation.CURVED_NORTH_EAST,
                        Rail.Orientation.CURVED_SOUTH_WEST
                    )
                        .filter { o: Rail.Orientation -> HashSet(faces).containsAll(o.connectingDirections()) }
                        .findFirst().get().connectingDirections()
                    val f1 = cd[0]
                    val f2 = cd[1]
                    this.setRailDirection(this.connect(rails[faces.indexOf(f1)], f1, rails[faces.indexOf(f2)], f2))
                }
            } else {
                val f = faces.stream()
                    .min { f1: BlockFace?, f2: BlockFace? -> if (f1!!.index < f2!!.index) 1 else (if (position.x == position.y) 0 else -1) }
                    .get()
                val fo = f.getOpposite()
                if (faces.contains(fo)) { //Opposite connectable
                    this.setRailDirection(this.connect(rails[faces.indexOf(f)], f, rails[faces.indexOf(fo)], fo))
                } else {
                    this.setRailDirection(this.connect(rails[faces.indexOf(f)], f))
                }
            }
        }
        level.setBlock(this.position, this, direct = true, update = true)
        if (!isAbstract) {
            level.scheduleUpdate(this, this.position, 0)
        }

        return true
    }

    private fun checkCanBePlace(check: Block?): Boolean {
        if (check == null) {
            return false
        }
        return check.isSolid(BlockFace.UP) || check is BlockCauldron
    }

    private fun connect(rail1: BlockRail, face1: BlockFace, rail2: BlockRail, face2: BlockFace): Rail.Orientation {
        this.connect(rail1, face1)
        this.connect(rail2, face2)

        if (face1.getOpposite() == face2) {
            val delta1 = (position.y - rail1.position.y).toInt()
            val delta2 = (position.y - rail2.position.y).toInt()

            if (delta1 == -1) {
                return ascending(face1)
            } else if (delta2 == -1) {
                return ascending(face2)
            }
        }
        return straightOrCurved(face1, face2)
    }

    private fun connect(other: BlockRail, face: BlockFace): Rail.Orientation {
        val delta = (position.y - other.position.y).toInt()
        val rails = other.checkRailsConnected()
        if (rails.isEmpty()) { //Only one
            other.setOrientation(
                if (delta == 1) ascending(face.getOpposite()) else straight(
                    face
                )
            )
            return if (delta == -1) ascending(face) else straight(face)
        } else if (rails.size == 1) { //Already connected
            val faceConnected = rails.values.iterator().next()

            if (other.isAbstract && faceConnected != face) { //Curve!
                other.setOrientation(curved(face.getOpposite(), faceConnected))
                return if (delta == -1) ascending(face) else straight(face)
            } else if (faceConnected == face) { //Turn!
                if (!other.getOrientation()!!.isAscending) {
                    other.setOrientation(
                        if (delta == 1) ascending(face.getOpposite()) else straight(
                            face
                        )
                    )
                }
                return if (delta == -1) ascending(face) else straight(face)
            } else if (other.getOrientation()!!
                    .hasConnectingDirections(BlockFace.NORTH, BlockFace.SOUTH)
            ) { //North-south
                other.setOrientation(
                    if (delta == 1) ascending(face.getOpposite()) else straight(
                        face
                    )
                )
                return if (delta == -1) ascending(face) else straight(face)
            }
        }
        return Rail.Orientation.STRAIGHT_NORTH_SOUTH
    }

    private fun checkRailsAroundAffected(): Map<BlockRail, BlockFace?> {
        val railsAround =
            this.checkRailsAround(listOf(BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH))
        return railsAround.entries
            .filter { it.key.checkRailsConnected().size != 2 }
            .associate { it.key to it.value }
    }

    private fun checkRailsAround(faces: Collection<BlockFace>): Map<BlockRail, BlockFace> {
        val result: MutableMap<BlockRail, BlockFace> = HashMap()
        faces.forEach(Consumer { f: BlockFace ->
            val b = this.getSide(f)
            Stream.of<Block?>(b, b.up(), b.down())
                .filter { obj: Block? -> obj is BlockRail }
                .forEach { block: Block -> result[block as BlockRail] = f }
        })
        return result
    }

    protected fun checkRailsConnected(): Map<BlockRail, BlockFace> {
        val railsAround = this.checkRailsAround(
            getOrientation()!!.connectingDirections()
        )
        return railsAround.entries
            .filter { it.key.getOrientation()!!.hasConnectingDirections(it.value.getOpposite()) }
            .associate { it.key to it.value }
    }

    val isAbstract: Boolean
        get() = this.id == BlockID.RAIL

    fun canPowered(): Boolean {
        return this.canBePowered
    }

    /**
     * Changes the rail direction without changing anything else.
     *
     * @param orientation The new orientation
     */
    open fun setRailDirection(orientation: Rail.Orientation) {
        setPropertyValue(
            CommonBlockProperties.RAIL_DIRECTION_10,
            orientation.metadata()
        )
    }

    open fun getRailDirection(): Rail.Orientation {
        return getOrientation()!!
    }

    /**
     * Changes the rail direction and update the state in the world if the orientation changed in a single call.
     *
     *
     * Note that the level block won't change if the current block has already the given orientation.
     *
     * @see .setRailDirection
     * @see Level.setBlock
     */
    open fun setOrientation(orientation: Rail.Orientation) {
        if (orientation != getOrientation()) {
            setRailDirection(orientation)
            level.setBlock(this.position, this, direct = true, update = true)
        }
    }

    open fun getOrientation(): Rail.Orientation? {
        return Rail.Orientation.byMetadata(
            getPropertyValue(
                CommonBlockProperties.RAIL_DIRECTION_10
            )
        )
    }

    val realMeta: Int
        get() {
            // Check if this can be powered
            // Avoid modifying the value from meta (The rail orientation may be false)
            // Reason: When the rail is curved, the meta will return STRAIGHT_NORTH_SOUTH.
            // OR Null Pointer Exception
            if (!isAbstract) {
                return blockState.specialValue().toInt() and 0x7
            }
            // Return the default: This meta
            return blockState.specialValue().toInt()
        }

    open fun isActive(): Boolean {
        return properties.containProperty(CommonBlockProperties.ACTIVE) && getPropertyValue<Boolean, BooleanPropertyType>(
            CommonBlockProperties.ACTIVE
        )
    }

    /**
     * Changes the active flag and update the state in the world in a single call.
     *
     *
     * The active flag will not change if the block state don't have the [CommonBlockProperties.ACTIVE] property,
     * and it will not throw exceptions related to missing block properties.
     *
     *
     * The level block will always update.
     *
     * @see .setRailDirection
     * @see Level.setBlock
     */
    open fun setIsActive(active: Boolean) {
        if (properties.containProperty(CommonBlockProperties.ACTIVE)) {
            setRailActive(active)
        }
        level.setBlock(this.position, this, direct = true, update = true)
    }

    /**
     * @throws NoSuchElementException If attempt to set the rail to active but it don't have the [CommonBlockProperties.ACTIVE] property.
     */
    open fun setRailActive(active: Boolean) {
        if (!active && !properties.containProperty<Boolean, BooleanPropertyType>(CommonBlockProperties.ACTIVE)) {
            return
        }
        setPropertyValue<Boolean, BooleanPropertyType>(CommonBlockProperties.ACTIVE, active)
    }

    override fun toItem(): Item {
        return ItemBlock(this.blockState, name, 0)
    }

    override var blockFace: BlockFace
        get() = fromHorizontalIndex(blockState.specialValue().toInt() and 0x07)
        set(blockFace) {
            throw UnsupportedOperationException()
        }

    override fun breaksWhenMoved(): Boolean {
        return false
    }

    override val waterloggingLevel: Int
        get() = 1

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.RAIL, CommonBlockProperties.RAIL_DIRECTION_10)
    }
}
