package org.chorus_oss.chorus.item


abstract class ItemMusicDisc protected constructor(id: String) : Item(id) {
    override val maxStackSize: Int
        get() = 1

    abstract val soundId: String
}
