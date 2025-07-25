package org.chorus_oss.chorus.entity.mob.water_animal.fish

import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.utils.Utils

class EntityCod(chunk: IChunk?, nbt: CompoundTag) : EntityFish(chunk, nbt) {
    override fun getEntityIdentifier(): String {
        return EntityID.COD
    }


    override fun getOriginalName(): String {
        return "Cod"
    }

    override fun getWidth(): Float {
        return 0.6f
    }

    override fun getHeight(): Float {
        return 0.3f
    }

    public override fun initEntity() {
        this.maxHealth = 3
        super.initEntity()
    }

    override fun getDrops(): Array<Item> {
        //只能25%获得骨头
        if (Utils.rand(0, 3) == 1) {
            return arrayOf(
                Item.get(ItemID.BONE, 0, Utils.rand(1, 2)),
                Item.get((if (this.isOnFire()) ItemID.COOKED_COD else ItemID.COD))
            )
        }
        return arrayOf(Item.get((if (this.isOnFire()) ItemID.COOKED_COD else ItemID.COD)))
    }
}
