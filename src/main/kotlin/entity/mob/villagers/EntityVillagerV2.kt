package org.chorus_oss.chorus.entity.mob.villagers

import kotlinx.io.Buffer
import kotlinx.io.readByteString
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockBed
import org.chorus_oss.chorus.block.BlockDoor
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.ai.behavior.Behavior
import org.chorus_oss.chorus.entity.ai.behavior.IBehavior
import org.chorus_oss.chorus.entity.ai.behaviorgroup.BehaviorGroup
import org.chorus_oss.chorus.entity.ai.behaviorgroup.IBehaviorGroup
import org.chorus_oss.chorus.entity.ai.controller.FluctuateController
import org.chorus_oss.chorus.entity.ai.controller.IController
import org.chorus_oss.chorus.entity.ai.controller.LookController
import org.chorus_oss.chorus.entity.ai.controller.WalkController
import org.chorus_oss.chorus.entity.ai.evaluator.*
import org.chorus_oss.chorus.entity.ai.executor.*
import org.chorus_oss.chorus.entity.ai.executor.villager.*
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.route.finder.impl.SimpleFlatAStarRouteFinder
import org.chorus_oss.chorus.entity.ai.route.posevaluator.WalkingPosEvaluator
import org.chorus_oss.chorus.entity.ai.sensor.BlockSensor
import org.chorus_oss.chorus.entity.ai.sensor.ISensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestEntitySensor
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.data.profession.Profession
import org.chorus_oss.chorus.entity.item.EntityItem
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.mob.animal.EntityAnimal
import org.chorus_oss.chorus.entity.mob.monster.humanoid_monster.EntityZombie
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.inventory.EntityEquipmentInventory
import org.chorus_oss.chorus.inventory.InventoryHolder
import org.chorus_oss.chorus.inventory.InventorySlice
import org.chorus_oss.chorus.inventory.TradeInventory
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.ParticleEffect
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.NBTIO
import org.chorus_oss.chorus.nbt.tag.*
import org.chorus_oss.chorus.network.protocol.EntityEventPacket
import org.chorus_oss.chorus.registry.Registries
import org.chorus_oss.chorus.utils.ChorusRandom
import org.chorus_oss.chorus.utils.TradeRecipeBuildUtils
import org.chorus_oss.chorus.utils.Utils
import org.chorus_oss.nbt.TagSerialization
import java.util.function.Consumer
import kotlin.math.max
import kotlin.math.min

class EntityVillagerV2(chunk: IChunk?, nbt: CompoundTag?) : EntityMob(chunk, nbt!!), InventoryHolder {
    override fun getEntityIdentifier(): String {
        return EntityID.VILLAGER_V2
    }

    private val tradeNetId: MutableList<Int> = ArrayList()

    fun getTradeNetIds(): MutableList<Int> {
        if (tradeNetId == null) return ArrayList()
        return tradeNetId
    }

    fun getRecipes(): ListTag<CompoundTag> {
        return ListTag(
            Tag.TAG_COMPOUND.toInt(),
            TradeRecipeBuildUtils.RECIPE_MAP.entries.stream()
                .filter { t -> getTradeNetIds().contains(t.key) }.map { it.value }.toList()
        )
    }

    var tierExpRequirement: IntArray = intArrayOf(0, 10, 70, 150, 250)

    protected var tradeInventory: TradeInventory? = null

    override lateinit var inventory: EntityEquipmentInventory

    protected var canTrade: Boolean? = null

    protected var displayName: String = ""
        set(value) {
            field = value
            namedTag!!.putString("displayName", value)
        }

    override var tradeTier: Int? = 0

    protected var maxTradeTier: Int = 0
        set(value) {
            field = value
            this.setDataProperty(EntityDataTypes.MAX_TRADE_TIER, value)
            namedTag!!.putInt("maxTradeTier", value)
        }

    protected var tradeExp: Int = 0
        set(value) {
            field = value
            this.setDataProperty(EntityDataTypes.TRADE_EXPERIENCE, value)
            namedTag!!.putInt("tradeExp", value)
        }

    protected var tradeSeed: Int = 0
        set(value) {
            field = value
            namedTag!!.putInt("tradeSeed", value)
        }

    /**
     * 0 generic
     * 1 farmer
     * 2 fisherman
     * 3 shepherd
     * 4 fletcher
     * 5 librarian
     * 6 cartographer
     * 7 cleric
     * 8 armor
     * 9 weapon
     * 10 tool
     * 11 butcher
     * 12 butcher
     * 13 mason
     * 14 nitwit
     */
    var profession: Int = 0

    init {
        applyProfession()
    }

