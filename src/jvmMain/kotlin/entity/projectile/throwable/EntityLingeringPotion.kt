package org.chorus_oss.chorus.entity.projectile.throwable

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.effect.Effect
import org.chorus_oss.chorus.entity.effect.PotionType
import org.chorus_oss.chorus.entity.item.EntityAreaEffectCloud
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.FloatTag
import org.chorus_oss.chorus.nbt.tag.ListTag

class EntityLingeringPotion(chunk: IChunk?, nbt: CompoundTag, shootingEntity: Entity? = null) :
    EntitySplashPotion(chunk, nbt, shootingEntity) {
    override fun getEntityIdentifier(): String {
        return EntityID.LINGERING_POTION
    }

    override fun initEntity() {
        super.initEntity()
        setDataFlag(EntityFlag.LINGERING, true)
    }

    override fun splash(collidedWith: Entity?) {
        super.splash(collidedWith)
        saveNBT()
        val pos: ListTag<*> = namedTag!!.getList("Pos", CompoundTag::class.java).copy() as ListTag<*>
        val entity: EntityAreaEffectCloud? = createEntity(
            EntityID.AREA_EFFECT_CLOUD,
            locator.chunk,
            CompoundTag().putList("Pos", pos)
                .putList(
                    "Rotation", ListTag<FloatTag>()
                        .add(FloatTag(0f))
                        .add(FloatTag(0f))
                )
                .putList(
                    "Motion", ListTag<FloatTag>()
                        .add(FloatTag(0f))
                        .add(FloatTag(0f))
                        .add(FloatTag(0f))
                )
                .putShort("PotionId", potionId)
        ) as EntityAreaEffectCloud?

        val effects: List<Effect?> = PotionType.Companion.get(potionId).getEffects(true)
        for (effect: Effect? in effects) {
            if (effect != null && entity != null) {
                entity.cloudEffects!!.add(effect /*.setDuration(1)*/.setVisible(false).setAmbient(false))
                entity.spawnToAll()
            }
        }
    }

    override fun getOriginalName(): String {
        return "Lingering Potion"
    }
}
