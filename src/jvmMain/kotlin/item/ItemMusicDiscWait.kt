package org.chorus_oss.chorus.item

class ItemMusicDiscWait : ItemMusicDisc(ItemID.MUSIC_DISC_WAIT) {
    override val soundId: String
        get() = "record.wait"
}