    public override fun requireBehaviorGroup(): IBehaviorGroup {
        return BehaviorGroup(
            this.tickSpread,
            setOf<IBehavior>(
                Behavior(
                    DoorExecutor(), all(
                        IBehaviorEvaluator { entity: EntityMob? ->
                            val block = memoryStorage.get<Block>(CoreMemoryTypes.NEAREST_BLOCK_2)
                            if (block == null || moveDirectionEnd == null) return@IBehaviorEvaluator false
                            level!!.raycastBlocks(this.position, moveDirectionEnd!!, true, false, 0.5).contains(block)
                        }
                    ), 4, 1),
                Behavior(
                    WillingnessExecutor(),
                    all(
                        IBehaviorEvaluator { entity: EntityMob? -> getFoodPoints() >= 12 },
                        IBehaviorEvaluator { entity: EntityMob? -> !isBaby() },
                        IBehaviorEvaluator { entity: EntityMob? -> !memoryStorage.get<Boolean>(CoreMemoryTypes.WILLING) },
                        PassByTimeEvaluator(CoreMemoryTypes.LAST_IN_LOVE_TIME, 6000, Int.MAX_VALUE)
                    ), 3, 1, 1, false
                ),  //生长
                Behavior(
                    AnimalGrowExecutor(),
                    all(
                        PassByTimeEvaluator(CoreMemoryTypes.ENTITY_SPAWN_TIME, 20 * 60 * 20, Int.MAX_VALUE),
                        IBehaviorEvaluator { entity: EntityMob -> entity is EntityAnimal && entity.isBaby() }
                    ), 2, 1, 1200
                ),
                Behavior(
                    PlaySoundExecutor(
                        Sound.MOB_VILLAGER_IDLE,
                        if (isBaby()) 1.3f else 0.8f,
                        if (isBaby()) 1.7f else 1.2f,
                        1f,
                        1f
                    ), RandomSoundEvaluator(), 1, 1
                )
            ),
            setOf<IBehavior>(
                Behavior(
                    { entity: EntityMob? ->
                        moveTarget = null
                        lookTarget = tradeInventory!!.viewers.stream().findFirst().get().position
                        true
                    },
                    { entity: EntityMob? -> tradeInventory != null && !tradeInventory!!.viewers.isEmpty() },
                    9,
                    1
                ),
                Behavior(
                    FleeFromTargetExecutor(CoreMemoryTypes.NEAREST_ZOMBIE, 0.5f, true, 8f), all(
                        EntityCheckEvaluator(CoreMemoryTypes.NEAREST_ZOMBIE),
                        DistanceEvaluator(CoreMemoryTypes.NEAREST_ZOMBIE, 8.0),
                        IBehaviorEvaluator { entity ->
                            memoryStorage.notEmpty(CoreMemoryTypes.NEAREST_ZOMBIE)
                                    && memoryStorage[CoreMemoryTypes.NEAREST_ZOMBIE]!! is EntityMob
                                    && entity.memoryStorage
                                .notEmpty(CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET)
                                    && entity.memoryStorage[CoreMemoryTypes.NEAREST_SUITABLE_ATTACK_TARGET]!! === this
                        },
                        IBehaviorEvaluator {
                            memoryStorage.notEmpty(CoreMemoryTypes.NEAREST_ZOMBIE) && level!!.raycastBlocks(
                                this.position,
                                memoryStorage.get<Entity>(CoreMemoryTypes.NEAREST_ZOMBIE)!!.position
                            ).isEmpty()
                        }
                    ), 8, 1),
                Behavior(
                    SleepExecutor(), all(
                        MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.OCCUPIED_BED),
                        DistanceEvaluator(CoreMemoryTypes.OCCUPIED_BED, 2.0),
                        PassByTimeEvaluator(CoreMemoryTypes.LAST_BE_ATTACKED_TIME, 100),
                        IBehaviorEvaluator { entity: EntityMob -> level!!.dayTime >= 12000 && entity.level!!.dayTime < Level.TIME_FULL }
                    ), 7, 1),
                Behavior(
                    MoveToTargetExecutor(CoreMemoryTypes.OCCUPIED_BED, 0.3f, true), all(
                        MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.OCCUPIED_BED),
                        any(
                            IBehaviorEvaluator { entity: EntityMob -> level!!.dayTime >= 12000 && entity.level!!.dayTime < Level.TIME_FULL },
                            all(
                                IBehaviorEvaluator { entity: EntityMob -> level!!.dayTime >= 11000 && entity.level!!.dayTime < 12000 },
                                not(DistanceEvaluator(CoreMemoryTypes.OCCUPIED_BED, 5.0))
                            )
                        )
                    ), 6, 1
                ),
                Behavior(
                    NearbyFlatRandomRoamExecutor(
                        CoreMemoryTypes.OCCUPIED_BED,
                        0.2f,
                        5,
                        100,
                        false,
                        -1,
                        true,
                        10
                    ), all(
                        MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.OCCUPIED_BED),
                        IBehaviorEvaluator { entity: EntityMob -> level!!.dayTime >= 11000 && entity.level!!.dayTime < 12000 }
                    ), 5, 1),
                Behavior(
                    WorkExecutor(), all(
                        MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.SITE_BLOCK),
                        any(
                            IBehaviorEvaluator { entity: EntityMob -> level!!.dayTime >= 0 && entity.level!!.dayTime < 8000 },
                            IBehaviorEvaluator { entity: EntityMob -> level!!.dayTime >= 10000 && entity.level!!.dayTime < 11000 }
                        )
                    ), 4, 1, 1),
                Behavior(
                    GossipExecutor(CoreMemoryTypes.GOSSIP_TARGET), all(
                        MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.GOSSIP_TARGET),
                        IBehaviorEvaluator { entity: EntityMob? -> !isBaby() }
                    ), 3, 1),
                Behavior(
                    VillagerBreedingExecutor(
                        EntityVillagerV2::class.java,
                        16,
                        100,
                        0.5f
                    ),
                    MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.ENTITY_SPOUSE),
                    2,
                    1
                ),
                Behavior(
                    FlatRandomRoamExecutor(0.2f, 12, 100, false, -1, true, 10),
                    (IBehaviorEvaluator { entity: EntityMob? -> true }),
                    1,
                    1
                )
            ),
            setOf<ISensor>(
                ISensor { entity: EntityMob ->
                    if (level!!.tick % 120 == 0) {
                        if (memoryStorage.isEmpty(CoreMemoryTypes.OCCUPIED_BED)) {
                            val range = 48
                            val lookY = 5
                            var block: BlockBed? = null
                            for (x in -range..range) {
                                for (z in -range..range) {
                                    for (y in -lookY..lookY) {
                                        val lookTransform =
                                            entity.transform.add(x.toDouble(), y.toDouble(), z.toDouble())
                                        val lookBlock = lookTransform.levelBlock
                                        if (lookBlock is BlockBed) {
                                            if (!lookBlock.isHeadPiece &&
                                                level!!.entities.values.stream().noneMatch { entity1: Entity ->
                                                    entity1 is EntityVillagerV2 && entity1.memoryStorage
                                                        .notEmpty(CoreMemoryTypes.OCCUPIED_BED) && entity1.getBed() == lookBlock
                                                }
                                            ) {
                                                block = lookBlock.footPart
                                            }
                                        }
                                    }
                                }
                            }
                            if (block != null && !block.isOccupied) setBed(block)
                        } else if (!memoryStorage.get<BlockBed>(CoreMemoryTypes.OCCUPIED_BED)!!.isBedValid) {
                            namedTag!!.remove("bed")
                            memoryStorage.clear(CoreMemoryTypes.OCCUPIED_BED)
                        }
                    }
                },
                ISensor { entity: EntityMob ->
                    if (level!!.tick % 60 == 0) {
                        val entities =
                            entity.level!!.getCollidingEntities(
                                entity.getBoundingBox().grow(64.0, 3.0, 64.0)
                            )
                                .filter { entity1: Entity -> entity1 is EntityVillagerV2 && entity1 !== this }
                                .map { entity1: Entity -> (entity1 as EntityVillagerV2) }
                        if (level!!.dayTime > 8000 && level!!.dayTime < 10000) {
                            if (memoryStorage.isEmpty(CoreMemoryTypes.GOSSIP_TARGET)) {
                                var minDistance = Float.MAX_VALUE.toDouble()
                                var nearest: EntityVillagerV2? = null
                                for (entity1 in entities.toList()) {
                                    val distance = entity1.position.distance(this.position)
                                    if (distance < minDistance) {
                                        minDistance = distance
                                        nearest = entity1
                                    }
                                }
                                if (nearest != null) {
                                    memoryStorage[CoreMemoryTypes.GOSSIP_TARGET] = nearest
                                } else memoryStorage.clear(CoreMemoryTypes.GOSSIP_TARGET)
                            }
                        } else {
                            if (shouldShareFood()) {
                                entities.filter { entity1: EntityVillagerV2 ->
                                    entity1.isHungry() && entity1.position.distance(
                                        this.position
                                    ) < 16
                                }.stream().findAny().ifPresentOrElse(
                                    { entity1: EntityVillagerV2 ->
                                        memoryStorage[CoreMemoryTypes.GOSSIP_TARGET] = entity1
                                        entity1.memoryStorage[CoreMemoryTypes.GOSSIP_TARGET] = this
                                    },
                                    { memoryStorage.clear(CoreMemoryTypes.GOSSIP_TARGET) })
                            } else if (!isHungry()) memoryStorage.clear(CoreMemoryTypes.GOSSIP_TARGET)
                        }
                    }
                },
                ISensor { entity: EntityMob? ->
                    if (level!!.tick % 30 == 0) {
                        if (!isBaby()) {
                            val siteBlock = memoryStorage.get<Block>(CoreMemoryTypes.SITE_BLOCK)
                            if (siteBlock != null) if (siteBlock.levelBlock.id != siteBlock.id) {
                                memoryStorage.clear(CoreMemoryTypes.SITE_BLOCK)
                            }
                            if (memoryStorage.isEmpty(CoreMemoryTypes.SITE_BLOCK)) {
                                for (block in level!!.getCollisionBlocks(
                                    getBoundingBox()
                                        .grow(16.0, 4.0, 16.0)
                                )) {
                                    if (level!!.entities.values.stream().noneMatch { entity1: Entity ->
                                            entity1 is EntityVillagerV2 && entity1.memoryStorage
                                                .notEmpty(CoreMemoryTypes.SITE_BLOCK) && entity1.getSite() == block
                                        }) if (setProfessionBlock(block)) return@ISensor
                                }
                                if (tradeExp == 0) setProfession(0, true)
                            }
                        }
                    }
                },
                ISensor { entity: EntityMob ->
                    if (level!!.tick % 100 == 0) {
                        if (memoryStorage.get<Boolean>(CoreMemoryTypes.WILLING)) {
                            val entities = entity.level!!.entities.values
                            var maxDistanceSquared = -1.0
                            var nearestInLove: EntityVillagerV2? = null
                            for (e in entities) {
                                val newDistance = e.position.distanceSquared(entity.position)
                                if (e is EntityVillagerV2 && e !== entity) {
                                    if (!e.isBaby() && e.memoryStorage.get<Boolean>(CoreMemoryTypes.WILLING) && e.memoryStorage
                                            .isEmpty(CoreMemoryTypes.ENTITY_SPOUSE) && (maxDistanceSquared == -1.0 || newDistance < maxDistanceSquared)
                                    ) {
                                        maxDistanceSquared = newDistance
                                        nearestInLove = e
                                    }
                                }
                            }
                            if (nearestInLove != null) {
                                nearestInLove.memoryStorage[CoreMemoryTypes.ENTITY_SPOUSE] = this
                                memoryStorage[CoreMemoryTypes.ENTITY_SPOUSE] = nearestInLove
                            }
                        }
                    }
                },
                BlockSensor(BlockDoor::class.java, CoreMemoryTypes.NEAREST_BLOCK_2, 1, 0, 10),
                NearestEntitySensor(EntityZombie::class.java, CoreMemoryTypes.NEAREST_ZOMBIE, 8.0, 0.0)
            ),
            setOf<IController>(WalkController(), LookController(true, true), FluctuateController()),
            SimpleFlatAStarRouteFinder(WalkingPosEvaluator(), this),
            this
        )
    }

    override fun getFloatingHeight(): Float {
        return super.getFloatingHeight() * 0.7f
    }

    fun isHungry(): Boolean {
        return getFoodPoints() < 12
    }

    fun shouldShareFood(): Boolean {
        for (item in inventory.contents.values) {
            if ((item.id == ItemID.BREAD && item.getCount() >= 6)
                || ((item.id == ItemID.CARROT || item.id == BlockID.BEETROOT) && item.getCount() >= 24)
                || (item.id == BlockID.WHEAT && item.getCount() >= 18 && profession == 1)
            ) return true
        }
        return false
    }

    fun getFoodPoints(): Int {
        var points = 0
        for (item in inventory.contents.values) {
            points += when (item.id) {
                ItemID.BREAD -> 4
                ItemID.CARROT, ItemID.POTATO, BlockID.BEETROOT -> 1
                else -> 0
            } * item.getCount()
        }
        return points
    }

    fun setBed(bed: BlockBed) {
        if (bed.isBedValid) {
            memoryStorage.set(CoreMemoryTypes.OCCUPIED_BED, bed)
            for (i in 0..4) {
                val randX = Utils.rand(0f, 0.5f)
                val randY = Utils.rand(0f, 0.3f)
                val randZ = Utils.rand(0f, 0.5f)
                level!!.addParticleEffect(
                    position.add(
                        randX.toDouble(),
                        (this.getEyeHeight() + randY).toDouble(),
                        randZ.toDouble()
                    ), ParticleEffect.VILLAGER_HAPPY
                )
                level!!.addParticleEffect(
                    bed.position.add(
                        randX.toDouble(),
                        (0.5625f + randY).toDouble(),
                        randZ.toDouble()
                    ), ParticleEffect.VILLAGER_HAPPY
                )
            }
        }
    }

    fun getBed(): BlockBed? {
        return memoryStorage[CoreMemoryTypes.OCCUPIED_BED]
    }

    fun getSite(): Block? {
        return memoryStorage[CoreMemoryTypes.SITE_BLOCK]
    }

    override fun getHeight(): Float {
        if (isSleeping()) return getWidthR()
        return getHeightR()
    }

    override fun getWidth(): Float {
        return getWidthR()
    }

    override fun getLength(): Float {
        if (isSleeping()) return getHeightR()
        return super.getLength()
    }

    private fun getWidthR(): Float {
        if (this.isBaby()) {
            return 0.3f
        }
        return 0.6f
    }

    private fun getHeightR(): Float {
        if (this.isBaby()) {
            return 0.95f
        }
        return 1.9f
    }

    fun isSleeping(): Boolean {
        return getDataFlag(EntityFlag.SLEEPING)
    }

    override fun getOriginalName(): String {
        return "VillagerV2"
    }

    public override fun initEntity() {
        this.maxHealth = 20
        super.initEntity()
        setTradingPlayer(0L)
        if (!namedTag!!.contains("tradeSeed")) {
            this.tradeSeed = (ChorusRandom().nextInt(Int.MAX_VALUE - 1))
        } else {
            this.tradeSeed = namedTag!!.getInt("tradeSeed")
        }
        if (!namedTag!!.contains("canTrade")) {
            this.setCanTrade(!(profession == 0 || profession == 14))
        } else {
            this.canTrade = namedTag!!.getBoolean("canTrade")
        }
        if (!namedTag!!.contains("displayName") && profession != 0) {
            this.displayName = (getProfessionName(profession)!!)
        } else {
            this.displayName = namedTag!!.getString("displayName")
        }
        if (!namedTag!!.contains("tradeTier")) {
            this.setTradeTier(1)
        } else {
            this.tradeTier = namedTag!!.getInt("tradeTier")
        }
        if (!namedTag!!.contains("maxTradeTier")) {
            this.maxTradeTier = (5)
        } else {
            val maxTradeTier = namedTag!!.getInt("maxTradeTier")
            this.maxTradeTier = maxTradeTier
            this.setDataProperty(EntityDataTypes.MAX_TRADE_TIER, maxTradeTier)
        }
        if (!namedTag!!.contains("tradeExp")) {
            this.tradeExp = (0)
        } else {
            val tradeExp = namedTag!!.getInt("tradeExp")
            this.tradeExp = tradeExp
            this.setDataProperty(EntityDataTypes.TRADE_EXPERIENCE, tradeExp)
        }
        if (namedTag!!.containsInt("clothing")) {
            this.setDataProperty(EntityDataTypes.MARK_VARIANT, namedTag!!.getInt("clothing"))
        } else {
            val bv = position.asBlockVector3()
            this.setDataProperty(
                EntityDataTypes.MARK_VARIANT, Clothing.getClothing(
                    level!!.getBiomeId(bv.x, bv.y, bv.z)
                ).ordinal
            )
        }
        if (namedTag!!.containsCompound("bed")) {
            val compound = namedTag!!.getCompound("bed")
            val vector = Vector3(
                compound.getInt("x").toDouble(),
                compound.getInt("y").toDouble(),
                compound.getInt("z").toDouble()
            )
            val bed = level!!.getBlock(vector)
            if (bed is BlockBed) {
                setBed(bed)
            }
        }
        memoryStorage[CoreMemoryTypes.GOSSIP] = mutableMapOf()
        if (namedTag!!.containsCompound("gossip")) {
            val gossipMap = memoryStorage[CoreMemoryTypes.GOSSIP]!!
            val gossipTag = namedTag!!.getCompound("gossip")
            for (key in gossipTag.tags.keys) {
                val gossipValues = gossipTag.getList(key, IntTag::class.java)
                val valueMap = mutableListOf<Int>()
                for (i in 0..<gossipValues.size()) {
                    valueMap.add(i, gossipValues[i].data)
                }
                gossipMap[key] = valueMap
            }
        }
        if (namedTag!!.containsString("purifyPlayer")) {
            val xuid = namedTag!!.removeAndGet<StringTag>("purifyPlayer")!!.parseValue()
            this.addGossip(xuid, Gossip.MAJOR_POSITIVE, 20)
            this.addGossip(xuid, Gossip.MINOR_POSITIVE, 25)
        }
        if (namedTag!!.containsCompound("siteBlock")) {
            val tag = namedTag!!.getCompound("siteBlock")
            val vector3 = Vector3(tag.getInt("x").toDouble(), tag.getInt("y").toDouble(), tag.getInt("z").toDouble())
            val block = level!!.getBlock(vector3)
            setProfessionBlock(block)
        }
        if (namedTag!!.containsInt("profession")) {
            setProfession(namedTag!!.getInt("profession"), false)
        }
        this.inventory = EntityEquipmentInventory(this)
        if (namedTag!!.contains("Inventory") && namedTag!!["Inventory"] is ListTag<*>) {
            val inventory = this.inventory
            val inventoryList = namedTag!!.getList(
                "Inventory",
                CompoundTag::class.java
            )
            for (item in inventoryList.all) {
                val slot = item.getByte("Slot").toInt()
                inventory.setItem(slot, NBTIO.getItemHelper(item)) //inventory 0-39
            }
        }
        if (canTrade!!) {
            tradeInventory = TradeInventory(this)
        }
    }

    override fun attack(source: EntityDamageEvent): Boolean {
        if (super.attack(source)) {
            if (source is EntityDamageByEntityEvent) {
                if (source.damager is Player) {
                    addGossip(source.damager.loginChainData.xuid!!, Gossip.MINOR_NEGATIVE, 25)
                    val pk = EntityEventPacket()
                    pk.eid = getRuntimeID()
                    pk.event = EntityEventPacket.VILLAGER_ANGRY
                    Server.broadcastPacket(viewers.values, pk)
                }
            }
            return true
        } else return false
    }

    override fun kill() {
        val event = getLastDamageCause()
        if (event is EntityDamageByEntityEvent) {
            val player = event.entity
            if (player is Player) {
                println("1")
                level!!.getCollidingEntities(
                    getBoundingBox().grow(16.0, 16.0, 16.0)
                ).filterIsInstance<EntityVillagerV2>().forEach { entity: Entity ->
                    (entity as EntityVillagerV2).addGossip(
                        player.loginChainData.xuid!!, Gossip.MAJOR_NEGATIVE, 25
                    )
                }
            }
        }
        super.kill()
    }

    fun addGossip(xuid: String, gossip: Gossip, value: Int) {
        val gossipMap = memoryStorage[CoreMemoryTypes.GOSSIP]!!
        if (!gossipMap.containsKey(xuid)) gossipMap[xuid] = MutableList(Gossip.VALUES.size) { 0 }

        val values = gossipMap[xuid]
        val ordinal = gossip.ordinal
        values!![ordinal] = min(gossip.max.toDouble(), (values[ordinal] + value).toDouble()).toInt()
        level!!.players.values.stream().filter { player: Player -> player.loginChainData.xuid == xuid }.findFirst()
            .ifPresent { player: Player ->
                this.updateTrades(
                    player
                )
            }
    }

    fun spreadGossip() {
        level!!.getCollidingEntities(getBoundingBox().grow(2.0, 0.0, 2.0))
            .filter { entity2: Entity? -> entity2 is EntityVillagerV2 }
            .map { entity2: Entity -> (entity2 as EntityVillagerV2) }
            .forEach { target: EntityVillagerV2 ->
                val gossipMap = memoryStorage[CoreMemoryTypes.GOSSIP]!!
                val targetGossipMap = target.memoryStorage[CoreMemoryTypes.GOSSIP]!!
                for ((xuid, value) in gossipMap.entries) {
                    if (!targetGossipMap.containsKey(xuid)) targetGossipMap[xuid] =
                        MutableList(Gossip.VALUES.size) { 0 }
                    val targetValues = targetGossipMap[xuid]
                    for (gossip in Gossip.VALUES) {
                        val ordinal = gossip.ordinal
                        targetValues!![ordinal] = max(
                            targetValues[ordinal].toDouble(),
                            (value[ordinal] - gossip.penalty).toDouble()
                        ).toInt()
                    }
                }
            }
    }

    fun getGossip(xuid: String, gossip: Gossip): Int {
        val gossipMap = memoryStorage[CoreMemoryTypes.GOSSIP]!!
        if (!gossipMap.containsKey(xuid)) gossipMap[xuid] = MutableList(Gossip.VALUES.size) { 0 }
        val values = gossipMap[xuid]
        val ordinal = gossip.ordinal
        return values!!.get(ordinal) * gossip.multiplier
    }

    fun getReputation(player: Player): Int {
        var reputation = 0
        val values = memoryStorage.get(CoreMemoryTypes.GOSSIP)!![player.loginChainData.xuid]
        if (values != null) {
            for (gossip in Gossip.VALUES) {
                reputation += (values.get(gossip.ordinal) * gossip.multiplier)
            }
        }
        return reputation
    }

    override fun saveNBT() {
        super.saveNBT()
        namedTag!!.putInt("profession", this.profession)
        namedTag!!.putBoolean("isTrade", this.canTrade!!)
        namedTag!!.putString("displayName", displayName)
        namedTag!!.putInt("tradeTier", this.tradeTier!!)
        namedTag!!.putInt("maxTradeTier", this.maxTradeTier)
        namedTag!!.putInt("tradeExp", this.tradeExp)
        namedTag!!.putInt("tradeSeed", this.tradeSeed)
        namedTag!!.putInt("clothing", this.getDataProperty<Int>(EntityDataTypes.MARK_VARIANT))
        val gossipTag = CompoundTag()
        for ((key, value) in memoryStorage.get(CoreMemoryTypes.GOSSIP)!!
            .entries) {
            val gossipValues = ListTag<IntTag>()
            for (v2 in value) {
                gossipValues.add(IntTag(v2))
            }
            gossipTag.putList(key, gossipValues)
        }
        namedTag!!.putCompound("gossip", gossipTag)
        if (memoryStorage.notEmpty(CoreMemoryTypes.OCCUPIED_BED)) {
            val bed = memoryStorage.get<BlockBed>(CoreMemoryTypes.OCCUPIED_BED)!!
            namedTag!!.putCompound(
                "bed",
                CompoundTag().putInt("x", bed.position.floorX).putInt("y", bed.position.floorY)
                    .putInt("z", bed.position.floorZ)
            )
        }
        if (memoryStorage.notEmpty(CoreMemoryTypes.SITE_BLOCK)) {
            val site = memoryStorage.get<Block>(CoreMemoryTypes.SITE_BLOCK)!!
            namedTag!!.putCompound(
                "siteBlock",
                CompoundTag().putInt("x", site.position.floorX).putInt("y", site.position.floorY)
                    .putInt("z", site.position.floorZ)
            )
        }
        val inventoryTag: ListTag<CompoundTag> = ListTag()
        namedTag!!.putList("Inventory", inventoryTag)
        for ((key, value) in inventory.contents) {
            inventoryTag.add(NBTIO.putItemHelper(value, key))
        }
    }

    /**
     * 获取村民职业id对应的displayName硬编码
     */
    private fun getProfessionName(profession: Int): String? {
        return when (profession) {
            1 -> "entity.villager.farmer"
            2 -> "entity.villager.fisherman"
            3 -> "entity.villager.shepherd"
            4 -> "entity.villager.fletcher"
            5 -> "entity.villager.librarian"
            6 -> "entity.villager.cartographer"
            7 -> "entity.villager.cleric"
            8 -> "entity.villager.armor"
            9 -> "entity.villager.weapon"
            10 -> "entity.villager.tool"
            11 -> "entity.villager.butcher"
            12 -> "entity.villager.leather"
            13 -> "entity.villager.mason"
            else -> null
        }
    }


    fun setProfession(profession: Int, apply: Boolean) {
        this.profession = profession
        this.setDataProperty(EntityDataTypes.VARIANT, profession)
        if (apply) applyProfession()
    }

    fun setProfessionBlock(block: Block): Boolean {
        for (prof in Profession.getProfessions().values) {
            if (tradeExp != 0 && prof.getIndex() != this.profession) continue
            if (block.id == prof.getBlockID()) {
                memoryStorage[CoreMemoryTypes.SITE_BLOCK] = block
                setProfession(prof.getIndex(), true)
                return true
            }
        }
        return false
    }

    /**
     * 这个方法插件一般不用
     */
    fun setTradingPlayer(eid: Long?) {
        this.setDataProperty(EntityDataTypes.TRADE_TARGET_EID, eid!!)
    }

    /**
     * 设置村民是否可以交易
     *
     * @param canTrade true 可以交易
     */
    fun setCanTrade(canTrade: Boolean) {
        this.canTrade = canTrade
        namedTag!!.putBoolean("canTrade", canTrade)
    }

    /**
     * @return 该村民的交易等级
     */
    fun getTradeTier(): Int {
        return tradeTier!!
    }

    /**
     * @param tradeTier
     *
     *村民的交易等级(1-[EntityVillagerV2.maxTradeTier])
     */
    fun setTradeTier(tradeTier: Int) {
        var tradeTier1 = tradeTier
        this.tradeTier = --tradeTier1
        namedTag!!.putInt("tradeTier", this.tradeTier!!)
    }

    fun updateTrades(player: Player) {
        if (player.topWindow.isEmpty || player.topWindow.get() !== tradeInventory) return
        val tierExpRequirements = ListTag<CompoundTag>()
        var i = 0
        val len = tierExpRequirement.size
        while (i < len) {
            tierExpRequirements.add(i, CompoundTag().putInt(i.toString(), tierExpRequirement[i]))
            ++i
        }
        val recipes = getRecipes().copy() as ListTag<CompoundTag>
        val reputation = getReputation(player)
        for (tag in recipes.all) {
            if (tag.containsCompound("buyA")) {
                val buyA = tag.getCompound("buyA")
                var multiplier = 0f
                if (tag.containsFloat("priceMultiplierA")) multiplier = tag.getFloat("priceMultiplierA")
                buyA.putByte(
                    "Count",
                    max((buyA.getByte("Count") - (reputation * multiplier).toInt()).toDouble(), 1.0).toInt()
                )
            }
            if (tag.containsCompound("buyB")) {
                val buyB = tag.getCompound("buyB")
                var multiplier = 0f
                if (tag.containsFloat("priceMultiplierB")) multiplier = tag.getFloat("priceMultiplierB")
                buyB.putByte(
                    "Count",
                    max((buyB.getByte("Count") - (reputation * multiplier).toInt()).toDouble(), 1.0).toInt()
                )
            }
        }

        val packet = org.chorus_oss.protocol.packets.UpdateTradePacket(
            windowID = player.getWindowId(tradeInventory!!).toByte(),
            windowType = 15,
            size = 0,
            tradeTier = getTradeTier(),
            villagerUniqueID = getUniqueID(),
            entityUniqueID = player.getUniqueID(),
            displayName = displayName,
            newTradeUI = true,
            demandBasedPrices = true,
            serializedOffers = Buffer().apply {
                org.chorus_oss.nbt.Tag.serialize(
                    org.chorus_oss.nbt.tags.CompoundTag(
                        CompoundTag()
                            .putList("Recipes", recipes)
                            .putList("TierExpRequirements", tierExpRequirements)
                    ),
                    this,
                    type = TagSerialization.NetLE
                )
            }.readByteString()
        )
        player.sendPacket(packet)
    }

    override fun close() {
        getTradeNetIds().forEach(Consumer { key: Int? -> TradeRecipeBuildUtils.RECIPE_MAP.remove(key) })
        super.close()
    }


    override fun onInteract(player: Player, item: Item, clickedPos: Vector3): Boolean {
        if (this.canTrade!!) {
            player.addWindow(tradeInventory!!)
            return true
        } else return false
    }

    fun addExperience(xp: Int) {
        this.tradeExp += xp
        this.setDataProperty(EntityDataTypes.TRADE_EXPERIENCE, this.tradeExp)
        val next = getTradeTier() + 1
        if (next < tierExpRequirement.size) {
            if (tradeExp >= tierExpRequirement[next]) {
                setTradeTier(next + 1)
            }
        }
    }

    override fun onUpdate(tick: Int): Boolean {
        if (ticksLived % 24000 == 23999) {
            for ((_, values) in memoryStorage.get(CoreMemoryTypes.GOSSIP)!!
                .entries) {
                for (gossip in Gossip.VALUES) {
                    values[gossip.ordinal] =
                        max(0.0, (values.get(gossip.ordinal) - gossip.decay).toDouble()).toInt()
                }
            }
        }

        if (tick % 20 == 0) {
            for (i in level!!.getNearbyEntities(getBoundingBox().grow(1.0, 0.5, 1.0))) {
                if (i is EntityItem) {
                    val item = i.item
                    if (when (item.id) {
                            ItemID.BREAD, ItemID.CARROT, ItemID.POTATO, BlockID.WHEAT, ItemID.WHEAT_SEEDS, ItemID.BEETROOT_SEEDS, BlockID.BEETROOT, ItemID.TORCHFLOWER_SEEDS, ItemID.PITCHER_POD, ItemID.BONE_MEAL -> true
                            else -> false
                        }
                    ) {
                        val slice = InventorySlice(inventory, 1, inventory.size)
                        if (slice.canAddItem(item)) {
                            val pk = org.chorus_oss.protocol.packets.TakeItemEntityPacket(
                                itemEntityRuntimeID = i.getRuntimeID().toULong(),
                                takerEntityRuntimeID = this.getRuntimeID().toULong(),
                            )
                            Server.broadcastPacket(viewers.values, pk)
                            slice.addItem(item)
                            i.close()
                        }
                    }
                }
            }
        }
        return super.onUpdate(tick)
    }

    fun applyProfession() {
        if (this.profession == 0) {
            this.setCanTrade(false)
        } else {
            getTradeNetIds().forEach(Consumer { key -> TradeRecipeBuildUtils.RECIPE_MAP.remove(key) })
            val profession = Profession.getProfession(this.profession)!!
            this.displayName = (profession.getName())
            for (trade in profession.buildTrades(tradeSeed).all) {
                getTradeNetIds().add(trade.getInt("netId"))
            }
            this.setCanTrade(true)
            if (tradeInventory == null) {
                tradeInventory = TradeInventory(this)
            }
        }
    }

    override fun getExperienceDrops(): Int {
        return 0
    }

    enum class Clothing {
        PLAINS,
        DESERT,
        JUNGLE,
        SAVANNA,
        SNOW,
        SWAMP,
        TAIGA;

        companion object {
            fun getClothing(biomeId: Int): Clothing {
                val definition = Registries.BIOME[biomeId]!!
                val tags = definition.tags
                if (tags.contains("desert") || tags.contains("mesa")) return DESERT
                if (tags.contains("jungle")) return JUNGLE
                if (tags.contains("savanna")) return SAVANNA
                if (tags.contains("frozen")) return SNOW
                if (tags.contains("swamp")) return SWAMP
                if (tags.contains("taiga") || tags.contains("extreme_hills")) return TAIGA
                return PLAINS
            }
        }
    }

    enum class Gossip(val gain: Int, val decay: Int, val penalty: Int, val max: Int, val multiplier: Int) {
        MAJOR_POSITIVE(20, 0, 100, 20, 5),
        MINOR_POSITIVE(25, 1, 5, 200, 1),
        MINOR_NEGATIVE(25, 20, 20, 200, -1),
        MAJOR_NEGATIVE(25, 10, 10, 100, -5),
        TRADING(2, 2, 20, 25, 1);

        companion object {
            val VALUES: Array<Gossip> = entries.toTypedArray()
        }
    }
}
