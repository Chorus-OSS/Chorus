package org.chorus_oss.chorus.dispenser

import org.chorus_oss.chorus.block.BlockDispenser
import org.chorus_oss.chorus.block.BlockFlowingWater
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.item.EntityBoat
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3

open class BoatDispenseBehavior : DefaultDispenseBehavior() {
    override fun dispense(block: BlockDispenser, face: BlockFace, item: Item): Item? {
        val pos = block.getSide(face).position.multiply(1.125)

        val target = block.getSide(face)

        if (target is BlockFlowingWater) {
            pos.y += 1.0
        } else if (!target.isAir || target.down() !is BlockFlowingWater) {
            return super.dispense(block, face, item)
        }

        spawnBoatEntity(
            block.level,
            target.position.add(face.xOffset * 0.75, face.yOffset * 0.75, face.zOffset * 0.75),
            item
        )

        return null
    }

    protected open fun spawnBoatEntity(level: Level, pos: Vector3, item: Item) {
        val boat = EntityBoat(
            level.getChunk(pos.chunkX, pos.chunkZ),
            Entity.getDefaultNBT(pos)
                .putInt("Variant", item.damage)
        )
        boat.spawnToAll()
    }
}
