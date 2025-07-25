package org.chorus_oss.chorus.dispenser

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.item.EntityChestBoat
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemChestBoat
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.Vector3

class ChestBoatDispenseBehavior : BoatDispenseBehavior() {
    override fun spawnBoatEntity(level: Level, pos: Vector3, item: Item) {
        val boat = EntityChestBoat(
            level.getChunk(pos.chunkX, pos.chunkZ),
            Entity.getDefaultNBT(pos)
                .putInt("Variant", (item as ItemChestBoat).boatId)
        )
        boat.spawnToAll()
    }
}
