package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityCanAttack
import org.chorus_oss.chorus.entity.EntityLiving
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.memory.NullableMemoryType
import org.chorus_oss.chorus.entity.effect.Effect
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageModifier
import org.chorus_oss.chorus.inventory.EntityInventoryHolder
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.protocol.packets.ActorEventPacket
import java.util.*

/**
 * 通用近战攻击执行器.
 *
 *
 * Universal melee attack actuator.
 */
open class MeleeAttackExecutor(
    protected var memory: NullableMemoryType<out Entity>,
    protected var speed: Float,
    maxSenseRange: Int,
    protected var clearDataWhenLose: Boolean,
    protected var coolDown: Int,
    protected var attackRange: Float,
    vararg effects: Effect
) :
    EntityControl, IBehaviorExecutor {
    protected var maxSenseRangeSquared: Int = maxSenseRange * maxSenseRange

    protected var attackTick: Int = 0

    protected var oldTarget: Vector3? = null

    /**
     * 用来指定特定的攻击目标.
     *
     *
     * Used to specify a specific attack target.
     */
    protected var target: Entity? = null

    /**
     * 用来指定特定的视线目标
     *
     *
     * Used to specify a specific look target.
     */
    protected var lookTarget: Vector3? = null

    /**
     * 给予目标药水效果
     *
     *
     * Give target potion effect
     */
    protected var effects: Array<Effect> = effects.toList().toTypedArray()

    constructor(
        memory: NullableMemoryType<out Entity>,
        speed: Float,
        maxSenseRange: Int,
        clearDataWhenLose: Boolean,
        coolDown: Int
    ) : this(memory, speed, maxSenseRange, clearDataWhenLose, coolDown, 2.5f)

    constructor(
        memory: NullableMemoryType<out Entity>,
        speed: Float,
        maxSenseRange: Int,
        clearDataWhenLose: Boolean,
        coolDown: Int,
        vararg effects: Effect
    ) : this(memory, speed, maxSenseRange, clearDataWhenLose, coolDown, 2.5f, *effects)

    override fun execute(entity: EntityMob): Boolean {
        attackTick++
        if (entity.behaviorGroup.memoryStorage.isEmpty(memory)) return false
        val newTarget = entity.behaviorGroup.memoryStorage[memory]!!

        //first is null
        if (this.target == null) {
            this.target = newTarget
        }
        if (this.lookTarget == null) {
            this.lookTarget = target!!.position.clone()
        }

        //some check
        if (!target!!.isAlive()) return false
        else if (entity.position.distanceSquared(target!!.position) > maxSenseRangeSquared) return false
        else if (target is Player) {
            val player = target as Player
            if (player.isCreative || player.isSpectator || !player.isOnline || (entity.level!!.name != player.level!!.name)) {
                return false
            }
        }


        //update target and look target
        if (target!!.locator != newTarget.locator) {
            target = newTarget
        }
        if (this.lookTarget != newTarget.vector3) {
            lookTarget = newTarget.transform.position
        }

        //set some motion control
        if (!entity.isEnablePitch) entity.isEnablePitch = true
        if (entity.movementSpeed != speed) entity.movementSpeed = speed
        //set target and look target
        setRouteTarget(entity, target!!.position.clone())
        setLookTarget(entity, lookTarget!!.clone())

        val floor = target!!.position.floor()
        if (oldTarget == null || oldTarget != floor) entity.behaviorGroup.isForceUpdateRoute = true
        oldTarget = floor

        //attack logic
        if (entity.position.distanceSquared(target!!.position) <= attackRange && attackTick > coolDown) {
            val item = if (entity is EntityInventoryHolder) entity.itemInHand else Item.AIR

            var defaultDamage = 0f
            if (entity is EntityCanAttack) {
                defaultDamage = entity.getDiffHandDamage(Server.instance.getDifficulty())
            }
            var itemDamage = item.getAttackDamage(entity) + defaultDamage

            val enchantments = item.enchantments
            if (item.applyEnchantments()) {
                for (enchantment in enchantments) {
                    itemDamage += enchantment.getDamageBonus(target!!, entity).toFloat()
                }
            }

            val damage: MutableMap<DamageModifier, Float> = EnumMap(
                DamageModifier::class.java
            )
            damage[DamageModifier.BASE] = itemDamage

            var knockBack = 0.3f
            if (item.applyEnchantments()) {
                val knockBackEnchantment = item.getEnchantment(Enchantment.ID_KNOCKBACK)
                if (knockBackEnchantment != null) {
                    knockBack += knockBackEnchantment.level * 0.1f
                }
            }

            val ev = EntityDamageByEntityEvent(
                entity,
                target!!,
                DamageCause.ENTITY_ATTACK,
                damage,
                knockBack,
                if (item.applyEnchantments()) enchantments else null
            )

            ev.isBreakShield = item.canBreakShield()

            target!!.attack(ev)
            if (!ev.cancelled) {
                for (e in effects) {
                    target!!.addEffect(e)
                }

                playAttackAnimation(entity)
                entity.memoryStorage.set(CoreMemoryTypes.LAST_ATTACK_TIME, entity.level!!.tick)
                entity.memoryStorage.set(CoreMemoryTypes.LAST_ATTACK_ENTITY, target!!)
                attackTick = 0
            }

            return target!!.health != 0f
        }
        return true
    }

    override fun onStop(entity: EntityMob) {
        removeRouteTarget(entity)
        removeLookTarget(entity)
        //重置速度
        entity.movementSpeed = EntityLiving.DEFAULT_SPEED
        if (clearDataWhenLose) {
            entity.behaviorGroup.memoryStorage.clear(memory)
        }
        entity.isEnablePitch = false
        this.target = null
        this.lookTarget = null
    }

    override fun onInterrupt(entity: EntityMob) {
        removeRouteTarget(entity)
        removeLookTarget(entity)
        //重置速度
        entity.movementSpeed = EntityLiving.DEFAULT_SPEED
        if (clearDataWhenLose) {
            entity.behaviorGroup.memoryStorage.clear(memory)
        }
        entity.isEnablePitch = false
        this.target = null
        this.lookTarget = null
    }

    protected fun playAttackAnimation(entity: EntityMob) {
        val pk = ActorEventPacket(
            actorRuntimeID = entity.getRuntimeID().toULong(),
            eventType = ActorEventPacket.Companion.Type.ArmSwing,
            eventData = 0
        )
        Server.broadcastPacket(entity.viewers.values, pk)
    }
}
