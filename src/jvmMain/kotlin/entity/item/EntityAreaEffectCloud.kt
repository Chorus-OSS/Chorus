package org.chorus_oss.chorus.entity.item

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.EntityLiving
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.effect.Effect
import org.chorus_oss.chorus.entity.effect.PotionType
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import java.awt.Color

class EntityAreaEffectCloud(chunk: IChunk?, nbt: CompoundTag?) : Entity(chunk, nbt) {
    override fun getEntityIdentifier(): String {
        return EntityID.AREA_EFFECT_CLOUD
    }

    var cloudEffects: MutableList<Effect>? = null

    protected var reapplicationDelay: Int = 0

    protected var durationOnUse: Int = 0

    protected var initialRadius: Float = 0f

    protected var radiusOnUse: Float = 0f

    @JvmField
    protected var height: Float = 0f

    protected var nextApply: Int = 0
    private var lastAge: Int = 0

    fun getWaitTime(): Int {
        return this.getDataProperty<Int>(EntityDataTypes.AREA_EFFECT_CLOUD_WAITING, 0)
    }

    fun setWaitTime(waitTime: Int) {
        setWaitTime(waitTime, true)
    }

    fun setWaitTime(waitTime: Int, send: Boolean) {
        this.setDataProperty(EntityDataTypes.AREA_EFFECT_CLOUD_WAITING, waitTime, send)
    }

    fun getPotionId(): Int {
        return getDataProperty<Short>(EntityDataTypes.AUX_VALUE_DATA).toInt()
    }

    fun setPotionId(potionId: Int) {
        setPotionId(potionId, true)
    }

    fun setPotionId(potionId: Int, send: Boolean) {
        this.setDataProperty(EntityDataTypes.AUX_VALUE_DATA, potionId and 0xFFFF, send)
    }

    @JvmOverloads
    fun recalculatePotionColor(send: Boolean = true) {
        val color: IntArray = IntArray(4)
        var count: Int = 0

        if (namedTag!!.contains("ParticleColor")) {
            val effectColor: Int = namedTag!!.getInt("ParticleColor")
            color[0] = (effectColor and -0x1000000) shr 24
            color[1] = (effectColor and 0x00FF0000) shr 16
            color[2] = (effectColor and 0x0000FF00) shr 8
            color[3] = effectColor and 0x000000FF
        } else {
            color[0] = 255

            val potion: PotionType = PotionType.Companion.get(getPotionId())
            for (effect: Effect in potion.getEffects(true)) {
                val effectColor: Color = effect.getColor()
                color[1] += effectColor.red * effect.getLevel()
                color[2] += effectColor.green * effect.getLevel()
                color[3] += effectColor.blue * effect.getLevel()
                count += effect.getLevel()
            }
        }

        val a: Int = (color.get(0) / count) and 0xff
        val r: Int = (color.get(1) / count) and 0xff
        val g: Int = (color.get(2) / count) and 0xff
        val b: Int = (color.get(3) / count) and 0xff

        setPotionColor(a, r, g, b, send)
    }

    fun getPotionColor(): Int {
        return this.getDataProperty<Int>(EntityDataTypes.EFFECT_COLOR)
    }

    fun setPotionColor(argp: Int) {
        setPotionColor(argp, true)
    }

    fun setPotionColor(alpha: Int, red: Int, green: Int, blue: Int, send: Boolean) {
        setPotionColor(
            ((alpha and 0xff) shl 24) or ((red and 0xff) shl 16) or ((green and 0xff) shl 8) or (blue and 0xff),
            send
        )
    }

    fun setPotionColor(argp: Int, send: Boolean) {
        this.setDataProperty(EntityDataTypes.EFFECT_COLOR, argp, send)
    }

    fun getPickupCount(): Int {
        return this.getDataProperty<Int>(EntityDataTypes.AREA_EFFECT_CLOUD_PICKUP_COUNT)
    }

    fun setPickupCount(pickupCount: Int) {
        setPickupCount(pickupCount, true)
    }

    fun setPickupCount(pickupCount: Int, send: Boolean) {
        this.setDataProperty(EntityDataTypes.AREA_EFFECT_CLOUD_PICKUP_COUNT, pickupCount, send)
    }

    fun getRadiusChangeOnPickup(): Float {
        return this.getDataProperty<Float>(EntityDataTypes.AREA_EFFECT_CLOUD_CHANGE_ON_PICKUP)
    }

    fun setRadiusChangeOnPickup(radiusChangeOnPickup: Float) {
        setRadiusChangeOnPickup(radiusChangeOnPickup, true)
    }

    fun setRadiusChangeOnPickup(radiusChangeOnPickup: Float, send: Boolean) {
        this.setDataProperty(EntityDataTypes.AREA_EFFECT_CLOUD_CHANGE_ON_PICKUP, radiusChangeOnPickup, send)
    }

