package org.chorus_oss.chorus.item

class ItemMusicDiscMall : ItemMusicDisc(ItemID.MUSIC_DISC_MALL) {
    override val soundId: String
        get() = "record.mall"
}