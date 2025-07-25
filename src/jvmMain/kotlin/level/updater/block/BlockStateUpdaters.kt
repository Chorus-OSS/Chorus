package org.chorus_oss.chorus.level.updater.block

import org.chorus_oss.chorus.level.updater.Updater
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import java.util.function.Consumer

object BlockStateUpdaters {
    private val CONTEXT: CompoundTagUpdaterContext
    val latestVersion: Int

    init {
        val updaters: MutableList<Updater> = ArrayList()
        updaters.add(BlockStateUpdaterBase.Companion.INSTANCE)
        updaters.add(BlockStateUpdater_1_10_0.Companion.INSTANCE)
        updaters.add(BlockStateUpdater_1_12_0.Companion.INSTANCE)
        updaters.add(BlockStateUpdater_1_13_0.Companion.INSTANCE)
        updaters.add(BlockStateUpdater_1_14_0.Companion.INSTANCE)
        updaters.add(BlockStateUpdater_1_15_0.Companion.INSTANCE)
        updaters.add(BlockStateUpdater_1_16_0.Companion.INSTANCE)
        updaters.add(BlockStateUpdater_1_16_210.Companion.INSTANCE)
        updaters.add(BlockStateUpdater_1_17_30.Companion.INSTANCE)
        updaters.add(BlockStateUpdater_1_17_40.Companion.INSTANCE)
        updaters.add(BlockStateUpdater_1_18_10.Companion.INSTANCE)
        updaters.add(BlockStateUpdater_1_18_30.Companion.INSTANCE)
        updaters.add(BlockStateUpdater_1_19_0.Companion.INSTANCE)
        updaters.add(BlockStateUpdater_1_19_20.Companion.INSTANCE)
        updaters.add(BlockStateUpdater_1_19_70.Companion.INSTANCE)
        updaters.add(BlockStateUpdater_1_19_80.Companion.INSTANCE)
        updaters.add(BlockStateUpdater_1_20_0.Companion.INSTANCE)
        updaters.add(BlockStateUpdater_1_20_10.Companion.INSTANCE)
        updaters.add(BlockStateUpdater_1_20_30.Companion.INSTANCE)
        updaters.add(BlockStateUpdater_1_20_40.Companion.INSTANCE)
        updaters.add(BlockStateUpdater_1_20_50.Companion.INSTANCE)
        updaters.add(BlockStateUpdater_1_20_60.Companion.INSTANCE)
        updaters.add(BlockStateUpdater_1_20_70.Companion.INSTANCE)
        updaters.add(BlockStateUpdater_1_20_80.Companion.INSTANCE)
        updaters.add(BlockStateUpdater_1_21_0.Companion.INSTANCE)
        updaters.add(BlockStateUpdater_1_21_10.Companion.INSTANCE)
        updaters.add(BlockStateUpdater_1_21_20.Companion.INSTANCE)
        updaters.add(BlockStateUpdater_1_21_30.Companion.INSTANCE)
        updaters.add(BlockStateUpdater_1_21_40.Companion.INSTANCE)
        updaters.add(BlockStateUpdater_1_21_60.Companion.INSTANCE)

        val context = CompoundTagUpdaterContext()
        updaters.forEach(Consumer { updater: Updater -> updater.registerUpdaters(context) })
        CONTEXT = context
        latestVersion = context.latestVersion
    }

    @JvmStatic
    fun updateBlockState(tag: CompoundTag, version: Int): CompoundTag {
        return CONTEXT.update(tag, version)
    }

    fun serializeCommon(builder: MutableMap<String?, Any?>, id: String?) {
        builder["version"] = latestVersion
        builder["name"] = id
    }
}
