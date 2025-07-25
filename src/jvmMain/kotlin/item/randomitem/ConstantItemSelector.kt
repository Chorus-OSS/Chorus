package org.chorus_oss.chorus.item.randomitem

import org.chorus_oss.chorus.item.Item

open class ConstantItemSelector(val item: Item, parent: Selector?) : Selector(parent) {
    constructor(id: String, parent: Selector?) : this(id, 0, parent)

    constructor(id: String, meta: Int, parent: Selector?) : this(id, meta, 1, parent)

    constructor(id: String, meta: Int, count: Int, parent: Selector?) : this(Item.get(id, meta, count), parent)

    override fun select(): Any {
        return item
    }
}
