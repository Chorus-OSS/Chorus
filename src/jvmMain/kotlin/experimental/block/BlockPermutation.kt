package org.chorus_oss.chorus.experimental.block

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import org.chorus_oss.chorus.experimental.block.state.BlockState
import org.chorus_oss.chorus.experimental.utils.BlockStates
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import kotlin.collections.plus

class BlockPermutation internal constructor(
    val identifier: String,
    val states: Map<String, Any>,
    @PublishedApi
    internal val ecs: Entity,
) {
    val hash: Int = BlockStates.getHash(identifier, states)
    val tag: CompoundTag = BlockStates.getTag(identifier, states)

    fun <T : Any> getState(state: BlockState<T>): T? {
        return getState<T>(state.identifier)
    }

    @JvmName("getStateTyped")
    fun <T : Any> getState(identifier: String): T? {
        @Suppress("UNCHECKED_CAST")
        return getState(identifier) as? T?
    }

    fun getState(identifier: String): Any? {
        return this.states[identifier]
    }

    operator fun get(identifier: String): Any? = getState(identifier)

    operator fun <T : Any> get(state: BlockState<T>): T? = getState(state)

    fun <T : Any> withState(blockState: BlockState<T>, value: T): BlockPermutation {
        return withState(blockState.identifier, value)
    }

    fun withState(identifier: String, value: Any): BlockPermutation {
        return resolve(this.identifier, this.states + (identifier to value))
    }

    fun withStates(vararg states: Pair<BlockState<*>, Any>): BlockPermutation {
        return withStates(states.toMap())
    }

    fun withStates(states: Map<BlockState<*>, Any>): BlockPermutation {
        return withStates(states.entries.associate { it.key.identifier to it.value })
    }

    @JvmName("withStatesByIdentifier")
    fun withStates(vararg states: Pair<String, Any>): BlockPermutation {
        return withStates(states.toMap())
    }

    @JvmName("withStatesByIdentifier")
    fun withStates(states: Map<String, Any>): BlockPermutation {
        return resolve(this.identifier, this.states + states)
    }

    @JvmName("plusByIdentifier")
    operator fun plus(state: Pair<String, Any>): BlockPermutation = withStates(state)

    operator fun <T : Any> plus(state: Pair<BlockState<T>, T>) = withStates(state)

    @JvmName("plusByIdentifier")
    operator fun plus(states: Map<String, Any>) = withStates(states)

    operator fun plus(states: Map<BlockState<*>, Any>) = withStates(states)

    inline fun <reified T : Component<*>> getComponent(type: ComponentType<T>): T? {
        with(BlockRegistry.ECS) {
            return ecs.getOrNull(type)
        }
    }

    inline operator fun <reified T : Component<*>> get(type: ComponentType<T>): T? = getComponent(type)

    fun hasComponent(type: ComponentType<*>): Boolean {
        with(BlockRegistry.ECS) {
            return type in ecs
        }
    }

    operator fun contains(type: ComponentType<*>): Boolean = hasComponent(type)

    infix fun has(type: ComponentType<*>): Boolean = hasComponent(type)

    companion object {
        fun resolve(identifier: String, states: Map<String, Any>? = null): BlockPermutation {
            val default = requireNotNull(BlockRegistry[identifier]) {
                "BlockDefinition not registered for \"$identifier\""
            }

            if (states.isNullOrEmpty()) return default

            val finalHash = states.entries.fold(default.hash) { _, (key, value) ->
                val expected = requireNotNull(default.states[key]) {
                    "BlockState \"$value\" is invalid for \"${default.identifier}\""
                }

                require(expected::class == value::class) {
                    "Value \"$value\" is invalid for BlockState \"$key\""
                }

                val hash = BlockStates.getHash(default.identifier, default.states + (key to value))
                require(hash in BlockRegistry) {
                    "Value \"$value\" is invalid for BlockState \"$key\""
                }

                hash
            }

            return requireNotNull(BlockRegistry[finalHash]) { "Unreachable" }
        }
    }
}
