package org.chorus_oss.chorus.dispenser

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.BlockDispenser
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.inventory.EntityInventoryHolder
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.math.Vector3
import java.util.*
import java.util.concurrent.ThreadLocalRandom


open class DefaultDispenseBehavior : DispenseBehavior {
    var success: Boolean = true


    override fun dispense(block: BlockDispenser, face: BlockFace, item: Item): Item? {
        val dispensePos = block.dispensePosition

        if (face.axis == BlockFace.Axis.Y) {
            dispensePos.y -= 0.125
        } else {
            dispensePos.y -= 0.15625
        }

        val rand: Random = ThreadLocalRandom.current()
        val motion = Vector3()

        val offset = rand.nextDouble() * 0.1 + 0.2

        motion.x = face.xOffset * offset
        motion.y = 0.20000000298023224
        motion.z = face.zOffset * offset

        motion.x += rand.nextGaussian() * 0.007499999832361937 * 6
        motion.y += rand.nextGaussian() * 0.007499999832361937 * 6
        motion.z += rand.nextGaussian() * 0.007499999832361937 * 6

        val clone = item.clone()
        clone.count = 1


        val dropPos = dispensePos.add(face.xOffset.toDouble(), face.yOffset.toDouble(), face.zOffset.toDouble())
        val bb: AxisAlignedBB = SimpleAxisAlignedBB(
            dropPos.x - 0.5,
            dropPos.y - 1,
            dropPos.z - 0.5,
            dropPos.x + 0.5,
            dropPos.y + 1,
            dropPos.z + 0.5
        )
        for (e in block.level.getNearbyEntities(bb)) {
            if (e is EntityInventoryHolder && e.canEquipByDispenser()) {
                if (clone.isHelmet && e.helmet.id == BlockID.AIR) {
                    e.setHelmet(clone)
                    return null
                } else if (clone.isChestplate && e.chestplate.id == BlockID.AIR) {
                    e.setChestplate(clone)
                    return null
                } else if (clone.isLeggings && e.leggings.id == BlockID.AIR) {
                    e.setLeggings(clone)
                    return null
                } else if (clone.isBoots && e.boots.id == BlockID.AIR) {
                    e.setBoots(clone)
                    return null
                } else if (e.itemInHand.id == BlockID.AIR) {
                    e.setItemInHand(clone, true)
                    return null
                }
            } else if (e is Player) {
                val armorInventory = e.inventory
                if (clone.isHelmet && armorInventory.helmet.id == BlockID.AIR) {
                    armorInventory.setHelmet(clone)
                    return null
                } else if (clone.isChestplate && armorInventory.chestplate.id == BlockID.AIR) {
                    armorInventory.setChestplate(clone)
                    return null
                } else if (clone.isLeggings && armorInventory.leggings.id == BlockID.AIR) {
                    armorInventory.setLeggings(clone)
                    return null
                } else if (clone.isBoots && armorInventory.boots.id == BlockID.AIR) {
                    armorInventory.setBoots(clone)
                    return null
                }
            }
        }
        block.level.dropItem(dispensePos, clone, motion)
        return null
    }

    private fun getParticleMetadataForFace(face: BlockFace): Int {
        return face.xOffset + 1 + (face.zOffset + 1) * 3
    }
}
