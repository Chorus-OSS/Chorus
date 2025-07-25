package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockMagma
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityLiving
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.effect.EffectType
import org.chorus_oss.chorus.entity.effect.PotionType
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.mob.monster.EntityMonster
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemPotion
import org.chorus_oss.chorus.plugin.InternalPlugin
import java.util.*
import java.util.function.Consumer

class UsePotionExecutor
/**
 *
 * @param speed             <br></br>The speed of movement towards the attacking target
 * @param coolDownTick      <br></br>Attack cooldown (tick)
 * @param useDelay          <br></br>Attack Animation time(tick)
 */(protected var speed: Float, protected val coolDownTick: Int, protected val useDelay: Int) : EntityControl,
    IBehaviorExecutor {
    private var tick1 = 0 //control the coolDownTick
    private var tick2 = 0 //control the pullBowTick

    override fun execute(entity: EntityMob): Boolean {
        if (tick2 == 0) {
            tick1++
        }
        if (!entity.isEnablePitch) entity.isEnablePitch = true

        if (entity.movementSpeed != speed) entity.movementSpeed = speed

        setRouteTarget(entity, null)

        if (tick2 == 0 && tick1 > coolDownTick) {
            this.tick1 = 0
            tick2++
            startShootSequence(entity)
        } else if (tick2 != 0) {
            tick2++
            if (tick2 > useDelay) {
                entity.level!!.scheduler.scheduleDelayedTask(
                    InternalPlugin.INSTANCE,
                    { endShootSequence(entity) }, 20
                )
                tick2 = 0
                return true
            }
        }
        return true
    }

    override fun onStop(entity: EntityMob) {
        entity.movementSpeed = EntityLiving.Companion.DEFAULT_SPEED
        entity.isEnablePitch = false
        endShootSequence(entity)
    }

    override fun onInterrupt(entity: EntityMob) {
        entity.movementSpeed = EntityLiving.Companion.DEFAULT_SPEED
        entity.isEnablePitch = false
        endShootSequence(entity)
    }


    private fun startShootSequence(entity: Entity) {
        if (entity is EntityMonster) {
            entity.setItemInHand(getPotion(entity))
        }
    }

    private fun endShootSequence(entity: Entity) {
        if (entity is EntityMonster) {
            val item = entity.itemInHand
            if (item is ItemPotion) {
                PotionType.Companion.get(item.damage).getEffects(false)
                    .forEach(Consumer { effect -> entity.addEffect(effect) })
            }
            entity.setItemInHand(Item.AIR)
        }
    }

    fun getPotion(entity: Entity): Item {
        if (entity.isInsideOfWater() && !entity.hasEffect(EffectType.WATER_BREATHING)) {
            return ItemPotion.fromPotion(PotionType.Companion.WATER_BREATHING)
        } else if (!entity.hasEffect(EffectType.FIRE_RESISTANCE) && (entity.isOnFire() || Arrays.stream<Block>(
                entity.level!!.getCollisionBlocks(
                    entity.getBoundingBox().getOffsetBoundingBox(0.0, -1.0, 0.0)
                )
            ).anyMatch { block: Block? -> block is BlockMagma })
        ) {
            return ItemPotion.fromPotion(PotionType.Companion.FIRE_RESISTANCE)
        } else if (entity.health < entity.maxHealth) {
            return ItemPotion.fromPotion(PotionType.Companion.HEALING)
        } else if (entity is EntityMob) {
            if (entity.memoryStorage.notEmpty(CoreMemoryTypes.BE_ATTACKED_EVENT)) {
                val event = entity.memoryStorage.get(CoreMemoryTypes.BE_ATTACKED_EVENT)
                if (event is EntityDamageByEntityEvent) {
                    if (event.damager.position.distance(entity.position) > 11) {
                        return ItemPotion.fromPotion(PotionType.Companion.SWIFTNESS)
                    }
                }
            }
        }
        return Item.AIR
    }
}
