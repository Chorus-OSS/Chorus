package org.chorus_oss.chorus.entity.ai.memory.codec

import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.nbt.tag.CompoundTag

import java.util.function.BiConsumer
import java.util.function.Function


open class MemoryCodec<Data>(
    override val decoder: Function<CompoundTag, Data?>,
    override val encoder: BiConsumer<Data, CompoundTag>
) : IMemoryCodec<Data> {
    private var onInit: BiConsumer<Data, EntityMob>? = null

    /**
     * BiConsumer<Data></Data>, EntityMob> Data can be Null
     */
    fun onInit(onInit: BiConsumer<Data, EntityMob>?): MemoryCodec<Data> {
        this.onInit = onInit
        return this
    }

    override fun init(data: Data, entity: EntityMob) {
        if (onInit != null) {
            onInit!!.accept(data, entity)
        }
    }
}
