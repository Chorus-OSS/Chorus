package org.chorus_oss.chorus.experimental.block.state

import kotlin.reflect.KClass

data class BlockState<T : Any>(
    val identifier: String, val values: List<T>
) : Iterable<T> by values {
    init {
        require(values.distinct() == values) {
            "BlockState \"$identifier\" must have no duplicate values"
        }
        require(values.size >= 2) {
            "BlockState \"$identifier\" must have at least 2 values"
        }
        require(values.size <= 16 || identifier in ignoreLimitFor) {
            "BlockState \"$identifier\" must have at most 16 values"
        }
        require(values.all { it is Boolean } || values.all { it is Int } || values.all { it is String }) {
            "BlockState \"$identifier\" must have all Boolean, Int, or String values"
        }
    }

    val defaultValue: T
        get() = values.first()

    companion object {
        internal val ignoreLimitFor = listOf(
            "books_stored",
            "growing_plant_age",
            "kelp_age",
            "multi_face_direction_bits",
            "twisting_vines_age",
            "weeping_vines_age"
        )

        fun from(identifier: String, range: IntRange): BlockState<Int> {
            return BlockState(identifier, range.toList())
        }

        fun from(identifier: String, max: Int): BlockState<Int> {
            return from(identifier, 0, max)
        }

        fun from(identifier: String, min: Int, max: Int): BlockState<Int> {
            return from(identifier, min..max)
        }

        fun from(identifier: String, default: Boolean = false): BlockState<Boolean> {
            return BlockState(identifier, listOf(default, !default))
        }

        fun <T : Enum<T>> from(identifier: String, enumClass: Class<T>): BlockState<String> {
            return from(identifier, enumClass.enumConstants.toList())
        }

        fun <T : Enum<T>> from(identifier: String, enumClass: KClass<T>): BlockState<String> {
            return from(identifier, enumClass.java)
        }

        fun <T : Enum<T>> from(identifier: String, enumValues: List<T>): BlockState<String> {
            return BlockState(identifier, enumValues.map { it.name.lowercase() })
        }
    }
}
