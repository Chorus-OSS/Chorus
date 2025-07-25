package org.chorus_oss.chorus.experimental.block

import org.chorus_oss.chorus.experimental.block.state.BlockState
import org.chorus_oss.chorus.utils.Identifier

// TODO: MenuCategory

abstract class BlockDefinition(
    val identifier: String,
    val states: List<BlockState<*>> = emptyList(),
    val components: List<BlockComponent<*>> = emptyList(),
    val permutations: List<Permutation> = emptyList(),
) {
    init {
        Identifier.assertValid(identifier)
        require(states.fold(1) { acc, state -> acc * state.values.size } <= 65536) {
            "BlockDefinition \"$identifier\" exceeds the permutation limit"
        }
    }

    val default: BlockPermutation
        get() = BlockPermutation.resolve(this.identifier)

    data class Permutation(
        val condition: (Map<String, Any>) -> Boolean,
        val components: List<BlockComponent<*>>,
    ) {
        constructor(condition: (Map<String, Any>) -> Boolean, vararg components: BlockComponent<*>) : this(condition, components.toList())

        constructor(components: List<BlockComponent<*>>, condition: (Map<String, Any>) -> Boolean) : this(condition, components)

        constructor(vararg components: BlockComponent<*>, condition: (Map<String, Any>) -> Boolean) : this(condition, components.toList())
    }
}