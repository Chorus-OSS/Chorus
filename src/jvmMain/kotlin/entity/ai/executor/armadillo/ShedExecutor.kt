package org.chorus_oss.chorus.entity.ai.executor.armadillo

import org.chorus_oss.chorus.entity.ai.executor.EntityControl
import org.chorus_oss.chorus.entity.ai.executor.IBehaviorExecutor
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.utils.Utils

class ShedExecutor : EntityControl, IBehaviorExecutor {
    override fun execute(entity: EntityMob): Boolean {
        return false
    }

    override fun onStart(entity: EntityMob) {
        entity.level!!.dropItem(entity.position, Item.get(ItemID.ARMADILLO_SCUTE))
        entity.level!!.addSound(entity.position, Sound.MOB_ARMADILLO_SCUTE_DROP)
        entity.memoryStorage.set(CoreMemoryTypes.NEXT_SHED, entity.level!!.tick + Utils.rand(6000, 10800))
    }
}
