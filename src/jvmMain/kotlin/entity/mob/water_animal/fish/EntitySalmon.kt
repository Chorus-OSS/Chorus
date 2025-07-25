package org.chorus_oss.chorus.entity.mob.water_animal.fish

import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.utils.Utils

class EntitySalmon(chunk: IChunk?, nbt: CompoundTag) : EntityFish(chunk, nbt) {
    override fun getEntityIdentifier(): String {
        return EntityID.SALMON
    }


    override fun getOriginalName(): String {
        return "Salmon"
    }

    override fun getWidth(): Float {
        if (this.isBaby()) {
            return 0.25f
        } else if (this.isLarge()) {
            return 0.75f
        }
        return 0.5f
    }

    override fun getHeight(): Float {
        if (this.isBaby()) {
            return 0.25f
        } else if (this.isLarge()) {
            return 0.75f
        }
        return 0.5f
    }

    public override fun initEntity() {
        this.maxHealth = 3
        super.initEntity()
    }

    override fun getDrops(): Array<Item> {
        val rand = Utils.rand(0, 3)
        if (this.isLarge()) {
            //只有25%获得骨头 来自wiki https://zh.minecraft.wiki/w/%E9%B2%91%E9%B1%BC
            if (rand == 1) {
                return arrayOf(
                    Item.get(ItemID.BONE, 0, Utils.rand(1, 2)),
                    Item.get((if (this.isOnFire()) ItemID.COOKED_SALMON else ItemID.SALMON))
                )
            }
        } else if (!this.isLarge()) {
            //只有25%获得骨头 来自wiki https://zh.minecraft.wiki/w/%E9%B2%91%E9%B1%BC
            if (rand == 1) {
                return arrayOf(
                    Item.get(ItemID.BONE),
                    Item.get((if (this.isOnFire()) ItemID.COOKED_SALMON else ItemID.SALMON))
                )
            }
        }
        return arrayOf(Item.get((if (this.isOnFire()) ItemID.COOKED_SALMON else ItemID.SALMON)))
    }

    //巨型体系
    fun isLarge(): Boolean {
        return namedTag!!.getBoolean("isLarge")
    }
}
