package org.chorus_oss.chorus.level.vibration

import kotlinx.io.Buffer
import kotlinx.io.readByteString
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.event.level.VibrationArriveEvent
import org.chorus_oss.chorus.event.level.VibrationOccurEvent
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.math.Vector3f
import org.chorus_oss.chorus.math.VectorMath
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.plugin.InternalPlugin
import org.chorus_oss.chorus.tags.BlockTags
import org.chorus_oss.nbt.TagSerialization
import kotlin.math.pow

class SimpleVibrationManager(protected var level: Level) : VibrationManager {
    protected var listeners: MutableSet<VibrationListener> = mutableSetOf()

    override fun callVibrationEvent(event: VibrationEvent) {
        val vibrationOccurPluginEvent: VibrationOccurEvent = VibrationOccurEvent(event)
        Server.instance.pluginManager.callEvent(vibrationOccurPluginEvent)
        if (vibrationOccurPluginEvent.cancelled) {
            return
        }
        for (listener in listeners) {
            if ((listener.listenerVector != event.source) && listener.listenerVector.distanceSquared(event.source) <= listener.listenRange.pow(
                    2.0
                ) && canVibrationArrive(
                    level,
                    event.source,
                    listener.listenerVector
                ) && listener.onVibrationOccur(event)
            ) {
                this.createVibration(listener, event)
                level.scheduler.scheduleDelayedTask(InternalPlugin.INSTANCE, {
                    val vibrationArrivePluginEvent: VibrationArriveEvent = VibrationArriveEvent(event, listener)
                    Server.instance.pluginManager.callEvent(vibrationArrivePluginEvent)
                    if (vibrationArrivePluginEvent.cancelled) {
                        return@scheduleDelayedTask
                    }
                    listener.onVibrationArrive(event)
                }, event.source.distance(listener.listenerVector).toInt())
            }
        }
    }

    override fun addListener(listener: VibrationListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: VibrationListener) {
        listeners.remove(listener)
    }

    protected fun createVibration(listener: VibrationListener, event: VibrationEvent) {
        val listenerPos: Vector3f = listener.listenerVector.asVector3f()
        val sourcePos: Vector3f = event.source.asVector3f()
        val tag = CompoundTag()
            .putCompound("origin", createVec3fTag(sourcePos))
            .putFloat("speed", 20.0f)
            .putCompound(
                "target",
                if (listener.isEntity) createEntityTargetTag(listener.asEntity()) else createVec3fTag(listenerPos)
            )
            .putFloat("timeToLive", (listenerPos.distance(sourcePos) / 20.0).toFloat())
        val packet = org.chorus_oss.protocol.packets.LevelEventGenericPacket(
            eventID = org.chorus_oss.protocol.packets.LevelEventPacket.PARTICLE_VIBRATION_SIGNAL,
            serializedEventData = Buffer().apply {
                org.chorus_oss.nbt.Tag.serialize(
                    org.chorus_oss.nbt.tags.CompoundTag(tag),
                    this,
                    TagSerialization.NetLE,
                    false
                )
            }.readByteString()
        )
        // TODO: 只对在视野范围内的玩家发包
        Server.broadcastPacket(level.players.values, packet)
    }

    protected fun createVec3fTag(vec3f: Vector3f): CompoundTag {
        return CompoundTag()
            .putString("type", "vec3")
            .putFloat("x", vec3f.x)
            .putFloat("y", vec3f.y)
            .putFloat("z", vec3f.z)
    }

    protected fun createEntityTargetTag(entity: Entity): CompoundTag {
        return CompoundTag()
            .putString("type", "actor")
            .putLong("uniqueID", entity.getRuntimeID())
            .putInt("attachPos", 3) // TODO: check the use of this value :)
    }

    protected fun canVibrationArrive(level: Level, from: Vector3, to: Vector3): Boolean {
        return VectorMath.getPassByVector3(from, to)
            .stream()
            .noneMatch { vec ->
                level.getTickCachedBlock(vec)
                    .`is`(BlockTags.PNX_WOOL)
            }
    }
}