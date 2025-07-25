package org.chorus_oss.chorus.entity.mob.water_animal

import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag

abstract class EntityWaterAnimal(chunk: IChunk?, nbt: CompoundTag) : EntityMob(chunk, nbt)
