package org.chorus_oss.chorus.level.format.leveldb

import org.chorus_oss.chorus.nbt.tag.*
import org.chorus_oss.chorus.registry.Registries

object BDSEntityTranslator {
    fun translate(from: CompoundTag): CompoundTag? {
        val linkedCompoundTag = LinkedCompoundTag()
        if (from.contains("identifier")) {
            val identifier = from.getString("identifier")
            val entityNetworkId = Registries.ENTITY.getEntityNetworkId(identifier)
            if (entityNetworkId == 0) return null
            linkedCompoundTag.putString("identifier", identifier)
        }
        if (from.containsList("Pos", Tag.TAG_DOUBLE)) {
            val pos = from.getList("Pos", DoubleTag::class.java)
            val target = ListTag<FloatTag>()
            for (v in pos.all) {
                target.add(FloatTag(v.data))
            }
            linkedCompoundTag.putList("Pos", target)
        } else {
            val target = ListTag<FloatTag>()
            target.add(FloatTag(0f))
            target.add(FloatTag(0f))
            target.add(FloatTag(0f))
            linkedCompoundTag.putList("Pos", target)
        }
        if (from.containsList("Motion", Tag.TAG_DOUBLE)) {
            val pos = from.getList("Motion", DoubleTag::class.java)
            val target = ListTag<FloatTag>()
            for (v in pos.all) {
                target.add(FloatTag(v.data))
            }
            from.putList("Motion", target)
            linkedCompoundTag.putList("Pos", target)
        } else {
            val target = ListTag<FloatTag>()
            target.add(FloatTag(0f))
            target.add(FloatTag(0f))
            target.add(FloatTag(0f))
            linkedCompoundTag.putList("Motion", target)
        }
        linkedCompoundTag.putList("Rotation", from.getList("Rotation"))
        return linkedCompoundTag
    }
}
