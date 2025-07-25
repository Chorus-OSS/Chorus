package org.chorus_oss.chorus.entity.ai.memory

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.ai.memory.codec.IMemoryCodec
import org.chorus_oss.chorus.utils.Identifier

sealed interface IMemoryType<Data> {
    val identifier: Identifier

    val codec: IMemoryCodec<Data>?

    fun encode(entity: Entity, data: Data)

    fun decode(entity: Entity): Data?

    fun forceEncode(entity: Entity, data: Any)

    fun withCodec(codec: IMemoryCodec<Data>): IMemoryType<Data>

    companion object {
        val PERSISTENT_MEMORIES = mutableSetOf<IMemoryType<*>>()
    }
}