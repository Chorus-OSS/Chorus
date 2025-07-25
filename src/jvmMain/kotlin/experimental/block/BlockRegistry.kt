package org.chorus_oss.chorus.experimental.block

import com.github.quillraven.fleks.configureWorld
import org.chorus_oss.chorus.experimental.block.generated.definitions

object BlockRegistry {
    @PublishedApi
    internal val ECS = configureWorld {  }

    /** Identifier -> Definition */
    private val defaults = mutableMapOf<String, BlockPermutation>()

    /** Hash -> Permutation */
    private val permutations = mutableMapOf<Int, BlockPermutation>()

    init {
        this += definitions
    }

    fun register(vararg definitions: BlockDefinition) = definitions.forEach(::register)

    fun register(definitions: Iterable<BlockDefinition>) = definitions.forEach(::register)

    fun register(definition: BlockDefinition) {
        require(!defaults.contains(definition.identifier)) {
            "BlockDefinition already registered for \"${definition.identifier}\""
        }

        defaults[definition.identifier] = definition.states.fold(listOf(emptyMap<String, Any>())) { acc, state ->
            acc.flatMap { map ->
                state.values.map {
                    map + (state.identifier to it)
                }
            }
        }.map { state ->
            val ecsEntity = ECS.entity { entity ->
                entity += definition.components + definition.permutations
                    .filter { it.condition(state) }
                    .flatMap { it.components }
            }
            
            BlockPermutation(
                definition.identifier,
                state,
                ecsEntity
            ).also { permutations[it.hash] = it }
        }.first()
    }

    operator fun plusAssign(definition: BlockDefinition) = definition.let(::register)

    operator fun plusAssign(definitions: Iterable<BlockDefinition>) = definitions.let(::register)

    operator fun plus(definition: BlockDefinition) = definition.let(::register)

    operator fun plus(definitions: Iterable<BlockDefinition>) = definitions.let(::register)

    operator fun get(identifier: String): BlockPermutation? = defaults[identifier]

    operator fun get(hash: Int): BlockPermutation? = permutations[hash]

    operator fun contains(identifier: String): Boolean = identifier in defaults

    operator fun contains(hash: Int): Boolean = hash in permutations
}