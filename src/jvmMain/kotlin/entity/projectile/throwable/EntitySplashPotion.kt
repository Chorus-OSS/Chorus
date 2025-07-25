package org.chorus_oss.chorus.entity.projectile.throwable

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.effect.Effect
import org.chorus_oss.chorus.entity.effect.PotionType
import org.chorus_oss.chorus.entity.mob.monster.EntityBlaze
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.event.potion.PotionCollideEvent
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.level.particle.Particle
import org.chorus_oss.chorus.level.particle.SpellParticle
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.utils.BlockColor
import java.awt.Color
import kotlin.math.sqrt

open class EntitySplashPotion(chunk: IChunk?, nbt: CompoundTag, shootingEntity: Entity? = null) :
    EntityThrowable(chunk, nbt, shootingEntity) {
    override fun getEntityIdentifier(): String {
        return EntityID.SPLASH_POTION
    }

    @JvmField
    var potionId: Int = 0

    override fun initEntity() {
        super.initEntity()

        potionId = namedTag!!.getShort("PotionId").toInt()

        entityDataMap.put(EntityDataTypes.AUX_VALUE_DATA, this.potionId)

        /*Effect effect = Potion.getEffect(potionId, true); TODO: potion color

        if(effect != null) {
            int count = 0;
            int[] c = effect.getColor();
            count += effect.getAmplifier() + 1;

            int r = ((c[0] * (effect.getAmplifier() + 1)) / count) & 0xff;
            int g = ((c[1] * (effect.getAmplifier() + 1)) / count) & 0xff;
            int b = ((c[2] * (effect.getAmplifier() + 1)) / count) & 0xff;

            this.setDataProperty(new IntEntityData(Entity.DATA_UNKNOWN, (r << 16) + (g << 8) + b));
        }*/
    }


    override fun getWidth(): Float {
        return 0.25f
    }

    override fun getLength(): Float {
        return 0.25f
    }

    override fun getHeight(): Float {
        return 0.25f
    }

    override fun getGravity(): Float {
        return 0.05f
    }

    override fun getDrag(): Float {
        return 0.01f
    }

    override fun onCollideWithEntity(entity: Entity) {
        this.splash(entity)
    }

    protected open fun splash(collidedWith: Entity?) {
        var potion = PotionType.Companion.get(this.potionId)
        val event: PotionCollideEvent = PotionCollideEvent(potion, this)
        Server.instance.pluginManager.callEvent(event)

        if (event.cancelled) {
            return
        }

        this.close()

        potion = event.potion

        if (potion == PotionType.Companion.WATER) {
            if (collidedWith is EntityBlaze) {
                collidedWith.attack(EntityDamageByEntityEvent(this, collidedWith, DamageCause.MAGIC, 1f))
            }
        }

        val color: IntArray = IntArray(3)
        var count: Int = 0

        if (!potion.getEffects(true).isEmpty()) {
            for (effect: Effect in potion.getEffects(true)) {
                val effectColor: Color = effect.getColor()
                color[0] += effectColor.red * effect.getLevel()
                color[1] += effectColor.green * effect.getLevel()
                color[2] += effectColor.blue * effect.getLevel()
                count += effect.getLevel()
            }
        } else {
            val water: BlockColor = BlockColor.WATER_BLOCK_COLOR
            color[0] = water.red
            color[1] = water.green
            color[2] = water.blue
            count = 1
        }

        val r: Int = (color.get(0) / count) and 0xff
        val g: Int = (color.get(1) / count) and 0xff
        val b: Int = (color.get(2) / count) and 0xff
        val particle: Particle = SpellParticle(this.position, r, g, b)

        level!!.addParticle(particle)
        level!!.addSound(this.position, Sound.RANDOM_GLASS)

        val entities = level!!.getNearbyEntities(
            getBoundingBox().grow(4.125, 2.125, 4.125)
        )
        for (anEntity in entities) {
            val distance: Double = anEntity.position.distanceSquared(this.position)
            if (distance < 16) {
                val splashDistance: Double = if (anEntity == collidedWith) 1.0 else 1 - sqrt(distance) / 4
                potion.applyEffects(anEntity, true, splashDistance)
            }
        }
    }

    override fun onUpdate(currentTick: Int): Boolean {
        if (this.closed) {
            return false
        }

        var hasUpdate: Boolean = super.onUpdate(currentTick)

        if (this.age > 1200) {
            this.kill()
            hasUpdate = true
        } else if (this.isCollided) {
            this.splash(null)
            hasUpdate = true
        }
        return hasUpdate
    }

    override fun getOriginalName(): String {
        return "Potion"
    }

    companion object {
        const val DATA_POTION_ID: Int = 37
    }
}
