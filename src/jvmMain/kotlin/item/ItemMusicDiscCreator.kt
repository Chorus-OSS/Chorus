package org.chorus_oss.chorus.item

class ItemMusicDiscCreator : ItemMusicDisc(ItemID.Companion.MUSIC_DISC_CREATOR) {
    override val soundId: String
        get() = "record.creator"
}