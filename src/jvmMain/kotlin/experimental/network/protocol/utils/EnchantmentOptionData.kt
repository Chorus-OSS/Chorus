package org.chorus_oss.chorus.experimental.network.protocol.utils

import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.protocol.types.EnchantmentOption

data class EnchantmentOptionData(
    val index: Int,
    val enchantments: List<Enchantment>,
    val data: EnchantmentOption
)
