package org.chorus_oss.chorus.entity.ai.memory

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.ai.memory.codec.IMemoryCodec
import org.chorus_oss.chorus.utils.Identifier
import java.util.function.Supplier

class MemoryType<Data : Any>(
    override val identifier: Identifier,
    private val defaultDataSupplier: Supplier<Data>
) : IMemoryType<Data> {
    override var codec: IMemoryCodec<Data>? = null
        private set

    constructor(identifier: Identifier, defaultData: Data) : this(
        identifier,
        Supplier { defaultData }
    )

    constructor(identifier: String, defaultData: Data) : this(
        Identifier(identifier),
        Supplier { defaultData }
    )

    constructor(identifier: String, defaultData: Supplier<Data>) : this(
        Identifier(identifier),
        defaultData
    )

    fun getDefaultData(): Data {
        return defaultDataSupplier.get()
    }

    override fun withCodec(codec: IMemoryCodec<Data>): MemoryType<Data> {
        this.codec = codec
        IMemoryType.PERSISTENT_MEMORIES.add(this)
        return this
    }

    override fun encode(entity: Entity, data: Data) {
        codec?.encode(data, entity.namedTag!!)
    }

    override fun decode(entity: Entity): Data? {
        return codec?.decode(entity.namedTag!!)
    }

    @Suppress("UNCHECKED_CAST")
    override fun forceEncode(entity: Entity, data: Any) {
        codec?.encode(data as Data, entity.namedTag!!)
    }

    override fun hashCode(): Int {
        return identifier.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is MemoryType<*>) {
            return identifier == other.identifier
        }
        return false
    }
}
