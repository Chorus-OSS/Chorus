package org.chorus_oss.chorus.recipe

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.registry.Registries
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class RecipeTest {
    @Test
    fun test_getNetworkIdRecipeList() {
        Registries.RECIPE.getNetworkIdRecipeList()
    }

    @Test
    fun test_findBlastFurnaceRecipe() {
        val blastFurnaceRecipe = Registries.RECIPE.findBlastFurnaceRecipe(Item.get(ItemID.IRON_PICKAXE))!!
        Assertions.assertEquals("minecraft:iron_nugget", blastFurnaceRecipe.result.id)
    }

    @Test
    fun test_findShapelessRecipe() {
        val shapelessRecipe =
            Registries.RECIPE.findShapelessRecipe(Item.get(BlockID.BLUE_SHULKER_BOX), Item.get(ItemID.BROWN_DYE))!!
        Assertions.assertEquals("minecraft:brown_shulker_box", shapelessRecipe.result.id)
    }

    @Test
    fun test_tryShrinkMatrix1() {
        val item1 = listOf(Item.AIR, Item.AIR, Item.AIR).toTypedArray()
        val item2 = listOf(Item.AIR, Item.get(ItemID.PLANKS), Item.get(ItemID.DIAMOND)).toTypedArray()
        val item3 = listOf(Item.AIR, Item.get(ItemID.TORCHFLOWER_SEEDS), Item.get(ItemID.MELON_SEEDS)).toTypedArray()
        val data = arrayOf(item1, item2, item3)
        val input = Input(3, 3, data)
        ShapedRecipe.tryShrinkMatrix(input)
        val items = input.data

        val expected1 = listOf(Item.get(ItemID.PLANKS), Item.get(ItemID.DIAMOND)).toTypedArray()
        val expected2 =
            listOf(Item.get(ItemID.TORCHFLOWER_SEEDS), Item.get(ItemID.MELON_SEEDS)).toTypedArray()
        val expected = arrayOf(expected1, expected2)
        Assertions.assertArrayEquals(expected, items)
    }

    @Test
    fun test_tryShrinkMatrix2() {
        val item1 = listOf(Item.get(ItemID.PLANKS), Item.get(ItemID.DIAMOND), Item.AIR).toTypedArray()
        val item2 = listOf(Item.get(ItemID.TORCHFLOWER_SEEDS), Item.get(ItemID.MELON_SEEDS), Item.AIR).toTypedArray()
        val item3 = listOf(Item.AIR, Item.AIR, Item.AIR).toTypedArray()
        val data = arrayOf(item1, item2, item3)
        val input = Input(3, 3, data)
        ShapedRecipe.tryShrinkMatrix(input)
        val items = input.data

        val expected1 = listOf(Item.get(ItemID.PLANKS), Item.get(ItemID.DIAMOND)).toTypedArray()
        val expected2 =
            listOf(Item.get(ItemID.TORCHFLOWER_SEEDS), Item.get(ItemID.MELON_SEEDS)).toTypedArray()
        val expected = arrayOf(expected1, expected2)
        Assertions.assertArrayEquals(expected, items)
    }

    @Test
    fun test_tryShrinkMatrix3() {
        val item1 = listOf(Item.get(ItemID.PLANKS), Item.get(ItemID.DIAMOND), Item.get(ItemID.TORCHFLOWER_SEEDS))
            .toTypedArray()
        val item2 = listOf(Item.AIR, Item.get(ItemID.STICK), Item.AIR).toTypedArray()
        val item3 = listOf(Item.AIR, Item.get(ItemID.STICK), Item.AIR).toTypedArray()
        val data = arrayOf(item1, item2, item3)
        val input = Input(3, 3, data)
        ShapedRecipe.tryShrinkMatrix(input)
        val items = input.data
        val expected = items.clone()
        Assertions.assertArrayEquals(expected, items)
    }

    @Test
    fun test_tryShrinkMatrix4() {
        val item1 = listOf(Item.get(ItemID.STICK), Item.AIR, Item.AIR).toTypedArray()
        val item2 = listOf(Item.get(ItemID.STICK), Item.AIR, Item.AIR).toTypedArray()
        val item3 = listOf(Item.AIR, Item.AIR, Item.AIR).toTypedArray()
        val data = arrayOf(item1, item2, item3)
        val input = Input(3, 3, data)
        ShapedRecipe.tryShrinkMatrix(input)
        val items = input.data
        //                                row0                      row1
        val expected = arrayOf(arrayOf(Item.get(ItemID.STICK)), arrayOf(Item.get(ItemID.STICK)))
        Assertions.assertArrayEquals(expected, items)
    }

    @Test
    fun test_tryShrinkMatrix5() {
        val item1 = listOf(Item.AIR, Item.AIR, Item.AIR).toTypedArray()
        val item2 = listOf(Item.AIR, Item.AIR, Item.AIR).toTypedArray()
        val item3 = listOf(Item.get(ItemID.PLANKS), Item.get(ItemID.PLANKS), Item.get(ItemID.PLANKS)).toTypedArray()
        val data = arrayOf(item1, item2, item3)
        val input = Input(3, 3, data)
        ShapedRecipe.tryShrinkMatrix(input)
        val items = input.data
        //                                row0                      row1                          row2
        val expected = arrayOf(arrayOf(Item.get(ItemID.PLANKS), Item.get(ItemID.PLANKS), Item.get(ItemID.PLANKS)))
        Assertions.assertArrayEquals(expected, items)
    }

    @Test
    fun test_tryShrinkMatrix6() {
        val item1 = listOf(Item.AIR, Item.AIR, Item.AIR).toTypedArray()
        val item2 = listOf(Item.get(ItemID.PLANKS), Item.AIR, Item.get(ItemID.PLANKS)).toTypedArray()
        val item3 = listOf(Item.get(ItemID.PLANKS), Item.AIR, Item.get(ItemID.PLANKS)).toTypedArray()
        val data = arrayOf(item1, item2, item3)
        val input = Input(3, 3, data)
        ShapedRecipe.tryShrinkMatrix(input)
        val items = input.data
        //         row0                      row1              row2
        val expected = arrayOf(
            arrayOf(Item.get(ItemID.PLANKS), Item.AIR, Item.get(ItemID.PLANKS)),
            arrayOf(Item.get(ItemID.PLANKS), Item.AIR, Item.get(ItemID.PLANKS))
        )
        Assertions.assertArrayEquals(expected, items)
    }

    @Test
    fun test_tryShrinkMatrix7() {
        val item1 = listOf(Item.AIR, Item.AIR).toTypedArray()
        val item2 = listOf(Item.get(ItemID.PLANKS), Item.AIR).toTypedArray()
        val data = arrayOf(item1, item2)
        val input = Input(2, 2, data)
        ShapedRecipe.tryShrinkMatrix(input)
        val items = input.data
        //         row0
        val expected = arrayOf(
            arrayOf(Item.get(ItemID.PLANKS))
        )
        Assertions.assertArrayEquals(expected, items)
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun before() {
            Registries.POTION.init()
            Registries.BLOCKSTATE_ITEMMETA.init()
            Registries.BLOCK.init()
            Registries.ITEM.init()
            Registries.ITEM_RUNTIMEID.init()
            Registries.RECIPE.init()
        }
    }
}
