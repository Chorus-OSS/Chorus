package org.chorus_oss.chorus.entity.ai.memory.codec

import java.util.function.BiConsumer
import java.util.function.Function


class StringMemoryCodec(key: String) : MemoryCodec<String>(
    Function { tag ->
        if (tag.contains(key))
            tag.getString(key)
        else null
    },
    BiConsumer { data, tag ->
        tag.putString(key, data)
    }
)
