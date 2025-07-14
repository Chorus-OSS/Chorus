package org.chorus_oss.chorus.network.protocol

import org.chorus_oss.chorus.network.DataPacket
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.network.connection.util.HandleByteBuf
import org.chorus_oss.chorus.utils.Utils


class LevelEventPacket : DataPacket() {
    @JvmField
    var evid: Int = 0

    @JvmField
    var x: Float = 0f

    @JvmField
    var y: Float = 0f

    @JvmField
    var z: Float = 0f

    @JvmField
    var data: Int = 0

    override fun encode(byteBuf: HandleByteBuf) {
        byteBuf.writeVarInt(this.evid)
        byteBuf.writeVector3f(this.x, this.y, this.z)
        byteBuf.writeVarInt(this.data)
    }

    override fun pid(): Int {
        return ProtocolInfo.LEVEL_EVENT_PACKET
    }

    companion object {
        val EVENT_UNDEFINED: Int = Utils.dynamic(0)
        val EVENT_SOUND_CLICK: Int = Utils.dynamic(1000)
        val EVENT_SOUND_CLICK_FAIL: Int = Utils.dynamic(1001)
        val EVENT_SOUND_LAUNCH: Int = Utils.dynamic(1002)
        val EVENT_SOUND_DOOR_OPEN: Int = Utils.dynamic(1003)

        @JvmField
        val EVENT_SOUND_FIZZ: Int = Utils.dynamic(1004)
        val EVENT_SOUND_FUSE: Int = Utils.dynamic(1005)
        val EVENT_SOUND_PLAY_RECORDING: Int = Utils.dynamic(1006)
        val EVENT_SOUND_GHAST_WARNING: Int = Utils.dynamic(1007)
        val EVENT_SOUND_GHAST_FIREBALL: Int = Utils.dynamic(1008)
        val EVENT_SOUND_BLAZE_FIREBALL: Int = Utils.dynamic(1009)
        val EVENT_SOUND_ZOMBIE_DOOR_BUMP: Int = Utils.dynamic(1010)
        val EVENT_SOUND_ZOMBIE_DOOR_CRASH: Int = Utils.dynamic(1012)
        val EVENT_SOUND_ZOMBIE_INFECTED: Int = Utils.dynamic(1016)
        val EVENT_SOUND_ZOMBIE_CONVERTED: Int = Utils.dynamic(1017)
        val EVENT_SOUND_ENDERMAN_TELEPORT: Int = Utils.dynamic(1018)
        val EVENT_SOUND_ANVIL_BROKEN: Int = Utils.dynamic(1020)
        val EVENT_SOUND_ANVIL_USED: Int = Utils.dynamic(1021)
        val EVENT_SOUND_ANVIL_LAND: Int = Utils.dynamic(1022)
        val EVENT_SOUND_INFINITY_ARROW_PICKUP: Int = Utils.dynamic(1030)
        val EVENT_SOUND_TELEPORT_ENDERPEARL: Int = Utils.dynamic(1032)

        @JvmField
        val EVENT_SOUND_ITEMFRAME_ITEM_ADD: Int = Utils.dynamic(1040)

        @JvmField
        val EVENT_SOUND_ITEMFRAME_BREAK: Int = Utils.dynamic(1041)
        val EVENT_SOUND_ITEMFRAME_PLACE: Int = Utils.dynamic(1042)
        val EVENT_SOUND_ITEMFRAME_ITEM_REMOVE: Int = Utils.dynamic(1043)

        @JvmField
        val EVENT_SOUND_ITEMFRAME_ITEM_ROTATE: Int = Utils.dynamic(1044)
        val EVENT_SOUND_CAMERA: Int = Utils.dynamic(1050)

        @JvmField
        val EVENT_SOUND_EXPERIENCE_ORB_PICKUP: Int = Utils.dynamic(1051)
        val EVENT_SOUND_TOTEM_USED: Int = Utils.dynamic(1052)
        val EVENT_SOUND_ARMOR_STAND_BREAK: Int = Utils.dynamic(1060)
        val EVENT_SOUND_ARMOR_STAND_HIT: Int = Utils.dynamic(1061)
        val EVENT_SOUND_ARMOR_STAND_LAND: Int = Utils.dynamic(1062)
        val EVENT_SOUND_ARMOR_STAND_PLACE: Int = Utils.dynamic(1063)
        val EVENT_SOUND_POINTED_DRIPSTONE_LAND: Int = Utils.dynamic(1064)

        @JvmField
        val EVENT_SOUND_DYE_USED: Int = Utils.dynamic(1065)

        @JvmField
        val EVENT_SOUND_INK_SACE_USED: Int = Utils.dynamic(1066)
        val EVENT_SOUND_AMETHYST_RESONATE: Int = Utils.dynamic(1067)

        @JvmField
        val EVENT_PARTICLE_SHOOT: Int = Utils.dynamic(2000)

        @JvmField
        val EVENT_PARTICLE_DESTROY_BLOCK: Int = Utils.dynamic(2001)
        val EVENT_PARTICLE_POTION_SPLASH: Int = Utils.dynamic(2002)
        val EVENT_PARTICLE_EYE_OF_ENDER_DEATH: Int = Utils.dynamic(2003)
        val EVENT_PARTICLE_MOB_BLOCK_SPAWN: Int = Utils.dynamic(2004)
        val EVENT_PARTICLE_CROP_GROWTH: Int = Utils.dynamic(2005)
        val EVENT_PARTICLE_SOUND_GUARDIAN_GHOST: Int = Utils.dynamic(2006)
        val EVENT_PARTICLE_DEATH_SMOKE: Int = Utils.dynamic(2007)
        val EVENT_PARTICLE_DENY_BLOCK: Int = Utils.dynamic(2008)
        val EVENT_PARTICLE_GENERIC_SPAWN: Int = Utils.dynamic(2009)

        @JvmField
        val EVENT_PARTICLE_DRAGON_EGG: Int = Utils.dynamic(2010)
        val EVENT_PARTICLE_CROP_EATEN: Int = Utils.dynamic(2011)
        val EVENT_PARTICLE_CRIT: Int = Utils.dynamic(2012)
        val EVENT_PARTICLE_TELEPORT: Int = Utils.dynamic(2013)
        val EVENT_PARTICLE_CRACK_BLOCK: Int = Utils.dynamic(2014)
        val EVENT_PARTICLE_BUBBLES: Int = Utils.dynamic(2015)
        val EVENT_PARTICLE_EVAPORATE: Int = Utils.dynamic(2016)
        val EVENT_PARTICLE_DESTROY_ARMOR_STAND: Int = Utils.dynamic(2017)
        val EVENT_PARTICLE_BREAKING_EGG: Int = Utils.dynamic(2018)
        val EVENT_PARTICLE_DESTROY_EGG: Int = Utils.dynamic(2019)
        val EVENT_PARTICLE_EVAPORATE_WATER: Int = Utils.dynamic(2020)
        val EVENT_PARTICLE_DESTROY_BLOCK_NO_SOUND: Int = Utils.dynamic(2021)
        val EVENT_PARTICLE_KNOCKBACK_ROAR: Int = Utils.dynamic(2022)
        val EVENT_PARTICLE_TELEPORT_TRAIL: Int = Utils.dynamic(2023)
        val EVENT_PARTICLE_POINT_CLOUD: Int = Utils.dynamic(2024)
        val EVENT_PARTICLE_EXPLOSION: Int = Utils.dynamic(2025)
        val EVENT_PARTICLE_BLOCK_EXPLOSION: Int = Utils.dynamic(2026)
        val EVENT_PARTICLE_VIBRATION_SIGNAL: Int = Utils.dynamic(2027)
        val EVENT_PARTICLE_DRIPSTONE_DRIP: Int = Utils.dynamic(2028)
        val EVENT_PARTICLE_FIZZ_EFFECT: Int = Utils.dynamic(2029)
        val EVENT_PARTICLE_WAX_ON: Int = Utils.dynamic(2030)
        val EVENT_PARTICLE_WAX_OFF: Int = Utils.dynamic(2031)
        val EVENT_PARTICLE_SCRAPE: Int = Utils.dynamic(2032)
        val EVENT_PARTICLE_ELECTRIC_SPARK: Int = Utils.dynamic(2033)
        val EVENT_PARTICLE_TURTLE_EGG: Int = Utils.dynamic(2034)
        val EVENT_PARTICLE_SCULK_SHRIEK: Int = Utils.dynamic(2035)
        val EVENT_SCULK_CATALYST_BLOOM: Int = Utils.dynamic(2036)
        val EVENT_SCULK_CHARGE: Int = Utils.dynamic(2037)
        val EVENT_SCULK_CHARGE_POP: Int = Utils.dynamic(2038)
        val EVENT_SONIC_EXPLOSION: Int = Utils.dynamic(2039)
        val EVENT_DUST_PLUME: Int = Utils.dynamic(2040)
        val EVENT_START_RAINING: Int = Utils.dynamic(3001)
        val EVENT_START_THUNDERSTORM: Int = Utils.dynamic(3002)
        val EVENT_STOP_RAINING: Int = Utils.dynamic(3003)
        val EVENT_STOP_THUNDERSTORM: Int = Utils.dynamic(3004)
        val EVENT_GLOBAL_PAUSE: Int = Utils.dynamic(3005)
        val EVENT_SIM_TIME_STEP: Int = Utils.dynamic(3006)
        val EVENT_SIM_TIME_SCALE: Int = Utils.dynamic(3007)

        @JvmField
        val EVENT_ACTIVATE_BLOCK: Int = Utils.dynamic(3500)

        @JvmField
        val EVENT_CAULDRON_EXPLODE: Int = Utils.dynamic(3501)
        val EVENT_CAULDRON_DYE_ARMOR: Int = Utils.dynamic(3502)
        val EVENT_CAULDRON_CLEAN_ARMOR: Int = Utils.dynamic(3503)

        @JvmField
        val EVENT_CAULDRON_FILL_POTION: Int = Utils.dynamic(3504)

        @JvmField
        val EVENT_CAULDRON_TAKE_POTION: Int = Utils.dynamic(3505)
        val EVENT_CAULDRON_FILL_WATER: Int = Utils.dynamic(3506)

        @JvmField
        val EVENT_CAULDRON_TAKE_WATER: Int = Utils.dynamic(3507)
        val EVENT_CAULDRON_ADD_DYE: Int = Utils.dynamic(3508)
        val EVENT_CAULDRON_CLEAN_BANNER: Int = Utils.dynamic(3509)
        val EVENT_CAULDRON_FLUSH: Int = Utils.dynamic(3510)
        val EVENT_AGENT_SPAWN_EFFECT: Int = Utils.dynamic(3511)
        val EVENT_CAULDRON_FILL_LAVA: Int = Utils.dynamic(3512)
        val EVENT_CAULDRON_TAKE_LAVA: Int = Utils.dynamic(3513)
        val EVENT_CAULDRON_FILL_POWDER_SNOW: Int = Utils.dynamic(3514)
        val EVENT_CAULDRON_TAKE_POWDER_SNOW: Int = Utils.dynamic(3515)

        @JvmField
        val EVENT_BLOCK_START_BREAK: Int = Utils.dynamic(3600)

        @JvmField
        val EVENT_BLOCK_STOP_BREAK: Int = Utils.dynamic(3601)

        @JvmField
        val EVENT_BLOCK_UPDATE_BREAK: Int = Utils.dynamic(3602)
        val EVENT_PARTICLE_BREAK_BLOCK_DOWN: Int = Utils.dynamic(3603)
        val EVENT_PARTICLE_BREAK_BLOCK_UP: Int = Utils.dynamic(3604)
        val EVENT_PARTICLE_BREAK_BLOCK_NORTH: Int = Utils.dynamic(3605)
        val EVENT_PARTICLE_BREAK_BLOCK_SOUTH: Int = Utils.dynamic(3606)
        val EVENT_PARTICLE_BREAK_BLOCK_WEST: Int = Utils.dynamic(3607)
        val EVENT_PARTICLE_BREAK_BLOCK_EAST: Int = Utils.dynamic(3608)
        val EVENT_PARTICLE_SHOOT_WHITE_SMOKE: Int = Utils.dynamic(3609)
        val EVENT_PARTICLE_BREEZE_WIND_EXPLOSION: Int = Utils.dynamic(3610)
        val EVENT_PARTICLE_TRAIL_SPAWNER_DETECTION: Int = Utils.dynamic(3611)
        val EVENT_PARTICLE_TRAIL_SPAWNER_SPAWNING: Int = Utils.dynamic(3612)
        val EVENT_PARTICLE_TRAIL_SPAWNER_EJECTING: Int = Utils.dynamic(3613)
        val EVENT_PARTICLE_WIND_EXPLOSION: Int = Utils.dynamic(3614)
        val EVENT_ALL_PLAYERS_SLEEPING: Int = Utils.dynamic(3615)
        val EVENT_SET_DATA: Int = Utils.dynamic(4000)
        val EVENT_SLEEPING_PLAYERS: Int = Utils.dynamic(9801)
        val EVENT_JUMP_PREVENTED: Int = Utils.dynamic(9810)
        val EVENT_ANIMATION_VAULT_ACTIVATE: Int = Utils.dynamic(9811)
        val EVENT_ANIMATION_VAULT_DEACTIVATE: Int = Utils.dynamic(9812)
        val EVENT_ANIMATION_VAULT_EJECT_ITEM: Int = Utils.dynamic(9813)
        val EVENT_ANIMATION_SPAWN_COBWEB: Int = Utils.dynamic(9814)
        val EVENT_PARTICLE_SMASH_ATTACK_GROUND_DUST: Int = Utils.dynamic(9815)
        val EVENT_PARTICLE_CREAKING_HEART_TRIAL: Int = Utils.dynamic(9816)

        val EVENT_ADD_PARTICLE_MASK: Int = Utils.dynamic(0x4000)
    }
}
