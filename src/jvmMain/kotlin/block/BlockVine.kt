package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.block.BlockGrowEvent
import org.chorus_oss.chorus.event.block.BlockSpreadEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemBlock
import org.chorus_oss.chorus.item.ItemTool
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.BlockFace.Companion.random
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.max
import kotlin.math.min

class BlockVine @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockTransparent(blockstate) {
    override val name: String
        get() = "Vines"

    override val hardness: Double
        get() = 0.2

    override val resistance: Double
        get() = 1.0

    override fun canPassThrough(): Boolean {
        return true
    }

    override fun hasEntityCollision(): Boolean {
        return true
    }

    override fun canBeReplaced(): Boolean {
        return true
    }

    override fun canBeClimbed(): Boolean {
        return true
    }

    override val waterloggingLevel: Int
        get() = 1

    override fun canBeFlowedInto(): Boolean {
        return true
    }

    override fun onEntityCollide(entity: Entity) {
        entity.resetFallDistance()
        entity.onGround = true
    }

    override val isSolid: Boolean
        get() = false

    override fun isSolid(side: BlockFace): Boolean {
        return false
    }

    override fun recalculateBoundingBox(): AxisAlignedBB {
        var f1 = 1.0
        var f2 = 1.0
        var f3 = 1.0
        var f4 = 0.0
        var f5 = 0.0
        var f6 = 0.0
        var flag = blockState.specialValue() > 0
        if ((blockState.specialValue().toInt() and 0x02) > 0) {
            f4 = max(f4, 0.0625)
            f1 = 0.0
            f2 = 0.0
            f5 = 1.0
            f3 = 0.0
            f6 = 1.0
            flag = true
        }
        if ((blockState.specialValue().toInt() and 0x08) > 0) {
            f1 = min(f1, 0.9375)
            f4 = 1.0
            f2 = 0.0
            f5 = 1.0
            f3 = 0.0
            f6 = 1.0
            flag = true
        }
        if ((blockState.specialValue().toInt() and 0x01) > 0) {
            f3 = min(f3, 0.9375)
            f6 = 1.0
            f1 = 0.0
            f4 = 1.0
            f2 = 0.0
            f5 = 1.0
            flag = true
        }
        if (!flag && up().isSolid) {
            f2 = min(f2, 0.9375)
            f5 = 1.0
            f1 = 0.0
            f4 = 1.0
            f3 = 0.0
            f6 = 1.0
        }
        return SimpleAxisAlignedBB(
            position.x + f1,
            position.y + f2,
            position.z + f3,
            position.x + f4,
            position.y + f5,
            position.z + f6
        )
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
        if ((block.id != BlockID.VINE) && target!!.isSolid && face.horizontalIndex != -1) {
            this.setPropertyValue<Int, IntPropertyType>(
                CommonBlockProperties.VINE_DIRECTION_BITS, getMetaFromFace(
                    face.getOpposite()
                )
            )
            level.setBlock(block.position, this, true, true)
            return true
        }

        return false
    }

    override fun getDrops(item: Item): Array<Item> {
        return if (item.isShears) {
            arrayOf(
                toItem()
            )
        } else {
            Item.EMPTY_ARRAY
        }
    }

