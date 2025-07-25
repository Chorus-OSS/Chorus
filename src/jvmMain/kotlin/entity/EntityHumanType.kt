package org.chorus_oss.chorus.entity

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageModifier
import org.chorus_oss.chorus.inventory.HumanEnderChestInventory
import org.chorus_oss.chorus.inventory.HumanInventory
import org.chorus_oss.chorus.inventory.HumanOffHandInventory
import org.chorus_oss.chorus.inventory.Inventory
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemShield
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.utils.Utils
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.ceil

abstract class EntityHumanType(chunk: IChunk?, nbt: CompoundTag) : EntityMob(chunk, nbt), IHuman {
    override lateinit var inventory: HumanInventory
    override lateinit var enderChestInventory: HumanEnderChestInventory
    override lateinit var offhandInventory: HumanOffHandInventory

    override fun setInventories(inventory: Array<Inventory>) {
        this.inventory = inventory[0] as HumanInventory
        this.offhandInventory = inventory[1] as HumanOffHandInventory
        this.enderChestInventory = inventory[2] as HumanEnderChestInventory
    }

    override fun getDrops(): Array<Item> {
        if (this.inventory != null) {
            val drops: MutableList<Item> = ArrayList(
                inventory.contents.values
            )
            drops.addAll(offhandInventory.contents.values)
            return drops.stream().filter { item: Item -> !item.keepOnDeath() }.toList()
                .toTypedArray()
        }
        return Item.EMPTY_ARRAY
    }

    override fun asyncPrepare(currentTick: Int) {}

    override fun attack(source: EntityDamageEvent): Boolean {
        if (this.isClosed() || !this.isAlive()) {
            return false
        }

        if (source.cause != DamageCause.VOID && source.cause != DamageCause.CUSTOM && source.cause != DamageCause.MAGIC && source.cause != DamageCause.HUNGER) {
            var armorPoints: Int = 0
            var epf: Int = 0

            //            int toughness = 0;
            for (armor in inventory.armorContents) {
                if (armor == null) continue
                armorPoints += armor.armorPoints
                epf = (epf + calculateEnchantmentProtectionFactor(armor, source)).toInt()
                //toughness += armor.getToughness();
            }

            if (source.canBeReducedByArmor()) {
                source.setDamage(-source.finalDamage * armorPoints * 0.04f, DamageModifier.ARMOR)
            }

            source.setDamage(
                -source.finalDamage * ceil(
                    epf.coerceAtMost(25) * (ThreadLocalRandom.current().nextInt(50, 100).toFloat() / 100)
                ).toInt().coerceAtMost(20) * 0.04f,
                DamageModifier.ARMOR_ENCHANTMENTS
            )

            source.setDamage(-this.getAbsorption().coerceAtMost(source.finalDamage), DamageModifier.ABSORPTION)
        }

        if (super.attack(source)) {
            var damager: Entity? = null

            if (source is EntityDamageByEntityEvent) {
                damager = source.damager
            }

            for (slot in 0..3) {
                val armorOld: Item = inventory.getArmorItem(slot)
                if (armorOld.isArmor) {
                    val armor: Item = damageArmor(armorOld, damager, source)
                    inventory.setArmorItem(slot, armor, armor.id !== BlockID.AIR)
                }
            }

            return true
        } else {
            return false
        }
    }

    override fun calculateEnchantmentProtectionFactor(item: Item, source: EntityDamageEvent): Double {
        if (!item.hasEnchantments()) {
            return 0.0
        }

        var epf: Double = 0.0

        if (item.applyEnchantments()) {
            for (ench: Enchantment in item.enchantments) {
                epf += ench.getProtectionFactor(source).toDouble()
            }
        }

        return epf
    }

    override fun setOnFire(seconds: Int) {
        var seconds1: Int = seconds
        var level: Int = 0

        for (armor in inventory.armorContents) {
            if (armor == null) continue
            val fireProtection: Enchantment? = armor.getEnchantment(Enchantment.ID_PROTECTION_FIRE)

            if (fireProtection != null && fireProtection.level > 0) {
                level = level.coerceAtLeast(fireProtection.level)
            }
        }

        seconds1 = (seconds1 * (1 - level * 0.15)).toInt()

        super.setOnFire(seconds1)
    }

    override fun playerApplyNameTag(player: Player, item: Item): Boolean {
        return false
    }

    protected fun damageArmor(armor: Item, damager: Entity?, event: EntityDamageEvent): Item {
        if (armor.hasEnchantments()) {
            if (damager != null) {
                if (armor.applyEnchantments()) {
                    for (enchantment: Enchantment in armor.enchantments) {
                        enchantment.doPostAttack(damager, this)
                    }
                }
            }

            val durability: Enchantment? = armor.getEnchantment(Enchantment.ID_DURABILITY)
            if (durability != null && durability.level > 0 && (100 / (durability.level + 1)) <= Utils.random.nextInt(
                    100
                )
            ) {
                return armor
            }
        }

        if (event.cause != DamageCause.VOID && event.cause != DamageCause.MAGIC && event.cause != DamageCause.HUNGER && event.cause != DamageCause.DROWNING && event.cause != DamageCause.SUFFOCATION && event.cause != DamageCause.SUICIDE && event.cause != DamageCause.FIRE_TICK && event.cause != DamageCause.FALL) { // No armor damage

            if (armor.isUnbreakable || armor.maxDurability < 0) {
                return armor
            }

            if (armor is ItemShield) armor.damage = (
                    armor.damage + (if (event.damage >= 3) event.damage.toInt() + 1 else 0)
                    )
            else armor.damage = (armor.damage + 1.coerceAtLeast((event.damage / 4.0f).toInt()))

            if (armor.damage >= armor.maxDurability) {
                level!!.addSound(this.position, Sound.RANDOM_BREAK)
                return Item.get(BlockID.AIR, 0, 0)
            }
        }

        return armor
    }

    override fun getNetworkID(): Int {
        return IHuman.NETWORK_ID
    }

    override fun getEntityIdentifier(): String {
        return EntityID.PLAYER
    }
}
