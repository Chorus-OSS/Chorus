package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.EntityEquipment
import org.chorus_oss.chorus.entity.EntityLiving
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.effect.EffectType
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.mob.monster.EntityMonster
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemGoldIngot
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.level.Sound
import java.util.concurrent.ThreadLocalRandom

class PiglinTradeExecutor : EntityControl, IBehaviorExecutor {
    protected var tick: Int = 0

    override fun execute(entity: EntityMob): Boolean {
        tick++
        if (tick < 160) {
            if (tick % 30 == 0 && ThreadLocalRandom.current().nextBoolean()) {
                entity.level!!.addSound(entity.position, Sound.MOB_PIGLIN_ADMIRING_ITEM)
            }
            return true
        } else {
            if (entity is EntityMonster) {
                val offhand = entity.itemInOffhand
                if (offhand is ItemGoldIngot && !entity.isBaby()) {
                    entity.equipment.decreaseCount(EntityEquipment.Companion.OFF_HAND)
                    val motion = entity.getDirectionVector().multiply(0.4)
                    entity.level!!.dropItem(entity.position.add(0.0, 1.3, 0.0), drop, motion, 40)
                }
            }
            return false
        }
    }


    override fun onStart(entity: EntityMob) {
        tick = -1
        removeLookTarget(entity)
        entity.setDataFlag(EntityFlag.ADMIRING)
    }

    override fun onStop(entity: EntityMob) {
        if (entity is EntityMonster) {
            clearOffhand(entity, entity.itemInHand)
        }
        entity.movementSpeed = EntityLiving.DEFAULT_SPEED
        entity.isEnablePitch = false
        entity.setDataFlag(EntityFlag.ADMIRING, false)
    }

    val drop: Item
        get() {
            val item: Item
            val random = ThreadLocalRandom.current().nextInt(459)
            if (random < 5) {
                item = Item.get(ItemID.ENCHANTED_BOOK)
                item.addEnchantment(
                    Enchantment.get(Enchantment.ID_SOUL_SPEED)
                        .setLevel(ThreadLocalRandom.current().nextInt(1, 3))
                )
            } else if (random < 13) {
                item = Item.get(ItemID.IRON_BOOTS)
                item.addEnchantment(
                    Enchantment.get(Enchantment.ID_SOUL_SPEED)
                        .setLevel(ThreadLocalRandom.current().nextInt(1, 3))
                )
            } else if (random < 21) {
                item = Item.get(
                    ItemID.SPLASH_POTION,
                    EffectType.FIRE_RESISTANCE.id
                )
            } else if (random < 29) {
                item = Item.get(
                    ItemID.POTION,
                    EffectType.FIRE_RESISTANCE.id
                )
            } else if (random < 39) {
                item = Item.get(ItemID.POTION)
            } else if (random < 49) {
                item = Item.get(
                    ItemID.IRON_NUGGET,
                    0,
                    ThreadLocalRandom.current().nextInt(10, 37)
                )
            } else if (random < 59) {
                item = Item.get(
                    ItemID.ENDER_PEARL,
                    0,
                    ThreadLocalRandom.current().nextInt(2, 5)
                )
            } else if (random < 79) {
                item = Item.get(
                    ItemID.STRING,
                    0,
                    ThreadLocalRandom.current().nextInt(3, 10)
                )
            } else if (random < 99) {
                item = Item.get(
                    ItemID.QUARTZ,
                    0,
                    ThreadLocalRandom.current().nextInt(5, 13)
                )
            } else if (random < 139) {
                item = Item.get(BlockID.OBSIDIAN)
            } else if (random < 179) {
                item = Item.get(
                    BlockID.CRYING_OBSIDIAN,
                    0,
                    ThreadLocalRandom.current().nextInt(1, 4)
                )
            } else if (random < 219) {
                item = Item.get(ItemID.FIRE_CHARGE)
            } else if (random < 259) {
                item = Item.get(
                    ItemID.LEATHER,
                    0,
                    ThreadLocalRandom.current().nextInt(2, 5)
                )
            } else if (random < 299) {
                item = Item.get(
                    BlockID.SOUL_SAND,
                    0,
                    ThreadLocalRandom.current().nextInt(2, 9)
                )
            } else if (random < 339) {
                item = Item.get(
                    ItemID.NETHERBRICK,
                    0,
                    ThreadLocalRandom.current().nextInt(2, 9)
                )
            } else if (random < 379) {
                item = Item.get(
                    ItemID.ARROW,
                    0,
                    ThreadLocalRandom.current().nextInt(6, 12)
                )
            } else if (random < 419) {
                item = Item.get(
                    BlockID.GRAVEL,
                    0,
                    ThreadLocalRandom.current().nextInt(8, 17)
                )
            } else {
                item = Item.get(
                    BlockID.BLACKSTONE,
                    0,
                    ThreadLocalRandom.current().nextInt(8, 17)
                )
            }
            return item
        }

    override fun onInterrupt(entity: EntityMob) {
        onStop(entity)
    }

    private fun clearOffhand(mob: EntityMonster, item: Item) {
        mob.level!!.dropItem(mob.position, item) // TODO
        mob.equipment.clear(EntityEquipment.OFF_HAND)
    }
}


