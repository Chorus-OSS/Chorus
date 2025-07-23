package org.chorus_oss.chorus.item.enchantment

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.enchantment.bow.EnchantmentBowFlame
import org.chorus_oss.chorus.item.enchantment.bow.EnchantmentBowInfinity
import org.chorus_oss.chorus.item.enchantment.bow.EnchantmentBowKnockback
import org.chorus_oss.chorus.item.enchantment.bow.EnchantmentBowPower
import org.chorus_oss.chorus.item.enchantment.crossbow.EnchantmentCrossbowMultishot
import org.chorus_oss.chorus.item.enchantment.crossbow.EnchantmentCrossbowPiercing
import org.chorus_oss.chorus.item.enchantment.crossbow.EnchantmentCrossbowQuickCharge
import org.chorus_oss.chorus.item.enchantment.damage.EnchantmentDamageAll
import org.chorus_oss.chorus.item.enchantment.damage.EnchantmentDamageArthropods
import org.chorus_oss.chorus.item.enchantment.damage.EnchantmentDamageSmite
import org.chorus_oss.chorus.item.enchantment.loot.EnchantmentLootDigging
import org.chorus_oss.chorus.item.enchantment.loot.EnchantmentLootFishing
import org.chorus_oss.chorus.item.enchantment.loot.EnchantmentLootWeapon
import org.chorus_oss.chorus.item.enchantment.protection.*
import org.chorus_oss.chorus.item.enchantment.trident.EnchantmentTridentChanneling
import org.chorus_oss.chorus.item.enchantment.trident.EnchantmentTridentImpaling
import org.chorus_oss.chorus.item.enchantment.trident.EnchantmentTridentLoyalty
import org.chorus_oss.chorus.item.enchantment.trident.EnchantmentTridentRiptide
import org.chorus_oss.chorus.utils.Identifier
import org.chorus_oss.chorus.utils.TextFormat
import org.chorus_oss.chorus.utils.Utils
import java.util.concurrent.ThreadLocalRandom
import java.util.stream.Collectors

abstract class Enchantment : Cloneable {
    /**
     * The ID of this enchantment.
     */
    /**
     * The internal ID which this enchantment got registered.
     */
    @JvmField
    val id: Int

    /**
     * How rare this enchantment is.
     */
    val rarity: Rarity

    /**
     * The group of objects that this enchantment can be applied.
     */
    var type: EnchantmentType
    /**
     * The current level of this enchantment. `0` means that the enchantment is not applied.
     *
     * @return The level starting from `1`.
     */
    /**
     * The level of this enchantment. Starting from `1`.
     */
    var level: Int = 1
        protected set

    /**
     * The name visible by the player, this is used in conjunction with [.getName],
     * unless modified with an override, the getter will automatically add
     * "%enchantment." as prefix to grab the translation key
     */
    val originalName: String

    /**
     * 获取该附魔的标识符，只有自定义附魔才有
     *
     * @return the identifier
     */
    val identifier: Identifier?

    /**
     * This value is for treasures of [org.chorus_oss.chorus.item.randomitem.Fishing], specifically [org.chorus_oss.chorus.item.randomitem.fishing.FishingEnchantmentItemSelector]
     * If false, this enchantment cannot be fished using a [org.chorus_oss.chorus.item.ItemFishingRod]
     *
     * @return whether this enchantment can be fished or not
     */
    /**
     * Decides whether this enchantment can be fished ([org.chorus_oss.chorus.item.randomitem.fishing.FishingEnchantmentItemSelector]) using a [org.chorus_oss.chorus.item.ItemFishingRod]
     *
     * @param isFishable true if it is fishable
     */
    var isFishable: Boolean = true
    /**
     * This value is used for deciding which enchantments can be obtained within the [org.chorus_oss.chorus.block.BlockEnchantingTable], used within [EnchantmentHelper]
     *
     * @return true if it can be obtained
     */
    /**
     * Decides whether this enchantment can be obtained from a [org.chorus_oss.chorus.block.BlockEnchantingTable]
     *
     * @param obtainableFromEnchantingTable true if it is obtainable
     */
    var isObtainableFromEnchantingTable: Boolean = true

