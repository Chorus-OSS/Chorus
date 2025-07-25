package org.chorus_oss.chorus.entity.mob.monster

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.EntityInteractable
import org.chorus_oss.chorus.entity.EntityWalkable
import org.chorus_oss.chorus.entity.ai.behavior.Behavior
import org.chorus_oss.chorus.entity.ai.behavior.IBehavior
import org.chorus_oss.chorus.entity.ai.behaviorgroup.BehaviorGroup
import org.chorus_oss.chorus.entity.ai.behaviorgroup.IBehaviorGroup
import org.chorus_oss.chorus.entity.ai.controller.FluctuateController
import org.chorus_oss.chorus.entity.ai.controller.IController
import org.chorus_oss.chorus.entity.ai.controller.LookController
import org.chorus_oss.chorus.entity.ai.controller.WalkController
import org.chorus_oss.chorus.entity.ai.evaluator.EntityCheckEvaluator
import org.chorus_oss.chorus.entity.ai.evaluator.IBehaviorEvaluator
import org.chorus_oss.chorus.entity.ai.evaluator.MemoryCheckNotEmptyEvaluator
import org.chorus_oss.chorus.entity.ai.executor.EntityExplosionExecutor
import org.chorus_oss.chorus.entity.ai.executor.FlatRandomRoamExecutor
import org.chorus_oss.chorus.entity.ai.executor.FleeFromTargetExecutor
import org.chorus_oss.chorus.entity.ai.executor.MoveToTargetExecutor
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.route.finder.impl.SimpleFlatAStarRouteFinder
import org.chorus_oss.chorus.entity.ai.route.posevaluator.WalkingPosEvaluator
import org.chorus_oss.chorus.entity.ai.sensor.ISensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestEntitySensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestPlayerSensor
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.mob.animal.EntityCat
import org.chorus_oss.chorus.entity.mob.animal.EntityOcelot
import org.chorus_oss.chorus.entity.weather.EntityLightningStrike
import org.chorus_oss.chorus.event.entity.CreeperPowerEvent
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import java.util.concurrent.ThreadLocalRandom

class EntityCreeper(chunk: IChunk?, nbt: CompoundTag) : EntityMonster(chunk, nbt), EntityWalkable, EntityInteractable {
    override fun getEntityIdentifier(): String {
        return EntityID.CREEPER
    }

