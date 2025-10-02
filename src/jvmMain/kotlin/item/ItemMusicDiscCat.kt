package org.chorus_oss.chorus.item

class ItemMusicDiscCat : ItemMusicDisc(ItemID.MUSIC_DISC_CAT) {
    override val soundId: String
        get() = "record.cat"
}