package org.chorus_oss.chorus.item

class ItemMusicDiscPrecipice : ItemMusicDisc(ItemID.Companion.MUSIC_DISC_PRECIPICE) {
    override val soundId: String
        get() = "record.precipice"
}