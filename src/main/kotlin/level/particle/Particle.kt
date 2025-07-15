package org.chorus_oss.chorus.level.particle

import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.network.DataPacket
import org.chorus_oss.chorus.utils.Utils
import java.util.*


abstract class Particle : Vector3 {
    constructor() : super(0.0, 0.0, 0.0)

    constructor(x: Double) : super(x, 0.0, 0.0)

    constructor(x: Double, y: Double) : super(x, y, 0.0)

    constructor(x: Double, y: Double, z: Double) : super(x, y, z)

    abstract fun encode(): Array<DataPacket>

    companion object {
        val TYPE_UNDEFINED: Int = Utils.dynamic(0)
        val TYPE_BUBBLE: Int = Utils.dynamic(1)
        val TYPE_BUBBLE_MANUAL: Int = Utils.dynamic(2)
        val TYPE_CRIT: Int = Utils.dynamic(3)
        val TYPE_BLOCK_FORCE_FIELD: Int = Utils.dynamic(4)
        val TYPE_SMOKE: Int = Utils.dynamic(5)
        val TYPE_EXPLODE: Int = Utils.dynamic(6)
        val TYPE_EVAPORATION: Int = Utils.dynamic(7)
        val TYPE_FLAME: Int = Utils.dynamic(8)
        val TYPE_CANDLE_FLAME: Int = Utils.dynamic(9)
        val TYPE_LAVA: Int = Utils.dynamic(10)
        val TYPE_LARGE_SMOKE: Int = Utils.dynamic(11)
        val TYPE_RED_DUST: Int = Utils.dynamic(12)
        val TYPE_RISING_BORDER_DUST: Int = Utils.dynamic(13)
        val TYPE_ICON_CRACK: Int = Utils.dynamic(14)
        val TYPE_SNOWBALL_POOF: Int = Utils.dynamic(15)
        val TYPE_LARGE_EXPLODE: Int = Utils.dynamic(16)
        val TYPE_HUGE_EXPLOSION: Int = Utils.dynamic(17)
        val BREEZE_WIND_EXPLOSION: Int = Utils.dynamic(18)
        val TYPE_MOB_FLAME: Int = Utils.dynamic(19)
        val TYPE_HEART: Int = Utils.dynamic(20)
        val TYPE_TERRAIN: Int = Utils.dynamic(21)
        val TYPE_TOWN_AURA: Int = Utils.dynamic(22)
        val TYPE_PORTAL: Int = Utils.dynamic(23)
        val TYPE_MOB_PORTAL: Int = Utils.dynamic(24)
        val TYPE_WATER_SPLASH: Int = Utils.dynamic(25)
        val TYPE_WATER_SPLASH_MANUAL: Int = Utils.dynamic(26)
        val TYPE_WATER_WAKE: Int = Utils.dynamic(27)
        val TYPE_DRIP_WATER: Int = Utils.dynamic(28)
        val TYPE_DRIP_LAVA: Int = Utils.dynamic(29)
        val TYPE_DRIP_HONEY: Int = Utils.dynamic(30)
        val TYPE_STALACTITE_DRIP_WATER: Int = Utils.dynamic(31)
        val TYPE_STALACTITE_DRIP_LAVA: Int = Utils.dynamic(32)
        val TYPE_FALLING_DUST: Int = Utils.dynamic(33)
        val TYPE_MOB_SPELL: Int = Utils.dynamic(34)
        val TYPE_MOB_SPELL_AMBIENT: Int = Utils.dynamic(35)
        val TYPE_MOB_SPELL_INSTANTANEOUS: Int = Utils.dynamic(36)
        val TYPE_INK: Int = Utils.dynamic(37)
        val TYPE_SLIME: Int = Utils.dynamic(38)
        val TYPE_RAIN_SPLASH: Int = Utils.dynamic(39)
        val TYPE_VILLAGER_ANGRY: Int = Utils.dynamic(40)
        val TYPE_VILLAGER_HAPPY: Int = Utils.dynamic(41)
        val TYPE_ENCHANTING_TABLE: Int = Utils.dynamic(42)
        val TYPE_TRACKER_EMITTER: Int = Utils.dynamic(43)
        val TYPE_NOTE: Int = Utils.dynamic(44)
        val TYPE_WITCH_SPELL: Int = Utils.dynamic(45)
        val TYPE_CARROT_BOOST: Int = Utils.dynamic(46)
        val TYPE_MOB_APPEARANCE: Int = Utils.dynamic(47)
        val TYPE_END_ROD: Int = Utils.dynamic(48)
        val TYPE_DRAGON_BREATH: Int = Utils.dynamic(49)
        val TYPE_SPIT: Int = Utils.dynamic(50)
        val TYPE_TOTEM: Int = Utils.dynamic(51)
        val TYPE_FOOD: Int = Utils.dynamic(52)
        val TYPE_FIREWORKS_STARTER: Int = Utils.dynamic(53)
        val TYPE_FIREWORKS: Int = Utils.dynamic(54)
        val TYPE_FIREWORKS_OVERLAY: Int = Utils.dynamic(55)
        val TYPE_BALLOON_GAS: Int = Utils.dynamic(56)
        val TYPE_COLORED_FLAME: Int = Utils.dynamic(57)
        val TYPE_SPARKLER: Int = Utils.dynamic(58)
        val TYPE_CONDUIT: Int = Utils.dynamic(59)
        val TYPE_BUBBLE_COLUMN_UP: Int = Utils.dynamic(60)
        val TYPE_BUBBLE_COLUMN_DOWN: Int = Utils.dynamic(61)
        val TYPE_SNEEZE: Int = Utils.dynamic(62)
        val TYPE_SHULKER_BULLET: Int = Utils.dynamic(63)
        val TYPE_BLEACH: Int = Utils.dynamic(64)
        val TYPE_DRAGON_DESTROY_BLOCK: Int = Utils.dynamic(65)
        val TYPE_MYCELIUM_DUST: Int = Utils.dynamic(66)
        val TYPE_FALLING_BORDER_DUST: Int = Utils.dynamic(67)
        val TYPE_CAMPFIRE_SMOKE: Int = Utils.dynamic(68)
        val TYPE_CAMPFIRE_SMOKE_TALL: Int = Utils.dynamic(69)
        val TYPE_DRAGON_BREATH_FIRE: Int = Utils.dynamic(70)
        val TYPE_DRAGON_BREATH_TRAIL: Int = Utils.dynamic(71)
        val TYPE_BLUE_FLAME: Int = Utils.dynamic(72)
        val TYPE_SOUL: Int = Utils.dynamic(73)
        val TYPE_OBSIDIAN_TEAR: Int = Utils.dynamic(74)
        val TYPE_PORTAL_REVERSE: Int = Utils.dynamic(75)
        val TYPE_SNOWFLAKE: Int = Utils.dynamic(76)
        val TYPE_VIBRATION_SIGNAL: Int = Utils.dynamic(77)
        val TYPE_SCULK_SENSOR_REDSTONE: Int = Utils.dynamic(78)
        val TYPE_SPORE_BLOSSOM_SHOWER: Int = Utils.dynamic(79)
        val TYPE_SPORE_BLOSSOM_AMBIENT: Int = Utils.dynamic(80)
        val TYPE_WAX: Int = Utils.dynamic(81)
        val TYPE_ELECTRIC_SPARK: Int = Utils.dynamic(82)
        val TYPE_SHRIEK: Int = Utils.dynamic(83)
        val TYPE_SCULK_SOUL: Int = Utils.dynamic(84)
        val TYPE_SONIC_EXPLOSION: Int = Utils.dynamic(85)
        val TYPE_BRUSH_DUST: Int = Utils.dynamic(86)
        val TYPE_CHERRY_LEAVES: Int = Utils.dynamic(87)
        val TYPE_DUST_PLUME: Int = Utils.dynamic(88)
        val TYPE_WHITE_SMOKE: Int = Utils.dynamic(89)
        val TYPE_VAULT_CONNECTION: Int = Utils.dynamic(90)
        val TYPE_WIND_EXPLOSION: Int = Utils.dynamic(91)
        val TYPE_WOLF_ARMOR_BREAK: Int = Utils.dynamic(92)
        val TYPE_OMINOUS_ITEM_SPAWNER: Int = Utils.dynamic(93)
        val TYPE_CREAKING_CRUMBLE: Int = Utils.dynamic(94)
        val TYPE_PALE_OAK_LEAVES: Int = Utils.dynamic(95)
        val TYPE_EYEBLOSSOM_OPEN: Int = Utils.dynamic(96)
        val TYPE_EYEBLOSSOM_CLOSE: Int = Utils.dynamic(97)


        fun getParticleIdByName(name: String): Int? {
            val name1: String = name.uppercase(Locale.ENGLISH)

            try {
                val field =
                    Particle::class.java.getDeclaredField(if (name1.startsWith("TYPE_")) name1 else "TYPE_$name1")

                val type = field.type

                if (type == Int::class.javaPrimitiveType) {
                    return field.getInt(null)
                }
            } catch (_: Exception) {
            }
            return null
        }

        fun particleExists(name: String): Boolean {
            return getParticleIdByName(name) != null
        }
    }
}
