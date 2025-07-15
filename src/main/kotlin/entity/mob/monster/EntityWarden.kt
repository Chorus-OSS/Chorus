package org.chorus_oss.chorus.entity.mob.monster

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityCreature
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
import org.chorus_oss.chorus.entity.ai.evaluator.RandomTimeRangeEvaluator
import org.chorus_oss.chorus.entity.ai.executor.*
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.route.finder.impl.SimpleFlatAStarRouteFinder
import org.chorus_oss.chorus.entity.ai.route.posevaluator.WalkingPosEvaluator
import org.chorus_oss.chorus.entity.ai.sensor.ISensor
import org.chorus_oss.chorus.entity.ai.sensor.RouteUnreachableTimeSensor
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.effect.Effect
import org.chorus_oss.chorus.entity.effect.EffectType
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.projectile.EntityProjectile
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationListener
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.network.protocol.EntityEventPacket
import org.chorus_oss.chorus.network.protocol.LevelSoundEventPacket
import kotlin.math.abs

class EntityWarden(chunk: IChunk?, nbt: CompoundTag) : EntityMonster(chunk, nbt), EntityWalkable, VibrationListener {
    protected var lastDetectTime: Int = level!!.tick
    protected var lastCollideTime: Int = level!!.tick
    protected var waitForVibration: Boolean = false
    override fun getEntityIdentifier(): String {
        return EntityID.WARDEN
    }

