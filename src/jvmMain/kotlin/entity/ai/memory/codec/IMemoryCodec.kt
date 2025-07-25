package org.chorus_oss.chorus.entity.ai.memory.codec

import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import java.util.function.BiConsumer
import java.util.function.Function

/**
 * 记忆编解码器
 */
interface IMemoryCodec<Data> {
    /**
     * 获取记忆解码器，用于从CompoundTag读取持久化数据写入到实体记忆中
     *
     *
     * Obtain a memory decoder for reading persistent data from Compound Tag and writing it to entity memory
     *
     * @return the decoder
     */
    val decoder: Function<CompoundTag, Data?>

    /**
     * 获取记忆编码器，将实体记忆中的数据持久化进实体CompoundTag
     *
     *
     * Get the memory encoder to persist the data in the entity memory into the entity Compound Tag
     *
     * @return the encoder
     */
    val encoder: BiConsumer<Data, CompoundTag>

    /**
     * 从实体记忆初始化实体，可以用于初始化实体DataFlag.
     *
     *
     * Initialize the entity from the entity memory, which can be used to initialize the entity Data Flag.
     *
     * @param data   the data
     * @param entity the entity
     */
    fun init(data: Data, entity: EntityMob)

    fun decode(tag: CompoundTag): Data? {
        return decoder.apply(tag)
    }

    fun encode(data: Data, tag: CompoundTag) {
        encoder.accept(data, tag)
    }
}
