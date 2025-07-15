package org.chorus_oss.chorus.entity.mob.animal

import org.chorus_oss.chorus.entity.ClimateVariant
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.EntityWalkable
import org.chorus_oss.chorus.entity.ai.behavior.Behavior
import org.chorus_oss.chorus.entity.ai.behavior.IBehavior
import org.chorus_oss.chorus.entity.ai.behaviorgroup.BehaviorGroup
import org.chorus_oss.chorus.entity.ai.behaviorgroup.IBehaviorGroup
import org.chorus_oss.chorus.entity.ai.controller.FluctuateController
import org.chorus_oss.chorus.entity.ai.controller.IController
import org.chorus_oss.chorus.entity.ai.controller.LookController
import org.chorus_oss.chorus.entity.ai.controller.WalkController
import org.chorus_oss.chorus.entity.ai.evaluator.IBehaviorEvaluator
import org.chorus_oss.chorus.entity.ai.evaluator.MemoryCheckNotEmptyEvaluator
import org.chorus_oss.chorus.entity.ai.evaluator.PassByTimeEvaluator
import org.chorus_oss.chorus.entity.ai.evaluator.ProbabilityEvaluator
import org.chorus_oss.chorus.entity.ai.executor.*
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.route.finder.impl.SimpleFlatAStarRouteFinder
import org.chorus_oss.chorus.entity.ai.route.posevaluator.WalkingPosEvaluator
import org.chorus_oss.chorus.entity.ai.sensor.ISensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestFeedingPlayerSensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestPlayerSensor
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class EntityChicken(chunk: IChunk?, nbt: CompoundTag) : EntityAnimal(chunk, nbt), EntityWalkable, ClimateVariant {
    override fun getEntityIdentifier(): String {
        return EntityID.CHICKEN
    }


    override fun updateMovement() {
        //补充鸡的缓慢无伤落地特性
        if (!this.onGround && motion.y < -0.08f) {
            motion.y = -0.08
            this.highestPosition = position.y
        }
        super.updateMovement()
    }

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
                ),  //生长
                Behavior(
                    AnimalGrowExecutor(),  //todo：Growth rate
                    all(
                        PassByTimeEvaluator(CoreMemoryTypes.ENTITY_SPAWN_TIME, 20 * 60 * 20, Int.MAX_VALUE),
                        IBehaviorEvaluator { entity: EntityMob -> entity is EntityAnimal && entity.isBaby() }
                    ),
                    1, 1, 1200
                )
            ),
            setOf<IBehavior>(
                Behavior(
                    FlatRandomRoamExecutor(0.22f, 12, 40, true, 100, true, 10),
                    PassByTimeEvaluator(CoreMemoryTypes.LAST_BE_ATTACKED_TIME, 0, 100),
                    6,
                    1
                ),
                Behavior(
                    EntityBreedingExecutor<EntityChicken>(EntityChicken::class.java, 16, 100, 0.3f),
                    { entity: EntityMob -> entity.memoryStorage.get<Boolean>(CoreMemoryTypes.IS_IN_LOVE) },
                    5,
                    1
                ),
                Behavior(
                    MoveToTargetExecutor(CoreMemoryTypes.NEAREST_FEEDING_PLAYER, 0.22f, true),
                    MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.NEAREST_FEEDING_PLAYER),
                    4,
                    1
                ),
                Behavior(
                    LookAtTargetExecutor(CoreMemoryTypes.NEAREST_PLAYER, 100),
                    ProbabilityEvaluator(4, 10),
                    1,
                    1,
                    100
                ),
                Behavior(
                    FlatRandomRoamExecutor(0.22f, 12, 100, false, -1, true, 10),
                    (IBehaviorEvaluator { entity: EntityMob? -> true }),
                    1,
                    1
                ),
                Behavior(
                    { entity: EntityMob ->
                        entity.memoryStorage.set<Int>(
                            CoreMemoryTypes.LAST_EGG_SPAWN_TIME,
                            level!!.tick
                        )
                        entity.level!!.dropItem(entity.position, Item.get(ItemID.EGG))
                        entity.level!!.addSound(entity.position, Sound.MOB_CHICKEN_PLOP)
                        false
                    }, any(
                        all(
                            PassByTimeEvaluator(CoreMemoryTypes.LAST_EGG_SPAWN_TIME, 6000, 12000),
                            ProbabilityEvaluator(20, 100)
                        ),
                        PassByTimeEvaluator(CoreMemoryTypes.LAST_EGG_SPAWN_TIME, 12000, Int.MAX_VALUE)
                    ), 1, 1, 20
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
            return 0.3f
        }
        return 0.6f
    }

    override fun getHeight(): Float {
        if (this.isBaby()) {
            return 0.4f
        }
        return 0.8f
    }

    override fun getOriginalName(): String {
        return "Chicken"
    }

    override fun getDrops(): Array<Item> {
        return arrayOf(
            Item.get((if (this.isOnFire()) ItemID.COOKED_CHICKEN else ItemID.CHICKEN)),
            Item.get(ItemID.FEATHER)
        )
    }


    override fun initEntity() {
        this.maxHealth = 4
        super.initEntity()
        if (namedTag!!.contains("variant")) {
            this.climateVariant = ClimateVariant.Companion.Variant.get(namedTag!!.getString("variant"))
        } else {
            this.climateVariant = this.getBiomeVariant(
                this.level!!.getBiomeId(
                    this.position.x.toInt(),
                    this.position.y.toInt(),
                    this.position.z.toInt()
                )
            )
        }
    }

    override fun isBreedingItem(item: Item): Boolean {
        val id = item.id

        return id === ItemID.WHEAT_SEEDS || id === ItemID.MELON_SEEDS || id === ItemID.PUMPKIN_SEEDS || id === ItemID.BEETROOT_SEEDS
    }
}
