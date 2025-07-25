package org.chorus_oss.chorus.entity.effect

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.registry.Registries
import java.awt.Color

abstract class Effect @JvmOverloads constructor(
    private val type: EffectType,
    private val name: String,
    private var color: Color,
    private val bad: Boolean = false
) :
    Cloneable {
    private var duration: Int = 600
    private var amplifier: Int = 0
    private var visible: Boolean = true
    private var ambient: Boolean = false

    fun getType(): EffectType {
        return type
    }

    fun getId(): Int {
        return type.id
    }

    fun getName(): String {
        return name
    }

    /**
     * get the duration of this potion in 1 tick = 0.05 s
     */
    fun getDuration(): Int {
        return duration
    }

    /**
     * set the duration of this potion , 1 tick = 0.05 s
     *
     * @param duration the duration
     * @return the duration
     */
    fun setDuration(duration: Int): Effect {
        this.duration = duration
        return this
    }

    /**
     * Get amplifier.
     */
    fun getAmplifier(): Int {
        return amplifier
    }

    /**
     * Sets amplifier.
     *
     * @param amplifier the amplifier
     * @return the amplifier
     */
    fun setAmplifier(amplifier: Int): Effect {
        this.amplifier = amplifier
        return this
    }

    /**
     * Get the level of potion,level = amplifier + 1.
     *
     * @return the level
     */
    fun getLevel(): Int {
        return amplifier + 1
    }

    fun isVisible(): Boolean {
        return visible
    }

    fun setVisible(visible: Boolean): Effect {
        this.visible = visible
        return this
    }

    fun isAmbient(): Boolean {
        return ambient
    }

    fun setAmbient(ambient: Boolean): Effect {
        this.ambient = ambient
        return this
    }

    fun isBad(): Boolean {
        return bad
    }

    fun getColor(): Color {
        return color
    }

    fun setColor(color: Color): Effect {
        this.color = color
        return this
    }

    open fun canTick(): Boolean {
        return false
    }

    open fun apply(entity: Entity, tickCount: Double) {
    }

    open fun add(entity: Entity) {
    }

    open fun remove(entity: Entity) {
    }

    public override fun clone(): Effect {
        return super.clone() as Effect
    }

    companion object {
        @JvmStatic
        fun get(type: EffectType): Effect {
            return Registries.EFFECT.get(type) ?: throw RuntimeException("Unknown EffectType: $type")
        }

        fun get(id: String): Effect {
            return get(EffectType.get(id))
        }

        fun get(id: Int): Effect {
            return get(EffectType.get(id))
        }
    }
}