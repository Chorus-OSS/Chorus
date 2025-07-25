package org.chorus_oss.chorus.item.enchantment.bow

import org.chorus_oss.chorus.entity.EntityLiving
import org.chorus_oss.chorus.entity.projectile.EntityProjectile
import org.chorus_oss.chorus.item.ItemBow
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.item.enchantment.EnchantmentType


abstract class EnchantmentBow protected constructor(id: Int, name: String, rarity: Rarity) :
    Enchantment(id, name, rarity, EnchantmentType.BOW) {
    /**
     * 当弓箭射击时被调用
     *
     * @param user       使用弓的实体
     * @param projectile 箭实体
     * @param bow        弓物品
     */
    fun onBowShoot(user: EntityLiving?, projectile: EntityProjectile?, bow: ItemBow?) {
    }
}
