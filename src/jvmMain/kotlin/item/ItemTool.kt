package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.item.ItemWearEvent
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.nbt.tag.ByteTag
import org.chorus_oss.chorus.utils.Utils
import java.util.*


abstract class ItemTool @JvmOverloads constructor(id: String, meta: Int = 0, count: Int = 1, name: String? = null) :
    Item(id, meta, count, name), ItemDurable {
    override val maxStackSize: Int
        get() = 1

    override fun useOn(block: Block): Boolean {
        if (this.isUnbreakable || this.isDurable || noDamageOnBreak()) {
            return true
        }

        if (block.toolType == TYPE_PICKAXE && this.isPickaxe || block.toolType == TYPE_SHOVEL && this.isShovel || block.toolType == TYPE_AXE && this.isAxe || block.toolType == TYPE_HOE && this.isHoe || block.toolType == TYPE_SWORD && this.isSword || block.toolType == TYPE_SHEARS && this.isShears
        ) {
            incDamage(1)
        } else if (!this.isShears && block.calculateBreakTime(this) > 0) {
            incDamage(2)
        } else if (this.isHoe) {
            if (block.id == BlockID.GRASS_BLOCK || block.id == BlockID.DIRT) {
                incDamage(1)
            }
        } else {
            incDamage(1)
        }
        return true
    }

    override var damage: Int
        get() = super.damage
        set(damage) {
            val event = ItemWearEvent(this, damage)
            val pluginManager = Server.instance.pluginManager
            pluginManager.callEvent(event) //Method gets called on server start before plugin manager is initiated

            if (!event.cancelled) {
                super.damage = (event.newDurability)
                getOrCreateNamedTag().putInt("Damage", event.newDurability)
            }
        }

    fun incDamage(v: Int) {
        damage = v.let { this.meta += it; this.meta }
    }

    override fun useOn(entity: Entity?): Boolean {
        if (this.isUnbreakable || this.isDurable || noDamageOnAttack()) {
            return true
        }

        if ((entity != null) && !this.isSword) {
            incDamage(2)
        } else {
            incDamage(1)
        }

        return true
    }

    private val isDurable: Boolean
        get() {
            if (!hasEnchantments()) {
                return false
            }

            val durability =
                getEnchantment(Enchantment.Companion.ID_DURABILITY)
            return durability != null && durability.level > 0 && (100 / (durability.level + 1)) <= Random()
                .nextInt(100)
        }

    override val isUnbreakable: Boolean
        get() {
            val tag = this.getNamedTagEntry("Unbreakable")
            return tag is ByteTag && tag.data > 0
        }

    override val isPickaxe: Boolean
        get() = false

    override val isAxe: Boolean
        get() = false

    override val isSword: Boolean
        get() = false

    override val isShovel: Boolean
        get() = false

    override val isHoe: Boolean
        get() = false

    override val isShears: Boolean
        get() = (this.id == ItemID.Companion.SHEARS)

    override val isTool: Boolean
        get() = true

    override val enchantAbility: Int
        get() {
            val tier = this.tier
            return when (tier) {
                TIER_STONE -> {
                    5
                }

                TIER_WOODEN, TIER_NETHERITE -> {
                    15
                }

                TIER_DIAMOND -> {
                    10
                }

                TIER_GOLD -> {
                    22
                }

                TIER_IRON -> {
                    14
                }

                else -> {
                    0
                }
            }
        }

    /**
     * No damage to item when it's used to attack entities
     *
     * @return whether the item should take damage when used to attack entities
     */
    open fun noDamageOnAttack(): Boolean {
        return false
    }

    /**
     * No damage to item when it's used to break blocks
     *
     * @return whether the item should take damage when used to break blocks
     */
    open fun noDamageOnBreak(): Boolean {
        return false
    }

    companion object {
        const val TIER_WOODEN: Int = 1
        const val TIER_GOLD: Int = 2
        const val TIER_STONE: Int = 3
        const val TIER_IRON: Int = 4
        const val TIER_DIAMOND: Int = 5
        const val TIER_NETHERITE: Int = 6

        const val TYPE_NONE: Int = 0
        const val TYPE_SWORD: Int = 1
        const val TYPE_SHOVEL: Int = 2
        const val TYPE_PICKAXE: Int = 3
        const val TYPE_AXE: Int = 4
        const val TYPE_SHEARS: Int = 5
        const val TYPE_HOE: Int = 6

        /**
         * Same breaking speed independent of the tool.
         */
        @JvmField
        val TYPE_HANDS_ONLY: Int = Utils.dynamic(Int.MAX_VALUE)

        val DURABILITY_WOODEN: Int = Utils.dynamic(60)
        val DURABILITY_GOLD: Int = Utils.dynamic(33)
        val DURABILITY_STONE: Int = Utils.dynamic(132)
        val DURABILITY_IRON: Int = Utils.dynamic(251)
        val DURABILITY_DIAMOND: Int = Utils.dynamic(1562)
        val DURABILITY_NETHERITE: Int = Utils.dynamic(2032)
        val DURABILITY_FLINT_STEEL: Int = Utils.dynamic(65)
        val DURABILITY_SHEARS: Int = Utils.dynamic(239)
        val DURABILITY_BOW: Int = Utils.dynamic(385)
        val DURABILITY_TRIDENT: Int = Utils.dynamic(251)
        val DURABILITY_FISHING_ROD: Int = Utils.dynamic(384)
        val DURABILITY_CROSSBOW: Int = Utils.dynamic(464)
        val DURABILITY_CARROT_ON_A_STICK: Int = Utils.dynamic(26)
        val DURABILITY_WARPED_FUNGUS_ON_A_STICK: Int = Utils.dynamic(101)
        val DURABILITY_SHIELD: Int = Utils.dynamic(337)

        @JvmStatic
        fun getBestTool(toolType: Int): Item {
            when (toolType) {
                TYPE_NONE, TYPE_PICKAXE -> {
                    return get(ItemID.Companion.NETHERITE_PICKAXE)
                }

                TYPE_AXE -> {
                    return get(ItemID.Companion.NETHERITE_AXE)
                }

                TYPE_SHOVEL -> {
                    return get(ItemID.Companion.NETHERITE_SHOVEL)
                }

                TYPE_SHEARS -> {
                    return get(ItemID.Companion.SHEARS)
                }

                TYPE_SWORD -> {
                    return get(ItemID.Companion.NETHERITE_SWORD)
                }

                else -> {
                    // Can't use the switch-case syntax because they are dynamic types
                    if (toolType == TYPE_HOE) {
                        return get(ItemID.Companion.NETHERITE_HOE)
                    }
                    if (toolType == TYPE_HANDS_ONLY) {
                        return AIR
                    }
                    return get(ItemID.Companion.NETHERITE_PICKAXE)
                }
            }
        }
    }
}
