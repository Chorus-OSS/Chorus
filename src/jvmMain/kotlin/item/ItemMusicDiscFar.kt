package org.chorus_oss.chorus.item

class ItemMusicDiscFar : ItemMusicDisc(ItemID.Companion.MUSIC_DISC_FAR) {
    override val soundId: String
        get() = "record.far"
}