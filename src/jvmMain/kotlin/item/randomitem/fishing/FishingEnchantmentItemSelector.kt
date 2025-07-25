package org.chorus_oss.chorus.item.randomitem.fishing

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.item.randomitem.EnchantmentItemSelector
import org.chorus_oss.chorus.item.randomitem.Selector

class FishingEnchantmentItemSelector : EnchantmentItemSelector {
    constructor(id: String, parent: Selector?) : this(id, 0, parent)

    constructor(id: String, meta: Int, parent: Selector?) : super(id, meta, 1, parent)

    constructor(id: String, meta: Int, count: Int, parent: Selector?) : super(Item.get(id, meta, count), parent)

    override fun getSupportEnchantments(item: Item): List<Enchantment> {
        val enchantments = ArrayList<Enchantment>()
        for (enchantment in Enchantment.Companion.registeredEnchantments) {
            if (enchantment.isFishable && (item.id == ItemID.Companion.ENCHANTED_BOOK || enchantment.canEnchant(item))) {
                enchantments.add(enchantment)
            }
        }
        return enchantments
    }
}
