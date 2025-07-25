package org.chorus_oss.chorus.item

class ItemMusicDiscStal : ItemMusicDisc(ItemID.Companion.MUSIC_DISC_STAL) {
    override val soundId: String
        get() = "record.stal"
}