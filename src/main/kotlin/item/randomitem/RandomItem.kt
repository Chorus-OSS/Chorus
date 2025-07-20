package org.chorus_oss.chorus.item.randomitem

import java.util.*

object RandomItem {
    private val selectors: MutableMap<Selector, Float> = HashMap()

    val ROOT: Selector = Selector(null)

    @JvmOverloads
    fun putSelector(selector: Selector, chance: Float = 1f): Selector {
        if (selector.parent == null) selector.setParent(ROOT)
        selectors[selector] = chance
        return selector
    }

    fun selectFrom(selector: Selector?): Any? {
        requireNotNull(selector)
        val child: MutableMap<Selector, Float> = HashMap()
        selectors.forEach { (s, f) ->
            if (s.parent === selector) child[s] =
                f
        }
        if (child.size == 0) return selector!!.select()
        return selectFrom(Selector.Companion.selectRandom(child))
    }
}
