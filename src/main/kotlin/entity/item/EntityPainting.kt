package org.chorus_oss.chorus.entity.item

import com.google.common.collect.Sets
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.blockentity.BlockEntityPistonArm
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityHanging
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.item.ItemPainting
import org.chorus_oss.chorus.level.GameRule
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.protocol.core.Packet
import org.chorus_oss.protocol.types.Vector3f
import java.util.function.Function
import java.util.function.Predicate


class EntityPainting(chunk: IChunk?, nbt: CompoundTag?) : EntityHanging(chunk, nbt) {
    override fun getEntityIdentifier(): String {
        return EntityID.PAINTING
    }


    fun interface PaintingPlacePredicate {
        fun test(level: Level, blockFace: BlockFace, block: Block, target: Block): Boolean
    }

    private var motive: Motive? = null

    private var width: Float = 0f
    private var length: Float = 0f
    private var height: Float = 0f

    override fun getWidth(): Float {
        return width
    }

    override fun getLength(): Float {
        return length
    }

    override fun getHeight(): Float {
        return height
    }

    override fun onUpdate(currentTick: Int): Boolean {
        val b: Boolean = super.onUpdate(currentTick)
        if (currentTick % 20 == 0) {
            val tickCachedCollisionBlocks: Array<Block> = level!!.getTickCachedCollisionBlocks(
                this.getBoundingBox(), false, false,
                Predicate { bl: Block -> !bl.isAir })
            if (tickCachedCollisionBlocks.size < (getMotive()!!.height * getMotive()!!.width)) {
                level!!.dropItem(this.position, ItemPainting())
                this.close()
                return false
            }
        }
        return b
    }

    override fun initEntity() {
        this.motive = getMotive(namedTag!!.getString("Motive"))

        if (this.motive != null) {
            val face: BlockFace = getHorizontalFacing()

            val size: Vector3 = Vector3(
                motive!!.width.toDouble(),
                motive!!.height.toDouble(), motive!!.width.toDouble()
            ).multiply(0.5)

            if (face.axis == BlockFace.Axis.Z) {
                size.z = 0.5
            } else {
                size.x = 0.5
            }

            this.width = size.x.toFloat()
            this.length = size.z.toFloat()
            this.height = size.y.toFloat()

            this.boundingBox = SimpleAxisAlignedBB(
                position.x - size.x,
                position.y - size.y,
                position.z - size.z,
                position.x + size.x,
                position.y + size.y,
                position.z + size.z
            )
        } else {
            this.width = 0f
            this.height = 0f
            this.length = 0f
        }

        super.initEntity()
    }

    public override fun createAddEntityPacket(): Packet {
        return org.chorus_oss.protocol.packets.AddPaintingPacket(
            actorUniqueID = this.uniqueId,
            actorRuntimeID = this.runtimeId.toULong(),
            position = Vector3f(this.position),
            direction = this.getDirection().horizontalIndex,
            motif = namedTag.getString("Motive"),
        )
    }

    override fun attack(source: EntityDamageEvent): Boolean {
        if (super.attack(source)) {
            if (source is EntityDamageByEntityEvent) {
                val damager: Entity = source.damager
                if (damager is Player && (damager.isAdventure || damager.isSurvival) && level!!.gameRules
                        .getBoolean(GameRule.DO_ENTITY_DROPS)
                ) {
                    level!!.dropItem(this.position, ItemPainting())
                }
            }
            this.close()
            return true
        } else {
            return false
        }
    }

    override fun saveNBT() {
        super.saveNBT()
        namedTag!!.putString("Motive", motive!!.title)
    }

    override fun onPushByPiston(piston: BlockEntityPistonArm?) {
        if (level!!.gameRules.getBoolean(GameRule.DO_ENTITY_DROPS)) {
            level!!.dropItem(this.position, ItemPainting())
        }

        this.close()
    }

    fun getMotive(): Motive? {
        return Motive.BY_NAME.get(namedTag!!.getString("Motive"))
    }

    override fun getOriginalName(): String {
        return "Painting"
    }

    enum class Motive {
        KEBAB("Kebab", 1, 1),
        AZTEC("Aztec", 1, 1),
        ALBAN("Alban", 1, 1),
        AZTEC2("Aztec2", 1, 1),
        BOMB("Bomb", 1, 1),
        PLANT("Plant", 1, 1),
        WASTELAND("Wasteland", 1, 1),
        MEDITATIVE("meditative", 1, 1),
        WANDERER("Wanderer", 1, 2),
        GRAHAM("Graham", 1, 2),
        PRAIRIE_RIDE("prairie_ride", 1, 2),
        POOL("Pool", 2, 1),
        COURBET("Courbet", 2, 1),
        SUNSET("Sunset", 2, 1),
        SEA("Sea", 2, 1),
        CREEBET("Creebet", 2, 1),
        MATCH("Match", 2, 2),
        BUST("Bust", 2, 2),
        STAGE("Stage", 2, 2),
        VOID("Void", 2, 2),
        SKULL_AND_ROSES("SkullAndRoses", 2, 2),
        WITHER("Wither", 2, 2),
        BAROQUE("baroque", 2, 2),
        HUMBLE("humble", 2, 2),
        BOUQUET("bouquet", 3, 3, predicateFor3WidthHeight),
        CAVEBIRD("cavebird", 3, 3, predicateFor3WidthHeight),
        COTAN("cotan", 3, 3, predicateFor3WidthHeight),
        ENDBOSS("endboss", 3, 3, predicateFor3WidthHeight),
        FERN("fern", 3, 3, predicateFor3WidthHeight),
        OWLEMONS("owlemons", 3, 3, predicateFor3WidthHeight),
        SUNFLOWERS("sunflowers", 3, 3, predicateFor3WidthHeight),
        TIDES("tides", 3, 3, predicateFor3WidthHeight),
        BACKYARD("backyard", 3, 4, predicateFor3Width.apply(4)),
        POND("pond", 3, 4, predicateFor3Width.apply(4)),
        FIGHTERS("Fighters", 4, 2, predicateFor4Width.apply(2)),
        CHANGING("changing", 4, 2, predicateFor4Width.apply(2)),
        FINDING("finding", 4, 2, predicateFor4Width.apply(2)),
        LOWMIST("lowmist", 4, 2, predicateFor4Width.apply(2)),
        PASSAGE("passage", 4, 2, predicateFor4Width.apply(2)),
        SKELETON("Skeleton", 4, 3, predicateFor4Width.apply(3)),
        DONKEY_KONG("DonkeyKong", 4, 3, predicateFor4Width.apply(3)),
        POINTER("Pointer", 4, 4, predicateFor4WidthHeight),
        PIG_SCENE("Pigscene", 4, 4, predicateFor4WidthHeight),
        BURNING_SKULL("BurningSkull", 4, 4, predicateFor4WidthHeight),
        ORB("orb", 4, 4, predicateFor4WidthHeight),
        UNPACKED("unpacked", 4, 4, predicateFor4WidthHeight);