    public override fun requireBehaviorGroup(): IBehaviorGroup {
        return BehaviorGroup(
            this.tickSpread,
            setOf(),
            setOf<IBehavior>(
                Behavior(
                    FleeFromTargetExecutor(CoreMemoryTypes.NEAREST_SHARED_ENTITY, 0.3f, true, 4f),
                    EntityCheckEvaluator(CoreMemoryTypes.NEAREST_SHARED_ENTITY),
                    5,
                    1
                ),
                Behavior(
                    EntityExplosionExecutor(30, 3, CoreMemoryTypes.SHOULD_EXPLODE),
                    all(
                        { entity: EntityMob ->
                            entity.memoryStorage[CoreMemoryTypes.SHOULD_EXPLODE]
                        },
                        any(
                            { entity: EntityMob? ->
                                memoryStorage[CoreMemoryTypes.NEAREST_PLAYER] != null && level!!.raycastBlocks(
                                    this.position,
                                    memoryStorage[CoreMemoryTypes.NEAREST_PLAYER]!!.position
                                ).isEmpty()
                            },
                            { entity: EntityMob? ->
                                memoryStorage[CoreMemoryTypes.ATTACK_TARGET] != null && level!!.raycastBlocks(
                                    this.position,
                                    memoryStorage[CoreMemoryTypes.ATTACK_TARGET]!!.position
                                ).isEmpty()
                            }
                        )
                    ), 4, 1
                ),
                Behavior(
                    MoveToTargetExecutor(CoreMemoryTypes.ATTACK_TARGET, 0.3f, true, 16f, 3f, true), all(
                        MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.ATTACK_TARGET),
                        { entity: EntityMob ->
                            val attackTarget = entity.memoryStorage[CoreMemoryTypes.ATTACK_TARGET]
                            attackTarget == null || attackTarget !is Player || attackTarget.isSurvival
                        }
                    ), 3, 1),
                Behavior(
                    MoveToTargetExecutor(CoreMemoryTypes.NEAREST_PLAYER, 0.3f, true, 16f, 3f), all(
                        MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.NEAREST_PLAYER),
                        IBehaviorEvaluator { entity: EntityMob ->
                            if (entity.memoryStorage.isEmpty(CoreMemoryTypes.NEAREST_PLAYER)) return@IBehaviorEvaluator true
                            val player = entity.memoryStorage.get<Player>(CoreMemoryTypes.NEAREST_PLAYER)
                            player!!.isSurvival
                        }
                    ), 2, 1),
                Behavior(FlatRandomRoamExecutor(0.3f, 12, 100, false, -1, true, 10), none(), 1, 1)
            ),
            setOf(
                NearestPlayerSensor(16.0, 0.0, 20),
                NearestEntitySensor(EntityCat::class.java, CoreMemoryTypes.NEAREST_SHARED_ENTITY, 42.0, 0.0),
                NearestEntitySensor(
                    EntityOcelot::class.java,
                    CoreMemoryTypes.NEAREST_SHARED_ENTITY,
                    42.0,
                    0.0
                ),
                ISensor { entity: EntityMob ->
                    val memoryStorage = entity.memoryStorage
                    var attacker = memoryStorage[CoreMemoryTypes.ATTACK_TARGET]
                    if (attacker == null) attacker = memoryStorage[CoreMemoryTypes.NEAREST_PLAYER]
                    if (attacker != null && (attacker !is Player || attacker.isSurvival) && attacker.position.distanceSquared(
                            entity.position
                        ) <= 3 * 3 && (!memoryStorage[CoreMemoryTypes.SHOULD_EXPLODE])
                    ) {
                        memoryStorage.set<Boolean>(CoreMemoryTypes.SHOULD_EXPLODE, true)
                        return@ISensor
                    }
                    if ((attacker == null || (attacker is Player && !attacker.isSurvival) || attacker.position.distanceSquared(
                            entity.position
                        ) >= 7 * 7) && memoryStorage[CoreMemoryTypes.SHOULD_EXPLODE] && memoryStorage.get<Boolean>(
                            CoreMemoryTypes.EXPLODE_CANCELLABLE
                        )
                    ) {
                        memoryStorage.set<Boolean>(CoreMemoryTypes.SHOULD_EXPLODE, false)
                    }
                }),
            setOf<IController>(WalkController(), LookController(true, true), FluctuateController()),
            SimpleFlatAStarRouteFinder(WalkingPosEvaluator(), this),
            this
        )
    }


    override fun getWidth(): Float {
        return 0.6f
    }

    override fun getHeight(): Float {
        return 1.8f
    }

    override fun getFloatingHeight(): Float {
        return 0.6f
    }

    fun isPowered(): Boolean {
        return getDataProperty<Byte>(EntityDataTypes.HORSE_TYPE) > 0
    }

    fun setPowered(bolt: EntityLightningStrike?) {
        val ev = CreeperPowerEvent(this, bolt, CreeperPowerEvent.PowerCause.LIGHTNING)
        Server.instance.pluginManager.callEvent(ev)

        if (!ev.cancelled) {
            this.setDataProperty(EntityDataTypes.HORSE_TYPE, 1)
            namedTag!!.putBoolean("powered", true)
        }
    }

    fun setPowered(powered: Boolean) {
        val ev = CreeperPowerEvent(
            this,
            if (powered) CreeperPowerEvent.PowerCause.SET_ON else CreeperPowerEvent.PowerCause.SET_OFF
        )
        Server.instance.pluginManager.callEvent(ev)

        if (!ev.cancelled) {
            this.setDataProperty(EntityDataTypes.HORSE_TYPE, if (powered) 1 else 0)
            namedTag!!.putBoolean("powered", powered)
        }
    }

    override fun onStruckByLightning(entity: Entity) {
        this.setPowered(true)
    }

    override fun initEntity() {
        this.maxHealth = 20
        super.initEntity()

        if (namedTag!!.getBoolean("powered") || namedTag!!.getBoolean("IsPowered")) {
            entityDataMap[EntityDataTypes.HORSE_TYPE] = 1
        }
    }

    override fun getOriginalName(): String {
        return "Creeper"
    }

    override fun getDrops(): Array<Item> {
        if (lastDamageCause is EntityDamageByEntityEvent) {
            return arrayOf(Item.get(ItemID.GUNPOWDER, 0, ThreadLocalRandom.current().nextInt(2) + 1))
        }
        return Item.EMPTY_ARRAY
    }

    override fun isPreventingSleep(player: Player?): Boolean {
        return true
    }

    override fun onInteract(player: Player, item: Item, clickedPos: Vector3): Boolean {
        val memoryStorage = this.memoryStorage
        if (item.id === ItemID.FLINT_AND_STEEL && (!memoryStorage[CoreMemoryTypes.SHOULD_EXPLODE])
        ) {
            memoryStorage.set<Boolean>(CoreMemoryTypes.SHOULD_EXPLODE, true)
            memoryStorage.set<Boolean>(CoreMemoryTypes.EXPLODE_CANCELLABLE, false)
            level!!.addSound(this.position, Sound.FIRE_IGNITE)
            return true
        }
        return super.onInteract(player, item, clickedPos)
    }

    override fun getInteractButtonText(player: Player): String {
        return "action.interact.creeper"
    }

    override fun canDoInteraction(): Boolean {
        return true
    }
}