    fun getRadiusPerTick(): Float {
        return this.getDataProperty<Float>(EntityDataTypes.AREA_EFFECT_CLOUD_CHANGE_RATE)
    }

    fun setRadiusPerTick(radiusPerTick: Float) {
        setRadiusPerTick(radiusPerTick, true)
    }

    fun setRadiusPerTick(radiusPerTick: Float, send: Boolean) {
        this.setDataProperty(EntityDataTypes.AREA_EFFECT_CLOUD_CHANGE_RATE, radiusPerTick, send)
    }

    fun getSpawnTime(): Long {
        return getDataProperty<Int>(EntityDataTypes.AREA_EFFECT_CLOUD_SPAWN_TIME).toLong()
    }

    fun setSpawnTime(spawnTime: Long) {
        setSpawnTime(spawnTime, true)
    }

    fun setSpawnTime(spawnTime: Long, send: Boolean) {
        this.setDataProperty(EntityDataTypes.AREA_EFFECT_CLOUD_SPAWN_TIME, spawnTime, send)
    }

    fun getDuration(): Int {
        return this.getDataProperty<Int>(EntityDataTypes.AREA_EFFECT_CLOUD_DURATION)
    }

    fun setDuration(duration: Int) {
        setDuration(duration, true)
    }

    fun setDuration(duration: Int, send: Boolean) {
        this.setDataProperty(EntityDataTypes.AREA_EFFECT_CLOUD_DURATION, duration, send)
    }

    fun getRadius(): Float {
        return this.getDataProperty<Float>(EntityDataTypes.AREA_EFFECT_CLOUD_RADIUS)
    }

    fun setRadius(radius: Float) {
        setRadius(radius, true)
    }

    fun setRadius(radius: Float, send: Boolean) {
        this.setDataProperty(EntityDataTypes.AREA_EFFECT_CLOUD_RADIUS, radius, send)
    }

    fun setHeight(height: Float) {
        this.height = height
    }


    fun getParticleId(): Int {
        return this.getDataProperty<Int>(EntityDataTypes.AREA_EFFECT_CLOUD_PARTICLE)
    }

    fun setParticleId(particleId: Int) {
        setParticleId(particleId, true)
    }

    fun setParticleId(particleId: Int, send: Boolean) {
        this.setDataProperty(EntityDataTypes.AREA_EFFECT_CLOUD_PARTICLE, particleId, send)
    }

    override fun initEntity() {
        super.initEntity()
        this.invulnerable = true
        this.setDataFlag(EntityFlag.FIRE_IMMUNE, true)
        this.setDataFlag(EntityFlag.NO_AI, true)
        this.setSpawnTime(level!!.currentTick, false)
        this.setPickupCount(0, false)

        cloudEffects = mutableListOf()
        for (effectTag: CompoundTag in namedTag!!.getList("mobEffects", CompoundTag::class.java).all) {
            val effect: Effect = Effect.get(effectTag.getByte("Id").toInt())
                .setAmbient(effectTag.getBoolean("Ambient"))
                .setAmplifier(effectTag.getByte("Amplifier").toInt())
                .setVisible(effectTag.getBoolean("DisplayOnScreenTextureAnimation"))
                .setDuration(effectTag.getInt("Duration"))
            cloudEffects!!.add(effect)
        }
        if (namedTag!!.contains("PotionId")) {
            this.setParticleId(32, false)
            val displayedPotionId: Int = namedTag!!.getShort("PotionId").toInt()
            setPotionId(displayedPotionId, false)
            recalculatePotionColor()
        } else {
            setDragonBreath()
        }

        if (namedTag!!.contains("Duration")) {
            setDuration(namedTag!!.getInt("Duration"), false)
        } else {
            setDuration(600, false)
        }
        if (namedTag!!.contains("DurationOnUse")) {
            durationOnUse = namedTag!!.getInt("DurationOnUse")
        } else {
            durationOnUse = 0
        }
        if (namedTag!!.contains("ReapplicationDelay")) {
            reapplicationDelay = namedTag!!.getInt("ReapplicationDelay")
        } else {
            reapplicationDelay = 0
        }
        if (namedTag!!.contains("InitialRadius")) {
            initialRadius = namedTag!!.getFloat("InitialRadius")
        } else {
            initialRadius = 3.0f
        }
        if (namedTag!!.contains("Radius")) {
            setRadius(namedTag!!.getFloat("Radius"), false)
        } else {
            setRadius(initialRadius, false)
        }
        if (namedTag!!.contains("RadiusChangeOnPickup")) {
            setRadiusChangeOnPickup(namedTag!!.getFloat("RadiusChangeOnPickup"), false)
        } else {
            setRadiusChangeOnPickup(-0.5f, false)
        }
        if (namedTag!!.contains("RadiusOnUse")) {
            radiusOnUse = namedTag!!.getFloat("RadiusOnUse")
        } else {
            radiusOnUse = -0.5f
        }
        if (namedTag!!.contains("RadiusPerTick")) {
            setRadiusPerTick(namedTag!!.getFloat("RadiusPerTick"), false)
        } else {
            setRadiusPerTick(-0.005f, false)
        }
        if (namedTag!!.contains("WaitTime")) {
            setWaitTime(namedTag!!.getInt("WaitTime"), false)
        } else {
            setWaitTime(10, false)
        }
        if (namedTag!!.contains("Height")) {
            setHeight(namedTag!!.getFloat("Height"))
        } else {
            setHeight(0.3f + (getRadius() / 2f))
        }

        setMaxHealth(1)
        setHealthSafe(1f)
    }

