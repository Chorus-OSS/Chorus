package org.chorus_oss.chorus.item.customitem

/**
 * 继承这个类实现自定义物品,重写[Item]中的方法控制方块属性
 *
 *
 * Inherit this class to implement a custom item, override the methods in the [Item] to control the feature of the item.
 */
interface CustomItem {
    /**
     * 该方法设置自定义物品的定义
     *
     *
     * This method sets the definition of custom item
     */
    val definition: CustomItemDefinition
}
