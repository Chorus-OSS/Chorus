package org.chorus_oss.chorus.item.enchantment

class EnchantmentList(size: Int) {
    private val enchantments = arrayOfNulls<EnchantmentEntry>(size)

    /**
     * @param slot  The index of enchantment.
     * @param entry The given enchantment entry.
     * @return [EnchantmentList]
     */
    fun setSlot(slot: Int, entry: EnchantmentEntry?): EnchantmentList {
        enchantments[slot] = entry
        return this
    }

    fun getSlot(slot: Int): EnchantmentEntry? {
        return enchantments[slot]
    }

    val size: Int
        get() = enchantments.size
}
