package org.chorus_oss.chorus.network.protocol

import org.chorus_oss.chorus.network.DataPacket
import org.chorus_oss.chorus.network.PacketDecoder
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.network.connection.util.HandleByteBuf


class EntityEventPacket : DataPacket() {
    @JvmField
    var eid: Long = 0

    @JvmField
    var event: Int = 0
    var data: Int = 0

    override fun encode(byteBuf: HandleByteBuf) {
        byteBuf.writeActorRuntimeID(this.eid)
        byteBuf.writeByte(event.toByte().toInt())
        byteBuf.writeVarInt(this.data)
    }

    override fun pid(): Int {
        return ProtocolInfo.ENTITY_EVENT_PACKET
    }

    companion object : PacketDecoder<EntityEventPacket> {
        override fun decode(byteBuf: HandleByteBuf): EntityEventPacket {
            return EntityEventPacket().also {
                it.eid = byteBuf.readActorRuntimeID()
                it.event = byteBuf.readByte().toInt()
                it.data = byteBuf.readVarInt()
            }
        }

        const val NONE: Int = 0
        const val JUMP: Int = 1
        const val HURT_ANIMATION: Int = 2
        const val DEATH_ANIMATION: Int = 3
        const val ARM_SWING: Int = 4
        const val ATTACK_STOP: Int = 5
        const val TAME_FAIL: Int = 6
        const val TAME_SUCCESS: Int = 7
        const val SHAKE_WET: Int = 8
        const val USE_ITEM: Int = 9
        const val EAT_GRASS_ANIMATION: Int = 10
        const val FISH_HOOK_BUBBLE: Int = 11
        const val FISH_HOOK_POSITION: Int = 12
        const val FISH_HOOK_HOOK: Int = 13
        const val FISH_HOOK_TEASE: Int = 14
        const val SQUID_INK_CLOUD: Int = 15
        const val ZOMBIE_VILLAGER_CURE: Int = 16
        const val AMBIENT_SOUND: Int = 17
        const val RESPAWN: Int = 18
        const val IRON_GOLEM_OFFER_FLOWER: Int = 19
        const val IRON_GOLEM_WITHDRAW_FLOWER: Int = 20
        const val LOVE_PARTICLES: Int = 21
        const val VILLAGER_ANGRY: Int = 22
        const val VILLAGER_HAPPY: Int = 23
        const val WITCH_SPELL_PARTICLES: Int = 24
        const val FIREWORK_EXPLOSION: Int = 25
        const val IN_LOVE_HEARTS: Int = 26
        const val SILVERFISH_SPAWN_ANIMATION: Int = 27
        const val GUARDIAN_ATTACK_ANIMATION: Int = 28
        const val WITCH_DRINK_POTION: Int = 29
        const val WITCH_THROW_POTION: Int = 30
        const val MINECART_TNT_PRIME_FUSE: Int = 31
        const val PRIME_CREEPER: Int = 32
        const val AIR_SUPPLY: Int = 33
        const val ENCHANT: Int = 34
        const val ELDER_GUARDIAN_CURSE: Int = 35
        const val AGENT_ARM_SWING: Int = 36
        const val ENDER_DRAGON_DEATH: Int = 37
        const val DUST_PARTICLES: Int = 38
        const val ARROW_SHAKE: Int = 39
        const val EATING_ITEM: Int = 57
        const val BABY_ANIMAL_FEED: Int = 60
        const val DEATH_SMOKE_CLOUD: Int = 61
        const val COMPLETE_TRADE: Int = 62
        const val REMOVE_LEASH: Int = 63
        const val CARAVAN: Int = 64
        const val CONSUME_TOTEM: Int = 65
        const val PLAYER_CHECK_TREASURE_HUNTER_ACHIEVEMENT: Int = 66
        const val ENTITY_SPAWN: Int = 67
        const val DRAGON_PUKE: Int = 68
        const val MERGE_ITEMS: Int = 69
        const val START_SWIMMING: Int = 70
        const val BALLOON_POP: Int = 71
        const val TREASURE_HUNT: Int = 72
        const val SUMMON_AGENT: Int = 73
        const val FINISHED_CHARGING_CROSSBOW: Int = 74
        const val LANDED_ON_GROUND: Int = 75
        const val ENTITY_GROW_UP: Int = 76
        const val VIBRATION_DETECTED: Int = 77
        const val DRINK_MILK: Int = 78

    }
}
