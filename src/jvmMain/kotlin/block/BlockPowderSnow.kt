package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.projectile.EntitySmallFireball
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3

class BlockPowderSnow(blockState: BlockState = properties.defaultState) : BlockTransparent(blockState) {
    override val name: String
        get() = "Powder Snow"

    override val hardness: Double
        get() = 0.25

    override val resistance: Double
        get() = 0.1

    override val isSolid: Boolean
        get() = false

    override fun isSolid(side: BlockFace): Boolean {
        return false
    }

    override val isTransparent: Boolean
        get() = true

    override fun canPassThrough(): Boolean {
        return true
    }

    override fun getDrops(item: Item): Array<Item> {
        return Item.EMPTY_ARRAY
    }

    override fun onProjectileHit(projectile: Entity, locator: Locator, motion: Vector3): Boolean {
        if (projectile is EntitySmallFireball) {
            level.useBreakOn(this.position)
            return true
        }
        return false
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.POWDER_SNOW)
    }
}
