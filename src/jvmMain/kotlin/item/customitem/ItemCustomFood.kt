package org.chorus_oss.chorus.item.customitem

import org.chorus_oss.chorus.item.ItemFood

abstract class ItemCustomFood(id: String) : ItemFood(id), CustomItem {
    val isDrink: Boolean
        get() = false
}
