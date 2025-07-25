package org.chorus_oss.chorus.item.customitem

import org.chorus_oss.chorus.item.ItemEnchantedBook
import org.chorus_oss.chorus.item.customitem.data.CreativeCategory
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import java.util.function.Consumer

abstract class ItemCustomBookEnchanted(id: String) : ItemEnchantedBook(id), CustomItem {
    override val definition: CustomItemDefinition
        get() = CustomItemDefinition.Companion.customBuilder(this)
            .name(name!!)
            .texture("book_enchanted")
            .allowOffHand(false)
            .creativeCategory(CreativeCategory.ITEMS)
            .creativeGroup("itemGroup.name.enchantedBook")
            .foil(true)
            .customBuild(Consumer<CompoundTag> { nbt: CompoundTag ->
                nbt.getCompound("components")
                    .getCompound("item_properties")
                    .putString("enchantable_slot", "all")
                    .putInt("enchantable_value", 20)
                    .putBoolean("hand_equipped", false)
                    .putFloat("mining_speed", 1f)
                    .putBoolean("mirrored_art", false)
                    .putInt("use_animation", 0)
                    .putInt("use_duration", 0)
                    .putBoolean("animates_in_toolbar", false)
            }
            )

    val enchantment: Enchantment
        get() = Enchantment.getEnchantment(this.id)
}