    override fun attack(source: EntityDamageEvent): Boolean {
        return false
    }

    override fun saveNBT() {
        super.saveNBT()
        val effectsTag: ListTag<CompoundTag> = ListTag()
        for (effect: Effect in cloudEffects!!) {
            effectsTag.add(
                CompoundTag().putByte("Id", effect.getId())
                    .putBoolean("Ambient", effect.isAmbient())
                    .putByte("Amplifier", effect.getAmplifier())
                    .putBoolean("DisplayOnScreenTextureAnimation", effect.isVisible())
                    .putInt("Duration", effect.getDuration())
            )
        }
        namedTag!!.putList("mobEffects", effectsTag)
        namedTag!!.putInt("ParticleColor", getPotionColor())
        namedTag!!.putInt("Duration", getDuration())
        namedTag!!.putInt("DurationOnUse", durationOnUse)
        namedTag!!.putInt("ReapplicationDelay", reapplicationDelay)
        namedTag!!.putFloat("Radius", getRadius())
        namedTag!!.putFloat("RadiusChangeOnPickup", getRadiusChangeOnPickup())
        namedTag!!.putFloat("RadiusOnUse", radiusOnUse)
        namedTag!!.putFloat("RadiusPerTick", getRadiusPerTick())
        namedTag!!.putInt("WaitTime", getWaitTime())
        namedTag!!.putFloat("InitialRadius", initialRadius)
        namedTag!!.putInt("PotionId", getPotionId())
    }

    override fun onUpdate(currentTick: Int): Boolean {
        if (this.closed) {
            return false
        }

        super.onUpdate(currentTick)

        val sendRadius: Boolean = age % 10 == 0

        val age: Int = this.age
        var radius: Float = getRadius()
        val waitTime: Int = getWaitTime()
        if (age < waitTime) {
            radius = initialRadius
        } else if (age > waitTime + getDuration()) {
            kill()
        } else {
            val tickDiff: Int = age - lastAge
            radius += getRadiusPerTick() * tickDiff
            if ((tickDiff.let { nextApply -= it; nextApply }) <= 0) {
                nextApply = reapplicationDelay + 10

                val collidingEntities = level!!.getCollidingEntities(getBoundingBox())
                if (collidingEntities.size > 0) {
                    radius += radiusOnUse
                    radiusOnUse /= 2f

                    setDuration(getDuration() + durationOnUse)

                    for (collidingEntity in collidingEntities) {
                        if (collidingEntity === this || collidingEntity !is EntityLiving) continue

                        for (effect in cloudEffects!!) {
                            collidingEntity.addEffect(effect)
                        }
                    }
                }
            }
        }

        this.lastAge = age

        if (radius <= 1.5 && age >= waitTime) {
            setRadius(radius, false)
            kill()
        } else {
            setRadius(radius, sendRadius)
        }

        val height: Float = getHeight()
        boundingBox.setBounds(
            position.x - radius,
            position.y - height,
            position.z - radius,
            position.x + radius, position.y + height, position.z + radius
        )
        this.setDataProperty(EntityDataTypes.HEIGHT, height, false)
        this.setDataProperty(EntityDataTypes.WIDTH, radius, false)

        return true
    }

    override fun canCollideWith(entity: Entity): Boolean {
        return entity is EntityLiving
    }

    override fun getHeight(): Float {
        return height
    }

    override fun getWidth(): Float {
        return getRadius()
    }

    override fun getLength(): Float {
        return getRadius()
    }

    override fun getGravity(): Float {
        return 0f
    }

    override fun getDrag(): Float {
        return 0f
    }

    override fun getOriginalName(): String {
        return "Area Effect Cloud"
    }

    fun setDragonBreath() {
        setParticleId(49, false)
        setPotionColor(-5675670, false)
    }

    fun isDragonBreath(): Boolean {
        return getParticleId() == 49
    }
}
