package org.chorus_oss.chorus.block.customblock

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.item.Item
import kotlin.math.min

/**
 * 继承这个类实现自定义方块,重写[Block]中的方法控制方块属性
 *
 *
 * Inherit this class to implement a custom block, override the methods in the [Block] to control the feature of the block.
 */
interface CustomBlock {
    /**
     * 覆写该方法设置自定义方块的摩擦因数。
     *
     *
     * `@Override` this method to set the friction factor of the custom block.
     */
    val frictionFactor: Double

    /**
     * 覆写该方法设置自定义方块的爆炸抗性
     *
     *
     * `@Override` this method to set the Explosive resistance of the custom block
     */
    val resistance: Double

    /**
     * 覆写该方法设置自定义方块的吸收光的等级
     *
     *
     * `@Override` this method to set the level of light absorption of the custom block
     */
    val lightFilter: Int

    /**
     * 覆写该方法设置自定义方块的发出光的等级
     *
     *
     * `@Override` this method to set the level of light emitted by the custom block
     */
    val lightLevel: Int

    /**
     * 覆写该方法设置自定义方块的硬度，这有助于自定义方块在服务端侧计算挖掘时间(硬度越大服务端侧挖掘时间越长)
     *
     *
     * `@Override` this method to set the hardness of the custom block, which helps to calculate the break time of the custom block on the server-side (the higher the hardness the longer the break time on the server-side)
     */
    val hardness: Double

    val id: String

    /**
     * 一般不需要被覆写,继承父类会提供
     *
     *
     * Generally, it does not need to be `@Override`, extend from the parent class will provide
     */
    fun toItem(): Item?

    /**
     * 该方法设置自定义方块的定义
     *
     *
     * This method sets the definition of custom block
     */
    val definition: CustomBlockDefinition

    /**
     * Plugins do not need `@Override`
     *
     * @return the block
     */
    fun toBlock(): Block {
        return (this as Block).clone()
    }

    /**
     * 获取自定义方块的挖掘时间，它是服务端侧和客户端侧挖掘时间的最小值。
     *
     * @param item   the item
     * @param player the player
     * @return the break time
     */
    fun breakTime(item: Item, player: Player?): Double {
        val block = this.toBlock()
        var breakTime = block.calculateBreakTime(item, player)
        val comp = definition.nbt.getCompound("components")
        if (comp.containsCompound("minecraft:destructible_by_mining")) {
            var clientBreakTime = comp.getCompound("minecraft:destructible_by_mining").getFloat("value")
            if (player != null) {
                if (player.level!!.tick - player.lastInAirTick < 5) {
                    clientBreakTime *= 6f
                }
            }
            breakTime = min(breakTime, clientBreakTime.toDouble())
        }
        return breakTime
    }

    /**
     * 定义这个方块是否需要被注册到创造栏中
     * 当你对这个方块有其他的物品想作为其展示时推荐关闭
     */
    fun shouldBeRegisteredInCreative(): Boolean {
        return true
    }
}