    public override fun requireBehaviorGroup(): IBehaviorGroup {
        return BehaviorGroup(
            this.tickSpread,
            setOf<IBehavior>(
                Behavior({
                    //刷新随机播放音效
                    if (this.memoryStorage.notEmpty(CoreMemoryTypes.ATTACK_TARGET)) setAmbientSoundEvent(
                        Sound.MOB_WARDEN_ANGRY
                    )
                    else if (this.memoryStorage.notEmpty(CoreMemoryTypes.WARDEN_ANGER_VALUE)) setAmbientSoundEvent(
                        Sound.MOB_WARDEN_AGITATED
                    )
                    else setAmbientSoundEvent(Sound.MOB_WARDEN_IDLE)
                    false
                }, { true }, 1, 1, 20),
                Behavior({ entity: EntityMob ->
                    //刷新anger数值
                    val angerValueMap = this.memoryStorage[CoreMemoryTypes.WARDEN_ANGER_VALUE]!!
                    val iterator = angerValueMap.entries.iterator()
                    while (iterator.hasNext()) {
                        val next =
                            iterator.next()
                        if (entity.level!!.name != level!!.name || !isValidAngerEntity(next.key)) {
                            iterator.remove()
                            val attackTarget =
                                this.memoryStorage.get<Entity>(CoreMemoryTypes.ATTACK_TARGET)
                            if (attackTarget != null && attackTarget == next.key) this.memoryStorage.clear(
                                CoreMemoryTypes.ATTACK_TARGET
                            )
                            continue
                        }
                        val newAnger = next.value - 1
                        if (newAnger == 0) iterator.remove()
                        else next.setValue(newAnger)
                    }
                    false
                }, { entity: EntityMob? -> true }, 1, 1, 20),
                Behavior({ entity: EntityMob ->
                    //为玩家附加黑暗效果
                    for (player in entity.level!!.players.values) {
                        if (!player.isCreative && !player.isSpectator && entity.position.distanceSquared(player.position) <= 400) {
                            var effect =
                                player.getEffect(EffectType.DARKNESS)
                            if (effect == null) {
                                effect =
                                    Effect.get(EffectType.DARKNESS)
                                effect.setDuration(260)
                                player.addEffect(effect)
                                continue
                            }
                            effect.setDuration(effect.getDuration() + 260)
                            player.addEffect(effect)
                        }
                    }
                    false
                }, { entity: EntityMob? -> true }, 1, 1, 120),
                Behavior({ entity: EntityMob? ->
                    //计算心跳间隔
                    this.setDataProperty(
                        EntityDataTypes.HEARTBEAT_INTERVAL_TICKS,
                        calHeartBeatDelay()
                    )
                    false
                }, { entity: EntityMob? -> true }, 1, 1, 20)
            ),
            setOf<IBehavior>(
                Behavior(
                    WardenViolentAnimationExecutor((4.2 * 20).toInt()), all(
                        IBehaviorEvaluator { entity: EntityMob ->
                            entity.memoryStorage[CoreMemoryTypes.IS_ATTACK_TARGET_CHANGED]
                        },
                        MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.ATTACK_TARGET)
                    ), 5
                ),
                Behavior(
                    WardenRangedAttackExecutor((1.7 * 20).toInt(), (3.0 * 20).toInt()),
                    { entity: EntityMob? ->
                        this.memoryStorage.get<Int>(CoreMemoryTypes.ROUTE_UNREACHABLE_TIME) > 20 //1s
                                && this.memoryStorage.notEmpty(CoreMemoryTypes.ATTACK_TARGET)
                                && isInRangedAttackRange(
                            this.memoryStorage[CoreMemoryTypes.ATTACK_TARGET]!!
                        )
                    },
                    4, 1, 20
                ),
                Behavior(
                    WardenMeleeAttackExecutor(
                        CoreMemoryTypes.ATTACK_TARGET,
                        when (Server.instance.getDifficulty()) {
                            1 -> 16
                            2 -> 30
                            3 -> 45
                            else -> 0
                        }.toFloat(), 0.7f
                    ),
                    IBehaviorEvaluator { entity: EntityMob ->
                        if (entity.memoryStorage.isEmpty(CoreMemoryTypes.ATTACK_TARGET)) {
                            return@IBehaviorEvaluator false
                        } else {
                            val e = entity.memoryStorage.get<Entity>(CoreMemoryTypes.ATTACK_TARGET)
                            if (e is Player) {
                                return@IBehaviorEvaluator e.isSurvival || e.isAdventure
                            }
                            return@IBehaviorEvaluator true
                        }
                    }, 3, 1
                ),
                Behavior(WardenSniffExecutor((4.2 * 20).toInt(), 35), RandomTimeRangeEvaluator(5 * 20, 10 * 20), 2),
                Behavior(
                    FlatRandomRoamExecutor(0.1f, 12, 100, true, -1, true, 10),
                    (IBehaviorEvaluator { entity: EntityMob? -> true }),
                    1
                )
            ),
            setOf<ISensor>(RouteUnreachableTimeSensor(CoreMemoryTypes.ROUTE_UNREACHABLE_TIME)),
            setOf<IController>(WalkController(), LookController(true, true), FluctuateController()),
            SimpleFlatAStarRouteFinder(WalkingPosEvaluator(), this),
            this
        )
    }


    override fun getHeight(): Float {
        return 2.9f
    }

    override fun getFloatingHeight(): Float {
        return 0.8f
    }

    override fun getWidth(): Float {
        return 0.9f
    }

    override fun initEntity() {
        this.maxHealth = 500
        super.initEntity()
        this.setDataProperty(EntityDataTypes.HEARTBEAT_INTERVAL_TICKS, 40)
        this.setDataProperty(EntityDataTypes.HEARTBEAT_SOUND_EVENT, LevelSoundEventPacket.SOUND_HEARTBEAT)
        //空闲声音
        this.setAmbientSoundEvent(Sound.MOB_WARDEN_IDLE)
        this.setAmbientSoundInterval(8.0f)
        this.setAmbientSoundIntervalRange(16.0f)
        level!!.vibrationManager.addListener(this)
        this.diffHandDamage = floatArrayOf(16f, 30f, 45f)
    }

    override fun getOriginalName(): String {
        return "Warden"
    }

    override val listenerVector: Vector3
        get() = this.vector3

    override fun onVibrationOccur(event: VibrationEvent): Boolean {
        if (level!!.tick - this.lastDetectTime >= 40 && !waitForVibration && (event.initiator !is EntityWarden)) {
            this.waitForVibration = true
            return true
        } else {
            return false
        }
    }

    override fun onVibrationArrive(event: VibrationEvent) {
        this.waitForVibration = false
        this.lastDetectTime = level!!.tick
        val pk = EntityEventPacket()
        pk.eid = this.getRuntimeID()
        pk.event = EntityEventPacket.VIBRATION_DETECTED
        Server.broadcastPacket(this.viewers.values, pk)

        //handle anger value
        val initiator = event.initiator
        if (initiator is Entity) {
            if (isValidAngerEntity(initiator)) {
                val addition = if (initiator is EntityProjectile) 10 else 35
                addEntityAngerValue(initiator, addition)
            }
        }

        if (this.memoryStorage.notEmpty(CoreMemoryTypes.ATTACK_TARGET)) level!!.addSound(
            this.position,
            Sound.MOB_WARDEN_LISTENING_ANGRY
        )
        else level!!.addSound(this.position, Sound.MOB_WARDEN_LISTENING)
    }

    override val listenRange: Double
        get() = 16.0

    override fun close() {
        super.close()
        level!!.vibrationManager.removeListener(this)
    }

    override fun knockBack(attacker: Entity?, damage: Double, x: Double, z: Double, base: Double) {
        //anti-kb
    }

    override fun onCollide(currentTick: Int, collidingEntities: List<Entity>): Boolean {
        if (level!!.tick - this.lastCollideTime > 20) {
            for (collidingEntity in collidingEntities) {
                if (isValidAngerEntity(collidingEntity)) addEntityAngerValue(collidingEntity, 35)
            }
            this.lastCollideTime = level!!.tick
        }
        return super.onCollide(currentTick, collidingEntities)
    }

    override fun attack(source: EntityDamageEvent): Boolean {
        val cause = source.cause
        if (cause == DamageCause.LAVA || cause == DamageCause.HOT_FLOOR || cause == DamageCause.FIRE || cause == DamageCause.FIRE_TICK || cause == DamageCause.DROWNING) return false
        if (source is EntityDamageByEntityEvent && isValidAngerEntity(source.damager)) {
            val damager = source.damager
            val realDamager = if (damager is EntityProjectile) damager.shootingEntity!! else damager
            addEntityAngerValue(realDamager, 100)
        }
        return super.attack(source)
    }

    fun addEntityAngerValue(entity: Entity, addition: Int) {
        val angerValueMap = this.memoryStorage[CoreMemoryTypes.WARDEN_ANGER_VALUE]!!
        val attackTarget = this.memoryStorage[CoreMemoryTypes.ATTACK_TARGET]
        val origin = angerValueMap.getOrDefault(entity, 0)
        var added = (origin + addition).coerceIn(0, 150)
        if (added == 0) angerValueMap.remove(entity)
        else if (added >= 80) {
            added += 20
            added = added.coerceIn(0, 150)
            angerValueMap[entity] = added
            val changed = attackTarget == null ||
                    (entity is Player && attackTarget !is Player)
            if (changed) {
                this.memoryStorage
                    .set<Boolean>(CoreMemoryTypes.IS_ATTACK_TARGET_CHANGED, true)
                this.memoryStorage[CoreMemoryTypes.ATTACK_TARGET] = entity
            }
        } else angerValueMap[entity] = added
    }

    fun isValidAngerEntity(entity: Entity): Boolean {
        return isValidAngerEntity(entity, false)
    }

    fun isValidAngerEntity(entity: Entity, sniff: Boolean): Boolean {
        if (entity.isClosed()) return false
        if (entity.health <= 0) return false
        if (!(if (sniff) isInSniffRange(entity) else isInAngerRange(entity))) return false
        if (entity !is EntityCreature) return false
        if (entity is Player && (!entity.isSurvival && !entity.isAdventure)) return false
        return entity !is EntityWarden
    }

    fun isInSniffRange(entity: Entity): Boolean {
        val deltaX = position.x - entity.position.x
        val deltaZ = position.z - entity.position.z
        val distanceXZSqrt = deltaX * deltaX + deltaZ * deltaZ
        val deltaY = abs(position.y - entity.position.y)
        return distanceXZSqrt <= 36
                && deltaY <= 400
    }

    fun isInRangedAttackRange(entity: Entity): Boolean {
        val deltaX = position.x - entity.position.x
        val deltaZ = position.z - entity.position.z
        val distanceXZSqrt = deltaX * deltaX + deltaZ * deltaZ
        val deltaY = abs(position.y - entity.position.y)
        return distanceXZSqrt <= 225
                && deltaY <= 400
    }

    fun isInAngerRange(entity: Entity): Boolean {
        val distanceSqrt = position.distanceSquared(entity.position)
        return distanceSqrt <= 625
    }

    fun calHeartBeatDelay(): Int {
        val target = this.memoryStorage[CoreMemoryTypes.ATTACK_TARGET]
        val anger = this.memoryStorage[CoreMemoryTypes.WARDEN_ANGER_VALUE]!!.getOrDefault(target, 0)
        return (40 - (anger / 80f).coerceIn(0f, 1f) * 30f).toInt()
    }

    override fun setOnFire(seconds: Int) {
        //against fire
    }
}
