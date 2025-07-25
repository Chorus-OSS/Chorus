package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.EntityLiving
import org.chorus_oss.chorus.entity.ai.memory.NullableMemoryType
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.projectile.EntityProjectile
import org.chorus_oss.chorus.entity.projectile.abstract_arrow.EntityArrow
import org.chorus_oss.chorus.event.entity.EntityShootBowEvent
import org.chorus_oss.chorus.event.entity.ProjectileLaunchEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemCrossbow
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.FloatTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import java.util.function.Supplier
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class CrossBowShootExecutor(
    /**
     * 用来射击的物品
     */
    protected var item: Supplier<Item>,
    protected var memory: NullableMemoryType<out Entity>,
    protected var speed: Float,
    maxShootDistance: Int,
    protected var clearDataWhenLose: Boolean,
    protected val coolDownTick: Int,
    protected val pullBowTick: Int
) :
    EntityControl, IBehaviorExecutor {
    protected var maxShootDistanceSquared: Int = maxShootDistance * maxShootDistance

    /**
     * 用来指定特定的攻击目标.
     *
     *
     * Used to specify a specific attack target.
     */
    protected var target: Entity? = null
    private var tick1 = 0 //control the coolDownTick
    private var tick2 = 0 //control the pullBowTick

    override fun execute(entity: EntityMob): Boolean {
        if (tick2 == 0) {
            tick1++
        }
        if (!entity.isEnablePitch) entity.isEnablePitch = true
        if (entity.behaviorGroup.memoryStorage.isEmpty(memory)) return false
        val newTarget = entity.behaviorGroup.memoryStorage[memory]
        if (this.target == null) target = newTarget
        //some check
        if (!target!!.isAlive()) return false
        else if (target is Player) {
            val player = target as Player
            if (player.isCreative || player.isSpectator || !player.isOnline || (entity.level!!.name != player.level!!.name)) {
                return false
            }
        }

        if (target!!.locator != newTarget!!.locator) {
            //更新目标
            target = newTarget
        }

        if (entity.movementSpeed != speed) entity.movementSpeed = speed
        val clone = target!!.transform

        if (entity.position.distanceSquared(target!!.position) > maxShootDistanceSquared) {
            //更新寻路target
            setRouteTarget(entity, clone.position)
        } else {
            setRouteTarget(entity, null)
        }
        //更新视线target
        setLookTarget(entity, clone.position)

        if (tick2 == 0 && tick1 > coolDownTick) {
            if (entity.position.distanceSquared(target!!.position) <= maxShootDistanceSquared) {
                this.tick1 = 0
                tick2++
                playBowAnimation(entity, 0)
            }
        }
        if (tick2 != 0) {
            playBowAnimation(entity, tick2)
            tick2++
            if (tick2 > pullBowTick) {
                val tool = item.get()
                if (tool is ItemCrossbow) {
                    bowShoot(tool, entity)
                    stopBowAnimation(entity)
                    tick2 = 0
                    return target!!.health != 0f
                }
            }
        }
        return true
    }

    override fun onStop(entity: EntityMob) {
        removeRouteTarget(entity)
        removeLookTarget(entity)
        //重置速度
        entity.movementSpeed = EntityLiving.Companion.DEFAULT_SPEED
        if (clearDataWhenLose) {
            entity.behaviorGroup.memoryStorage.clear(memory)
        }
        entity.isEnablePitch = false
        stopBowAnimation(entity)
        this.target = null
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
        stopBowAnimation(entity)
        this.target = null
    }

    protected fun bowShoot(bow: ItemCrossbow, entity: EntityMob) {
        var damage = 2.0
        val bowDamage = bow.getEnchantment(Enchantment.ID_BOW_POWER)
        if (bowDamage != null && bowDamage.level > 0) {
            damage += bowDamage.level.toDouble() * 0.5 + 0.5
        }
        val flameEnchant = bow.getEnchantment(Enchantment.ID_BOW_FLAME)
        val flame = flameEnchant != null && flameEnchant.level > 0

        val nbt = CompoundTag()
            .putList(
                "Pos", ListTag<FloatTag>()
                    .add(FloatTag(entity.position.x))
                    .add(FloatTag(entity.position.y + entity.getCurrentHeight() / 2 + 0.2f))
                    .add(FloatTag(entity.position.z))
            )
            .putList(
                "Motion", ListTag<FloatTag>()
                    .add(FloatTag(-sin(entity.headYaw / 180 * Math.PI) * cos(entity.rotation.pitch / 180 * Math.PI)))
                    .add(FloatTag(-sin(entity.rotation.pitch / 180 * Math.PI)))
                    .add(FloatTag(cos(entity.headYaw / 180 * Math.PI) * cos(entity.rotation.pitch / 180 * Math.PI)))
            )
            .putList(
                "Rotation", ListTag<FloatTag>()
                    .add(FloatTag((if (entity.headYaw > 180) 360 else 0).toFloat() - entity.headYaw))
                    .add(FloatTag(-entity.rotation.pitch.toFloat()))
            )
            .putShort("Fire", if (flame) 45 * 60 else 0)
            .putDouble("damage", damage)

        val p = pullBowTick.toDouble() / 20
        val f = min((p * p + p * 2) / 3, 1.0) * 3

        val arrow = Entity.Companion.createEntity(
            EntityID.ARROW,
            entity.chunk!!, nbt, entity, f == 2.0
        ) as EntityArrow
        if (arrow == null) {
            return
        }
        arrow.pickupMode = EntityProjectile.PICKUP_NONE

        val entityShootBowEvent = EntityShootBowEvent(entity, bow, arrow, f)
        Server.instance.pluginManager.callEvent(entityShootBowEvent)
        if (entityShootBowEvent.cancelled) {
            entityShootBowEvent.projectile.kill()
        } else {
            entityShootBowEvent.projectile.setMotion(
                entityShootBowEvent.projectile.getMotion().multiply(entityShootBowEvent.force)
            )
            if (entityShootBowEvent.projectile != null) {
                val projectev = ProjectileLaunchEvent(entityShootBowEvent.projectile, entity)
                Server.instance.pluginManager.callEvent(projectev)
                if (projectev.cancelled) {
                    entityShootBowEvent.projectile.kill()
                } else {
                    entityShootBowEvent.projectile.spawnToAll()
                    entity.level!!.addSound(entity.position, Sound.RANDOM_BOW)
                }
            }
        }
    }

    private fun playBowAnimation(entity: Entity, chargeAmount: Int) {
        if (chargeAmount == 0) {
            entity.level!!.addSound(entity.position, Sound.CROSSBOW_LOADING_START)
            entity.setDataProperty(EntityDataTypes.TARGET_EID, target!!.getRuntimeID())
            entity.setDataFlag(EntityFlag.USING_ITEM)
        } else entity.setDataProperty(EntityDataTypes.CHARGE_AMOUNT, chargeAmount * 2)
        if (chargeAmount == 30) entity.level!!.addSound(entity.position, Sound.CROSSBOW_LOADING_MIDDLE)
        if (chargeAmount == 60) {
            entity.setDataFlag(EntityFlag.CHARGED)
            entity.level!!.addSound(entity.position, Sound.CROSSBOW_LOADING_END)
        }
    }

    private fun stopBowAnimation(entity: Entity) {
        entity.setDataProperty(EntityDataTypes.TARGET_EID, 0L)
        entity.setDataProperty(EntityDataTypes.CHARGE_AMOUNT, 0)
        entity.setDataFlag(EntityFlag.USING_ITEM, false)
        entity.setDataFlag(EntityFlag.CHARGED, false)
    }
}
