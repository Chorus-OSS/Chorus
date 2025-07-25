package org.chorus_oss.chorus.entity

import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.utils.ServerException


/**
 * 属性是作用于[Entity]上一个的增益/减益系统。
 *
 *
 * Attributes are buffs/debuffs systems that act on [Entity].
 */
class Attribute private constructor(
    val id: Int,
    var name: String,
    var minValue: Float,
    var maxValue: Float,
    var defaultMinimum: Float,
    var defaultMaximum: Float,
    var defaultValue: Float,
    var shouldSend: Boolean
) :
    Cloneable {
    private var currentValue: Float

    init {
        this.currentValue = this.defaultValue
    }

    fun setMinValue(minValue: Float): Attribute {
        require(!(minValue > this.maxValue)) { "Value $minValue is bigger than the maxValue!" }
        this.minValue = minValue
        return this
    }

    fun setMaxValue(maxValue: Float): Attribute {
        require(!(maxValue < this.minValue)) { "Value $maxValue is bigger than the minValue!" }
        this.maxValue = maxValue
        return this
    }

    fun setDefaultValue(defaultValue: Float): Attribute {
        require(!(defaultValue > this.maxValue || defaultValue < this.minValue)) { "Value $defaultValue exceeds the range!" }
        this.defaultValue = defaultValue
        return this
    }

    fun getValue(): Float {
        return this.currentValue
    }

    fun setValue(value: Float): Attribute {
        return setValue(value, true)
    }

    fun setValue(value: Float, fit: Boolean): Attribute {
        var value1: Float = value
        if (value1 > this.maxValue || value1 < this.minValue) {
            require(fit) { "Value $value1 exceeds the range!" }
            value1 = value1.coerceAtLeast(this.minValue).coerceAtMost(this.maxValue)
        }
        this.currentValue = value1
        return this
    }

    fun isSyncable(): Boolean {
        return this.shouldSend
    }

    public override fun clone(): Attribute {
        return super.clone() as Attribute
    }

    override fun toString(): String {
        return "Attribute(id=$id, name=$name, minValue=$minValue, maxValue=$maxValue, value=${getValue()}, defaultMinimum=$defaultMinimum, defaultMaximum=$defaultMaximum, defaultValue=$defaultValue, shouldSend=$shouldSend)"
    }

    companion object {
        /**
         * 方便执行[Collection.toArray]
         *
         *
         * Convenient execution of [Collection.toArray]
         */
        @JvmField
        val EMPTY_ARRAY: Array<Attribute?> = arrayOfNulls(0)

        /**
         * 伤害吸收
         *
         *
         * ABSORPTION
         */
        const val ABSORPTION: Int = 0

        /**
         * 饱食度
         *
         *
         * SATURATION
         */
        const val SATURATION: Int = 1
        const val EXHAUSTION: Int = 2
        const val KNOCKBACK_RESISTANCE: Int = 3
        const val MAX_HEALTH: Int = 4
        const val MOVEMENT_SPEED: Int = 5
        const val FOLLOW_RANGE: Int = 6
        const val MAX_HUNGER: Int = 7
        const val FOOD: Int = 7
        const val ATTACK_DAMAGE: Int = 8
        const val EXPERIENCE_LEVEL: Int = 9
        const val EXPERIENCE: Int = 10
        const val LUCK: Int = 11
        const val HORSE_JUMP_STRENGTH: Int = 12
        const val UNDER_WATER_MOVEMENT_SPEED: Int = 13
        const val LAVA_MOVEMENT_SPEED: Int = 14

        protected var attributes: MutableMap<Int, Attribute> = HashMap()

        @JvmStatic
        fun init() {
            addAttribute(ABSORPTION, "minecraft:absorption", 0.00f, 340282346638528859811704183484516925440.00f, 0.00f)
            addAttribute(SATURATION, "minecraft:player.saturation", 0.00f, 20.00f, 5.00f)
            addAttribute(EXHAUSTION, "minecraft:player.exhaustion", 0.00f, 5.00f, 0.41f)
            addAttribute(KNOCKBACK_RESISTANCE, "minecraft:knockback_resistance", 0.00f, 1.00f, 0.00f)
            addAttribute(MAX_HEALTH, "minecraft:health", 0.00f, 20.00f, 20.00f)
            addAttribute(
                MOVEMENT_SPEED,
                "minecraft:movement",
                0.00f,
                340282346638528859811704183484516925440.00f,
                0.10f
            )
            addAttribute(
                UNDER_WATER_MOVEMENT_SPEED,
                "minecraft:underwater_movement",
                0.00f,
                340282346638528859811704183484516925440.00f,
                0.02f
            )
            addAttribute(
                LAVA_MOVEMENT_SPEED,
                "minecraft:lava_movement",
                0.00f,
                340282346638528859811704183484516925440.00f,
                0.02f
            )
            addAttribute(FOLLOW_RANGE, "minecraft:follow_range", 0.00f, 2048.00f, 16.00f, false)
            addAttribute(MAX_HUNGER, "minecraft:player.hunger", 0.00f, 20.00f, 20.00f)
            addAttribute(
                ATTACK_DAMAGE,
                "minecraft:attack_damage",
                0.00f,
                340282346638528859811704183484516925440.00f,
                1.00f,
                false
            )
            addAttribute(EXPERIENCE_LEVEL, "minecraft:player.level", 0.00f, 24791.00f, 0.00f)
            addAttribute(EXPERIENCE, "minecraft:player.experience", 0.00f, 1.00f, 0.00f)
            addAttribute(LUCK, "minecraft:luck", -1024f, 1024f, 0f)
            addAttribute(HORSE_JUMP_STRENGTH, "minecraft:horse.jump_strength", 0f, 0.7101778f, 0.7101778f)
        }

        //SINCE 1.21.30
        @JvmOverloads
        fun addAttribute(
            id: Int,
            name: String,
            minValue: Float,
            maxValue: Float,
            defaultValue: Float,
            shouldSend: Boolean = true
        ): Attribute? {
            return addAttribute(id, name, minValue, maxValue, minValue, maxValue, defaultValue, shouldSend)
        }

        // END
        fun addAttribute(
            id: Int,
            name: String,
            minValue: Float,
            maxValue: Float,
            defaultMinimum: Float,
            defaultMaximum: Float,
            defaultValue: Float
        ): Attribute? {
            return addAttribute(id, name, minValue, maxValue, defaultMinimum, defaultMaximum, defaultValue, true)
        }

        fun addAttribute(
            id: Int,
            name: String,
            minValue: Float,
            maxValue: Float,
            defaultMinimum: Float,
            defaultMaximum: Float,
            defaultValue: Float,
            shouldSend: Boolean
        ): Attribute? {
            require(!(minValue > maxValue || defaultValue > maxValue || defaultValue < minValue)) { "Invalid ranges: min value: $minValue, max value: $maxValue, defaultValue: $defaultValue" }
            return attributes.put(
                id,
                Attribute(id, name, minValue, maxValue, defaultMinimum, defaultMaximum, defaultValue, shouldSend)
            )
        }

        /**
         * 将这个Attribute转换成NBT
         *
         *
         * Convert this attribute to NBT
         *
         *
         * like
         * <pre>
         * {
         * "Base": 0f,
         * "Current": 0f,
         * "DefaultMax": 1024f,
         * "DefaultMin": -1024f,
         * "Max": 1024f,
         * "Min": -1024f,
         * "Name": "minecraft:luck"
         * }
        </pre> *
         *
         * @param attribute the attribute
         * @return the compound tag
         */
        fun toNBT(attribute: Attribute): CompoundTag {
            return CompoundTag().putString("Name", attribute.name)
                .putFloat("Base", attribute.defaultValue)
                .putFloat("Current", attribute.getValue())
                .putFloat("DefaultMax", attribute.defaultMaximum)
                .putFloat("DefaultMin", attribute.defaultMinimum)
                .putFloat("Max", attribute.maxValue)
                .putFloat("Min", attribute.minValue)
        }

        /**
         * 从NBT获取Attribute
         *
         *
         * Get the Attribute from NBT
         *
         *
         * like
         * <pre>
         * {
         * "Base": 0f,
         * "Current": 0f,
         * "DefaultMax": 1024f,
         * "DefaultMin": -1024f,
         * "Max": 1024f,
         * "Min": -1024f,
         * "Name": "minecraft:luck"
         * }
        </pre> *
         *
         * @param nbt the nbt
         * @return the attribute
         */
        fun fromNBT(nbt: CompoundTag): Attribute {
            if (nbt.containsString("Name")
                && nbt.containsFloat("Base")
                && nbt.containsFloat("Current")
                && nbt.containsFloat("DefaultMax")
                && nbt.containsFloat("DefaultMin")
                && nbt.containsFloat("Max")
                && nbt.containsFloat("Min")
            ) {
                val attribute = getAttributeByName(nbt.getString("Name"))
                    ?: throw RuntimeException("Attribute not found: " + nbt.getString("Name"))
                return attribute
                    .setMinValue(nbt.getFloat("Min"))
                    .setMaxValue(nbt.getFloat("Max"))
                    .setValue(nbt.getFloat("Current"))
                    .setDefaultValue(nbt.getFloat("Base"))
            }
            throw IllegalArgumentException("NBT format error")
        }

        /**
         * 获取对应id的[Attribute]。
         *
         *
         * Get the [Attribute] of the corresponding id.
         *
         * @param id the id
         * @return the attribute
         */
        @JvmStatic
        fun getAttribute(id: Int): Attribute {
            if (attributes.containsKey(id)) {
                return attributes[id]!!.clone()
            }
            throw ServerException("Attribute id: " + id + " not found")
        }

        /**
         * 获取对应名字的[Attribute]。
         *
         *
         * Get the [Attribute] of the corresponding name.
         *
         * @param name name
         * @return null |Attribute
         */
        fun getAttributeByName(name: String): Attribute? {
            for (a: Attribute in attributes.values) {
                if (a.name == name) {
                    return a.clone()
                }
            }
            return null
        }
    }
}