        @JvmField
        val title: String

        @JvmField
        val width: Int

        @JvmField
        val height: Int

        @JvmField
        val predicate: PaintingPlacePredicate

        constructor(title: String, width: Int, height: Int) {
            this.title = title
            this.width = width
            this.height = height
            this.predicate = PaintingPlacePredicate { level, face, block, target ->
                for (x in 0..<width) {
                    for (z in 0..<height) {
                        if (checkPlacePaint(x, z, level, face, block, target)) return@PaintingPlacePredicate false
                    }
                }
                true
            }
        }

        constructor(title: String, width: Int, height: Int, predicate: PaintingPlacePredicate) {
            this.title = title
            this.width = width
            this.height = height
            this.predicate = predicate
        }

        companion object {
            val BY_NAME: MutableMap<String, Motive> = HashMap()

            init {
                for (motive: Motive in entries) {
                    BY_NAME.put(motive.title, motive)
                }
            }
        }
    }

    companion object {
        private fun checkPlacePaint(
            x: Int,
            z: Int,
            level: Level,
            face: BlockFace,
            block: Block,
            target: Block
        ): Boolean {
            if (target.getSide(face.rotateYCCW(), x).up(z).isTransparent ||
                block.getSide(face.rotateYCCW(), x).up(z).isSolid
            ) {
                return true
            } else {
                val side: Block = block.getSide(face.rotateYCCW(), x)
                val up: Block = block.getSide(face.rotateYCCW(), x).up(z).levelBlock
                val up1: Block = block.up(z)
                val chunks: Set<IChunk> = Sets.newHashSet(side.chunk, up.chunk, up1.chunk)
                val entities: Collection<Entity> =
                    chunks.stream().map<Collection<Entity>> { c: IChunk -> c.entities.values }.reduce(
                        ArrayList(),
                        { e1: ArrayList<Entity>, e2: Collection<Entity>? ->
                            e1.addAll(
                                e2!!
                            )
                            e1
                        },
                        { entities1: ArrayList<Entity>, entities2: ArrayList<Entity>? ->
                            entities1.addAll(
                                entities2!!
                            )
                            entities1
                        })
                for (e: Entity in entities) {
                    if (e is EntityPainting) {
                        if (e.getBoundingBox().intersectsWith(side) || e.getBoundingBox()
                                .intersectsWith(up) || e.getBoundingBox()
                                .intersectsWith(up1)
                        ) {
                            return true
                        }
                    }
                }
            }
            return false
        }

        private val predicateFor4Width: Function<Int, PaintingPlacePredicate> =
            Function { height: Int ->
                PaintingPlacePredicate { level: Level, face: BlockFace, block: Block, target: Block ->
                    for (x in -1..2) {
                        for (z in 0..<height) {
                            if (checkPlacePaint(x, z, level, face, block, target)) return@PaintingPlacePredicate false
                        }
                    }
                    true
                }
            }

        private val predicateFor4WidthHeight: PaintingPlacePredicate =
            PaintingPlacePredicate { level: Level, face: BlockFace, block: Block, target: Block ->
                for (x in -1..2) {
                    for (z in -1..2) {
                        if (checkPlacePaint(x, z, level, face, block, target)) return@PaintingPlacePredicate false
                    }
                }
                true
            }

        private val predicateFor3WidthHeight: PaintingPlacePredicate =
            PaintingPlacePredicate { level: Level, face: BlockFace, block: Block, target: Block ->
                for (x in -1..1) {
                    for (z in -1..1) {
                        if (checkPlacePaint(x, z, level, face, block, target)) return@PaintingPlacePredicate false
                    }
                }
                true
            }

        private val predicateFor3Width: Function<Int, PaintingPlacePredicate> =
            Function { height: Int ->
                PaintingPlacePredicate { level: Level, face: BlockFace, block: Block, target: Block ->
                    for (x in -1..1) {
                        for (z in 0..<height) {
                            if (checkPlacePaint(x, z, level, face, block, target)) return@PaintingPlacePredicate false
                        }
                    }
                    true
                }
            }


        @JvmField
        val motives: Array<Motive> = Motive.entries.toTypedArray()
        fun getMotive(name: String): Motive {
            return Motive.BY_NAME.getOrDefault(name, Motive.KEBAB)
        }
    }
}