    override fun toItem(): Item {
        return ItemBlock(this.blockState, name, 0)
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            val up = this.up()
            val upFaces: Set<BlockFace>? = if (up is BlockVine) up.faces else null
            val faces = this.faces
            for (face in BlockFace.Plane.HORIZONTAL_FACES) {
                if (!getSide(face).isSolid && (upFaces == null || !upFaces.contains(face))) {
                    faces.remove(face)
                }
            }
            if (faces.isEmpty() && !up.isSolid) {
                level.useBreakOn(this.position, null, null, true)
                return Level.BLOCK_UPDATE_NORMAL
            }
            val meta = getMetaFromFaces(faces)
            if (meta != blockState.specialValue().toInt()) {
                level.setBlock(
                    this.position,
                    get(BlockID.VINE).setPropertyValue<Int, IntPropertyType>(
                        CommonBlockProperties.VINE_DIRECTION_BITS,
                        meta
                    ),
                    true
                )
                return Level.BLOCK_UPDATE_NORMAL
            }
        } else if (type == Level.BLOCK_UPDATE_RANDOM) {
            val random: Random = ThreadLocalRandom.current()
            if (random.nextInt(4) == 0) {
                val face = random(random)
                val block = this.getSide(face)
                val faceMeta = getMetaFromFace(face)
                var meta = blockState.specialValue().toInt()

                if (position.y < 255 && face == BlockFace.UP && block.isAir) {
                    if (this.canSpread()) {
                        for (horizontalFace in BlockFace.Plane.HORIZONTAL_FACES) {
                            if (random.nextBoolean() || !getSide(horizontalFace).getSide(face).isSolid) {
                                meta = meta and getMetaFromFace(horizontalFace).inv()
                            }
                        }
                        putVineOnHorizontalFace(block, meta, this)
                    }
                } else if (face.horizontalIndex != -1 && (meta and faceMeta) != faceMeta) {
                    if (this.canSpread()) {
                        if (block.isAir) {
                            val cwFace = face.rotateY()
                            val ccwFace = face.rotateYCCW()
                            val cwBlock = block.getSide(cwFace)
                            val ccwBlock = block.getSide(ccwFace)
                            val cwMeta = getMetaFromFace(cwFace)
                            val ccwMeta = getMetaFromFace(ccwFace)
                            val onCw = (meta and cwMeta) == cwMeta
                            val onCcw = (meta and ccwMeta) == ccwMeta

                            if (onCw && cwBlock.isSolid) {
                                putVine(block, getMetaFromFace(cwFace), this)
                            } else if (onCcw && ccwBlock.isSolid) {
                                putVine(block, getMetaFromFace(ccwFace), this)
                            } else if (onCw && cwBlock.isAir && getSide(cwFace).isSolid) {
                                putVine(cwBlock, getMetaFromFace(face.getOpposite()), this)
                            } else if (onCcw && ccwBlock.isAir && getSide(ccwFace).isSolid) {
                                putVine(ccwBlock, getMetaFromFace(face.getOpposite()), this)
                            } else if (block.up().isSolid) {
                                putVine(block, 0, this)
                            }
                        } else if (!block.isTransparent) {
                            meta = meta or getMetaFromFace(face)
                            putVine(this, meta, null)
                        }
                    }
                } else if (position.y > 0) {
                    val below = this.down()
                    val id = below.id
                    if (id == BlockID.AIR || id == BlockID.VINE) {
                        for (horizontalFace in BlockFace.Plane.HORIZONTAL_FACES) {
                            if (random.nextBoolean()) {
                                meta = meta and getMetaFromFace(horizontalFace).inv()
                            }
                        }
                        putVineOnHorizontalFace(
                            below,
                            below.blockState.specialValue().toInt() or meta,
                            if (id == BlockID.AIR) this else null
                        )
                    }
                }
                return Level.BLOCK_UPDATE_RANDOM
            }
        }
        return 0
    }

    private fun canSpread(): Boolean {
        val blockX = position.floorX
        val blockY = position.floorY
        val blockZ = position.floorZ

        var count = 0
        for (x in blockX - 4..blockX + 4) {
            for (z in blockZ - 4..blockZ + 4) {
                for (y in blockY - 1..blockY + 1) {
                    if (level.getBlock(x, y, z).id == BlockID.VINE) {
                        if (++count >= 5) return false
                    }
                }
            }
        }
        return true
    }

    private fun putVine(block: Block, meta: Int, source: Block?) {
        if (block.id == BlockID.VINE && block.blockState.specialValue().toInt() == meta) return
        val vine =
            get(BlockID.VINE).setPropertyValue<Int, IntPropertyType>(CommonBlockProperties.VINE_DIRECTION_BITS, meta)
        val event = if (source != null) {
            BlockSpreadEvent(block, source, vine)
        } else {
            BlockGrowEvent(block, vine)
        }
        Server.instance.pluginManager.callEvent(event)
        if (!event.cancelled) {
            level.setBlock(block.position, vine, true)
        }
    }

    private fun putVineOnHorizontalFace(block: Block, meta: Int, source: Block?) {
        if (block.id == BlockID.VINE && block.blockState.specialValue().toInt() == meta) return
        var isOnHorizontalFace = false
        for (face in BlockFace.Plane.HORIZONTAL_FACES) {
            val faceMeta = getMetaFromFace(face)
            if ((meta and faceMeta) == faceMeta) {
                isOnHorizontalFace = true
                break
            }
        }
        if (isOnHorizontalFace) {
            putVine(block, meta, source)
        }
    }

    private val faces: MutableSet<BlockFace>
        get() {
            val faces: MutableSet<BlockFace> = EnumSet.noneOf(BlockFace::class.java)

            val meta = blockState.specialValue().toInt()
            if ((meta and 1) > 0) {
                faces.add(BlockFace.SOUTH)
            }
            if ((meta and 2) > 0) {
                faces.add(BlockFace.WEST)
            }
            if ((meta and 4) > 0) {
                faces.add(BlockFace.NORTH)
            }
            if ((meta and 8) > 0) {
                faces.add(BlockFace.EAST)
            }

            return faces
        }

    override val toolType: Int
        get() = ItemTool.TYPE_AXE

    override fun breaksWhenMoved(): Boolean {
        return true
    }

    override fun sticksToPiston(): Boolean {
        return false
    }

    override fun canSilkTouch(): Boolean {
        return true
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.VINE, CommonBlockProperties.VINE_DIRECTION_BITS)


        fun getMetaFromFaces(faces: Set<BlockFace>): Int {
            var meta = 0
            for (face in faces) {
                meta = meta or getMetaFromFace(face)
            }
            return meta
        }

        fun getMetaFromFace(face: BlockFace): Int {
            return when (face) {
                BlockFace.WEST -> 0x02
                BlockFace.NORTH -> 0x04
                BlockFace.EAST -> 0x08
                else -> 0x01
            }
        }
    }
}
