package org.chorus_oss.chorus.experimental.generator

import org.chorus_oss.chorus.utils.Loggable

object BlockDefinitionGeneratorUtils : Loggable {
    fun minimize(conditions: List<List<Pair<String, Any>>>): List<List<Pair<String, Any>>> {
        var input = conditions

        val final = mutableListOf<List<Pair<String, Any>>>()

        var i = 0
        while (true) {
            i++
            if (input.all { it.size < i }) break

            val discriminants = input.filter { it.size == i }
            if (discriminants.isEmpty()) continue

            val used = discriminants.associateWith { d ->
                input.filter {
                    it.containsAll(d)
                }
            }

            val unused = input.filterNot { i ->
                discriminants.any {
                    i.containsAll(it)
                }
            }

            val flattened = used.entries.associate { (d, u) ->
                d to u.map { c ->
                    c.filterNot { d.contains(it) }
                }
            }

            val minimized = flattened.entries.associate { (d, u) ->
                d to if (u.any { it.isEmpty() }) emptyList() else u
            }

            final += minimized.entries.flatMap { (d, u) ->
                if (u.isEmpty()) {
                    listOf(
                        d
                    )
                } else {
                    u.map { c ->
                        d + c
                    }
                }
            }

            i = 0
            input = unused
        }

        return final
    }
}