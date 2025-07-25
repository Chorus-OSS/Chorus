package org.chorus_oss.chorus.item.randomitem

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.utils.Utils
import java.util.*
import java.util.concurrent.ThreadLocalRandom

open class EnchantmentItemSelector(item: Item, parent: Selector?) :
    ConstantItemSelector(item, parent) {
    constructor(id: String, parent: Selector?) : this(id, 0, parent)

    constructor(id: String, meta: Int, parent: Selector?) : this(id, meta, 1, parent)

    constructor(id: String, meta: Int, count: Int, parent: Selector?) : this(Item.get(id, meta, count), parent)

    init {
        //TODO 贴近原版附魔概率
        val enchantments = getSupportEnchantments(item)
        if (!enchantments.isEmpty()) {
            val random: Random = ThreadLocalRandom.current()
            val enchantment = enchantments[random.nextInt(enchantments.size)]
            if (random.nextDouble() < 0.3) { //减少高等级附魔概率
                enchantment.setLevel(Utils.rand(1, enchantment.maxLevel))
            }
            item.addEnchantment(enchantment)
        }
    }

    /**
     * 根据物品获得支持的附魔
     *
     * @param item 物品
     * @return 支持的附魔
     */
    open fun getSupportEnchantments(item: Item): List<Enchantment> {
        val enchantments = ArrayList<Enchantment>()
        for (enchantment in Enchantment.registeredEnchantments) {
            if (item.id == ItemID.ENCHANTED_BOOK || enchantment.canEnchant(item)) {
                enchantments.add(enchantment)
            }
        }
        return enchantments
    }
}
