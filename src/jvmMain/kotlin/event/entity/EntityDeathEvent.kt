package org.chorus_oss.chorus.event.entity

import org.chorus_oss.chorus.entity.EntityLiving
import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.item.Item


open class EntityDeathEvent @JvmOverloads constructor(entity: EntityLiving, drops: Array<Item> = Item.EMPTY_ARRAY) :
    EntityEvent() {
    private var drops: Array<Item>

    init {
        this.entity = entity
        this.drops = drops
    }

    fun getDrops(): Array<Item> {
        return drops
    }

    fun setDrops(drops: Array<Item>?) {
        var drops1 = drops
        if (drops1 == null) {
            drops1 = Item.EMPTY_ARRAY
        }

        this.drops = drops1
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
