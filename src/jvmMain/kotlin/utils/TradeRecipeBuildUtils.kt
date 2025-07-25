package org.chorus_oss.chorus.utils

import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class TradeRecipeBuildUtils(tag: CompoundTag, private val size: Int) {
    private val recipe = tag

    /**
     * @param maxUses 设置该交易配方最大交易次数
     */
    fun setMaxUses(maxUses: Int): TradeRecipeBuildUtils {
        recipe.putInt("maxUses", maxUses) //最大交易次数
        return this
    }

    /**
     * @param priceMultiplierA 设置该交易配方价格增长系数(第一个交易物品)
     */
    fun setPriceMultiplierA(priceMultiplierA: Float): TradeRecipeBuildUtils {
        recipe.putFloat("priceMultiplierA", priceMultiplierA) //价格增长？
        return this
    }

    /**
     * @param priceMultiplierB 设置该交易配方价格增长系数(第二个交易物品)
     */
    fun setPriceMultiplierB(priceMultiplierB: Float): TradeRecipeBuildUtils {
        if (size == 3) {
            recipe.putFloat("priceMultiplierB", priceMultiplierB) //价格增长？
            return this
        }
        return this
    }

    /**
     * @param rewardExp 设置该交易配方奖励玩家的经验值
     */
    fun setRewardExp(rewardExp: Byte): TradeRecipeBuildUtils {
        recipe.putByte("rewardExp", rewardExp.toInt())
        return this
    }

    /**
     * @param tier 该配方需要的交易等级 从1开始
     */
    fun setTier(tier: Int): TradeRecipeBuildUtils {
        var tier1 = tier
        recipe.putInt("tier", --tier1) //需要村民等级
        return this
    }

    /**
     * @param traderExp 设置该交易配方给予村民的经验
     */
    fun setTraderExp(traderExp: Int): TradeRecipeBuildUtils {
        recipe.putInt("traderExp", traderExp) //村民增加的经验
        return this
    }

    fun build(): CompoundTag {
        RECIPE_MAP[recipe.getInt("netId")] = recipe
        return recipe
    }

    companion object {
        const val TRADE_RECIPE_ID: Int = 10000
        val RECIPE_MAP: ConcurrentHashMap<Int, CompoundTag> = ConcurrentHashMap()
        private val TRADE_RECIPE_NET_ID = AtomicInteger(TRADE_RECIPE_ID)

        /**
         * @param buyA   交易材料1
         * @param output 交易结果
         * @return 配方构造工具
         */
        fun of(buyA: Item, output: Item): TradeRecipeBuildUtils {
            val cmp = CompoundTag()
                .putCompound(
                    "buyA", CompoundTag()
                        .putByte("Count", buyA.getCount())
                        .putShort("Damage", buyA.damage)
                        .putString("Name", buyA.id)
                        .putBoolean("WasPickedUp", false)
                ) //是否是掉落物？？
                .putInt("buyCountA", buyA.getCount())
                .putInt("buyCountB", 0)
                .putInt("demand", 0) //未知
                .putInt("netId", TRADE_RECIPE_NET_ID.getAndIncrement())
                .putFloat("priceMultiplierB", 0f)
                .putCompound(
                    "sell", CompoundTag()
                        .putByte("Count", output.getCount())
                        .putShort("Damage", output.damage)
                        .putString("Name", output.id)
                        .putBoolean("WasPickedUp", false)
                )
                .putInt("uses", 0) //未知
            if (buyA.hasCompoundTag()) {
                cmp.getCompound("buyA").putCompound("tag", buyA.namedTag!!)
            }
            if (output.hasCompoundTag()) {
                cmp.getCompound("sell").putCompound("tag", output.namedTag!!)
            }
            return TradeRecipeBuildUtils(cmp, 2)
        }

        /**
         * @param buyA   交易材料1
         * @param buyB   交易材料2
         * @param output 交易结果
         * @return 配方构造工具
         */
        fun of(buyA: Item, buyB: Item, output: Item): TradeRecipeBuildUtils {
            val cmp = CompoundTag()
                .putCompound(
                    "buyA", CompoundTag()
                        .putByte("Count", buyA.getCount())
                        .putShort("Damage", buyA.damage)
                        .putString("Name", buyA.id)
                        .putBoolean("WasPickedUp", false)
                ) //是否是掉落物？？
                .putCompound(
                    "buyB", CompoundTag()
                        .putByte("Count", buyB.getCount())
                        .putShort("Damage", buyB.damage)
                        .putString("Name", buyB.id)
                        .putBoolean("WasPickedUp", false)
                )
                .putInt("buyCountA", buyA.getCount())
                .putInt("buyCountA", buyB.getCount())
                .putInt("demand", 0) //未知
                .putInt("netId", TRADE_RECIPE_NET_ID.getAndIncrement())
                .putCompound(
                    "sell", CompoundTag()
                        .putByte("Count", output.getCount())
                        .putShort("Damage", output.damage)
                        .putString("Name", output.id)
                        .putBoolean("WasPickedUp", false)
                )
                .putInt("uses", 0) //未知
            if (buyA.hasCompoundTag()) {
                cmp.getCompound("buyA").putCompound("tag", buyA.namedTag!!)
            }
            if (buyB.hasCompoundTag()) {
                cmp.getCompound("buyB").putCompound("tag", buyB.namedTag!!)
            }
            if (output.hasCompoundTag()) {
                cmp.getCompound("sell").putCompound("tag", output.namedTag!!)
            }
            return TradeRecipeBuildUtils(cmp, 3)
        }
    }
}
