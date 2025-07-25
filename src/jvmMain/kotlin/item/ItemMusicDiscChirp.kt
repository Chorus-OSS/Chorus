package org.chorus_oss.chorus.item

class ItemMusicDiscChirp : ItemMusicDisc(ItemID.Companion.MUSIC_DISC_CHIRP) {
    override val soundId: String
        get() = "record.chirp"
}