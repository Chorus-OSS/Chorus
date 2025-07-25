package org.chorus_oss.chorus.level.updater.util.tagupdater

import org.chorus_oss.chorus.level.updater.util.TagUtils
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class CompoundTagUpdaterContext {
    private val updaters: MutableList<CompoundTagUpdater> = ArrayList()

    @JvmOverloads
    fun addUpdater(
        major: Int,
        minor: Int,
        patch: Int,
        resetVersion: Boolean = false,
        bumpVersion: Boolean = true
    ): CompoundTagUpdater.Builder {
        var version = makeVersion(major, minor, patch)
        val prevUpdater = this.latestUpdater

        var updaterVersion: Int
        if (resetVersion || prevUpdater == null || baseVersion(prevUpdater.version) != version) {
            updaterVersion = 0
        } else {
            updaterVersion = updaterVersion(prevUpdater.version)
            if (bumpVersion) {
                updaterVersion++
            }
        }
        version = mergeVersions(version, updaterVersion)

        val updater = CompoundTagUpdater(version)
        updaters.add(updater)
        updaters.sort()
        return updater.builder()
    }

    fun update(tag: CompoundTag, version: Int): CompoundTag {
        val updated = this.updateStates0(tag, version)

        if (updated == null && version != this.latestVersion) {
            tag.putInt("version", this.latestVersion)
            return tag
        } else if (updated == null) {
            return tag
        } else {
            updated["version"] = latestVersion
            return TagUtils.toImmutable(updated) as CompoundTag
        }
    }

    fun updateStates(tag: CompoundTag, version: Int): CompoundTag {
        val updated: Map<String, Any?>? = this.updateStates0(tag, version)
        return if (updated == null) tag else TagUtils.toImmutable(updated) as CompoundTag
    }

    private fun updateStates0(tag: CompoundTag, version: Int): MutableMap<String, Any?>? {
        var mutableTag: MutableMap<String, Any?>? = null
        var updated = false
        for (updater in updaters) {
            if (updater.version < version) {
                continue
            }

            if (mutableTag == null) {
                mutableTag = TagUtils.toMutable(tag) as MutableMap<String, Any?>
            }
            updated = updated or updater.update(mutableTag)
        }

        if (mutableTag == null || !updated) {
            return null
        }
        return mutableTag
    }

    private val latestUpdater: CompoundTagUpdater?
        get() = if (updaters.isEmpty()) null else updaters[updaters.size - 1]

    val latestVersion: Int
        get() {
            val updater = this.latestUpdater
            return updater?.version ?: 0
        }

    companion object {
        private fun mergeVersions(baseVersion: Int, updaterVersion: Int): Int {
            return updaterVersion or baseVersion
        }

        private fun baseVersion(version: Int): Int {
            return version and -0x100
        }

        fun updaterVersion(version: Int): Int {
            return version and 0x000000FF
        }

        fun makeVersion(major: Int, minor: Int, patch: Int): Int {
            return (patch shl 8) or (minor shl 16) or (major shl 24)
        }
    }
}
