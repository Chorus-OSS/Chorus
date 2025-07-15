package org.chorus_oss.chorus.entity.ai.executor

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.EntityCanAttack
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageModifier
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.math.Vector3f
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.network.protocol.LevelEventGenericPacket
import org.chorus_oss.chorus.network.protocol.LevelEventPacket
import java.util.*

class WardenRangedAttackExecutor(protected var chargingTime: Int, protected var totalRunningTime: Int) :
    IBehaviorExecutor {
    protected var currentTick: Int = 0

    override fun execute(entity: EntityMob): Boolean {
        currentTick++
        if (entity.memoryStorage.isEmpty(CoreMemoryTypes.ATTACK_TARGET)) return false
        if (currentTick == this.chargingTime) {
            val target = entity.memoryStorage.get(CoreMemoryTypes.ATTACK_TARGET)!!

            if (!target.isAlive()) return false

            //particle
            sendAttackParticle(
                entity,
                entity.position.add(0.0, 1.5),
                target.position.add(0.0, (target.getHeight() / 2).toDouble())
            )

            //sound
            entity.level!!.addSound(entity.position, Sound.MOB_WARDEN_SONIC_BOOM)

            //            LevelSoundEventPacketV2 pk = new LevelSoundEventPacketV2();
//            pk.sound = LevelSoundEventPacket.SOUND_SONIC_BOOM;
//            pk.entityIdentifier = "minecraft:warden";
//            pk.x = (float) entity.x;
//            pk.y = (float) entity.y;
//            pk.z = (float) entity.z;
//
//            Server.broadcastPacket(entity.getViewers().values(), pk);

            //attack
            val damages: MutableMap<DamageModifier, Float> = EnumMap(
                DamageModifier::class.java
            )

            var damage = 0f
            if (entity is EntityCanAttack) {
                damage = entity.getDiffHandDamage(Server.instance.getDifficulty())
            }
            damages[DamageModifier.BASE] = damage

            val ev = EntityDamageByEntityEvent(
                entity,
                target, DamageCause.MAGIC, damages, 0.6f, null
            )

            entity.level!!.addSound(target.position, Sound.MOB_WARDEN_ATTACK)
            target.attack(ev)
        }
        if (currentTick > this.totalRunningTime) {
            return false
        } else {
            val target = entity.memoryStorage.get(CoreMemoryTypes.ATTACK_TARGET)!!
            //更新视线target
            entity.lookTarget = target.position.clone()
            entity.moveTarget = target.position.clone()
            return true
        }
    }

    override fun onInterrupt(entity: EntityMob) {
        this.currentTick = 0

        entity.setDataFlag(EntityFlag.SONIC_BOOM, false)
        entity.setDataFlagExtend(EntityFlag.SONIC_BOOM, false)
    }

    override fun onStart(entity: EntityMob) {
        entity.setDataFlag(EntityFlag.SONIC_BOOM, true)
        entity.setDataFlagExtend(EntityFlag.SONIC_BOOM, true)

        entity.level!!.addSound(entity.position, Sound.MOB_WARDEN_SONIC_CHARGE)
        //        LevelSoundEventPacketV2 pk = new LevelSoundEventPacketV2();
//        pk.sound = LevelSoundEventPacket.SOUND_SONIC_CHARGE;
//        pk.entityIdentifier = "minecraft:warden";
//        pk.x = (float) entity.x;
//        pk.y = (float) entity.y;
//        pk.z = (float) entity.z;
//
//        Server.broadcastPacket(entity.getViewers().values(), pk);
    }

    override fun onStop(entity: EntityMob) {
        this.currentTick = 0

        entity.setDataFlag(EntityFlag.SONIC_BOOM, false)
        entity.setDataFlagExtend(EntityFlag.SONIC_BOOM, false)
    }

    protected fun sendAttackParticle(entity: EntityMob, from: Vector3, to: Vector3) {
        val length = from.distance(to)
        val relativeVector = Vector3(to.x - from.x, to.y - from.y, to.z - from.z)
        var i = 1
        while (i <= (length + 4)) {
            val pk = LevelEventGenericPacket()
            pk.eventId = LevelEventPacket.EVENT_SONIC_EXPLOSION
            pk.tag = createVec3fTag(from.add(relativeVector.multiply(i / length)).asVector3f())
            Server.broadcastPacket(entity.viewers.values, pk)
            i++
        }
    }

    protected fun createVec3fTag(vec3f: Vector3f): CompoundTag {
        return CompoundTag()
            .putFloat("x", vec3f.x)
            .putFloat("y", vec3f.y)
            .putFloat("z", vec3f.z)
    }
}
