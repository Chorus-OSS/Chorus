package org.chorus_oss.chorus.item

class ItemMusicDiscTears : ItemMusicDisc(ItemID.MUSIC_DISC_TEARS) {
    override val soundId: String
        get() = "record.tears"
}