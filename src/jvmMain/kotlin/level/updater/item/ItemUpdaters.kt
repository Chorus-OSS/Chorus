package org.chorus_oss.chorus.level.updater.item

import org.chorus_oss.chorus.level.updater.Updater
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext
import org.chorus_oss.chorus.nbt.tag.CompoundTag

import java.util.function.Consumer

object ItemUpdaters {
    private val CONTEXT: CompoundTagUpdaterContext
    val latestVersion: Int

    init {
        val updaters: MutableList<Updater> = ArrayList()
        updaters.add(ItemUpdater_1_20_60.INSTANCE)
        updaters.add(ItemUpdater_1_20_70.INSTANCE)
        updaters.add(ItemUpdater_1_20_80.INSTANCE)
        updaters.add(ItemUpdater_1_21_0.INSTANCE)
        updaters.add(ItemUpdater_1_21_20.INSTANCE)
        updaters.add(ItemUpdater_1_21_30.INSTANCE)
        updaters.add(ItemUpdater_1_21_40.INSTANCE)

        val context = CompoundTagUpdaterContext()
        updaters.forEach(Consumer { updater: Updater -> updater.registerUpdaters(context) })
        CONTEXT = context
        latestVersion = context.latestVersion
    }

    @JvmStatic
    fun updateItem(tag: CompoundTag, version: Int): CompoundTag {
        return CONTEXT.update(tag, version)
    }
}
