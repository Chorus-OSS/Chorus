package org.chorus_oss.chorus.inventory

/**
 * 标注此物品栏拥有者的物品栏作为合成用途，常见于机器，如熔炉等。
 * <br></br>
 * This inventory holder's inventory is used for recipe, usually in machines, such as furnaces, etc.
 */
interface RecipeInventoryHolder : InventoryHolder {
    /**
     * 获取该物品栏拥有者的物品栏的原料视图，这个视图通常是以[InventorySlice]指向的原始物品栏用于存储原料的一部分。
     * <br></br>
     * Get the ingredients view of the inventory holder's inventory, which is usually a part of the raw inventory.
     *
     * @return 原料视图 the ingredients view of the inventory holder's inventory
     */
    val ingredientView: Inventory?

    /**
     * 获取该物品栏拥有者的物品栏的产物视图，这个视图通常是以[InventorySlice]指向的原始物品栏用于存储结果的一部分。
     * <br></br>
     * Get the result view of the inventory holder's inventory, which is usually a part of the raw inventory.
     *
     * @return 产物视图 the result view of the inventory holder's inventory
     */
    val productView: Inventory?
}