    /**
     * Constructs this instance using the given data and with level 1.
     *
     * @param id     The enchantment ID
     * @param name   The translation key without the "%enchantment." suffix
     * @param rarity How rare this enchantment is
     * @param type   Where the enchantment can be applied
     */
    protected constructor(id: Int, name: String, rarity: Rarity, type: EnchantmentType) {
        this.identifier = null
        this.id = id
        this.rarity = rarity
        this.type = type
        this.originalName = name
    }

    /**
     * 自定义附魔使用的构造函数
     *
     * @param name   The translation key without the "%enchantment." suffix
     * @param rarity How rare this enchantment is
     * @param type   Where the enchantment can be applied
     */
    protected constructor(identifier: Identifier, name: String, rarity: Rarity, type: EnchantmentType) {
        this.identifier = identifier
        this.id = CUSTOM_ENCHANTMENT_ID
        this.rarity = rarity
        this.type = type
        this.originalName = name
    }

    val lore: String
        /**
         * 获取该附魔在物品描述中的Lore,被自定义附魔用于添加描述,代表物品附魔描述中的一行
         *
         *
         * Get the enchantment in the item description Lore, which is used by the custom enchantment to add a description, representing a line in the item's enchantment description
         *
         * @return the lore
         */
        get() = TextFormat.GRAY.toString() + this.getName() + " " + getLevelString(
            level
        )

    /**
     * Changes the level of this enchantment.
     * The level is clamped between the values returned in [.getMinLevel] and [.getMaxLevel].
     *
     * @param level The level starting from `1`.
     * @return This object so you can do chained calls
     */
    fun setLevel(level: Int): Enchantment {
        return this.setLevel(level, true)
    }

    /**
     * Changes the level of this enchantment.
     * When the `safe` param is `true`, the level is clamped between the values
     * returned in [.getMinLevel] and [.getMaxLevel].
     *
     * @param level The level starting from `1`.
     * @param safe  If the level should clamped or applied directly
     * @return This object so you can do chained calls
     */
    fun setLevel(level: Int, safe: Boolean): Enchantment {
        if (!safe) {
            this.level = level
            return this
        }

        this.level = level.coerceIn(this.minLevel, this.maxLevel)

        return this
    }

    val minLevel: Int
        /**
         * The minimum safe level which is possible with this enchantment. It is usually `1`.
         */
        get() = 1

    open val maxLevel: Int
        /**
         * The maximum safe level which is possible with this enchantment.
         */
        get() = 1

    /**
     * The minimum enchantability for the given level as described in https://minecraft.wiki/w/Enchanting/Levels
     *
     * @param level The level being checked
     * @return The minimum value
     */
    open fun getMinEnchantAbility(level: Int): Int {
        return 1 + level * 10
    }

    /**
     * The maximum enchantability for the given level as described in https://minecraft.wiki/w/Enchanting/Levels
     *
     * @param level The level being checked
     * @return The maximum value
     */
    open fun getMaxEnchantAbility(level: Int): Int {
        return this.getMinEnchantAbility(level) + 5
    }

    /**
     * 当实体盔甲具有附魔时触发
     *
     *
     * 覆写该方法提供当实体被攻击时盔甲提供的保护值
     *
     *
     * 目前只生效于[HumanType][org.chorus_oss.chorus.entity.EntityHumanType] 和 [EntityMob][EntityMonster]
     *
     * @param event 该实体被攻击的事件
     * @return the protection factor
     */
    open fun getProtectionFactor(event: EntityDamageEvent): Float {
        return 0f
    }

    /**
     * 当实体武器具有附魔时触发
     *
     *
     * 覆写该方法提供当实体使用附魔武器攻击所增益的攻击力
     *
     *
     * 目前只生效于[Player][org.chorus_oss.chorus.Player] 和 使用了[MeleeAttackExecutor][org.chorus_oss.chorus.entity.ai.executor.MeleeAttackExecutor]行为的实体
     *
     * @param target 攻击的目标实体
     * @param damager the entity that deals the damage
     * @return the damage value
     */
    open fun getDamageBonus(target: Entity, damager: Entity): Double {
        return 0.0
    }

