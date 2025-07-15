package org.chorus_oss.chorus.entity.mob.animal

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.EntityShearable
import org.chorus_oss.chorus.entity.EntityWalkable
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
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.route.finder.impl.SimpleFlatAStarRouteFinder
import org.chorus_oss.chorus.entity.ai.route.posevaluator.WalkingPosEvaluator
import org.chorus_oss.chorus.entity.ai.sensor.ISensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestFeedingPlayerSensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestPlayerSensor
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemDye
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationType
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.utils.DyeColor
import java.util.concurrent.ThreadLocalRandom

class EntitySheep(chunk: IChunk?, nbt: CompoundTag) : EntityAnimal(chunk, nbt), EntityWalkable, EntityShearable {
    override fun getEntityIdentifier(): String {
        return EntityID.SHEEP
    }

    override var sheared: Boolean = false
    override var color: Byte = 0

    public override fun requireBehaviorGroup(): IBehaviorGroup {
        return BehaviorGroup(
            this.tickSpread,
            setOf<IBehavior>( //用于刷新InLove状态的核心行为
                Behavior(
                    InLoveExecutor(400),
                    all(
                        PassByTimeEvaluator(CoreMemoryTypes.LAST_BE_FEED_TIME, 0, 400),
                        PassByTimeEvaluator(CoreMemoryTypes.LAST_IN_LOVE_TIME, 6000, Int.MAX_VALUE)
                    ),
                    1, 1
                )
            ),
            setOf<IBehavior>(
                Behavior(
                    FlatRandomRoamExecutor(0.5f, 12, 40, true, 100, true, 10),
                    PassByTimeEvaluator(CoreMemoryTypes.LAST_BE_ATTACKED_TIME, 0, 100),
                    6,
                    1
                ),
                Behavior(
                    EntityBreedingExecutor<EntitySheep>(EntitySheep::class.java, 16, 100, 0.5f),
                    { entity: EntityMob -> entity.memoryStorage.get<Boolean>(CoreMemoryTypes.IS_IN_LOVE) },
                    5,
                    1
                ),
                Behavior(
                    MoveToTargetExecutor(CoreMemoryTypes.NEAREST_FEEDING_PLAYER, 0.5f, true, 8f, 1.5f),
                    MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.NEAREST_FEEDING_PLAYER),
                    4,
                    1
                ),
                Behavior(
                    EatGrassExecutor(40), all(
                        any(
                            all(
                                IBehaviorEvaluator { entity: EntityMob -> entity is EntityAnimal && !entity.isBaby() },
                                ProbabilityEvaluator(1, 100)
                            ),
                            all(
                                IBehaviorEvaluator { entity: EntityMob -> entity is EntityAnimal && entity.isBaby() },
                                ProbabilityEvaluator(43, 50)
                            )
                        ),
                        any(
                            BlockCheckEvaluator(BlockID.GRASS_BLOCK, Vector3(0.0, -1.0, 0.0)),
                            BlockCheckEvaluator(BlockID.TALL_GRASS, Vector3.ZERO)
                        )
                    ),
                    3, 1, 100
                ),
                Behavior(
                    LookAtTargetExecutor(CoreMemoryTypes.NEAREST_PLAYER, 100),
                    ProbabilityEvaluator(4, 10),
                    1,
                    1,
                    100
                ),
                Behavior(
                    FlatRandomRoamExecutor(0.2f, 12, 100, false, -1, true, 10),
                    (IBehaviorEvaluator { entity: EntityMob? -> true }),
                    1,
                    1
                )
            ),
            setOf<ISensor>(NearestFeedingPlayerSensor(8.0, 0.0), NearestPlayerSensor(8.0, 0.0, 20)),
            setOf<IController>(WalkController(), LookController(true, true), FluctuateController()),
            SimpleFlatAStarRouteFinder(WalkingPosEvaluator(), this),
            this
        )
    }

    override fun getWidth(): Float {
        if (this.isBaby()) {
            return 0.45f
        }
        return 0.9f
    }

    override fun getHeight(): Float {
        if (isBaby()) {
            return 0.65f
        }
        return 1.3f
    }

    override fun getOriginalName(): String {
        return "Sheep"
    }


    public override fun initEntity() {
        this.maxHealth = 8
        super.initEntity()

        if (!namedTag!!.contains("Color")) {
            this.setColor(randomColor())
        } else {
            this.setColor(namedTag!!.getByte("Color").toInt())
        }

        if (!namedTag!!.contains("Sheared")) {
            namedTag!!.putByte("Sheared", 0)
        } else {
            this.sheared = namedTag!!.getBoolean("Sheared")
        }

        this.setDataFlag(EntityFlag.SHEARED, this.sheared)
    }

    override fun saveNBT() {
        super.saveNBT()

        namedTag!!.putByte("Color", this.color.toInt())
        namedTag!!.putBoolean("Sheared", this.sheared)
    }

    override fun onInteract(player: Player, item: Item, clickedPos: Vector3): Boolean {
        if (super.onInteract(player, item, clickedPos)) {
            return true
        }

        if (item is ItemDye) {
            this.setColor(item.dyeColor.woolData)
            return true
        }

        return item.isShears && shear()
    }

    override fun shear(): Boolean {
        if (sheared || this.isBaby()) {
            return false
        }

        this.sheared = true
        this.setDataFlag(EntityFlag.SHEARED, true)

        val woolItem = this.getWoolItem()
        woolItem.setCount(ThreadLocalRandom.current().nextInt(2) + 1)
        level!!.dropItem(this.position, woolItem)

        level!!.addSound(this.position, Sound.MOB_SHEEP_SHEAR)
        level!!.vibrationManager.callVibrationEvent(
            VibrationEvent(
                this,
                this.vector3, VibrationType.SHEAR
            )
        )
        return true
    }

    fun growWool() {
        this.setDataFlag(EntityFlag.SHEARED, false)
        this.sheared = false
    }

    override fun getDrops(): Array<Item> {
        val woolItem = if (sheared) Item.AIR else this.getWoolItem()
        return arrayOf(Item.get((if (this.isOnFire()) ItemID.COOKED_MUTTON else ItemID.MUTTON)), woolItem)
    }

    fun getColor(): Int {
        return namedTag!!.getByte("Color").toInt()
    }

    fun setColor(color: Int) {
        this.color = color.toByte()
        this.setDataProperty(EntityDataTypes.COLOR, color)
        namedTag!!.putByte("Color", this.color.toInt())
    }

    fun getWoolItem(): Item {
        return when (getColor()) {
            0 -> Item.get(BlockID.WHITE_WOOL)
            1 -> Item.get(BlockID.ORANGE_WOOL)
            2 -> Item.get(BlockID.MAGENTA_WOOL)
            3 -> Item.get(BlockID.LIGHT_BLUE_WOOL)
            4 -> Item.get(BlockID.YELLOW_WOOL)
            5 -> Item.get(BlockID.LIME_WOOL)
            6 -> Item.get(BlockID.PINK_WOOL)
            7 -> Item.get(BlockID.GRAY_WOOL)
            8 -> Item.get(BlockID.LIGHT_GRAY_WOOL)
            9 -> Item.get(BlockID.CYAN_WOOL)
            10 -> Item.get(BlockID.PURPLE_WOOL)
            11 -> Item.get(BlockID.BLUE_WOOL)
            12 -> Item.get(BlockID.BROWN_WOOL)
            13 -> Item.get(BlockID.GREEN_WOOL)
            14 -> Item.get(BlockID.RED_WOOL)
            15 -> Item.get(BlockID.BLACK_WOOL)
            else -> throw IllegalStateException("Unexpected value: " + getColor())
        }
    }

    private fun randomColor(): Int {
        val random = ThreadLocalRandom.current()
        val rand = random.nextDouble(1.0, 100.0)

        if (rand <= 0.164) {
            return DyeColor.PINK.woolData
        }

        if (rand <= 15) {
            return if (random.nextBoolean()) DyeColor.BLACK.woolData else if (random.nextBoolean()) DyeColor.GRAY.woolData else DyeColor.LIGHT_GRAY.woolData
        }

        return DyeColor.WHITE.woolData
    }
}
