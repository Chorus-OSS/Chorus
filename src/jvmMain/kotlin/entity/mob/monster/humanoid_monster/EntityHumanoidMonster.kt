package org.chorus_oss.chorus.entity.mob.monster.humanoid_monster

import org.chorus_oss.chorus.entity.EntityCanAttack
import org.chorus_oss.chorus.entity.mob.monster.EntityMonster
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.NBTIO
import org.chorus_oss.chorus.nbt.tag.CompoundTag


abstract class EntityHumanoidMonster(chunk: IChunk?, nbt: CompoundTag?) : EntityMonster(chunk, nbt!!), EntityCanAttack {
    override lateinit var itemInHand: Item

    override fun initEntity() {
        super.initEntity()

        itemInHand = if (namedTag!!.contains(TAG_ITEM_IN_HAND)) {
            NBTIO.getItemHelper(namedTag!!.getCompound(TAG_ITEM_IN_HAND))
        } else Item.AIR
    }

    override fun saveNBT() {
        super.saveNBT()

        if (!itemInHand.isNothing) {
            namedTag!!.putCompound(TAG_ITEM_IN_HAND, NBTIO.putItemHelper(itemInHand))
        }
    }

    companion object {
        private const val TAG_ITEM_IN_HAND = "ItemInHand"
    }
}
