package org.chorus_oss.chorus.dispenser

import org.chorus_oss.chorus.block.BlockDispenser
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.item.EntityFireworksRocket
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.nbt.NBTIO

class FireworksDispenseBehavior : DefaultDispenseBehavior() {
    override fun dispense(block: BlockDispenser, face: BlockFace, item: Item): Item? {
        val opposite = face.getOpposite()
        val pos = block.getSide(face).position.add(
            0.5 + opposite.xOffset * 0.2,
            0.5 + opposite.yOffset * 0.2,
            0.5 + opposite.zOffset * 0.2
        )

        val nbt = Entity.getDefaultNBT(pos)
        nbt.putCompound("FireworkItem", NBTIO.putItemHelper(item))
        val firework = EntityFireworksRocket(block.level.getChunk(pos.chunkX, pos.chunkZ), nbt)
        firework.spawnToAll()

        return null
    }
}
