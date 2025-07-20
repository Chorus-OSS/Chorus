package org.chorus_oss.chorus.utils

import org.chorus_oss.chorus.nbt.tag.IntTag
import org.chorus_oss.chorus.nbt.tag.ListTag

@JvmRecord
data class SemVersion(val major: Int, val minor: Int, val patch: Int, val revision: Int, val build: Int) {
    fun toTag(): ListTag<IntTag> {
        val tag = ListTag<IntTag>()
        tag.add(IntTag(major))
        tag.add(IntTag(minor))
        tag.add(IntTag(patch))
        tag.add(IntTag(revision))
        tag.add(IntTag(build))
        return tag
    }

    companion object {
        fun from(versions: ListTag<IntTag>): SemVersion {
            if (versions.size() == 0) {
                return SemVersion(0, 0, 0, 0, 0)
            }
            check(versions.size() == 5)

            return SemVersion(
                versions[0].data,
                versions[1].data,
                versions[2].data,
                versions[3].data,
                versions[4].data
            )
        }
    }
}
