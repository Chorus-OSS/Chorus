package org.chorus_oss.chorus.item

abstract class ItemHarness(id: String) : Item(id) {
    override val maxStackSize: Int = 1
}