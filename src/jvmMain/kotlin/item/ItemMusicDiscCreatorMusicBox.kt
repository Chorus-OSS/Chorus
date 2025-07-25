package org.chorus_oss.chorus.item

class ItemMusicDiscCreatorMusicBox : ItemMusicDisc(ItemID.Companion.MUSIC_DISC_CREATOR_MUSIC_BOX) {
    override val soundId: String
        get() = "record.creator_music_box"
}