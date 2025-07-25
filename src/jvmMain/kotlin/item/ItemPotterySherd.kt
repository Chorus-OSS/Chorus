package org.chorus_oss.chorus.item


abstract class ItemPotterySherd : Item {
    constructor(id: String) : super(id)

    constructor(id: String, count: Int) : super(id, 0, count)
}
