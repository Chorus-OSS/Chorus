package org.chorus_oss.chorus.entity

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag

abstract class EntityCreature(chunk: IChunk?, nbt: CompoundTag?) : EntityLiving(chunk, nbt), EntityNameable,
    EntityAgeable {
    override fun onInteract(player: Player, item: Item, clickedPos: Vector3): Boolean {
        return super<EntityNameable>.onInteract(player, item, clickedPos)
    }
}