    /**
     * 当实体entity穿着附魔盔甲，被实体attacker攻击时触发
     *
     *
     * 覆写该方法实现该过程中的逻辑
     *
     * @param attacker the attacker
     * @param entity   the entity
     */
    open fun doPostAttack(attacker: Entity, entity: Entity) {
    }

    open fun doAttack(event: EntityDamageByEntityEvent) {
    }

    /**
     * 目前没有任何作用
     *
     * @param attacker the attacker
     * @param entity   the entity
     */
    fun doPostHurt(attacker: Entity?, entity: Entity?) {
    }

    /**
     * Returns true if and only if this enchantment is compatible with the other and
     * the other is also compatible with this enchantment.
     *
     * @param enchantment The enchantment which is being checked
     * @return If both enchantments are compatible
     * @implNote Cloudburst Nukkit added the final modifier, PowerNukkit removed it to maintain backward compatibility.
     * The right way to implement compatibility now is to override [.checkCompatibility]
     * and also make sure to keep it protected! Some overrides was incorrectly made public, let's avoid this mistake
     */
    fun isCompatibleWith(enchantment: Enchantment): Boolean {
        return this.checkCompatibility(enchantment) && enchantment.checkCompatibility(this)
    }

    /**
     * Checks if this enchantment can be applied to an item that have the give enchantment without doing reverse check.
     *
     * @param enchantment The enchantment to be checked
     * @return If this enchantment is compatible with the other enchantment.
     */
    protected open fun checkCompatibility(enchantment: Enchantment): Boolean {
        return this !== enchantment
    }

    //return the translation key for the enchantment
    open fun getName(): String {
        return if (this.identifier == null) "%enchantment." + this.originalName
        else originalName
    }

    /**
     * Checks if the given item have a type which is compatible with this enchantment. This method does not check
     * if the item already have incompatible enchantments.
     *
     * @param item The item to be checked
     * @return If the type of the item is valid for this enchantment
     */
    open fun canEnchant(item: Item): Boolean {
        return type.canEnchantItem(item)
    }

    open val isMajor: Boolean
        get() = false

    public override fun clone(): Enchantment {
        return super.clone() as Enchantment
    }

    private class UnknownEnchantment(id: Int) :
        Enchantment(id, "unknown", Rarity.VERY_RARE, EnchantmentType.ALL)

    /**
     * How rare an enchantment is.
     */
    enum class Rarity(val weight: Int) {
        COMMON(10),
        UNCOMMON(5),
        RARE(2),
        VERY_RARE(1);

        companion object {
            /**
             * Converts the weight to the closest rarity using floor semantic.
             *
             * @param weight The enchantment weight
             * @return The closest rarity
             */
            fun fromWeight(weight: Int): Rarity {
                if (weight < 2) {
                    return VERY_RARE
                } else if (weight < 5) {
                    return RARE
                } else if (weight < 10) {
                    return UNCOMMON
                }
                return COMMON
            }
        }
    }

