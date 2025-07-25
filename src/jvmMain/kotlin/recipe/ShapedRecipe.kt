package org.chorus_oss.chorus.recipe

import io.netty.util.collection.CharObjectHashMap
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.network.protocol.types.RecipeUnlockingRequirement
import org.chorus_oss.chorus.recipe.descriptor.DefaultDescriptor
import org.chorus_oss.chorus.recipe.descriptor.ItemDescriptor
import org.chorus_oss.chorus.registry.RecipeRegistry
import java.util.*

class ShapedRecipe @JvmOverloads constructor(
    recipeId: String?,
    uuid: UUID,
    priority: Int,
    primaryResult: Item,
    shape: Array<String>,
    ingredients: Map<Char, ItemDescriptor>,
    extraResults: Collection<Item>,
    mirror: Boolean,
    recipeUnlockingRequirement: RecipeUnlockingRequirement? = null
) :
    CraftingRecipe(
        recipeId
            ?: RecipeRegistry.computeRecipeId(
                listOf(
                    primaryResult,
                    *extraResults.toTypedArray()
                ), ingredients.values, RecipeType.SHAPED
            ), priority, recipeUnlockingRequirement
    ) {
    val shape: Array<String>
    private val shapedIngredients = CharObjectHashMap<ItemDescriptor>()
    val height: Int
    val width: Int
    val isMirror: Boolean

    constructor(
        recipeId: String?,
        priority: Int,
        primaryResult: Item,
        shape: Array<String>,
        ingredients: Map<Char, ItemDescriptor>,
        extraResults: Collection<Item>
    ) : this(recipeId, UUID.randomUUID(), priority, primaryResult, shape, ingredients, extraResults, false)

    init {
        this.uuid = uuid
        this.height = shape.size
        this.isMirror = mirror
        if (this.height > 3 || this.height == 0) {
            throw RuntimeException("Shaped recipes may only have 1, 2 or 3 rows, not " + this.height)
        }

        this.width = shape[0].length
        if (this.width > 3 || this.width == 0) {
            throw RuntimeException("Shaped recipes may only have 1, 2 or 3 columns, not " + this.width)
        }

        var i = 0
        val shapeLength = shape.size
        while (i < shapeLength) {
            val row = shape[i]
            if (row.length != this.width) {
                throw RuntimeException("Shaped recipe rows must all have the same length (expected " + this.width + ", got " + row.length + ")")
            }

            for (x in 0..<this.width) {
                val c = row[x]
                if (c != ' ' && !ingredients.containsKey(c)) {
                    throw RuntimeException("No item specified for symbol '$c'")
                }
            }
            shape[i] = row.intern()
            i++
        }
        results.add(primaryResult.clone()) //primaryResult
        results.addAll(extraResults) //extraResults
        this.shape = shape

        for ((key, item) in ingredients) {
            if (java.lang.String.join("", *this.shape).indexOf(key) < 0) {
                throw RuntimeException("Symbol does not appear in the shape: $key")
            }
            shapedIngredients.put(key, item)
            this.ingredients.add(item)
        }
    }

    val result: Item
        get() = results.first()

    fun setIngredient(key: String, item: Item): ShapedRecipe {
        return this.setIngredient(key[0], item)
    }

    fun setIngredient(key: Char, item: Item): ShapedRecipe {
        return this.setIngredient(key, DefaultDescriptor(item))
    }

    fun setIngredient(key: Char, item: ItemDescriptor): ShapedRecipe {
        if (java.lang.String.join("", *this.shape).indexOf(key) < 0) {
            throw RuntimeException("Symbol does not appear in the shape: $key")
        }
        shapedIngredients.put(key, item)

        val items: MutableList<ItemDescriptor> = ArrayList()
        var y = 0
        val y2 = height
        while (y < y2) {
            var x = 0
            val x2 = width
            while (x < x2) {
                items.add(getIngredient(x, y))
                ++x
            }
            ++y
        }
        ingredients.clear()
        ingredients.addAll(items)
        return this
    }

    /**
     * Gets ingredient with a row number and col number.
     *
     * @param x the col
     * @param y the row
     * @return the ingredient
     */
    fun getIngredient(x: Int, y: Int): ItemDescriptor {
        val shape = shape[y]
        val res = if (x < shape.length) shapedIngredients[shape[x]] else null
        return res ?: DefaultDescriptor(Item.AIR)
    }

    override fun match(input: Input): Boolean {
        var mirrorInput: Input? = null
        if (isMirror && input.col == 3 && input.row == 3) {
            mirrorInput = Input(3, 3, mirrorItemArray(input.data))
        }
        tryShrinkMatrix(input)

        var checkMirror = false
        next@ for (i in 0..<input.row) {
            for (j in 0..<input.col) {
                val ingredient = getIngredient(j, i)
                if (!ingredient.match(input.data[i][j])) {
                    if (mirrorInput != null) {
                        checkMirror = true
                        break@next
                    } else {
                        return false
                    }
                }
            }
        }
        if (checkMirror) {
            tryShrinkMatrix(mirrorInput!!)
            for (i in 0..<mirrorInput.row) {
                for (j in 0..<mirrorInput.col) {
                    val ingredient = getIngredient(j, i)
                    if (!ingredient.match(mirrorInput.data[i][j])) {
                        return false
                    }
                }
            }
        }
        return true
    }

    override val type: RecipeType
        get() = RecipeType.SHAPED

    companion object {
        private fun mirrorItemArray(data: Array<Array<Item>>): Array<Array<Item>> {
            val clone = Array(3) { i ->
                data[i]
            }
            var tmp: Item?
            for (i in 0..2) {
                tmp = clone[i][2]
                clone[i][2] = clone[i][0]
                clone[i][0] = tmp
            }
            return clone
        }

        /**
         * Try shrink the item matrix.This will remove air item that does not participate in the craft.
         *
         *
         * Example:
         * x represents air item, o represents valid items
         *
         *
         * <pre>
         * [              [
         * [x,x,x],
         * [x,o,o], =>    [o,o],
         * [x,o,o]        [o,o]
         * ]              ]
        </pre> *
         *
         * @param input the input
         */
        fun tryShrinkMatrix(input: Input) {
            val inputs = input.data
            var startRow = 0
            var endRow = inputs.size - 1
            for (row in inputs.indices) {
                if (notAllEmptyRow(inputs[row])) {
                    startRow = row
                    break
                }
                // 发现全部都是空气，直接返回空数组
                if (row == inputs.size - 1) {
                    input.col = (0)
                    input.row = (0)
                    input.data = (Input.Companion.EMPTY_INPUT_ARRAY)
                    return
                }
            }
            for (row in inputs.indices.reversed()) {
                if (notAllEmptyRow(inputs[row])) {
                    endRow = row
                    break
                }
            }
            var startColumn = 0
            var endColumn = inputs[0].size - 1
            for (column in inputs[0].indices) {
                if (notAllEmptyColumn(inputs, column)) {
                    startColumn = column
                    break
                }
            }
            for (column in inputs[0].indices.reversed()) {
                if (notAllEmptyColumn(inputs, column)) {
                    endColumn = column
                    break
                }
            }

            if (startRow == 0 && endRow == inputs.size - 1 && startColumn == 0 && endColumn == inputs[0].size - 1) {
                input.data = (inputs)
                return
            }
            val newRow = endRow - startRow + 1
            val newCol = endColumn - startColumn + 1
            val result = Array(newRow) { row ->
                Array(newCol) { col ->
                    inputs[startRow + row][startColumn + col]
                }
            }
            input.row = (newRow)
            input.col = (newCol)
            input.data = (result)
        }

        private fun notAllEmptyRow(inputs: Array<Item>): Boolean {
            for (item in inputs) {
                if (!item.isNothing) {
                    return true
                }
            }
            return false
        }

        private fun notAllEmptyColumn(inputs: Array<Array<Item>>, column: Int): Boolean {
            for (row in inputs) {
                if (!row[column].isNothing) {
                    return true
                }
            }
            return false
        }
    }
}
