package org.chorus_oss.chorus.dispenser

import org.chorus_oss.chorus.block.BlockDispenser
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.projectile.EntityProjectile
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag


open class ProjectileDispenseBehavior(protected val entityType: String) : DefaultDispenseBehavior() {
    override fun dispense(source: BlockDispenser, face: BlockFace, item: Item): Item? {
        val dispensePos = source.dispensePosition

        val nbt = Entity.getDefaultNBT(dispensePos)
        this.correctNBT(nbt, item)

        val projectile = Entity.createEntity(
            entityType,
            source.level.getChunk(dispensePos.chunkX, dispensePos.chunkZ),
            nbt
        ) as? EntityProjectile
            ?: return super.dispense(source, face, item)

        val motion = initMotion(face)

        projectile.setMotion(motion)
        projectile.inaccurate(accuracy)
        projectile.setMotion(projectile.getMotion().multiply(this.motion))

        projectile.updateRotation()

        projectile.spawnToAll()

        source.level.addSound(source.position, shootingSound)

        return null
    }

    protected open val shootingSound: Sound
        get() = Sound.RANDOM_BOW

    protected open fun initMotion(face: BlockFace): Vector3 {
        return Vector3(face.xOffset.toDouble(), (face.yOffset + 0.1f).toDouble(), face.zOffset.toDouble())
            .normalize()
    }

    protected open val motion: Double
        get() = 1.1

    protected open val accuracy: Float
        get() = 6f

    /**
     * you can add extra data of projectile here
     *
     * @param nbt tag
     */
    protected fun correctNBT(nbt: CompoundTag, item: Item? = null) {
        if (item != null) {
            if (item.id === ItemID.SPLASH_POTION || item.id === ItemID.LINGERING_POTION) {
                nbt.putInt("PotionId", item.damage)
            }
        }
    }
}
