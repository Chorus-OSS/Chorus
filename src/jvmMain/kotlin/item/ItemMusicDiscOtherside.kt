package org.chorus_oss.chorus.item

class ItemMusicDiscOtherside : ItemMusicDisc(ItemID.Companion.MUSIC_DISC_OTHERSIDE) {
    override val soundId: String
        get() = "record.otherside"
}