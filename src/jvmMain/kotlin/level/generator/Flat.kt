package org.chorus_oss.chorus.level.generator

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.level.DimensionData
import org.chorus_oss.chorus.level.generator.stages.FinishedStage
import org.chorus_oss.chorus.level.generator.stages.FlatGenerateStage
import org.chorus_oss.chorus.level.generator.stages.LightPopulationStage
import org.chorus_oss.chorus.registry.Registries


class Flat(dimensionData: DimensionData, options: Map<String?, Any>?) :
    Generator(dimensionData, options) {
    override fun stages(builder: GenerateStage.Builder) {
        builder.start(Registries.GENERATE_STAGE.get(FlatGenerateStage.NAME))
        if (Server.instance.settings.chunkSettings.lightUpdates) {
            builder.next(Registries.GENERATE_STAGE.get(LightPopulationStage.NAME))
        }
        builder.next(Registries.GENERATE_STAGE.get(FinishedStage.NAME))
    }

    override val name: String
        get() = "flat"
}
