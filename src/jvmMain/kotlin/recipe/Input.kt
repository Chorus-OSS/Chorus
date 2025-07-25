package org.chorus_oss.chorus.recipe

import org.chorus_oss.chorus.item.Item

/**
 * craft input,Upper left is origin point (0,0)
 */
class Input(
    var col: Int, var row: Int,
    /**
     * The item matrix
     *
     *
     * Each array element in the array represents a row of items in the craft table
     */
    var data: Array<Array<Item>>
) {
    fun canConsumerItemCount(): Int {
        var count = 0
        for (d in data) {
            for (item in d) {
                if (!item.isNothing) {
                    count++
                }
            }
        }
        return count
    }

    val flatItems: Array<Item>
        get() {
            return Array(col * row) { i ->
                val c = i % col
                val r = i / col
                data[r][c]
            }
        }

    companion object {
        val EMPTY_INPUT_ARRAY: Array<Array<Item>> = emptyArray()
    }
}