    companion object {
        val EMPTY_ARRAY: Array<Enchantment> = emptyArray()
        val CUSTOM_ENCHANTMENT_ID: Int = Utils.dynamic(256)
        protected lateinit var defaultEnchantments: Array<Enchantment?>
        protected var namedEnchantments: MutableMap<Identifier, Enchantment> = LinkedHashMap()

        const val ID_PROTECTION_ALL: Int = 0
        const val NAME_PROTECTION_ALL: String = "protection"
        const val ID_PROTECTION_FIRE: Int = 1
        const val NAME_PROTECTION_FIRE: String = "fire_protection"
        const val ID_PROTECTION_FALL: Int = 2
        const val NAME_PROTECTION_FALL: String = "feather_falling"
        const val ID_PROTECTION_EXPLOSION: Int = 3
        const val NAME_PROTECTION_EXPLOSION: String = "blast_protection"
        const val ID_PROTECTION_PROJECTILE: Int = 4
        const val NAME_PROTECTION_PROJECTILE: String = "projectile_protection"
        const val ID_THORNS: Int = 5
        const val NAME_THORNS: String = "thorns"
        const val ID_WATER_BREATHING: Int = 6
        const val NAME_WATER_BREATHING: String = "respiration"
        const val ID_WATER_WALKER: Int = 7
        const val NAME_WATER_WALKER: String = "depth_strider"
        const val ID_WATER_WORKER: Int = 8
        const val NAME_WATER_WORKER: String = "aqua_affinity"
        const val ID_DAMAGE_ALL: Int = 9
        const val NAME_DAMAGE_ALL: String = "sharpness"
        const val ID_DAMAGE_SMITE: Int = 10
        const val NAME_DAMAGE_SMITE: String = "smite"
        const val ID_DAMAGE_ARTHROPODS: Int = 11
        const val NAME_DAMAGE_ARTHROPODS: String = "bane_of_arthropods"
        const val ID_KNOCKBACK: Int = 12
        const val NAME_KNOCKBACK: String = "knockback"
        const val ID_FIRE_ASPECT: Int = 13
        const val NAME_FIRE_ASPECT: String = "fire_aspect"
        const val ID_LOOTING: Int = 14
        const val NAME_LOOTING: String = "looting"
        const val ID_EFFICIENCY: Int = 15
        const val NAME_EFFICIENCY: String = "efficiency"
        const val ID_SILK_TOUCH: Int = 16
        const val NAME_SILK_TOUCH: String = "silk_touch"
        const val ID_DURABILITY: Int = 17
        const val NAME_DURABILITY: String = "unbreaking"
        const val ID_FORTUNE_DIGGING: Int = 18
        const val NAME_FORTUNE_DIGGING: String = "fortune"
        const val ID_BOW_POWER: Int = 19
        const val NAME_BOW_POWER: String = "power"
        const val ID_BOW_KNOCKBACK: Int = 20
        const val NAME_BOW_KNOCKBACK: String = "punch"
        const val ID_BOW_FLAME: Int = 21
        const val NAME_BOW_FLAME: String = "flame"
        const val ID_BOW_INFINITY: Int = 22
        const val NAME_BOW_INFINITY: String = "infinity"
        const val ID_FORTUNE_FISHING: Int = 23
        const val NAME_FORTUNE_FISHING: String = "luck_of_the_sea"
        const val ID_LURE: Int = 24
        const val NAME_LURE: String = "lure"
        const val ID_FROST_WALKER: Int = 25
        const val NAME_FROST_WALKER: String = "frost_walker"
        const val ID_MENDING: Int = 26
        const val NAME_MENDING: String = "mending"
        const val ID_BINDING_CURSE: Int = 27
        const val NAME_BINDING_CURSE: String = "binding"
        const val ID_VANISHING_CURSE: Int = 28
        const val NAME_VANISHING_CURSE: String = "vanishing"
        const val ID_TRIDENT_IMPALING: Int = 29
        const val NAME_TRIDENT_IMPALING: String = "impaling"
        const val ID_TRIDENT_RIPTIDE: Int = 30
        const val NAME_TRIDENT_RIPTIDE: String = "riptide"
        const val ID_TRIDENT_LOYALTY: Int = 31
        const val NAME_TRIDENT_LOYALTY: String = "loyalty"
        const val ID_TRIDENT_CHANNELING: Int = 32
        const val NAME_TRIDENT_CHANNELING: String = "channeling"
        const val ID_CROSSBOW_MULTISHOT: Int = 33
        const val NAME_CROSSBOW_MULTISHOT: String = "multishot"
        const val ID_CROSSBOW_PIERCING: Int = 34
        const val NAME_CROSSBOW_PIERCING: String = "piercing"
        const val ID_CROSSBOW_QUICK_CHARGE: Int = 35
        const val NAME_CROSSBOW_QUICK_CHARGE: String = "quick_charge"
        const val ID_SOUL_SPEED: Int = 36
        const val NAME_SOUL_SPEED: String = "soul_speed"
        const val ID_SWIFT_SNEAK: Int = 37
        const val NAME_SWIFT_SNEAK: String = "swift_sneak"
        const val ID_DENSITY: Int = 39
        const val NAME_DENSITY: String = "density"
        const val ID_BREACH: Int = 40
        const val NAME_BREACH: String = "breach"


        @JvmStatic
        fun init() {
            defaultEnchantments = arrayOfNulls(256)
            defaultEnchantments[ID_PROTECTION_ALL] = EnchantmentProtectionAll()
            defaultEnchantments[ID_PROTECTION_FIRE] = EnchantmentProtectionFire()
            defaultEnchantments[ID_PROTECTION_FALL] = EnchantmentProtectionFall()
            defaultEnchantments[ID_PROTECTION_EXPLOSION] = EnchantmentProtectionExplosion()
            defaultEnchantments[ID_PROTECTION_PROJECTILE] = EnchantmentProtectionProjectile()
            defaultEnchantments[ID_THORNS] = EnchantmentThorns()
            defaultEnchantments[ID_WATER_BREATHING] = EnchantmentWaterBreath()
            defaultEnchantments[ID_WATER_WORKER] = EnchantmentWaterWorker()
            defaultEnchantments[ID_WATER_WALKER] = EnchantmentWaterWalker()
            defaultEnchantments[ID_DAMAGE_ALL] = EnchantmentDamageAll()
            defaultEnchantments[ID_DAMAGE_SMITE] = EnchantmentDamageSmite()
            defaultEnchantments[ID_DAMAGE_ARTHROPODS] = EnchantmentDamageArthropods()
            defaultEnchantments[ID_KNOCKBACK] = EnchantmentKnockback()
            defaultEnchantments[ID_FIRE_ASPECT] = EnchantmentFireAspect()
            defaultEnchantments[ID_LOOTING] = EnchantmentLootWeapon()
            defaultEnchantments[ID_EFFICIENCY] = EnchantmentEfficiency()
            defaultEnchantments[ID_SILK_TOUCH] = EnchantmentSilkTouch()
            defaultEnchantments[ID_DURABILITY] = EnchantmentDurability()
            defaultEnchantments[ID_FORTUNE_DIGGING] = EnchantmentLootDigging()
            defaultEnchantments[ID_BOW_POWER] = EnchantmentBowPower()
            defaultEnchantments[ID_BOW_KNOCKBACK] = EnchantmentBowKnockback()
            defaultEnchantments[ID_BOW_FLAME] = EnchantmentBowFlame()
            defaultEnchantments[ID_BOW_INFINITY] = EnchantmentBowInfinity()
            defaultEnchantments[ID_FORTUNE_FISHING] = EnchantmentLootFishing()
            defaultEnchantments[ID_LURE] = EnchantmentLure()
            defaultEnchantments[ID_FROST_WALKER] = EnchantmentFrostWalker()
            defaultEnchantments[ID_MENDING] = EnchantmentMending()
            defaultEnchantments[ID_BINDING_CURSE] = EnchantmentBindingCurse()
            defaultEnchantments[ID_VANISHING_CURSE] = EnchantmentVanishingCurse()
            defaultEnchantments[ID_TRIDENT_IMPALING] = EnchantmentTridentImpaling()
            defaultEnchantments[ID_TRIDENT_RIPTIDE] = EnchantmentTridentRiptide()
            defaultEnchantments[ID_TRIDENT_LOYALTY] = EnchantmentTridentLoyalty()
            defaultEnchantments[ID_TRIDENT_CHANNELING] = EnchantmentTridentChanneling()
            defaultEnchantments[ID_CROSSBOW_MULTISHOT] = EnchantmentCrossbowMultishot()
            defaultEnchantments[ID_CROSSBOW_PIERCING] = EnchantmentCrossbowPiercing()
            defaultEnchantments[ID_CROSSBOW_QUICK_CHARGE] = EnchantmentCrossbowQuickCharge()
            defaultEnchantments[ID_SOUL_SPEED] = EnchantmentSoulSpeed()
            defaultEnchantments[ID_SWIFT_SNEAK] = EnchantmentSwiftSneak()
            defaultEnchantments[38] = null
            defaultEnchantments[ID_DENSITY] = EnchantmentDensity()
            defaultEnchantments[ID_BREACH] = EnchantmentBreach()
            //custom
            namedEnchantments[Identifier(
                "minecraft",
                NAME_PROTECTION_ALL
            )] = defaultEnchantments[0]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_PROTECTION_FIRE
            )] = defaultEnchantments[1]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_PROTECTION_FALL
            )] = defaultEnchantments[2]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_PROTECTION_EXPLOSION
            )] = defaultEnchantments[3]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_PROTECTION_PROJECTILE
            )] = defaultEnchantments[4]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_THORNS
            )] = defaultEnchantments[5]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_WATER_BREATHING
            )] = defaultEnchantments[6]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_WATER_WORKER
            )] = defaultEnchantments[7]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_WATER_WALKER
            )] = defaultEnchantments[8]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_DAMAGE_ALL
            )] = defaultEnchantments[9]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_DAMAGE_SMITE
            )] = defaultEnchantments[10]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_DAMAGE_ARTHROPODS
            )] = defaultEnchantments[11]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_KNOCKBACK
            )] = defaultEnchantments[12]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_FIRE_ASPECT
            )] = defaultEnchantments[13]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_LOOTING
            )] = defaultEnchantments[14]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_EFFICIENCY
            )] = defaultEnchantments[15]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_SILK_TOUCH
            )] = defaultEnchantments[16]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_DURABILITY
            )] = defaultEnchantments[17]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_FORTUNE_DIGGING
            )] = defaultEnchantments[18]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_BOW_POWER
            )] = defaultEnchantments[19]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_BOW_KNOCKBACK
            )] = defaultEnchantments[20]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_BOW_FLAME
            )] = defaultEnchantments[21]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_BOW_INFINITY
            )] = defaultEnchantments[22]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_FORTUNE_FISHING
            )] = defaultEnchantments[23]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_LURE
            )] = defaultEnchantments[24]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_FROST_WALKER
            )] = defaultEnchantments[25]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_MENDING
            )] = defaultEnchantments[26]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_BINDING_CURSE
            )] = defaultEnchantments[27]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_VANISHING_CURSE
            )] = defaultEnchantments[28]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_TRIDENT_IMPALING
            )] = defaultEnchantments[29]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_TRIDENT_RIPTIDE
            )] = defaultEnchantments[30]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_TRIDENT_LOYALTY
            )] = defaultEnchantments[31]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_TRIDENT_CHANNELING
            )] = defaultEnchantments[32]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_CROSSBOW_MULTISHOT
            )] = defaultEnchantments[33]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_CROSSBOW_PIERCING
            )] = defaultEnchantments[34]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_CROSSBOW_QUICK_CHARGE
            )] = defaultEnchantments[35]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_SOUL_SPEED
            )] = defaultEnchantments[36]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_SWIFT_SNEAK
            )] = defaultEnchantments[37]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_DENSITY
            )] = defaultEnchantments[39]!!
            namedEnchantments[Identifier(
                "minecraft",
                NAME_BREACH
            )] = defaultEnchantments[40]!!
        }

        private fun getLevelString(level: Int): String {
            return when (level) {
                1 -> "I"
                2 -> "II"
                3 -> "III"
                4 -> "IV"
                5 -> "V"
                6 -> "VI"
                7 -> "VII"
                8 -> "VIII"
                9 -> "IX"
                10 -> "X"
                else -> "∞"
            }
        }

        @JvmStatic
        fun reload() {
            defaultEnchantments = arrayOfNulls(256)
            namedEnchantments.clear()
            init()
        }

        /**
         * Returns the enchantment object registered with this ID, any change to the returned object affects
         * the creation of new enchantments as the returned object is not a copy.
         *
         * @param id The enchantment id.
         * @return The enchantment, if no enchantment is found with that id, [UnknownEnchantment] is returned.
         * The UnknownEnchantment will be always a new instance and changes to it does not affects other calls.
         */
        fun get(id: Int): Enchantment {
            var enchantment: Enchantment? = null
            if (id >= 0 && id < defaultEnchantments.size) {
                enchantment = defaultEnchantments[id]
            }
            if (enchantment == null) {
                return UnknownEnchantment(id)
            }
            return enchantment
        }

        /**
         * Returns the enchantment object registered with this ID
         *
         * @param id The enchantment id.
         * @return The enchantment, if no enchantment is found with that id, [UnknownEnchantment] is returned.
         * The UnknownEnchantment will be always a new instance and changes to it does not affects other calls.
         */
        fun getEnchantment(id: Int): Enchantment {
            var enchantment: Enchantment? = null
            if (id >= 0 && id < defaultEnchantments.size) {
                enchantment = defaultEnchantments[id]
            }
            if (enchantment == null) {
                return UnknownEnchantment(id)
            }
            return enchantment.clone()
        }

        /**
         * 使用附魔标识符来获取附魔，原版附魔可以不加命名空间，但是自定义附魔必须加上命名空间才能获取
         *
         *
         * Gets enchantment.
         *
         * @param name Enchantment Identifier
         * @return the enchantment
         */
        @JvmStatic
        fun getEnchantment(name: String): Enchantment {
            return (if (Identifier.isValid(name)) {
                namedEnchantments[Identifier.tryParse(
                    name
                )]
            } else namedEnchantments[Identifier(
                Identifier.DEFAULT_NAMESPACE,
                name
            )])?.clone() ?: throw RuntimeException("Unknown Enchantment Name: $name")
        }

        fun getEnchantment(name: Identifier): Enchantment {
            return namedEnchantments[name]?.clone() ?: throw RuntimeException("Unknown Enchantment Identifier: $name")
        }

        /**
         * Gets an array of all registered enchantments, the objects in the array are linked to the registry,
         * it's not safe to change them. Changing them can cause the same issue as documented in [.get]
         *
         * @return An array with the enchantment objects, the array may contain null objects but is very unlikely.
         */
        @JvmStatic
        fun getEnchantments(): Array<Enchantment> {
            return namedEnchantments.values.toTypedArray()
        }

        @JvmStatic
        val registeredEnchantments: Collection<Enchantment>
            /**
             * Gets a collection with a safe copy of all enchantments that are currently registered.
             *
             * @return The objects can be modified without affecting the registry and the collection will not have null values.
             */
            get() = getRegisteredEnchantments(false)

        fun getRegisteredEnchantments(allowCustom: Boolean): Collection<Enchantment> {
            if (!allowCustom) {
                val enchantments: MutableCollection<Enchantment> = LinkedHashSet()
                namedEnchantments.forEach { (i, v) ->
                    if (i.namespace == Identifier.DEFAULT_NAMESPACE) {
                        enchantments.add(v)
                    }
                }

                return enchantments
            }
            return ArrayList(namedEnchantments.values)
        }

        val enchantmentName2IDMap: Map<String, Int>
            get() = namedEnchantments.entries.stream().collect(
                Collectors.toMap<Map.Entry<Identifier?, Enchantment?>, String, Int>(
                    { it.key.toString() },
                    { it.value!!.id }
                )
            )

        val words: Array<String> = arrayOf(
            "the",
            "elder",
            "scrolls",
            "klaatu",
            "berata",
            "niktu",
            "xyzzy",
            "bless",
            "curse",
            "light",
            "darkness",
            "fire",
            "air",
            "earth",
            "water",
            "hot",
            "dry",
            "cold",
            "wet",
            "ignite",
            "snuff",
            "embiggen",
            "twist",
            "shorten",
            "stretch",
            "fiddle",
            "destroy",
            "imbue",
            "galvanize",
            "enchant",
            "free",
            "limited",
            "range",
            "of",
            "towards",
            "inside",
            "sphere",
            "cube",
            "self",
            "other",
            "ball",
            "mental",
            "physical",
            "grow",
            "shrink",
            "demon",
            "elemental",
            "spirit",
            "animal",
            "creature",
            "beast",
            "humanoid",
            "undead",
            "fresh",
            "stale"
        )

        val randomName: String
            get() {
                val count = ThreadLocalRandom.current().nextInt(3, 6)
                val set: HashSet<String> = LinkedHashSet()
                while (set.size < count) {
                    set.add(
                        words[ThreadLocalRandom.current()
                            .nextInt(0, words.size)]
                    )
                }

                val words = set.toTypedArray()
                return java.lang.String.join(" ", *words)
            }

        fun equal(e1: Enchantment, e2: Enchantment): Boolean {
            return if (e1.identifier == null && e2.identifier == null) {
                e1.id == e2.id
            } else if (e1.identifier != null && e2.identifier != null) {
                e1.identifier === e2.identifier
            } else false
        }
    }
}
