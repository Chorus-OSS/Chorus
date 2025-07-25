package org.chorus_oss.chorus.entity.mob.villagers

import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.entity.mob.monster.humanoid_monster.EntityDrowned
import org.chorus_oss.chorus.entity.mob.monster.humanoid_monster.EntityZombie
import org.chorus_oss.chorus.entity.mob.monster.humanoid_monster.EntityZombieVillager
import org.chorus_oss.chorus.entity.projectile.abstract_arrow.EntityThrownTrident
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag

class EntityVillager(chunk: IChunk?, nbt: CompoundTag) : EntityMob(chunk, nbt) {
    override fun getEntityIdentifier(): String {
        return EntityID.VILLAGER
    }

    override fun getWidth(): Float {
        if (this.isBaby()) {
            return 0.3f
        }
        return 0.6f
    }

    override fun getHeight(): Float {
        if (this.isBaby()) {
            return 0.95f
        }
        return 1.9f
    }

    override fun getOriginalName(): String {
        return "Villager"
    }

    public override fun initEntity() {
        this.maxHealth = 20
        super.initEntity()

        if (!namedTag!!.contains("Profession")) {
            this.setProfession(PROFESSION_GENERIC)
        }
    }

    fun getProfession(): Int {
        return namedTag!!.getInt("Profession")
    }

    fun setProfession(profession: Int) {
        namedTag!!.putInt("Profession", profession)
    }

    override fun attack(source: EntityDamageEvent): Boolean {
        if (health - source.finalDamage <= 1) {
            if (source is EntityDamageByEntityEvent) {
                if (source.damager is EntityThrownTrident) {
                    if (source.damager.shootingEntity is EntityDrowned) {
                        transform()
                        return true
                    }
                } else if (source.damager is EntityZombie) {
                    transform()
                    return true
                }
            }
        }
        return super.attack(source)
    }

    private fun transform() {
        this.close()
        val zombieVillager = EntityZombieVillager(this.locator.chunk, this.namedTag)
        zombieVillager.setPosition(this.position)
        zombieVillager.setRotation(rotation.yaw, rotation.pitch)
        zombieVillager.spawnToAll()
    }

    override fun getExperienceDrops(): Int {
        return 0
    }

    companion object {
        const val PROFESSION_FARMER: Int = 0
        const val PROFESSION_LIBRARIAN: Int = 1
        const val PROFESSION_PRIEST: Int = 2
        const val PROFESSION_BLACKSMITH: Int = 3
        const val PROFESSION_BUTCHER: Int = 4
        const val PROFESSION_GENERIC: Int = 5
    }
}
