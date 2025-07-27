package org.chorus_oss.chorus.experimental.generator

import org.chorus_oss.chorus.utils.Loggable

object BlockDefinitionGeneratorUtils : Loggable {
    fun minimize(conditions: List<List<Pair<String, Any>>>): List<List<Pair<String, Any>>> {
        val discriminants = conditions.filter { it.size == 1 }.map { it.first() }

        val uses = discriminants.associateWith { d ->
            conditions.filter {
                it.contains(d)
            }
        }

        val flattened = uses.entries.associate { (d, u) ->
            d to u.map { c ->
                c.filter { it != d }
            }
        }

        val minimized = flattened.entries.associate { (d, u) ->
            d to if (u.any { it.isEmpty() }) emptyList() else u
        }

        val final = minimized.entries.flatMap { (d, u) ->
            if (u.isEmpty()) {
                listOf(
                    listOf(d)
                )
            } else {
                u.map { c ->
                    listOf(d) + c
                }
            }
        }

        return final
    }
}