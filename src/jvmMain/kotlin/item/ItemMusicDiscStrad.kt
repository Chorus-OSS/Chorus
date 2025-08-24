package org.chorus_oss.chorus.item

class ItemMusicDiscStrad : ItemMusicDisc(ItemID.MUSIC_DISC_STRAD) {
    override val soundId: String
        get() = "record.strad"
}