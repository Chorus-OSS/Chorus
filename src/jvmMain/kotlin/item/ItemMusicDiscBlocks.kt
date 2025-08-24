package org.chorus_oss.chorus.item

class ItemMusicDiscBlocks : ItemMusicDisc(ItemID.MUSIC_DISC_BLOCKS) {
    override val soundId: String
        get() = "record.blocks"
}