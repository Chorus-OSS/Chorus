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
        updaters.add(BlockStateUpdaterBase.INSTANCE)
        updaters.add(BlockStateUpdater_1_10_0.INSTANCE)
        updaters.add(BlockStateUpdater_1_12_0.INSTANCE)
        updaters.add(BlockStateUpdater_1_13_0.INSTANCE)
        updaters.add(BlockStateUpdater_1_14_0.INSTANCE)
        updaters.add(BlockStateUpdater_1_15_0.INSTANCE)
        updaters.add(BlockStateUpdater_1_16_0.INSTANCE)
        updaters.add(BlockStateUpdater_1_16_210.INSTANCE)
        updaters.add(BlockStateUpdater_1_17_30.INSTANCE)
        updaters.add(BlockStateUpdater_1_17_40.INSTANCE)
        updaters.add(BlockStateUpdater_1_18_10.INSTANCE)
        updaters.add(BlockStateUpdater_1_18_30.INSTANCE)
        updaters.add(BlockStateUpdater_1_19_0.INSTANCE)
        updaters.add(BlockStateUpdater_1_19_20.INSTANCE)
        updaters.add(BlockStateUpdater_1_19_70.INSTANCE)
        updaters.add(BlockStateUpdater_1_19_80.INSTANCE)
        updaters.add(BlockStateUpdater_1_20_0.INSTANCE)
        updaters.add(BlockStateUpdater_1_20_10.INSTANCE)
        updaters.add(BlockStateUpdater_1_20_30.INSTANCE)
        updaters.add(BlockStateUpdater_1_20_40.INSTANCE)
        updaters.add(BlockStateUpdater_1_20_50.INSTANCE)
        updaters.add(BlockStateUpdater_1_20_60.INSTANCE)
        updaters.add(BlockStateUpdater_1_20_70.INSTANCE)
        updaters.add(BlockStateUpdater_1_20_80.INSTANCE)
        updaters.add(BlockStateUpdater_1_21_0.INSTANCE)
        updaters.add(BlockStateUpdater_1_21_10.INSTANCE)
        updaters.add(BlockStateUpdater_1_21_20.INSTANCE)
        updaters.add(BlockStateUpdater_1_21_30.INSTANCE)
        updaters.add(BlockStateUpdater_1_21_40.INSTANCE)
        updaters.add(BlockStateUpdater_1_21_60.INSTANCE)

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
