package org.chorus_oss.chorus.entity

import org.chorus_oss.chorus.nbt.tag.CompoundTag

class EntityAbilities {
    var attackMobs: Boolean = false
    var attackPlayers: Boolean = false
    var build: Boolean = false
    var doorsAndSwitches: Boolean = false
    var flying: Boolean = false
    var flySpeed: Float = 0.05f
    var instaBuild: Boolean = false
    var invulnerable: Boolean = false
    var lightning: Boolean = false
    var mayfly: Boolean = false
    var mine: Boolean = false
    var mute: Boolean = false
    var noClip: Boolean = false
    var op: Boolean = false
    var openContainers: Boolean = false
    var permissionsLevel: Int = 0
    var playerPermissionsLevel: Int = 0
    var teleport: Boolean = false
    var walkSpeed: Float = 0.1f
    var worldBuilder: Boolean = false

    constructor()

    constructor(abilities: CompoundTag) {
        this.attackMobs = abilities.getBoolean(TAG_ATTACK_MOBS)
        this.attackPlayers = abilities.getBoolean(TAG_ATTACK_PLAYERS)
        this.build = abilities.getBoolean(TAG_BUILD)
        this.doorsAndSwitches = abilities.getBoolean(TAG_DOORS_AND_SWITCHES)
        this.flying = abilities.getBoolean(TAG_FLYING)
        this.flySpeed = abilities.getFloat(TAG_FLY_SPEED)
        this.instaBuild = abilities.getBoolean(TAG_INSTA_BUILD)
        this.invulnerable = abilities.getBoolean(TAG_INVULNERABLE)
        this.lightning = abilities.getBoolean(TAG_LIGHTNING)
        this.mayfly = abilities.getBoolean(TAG_MAYFLY)
        this.mine = abilities.getBoolean(TAG_MINE)
        this.mute = abilities.getBoolean(TAG_MUTE)
        this.noClip = abilities.getBoolean(TAG_NO_CLIP)
        this.op = abilities.getBoolean(TAG_OP)
        this.openContainers = abilities.getBoolean(TAG_OPEN_CONTAINERS)
        this.permissionsLevel = abilities.getInt(TAG_PERMISSIONS_LEVEL)
        this.playerPermissionsLevel = abilities.getInt(TAG_PLAYER_PERMISSIONS_LEVEL)
        this.teleport = abilities.getBoolean(TAG_TELEPORT)
        this.walkSpeed = abilities.getFloat(TAG_WALK_SPEED)
        this.worldBuilder = abilities.getBoolean(TAG_WORLD_BUILDER)
    }

    fun get(): CompoundTag {
        val abilities: CompoundTag = CompoundTag()
        abilities.putBoolean(TAG_ATTACK_MOBS, this.attackMobs)
        abilities.putBoolean(TAG_ATTACK_PLAYERS, this.attackPlayers)
        abilities.putBoolean(TAG_BUILD, this.build)
        abilities.putBoolean(TAG_DOORS_AND_SWITCHES, this.doorsAndSwitches)
        abilities.putBoolean(TAG_FLYING, this.flying)
        abilities.putFloat(TAG_FLY_SPEED, this.flySpeed)
        abilities.putBoolean(TAG_INSTA_BUILD, this.instaBuild)
        abilities.putBoolean(TAG_INVULNERABLE, this.invulnerable)
        abilities.putBoolean(TAG_LIGHTNING, this.lightning)
        abilities.putBoolean(TAG_MAYFLY, this.mayfly)
        abilities.putBoolean(TAG_MINE, this.mine)
        abilities.putBoolean(TAG_MUTE, this.mute)
        abilities.putBoolean(TAG_OP, this.op)
        abilities.putBoolean(TAG_OPEN_CONTAINERS, this.openContainers)
        abilities.putInt(TAG_PERMISSIONS_LEVEL, this.permissionsLevel)
        abilities.putInt(TAG_PLAYER_PERMISSIONS_LEVEL, this.playerPermissionsLevel)
        abilities.putBoolean(TAG_TELEPORT, this.teleport)
        abilities.putFloat(TAG_WALK_SPEED, this.walkSpeed)
        abilities.putBoolean(TAG_WORLD_BUILDER, this.worldBuilder)
        return abilities
    }

    companion object {
        const val TAG_ATTACK_MOBS: String = "attackmobs"
        const val TAG_ATTACK_PLAYERS: String = "attackplayers"
        const val TAG_BUILD: String = "build"
        const val TAG_DOORS_AND_SWITCHES: String = "doorsandswitches"
        const val TAG_FLYING: String = "flying"
        const val TAG_FLY_SPEED: String = "flySpeed"
        const val TAG_INSTA_BUILD: String = "instabuild"
        const val TAG_INVULNERABLE: String = "invulnerable"
        const val TAG_LIGHTNING: String = "lightning"
        const val TAG_MAYFLY: String = "mayfly"
        const val TAG_MINE: String = "mine"
        const val TAG_MUTE: String = "mute"
        const val TAG_NO_CLIP: String = "noclip"
        const val TAG_OP: String = "op"
        const val TAG_OPEN_CONTAINERS: String = "opencontainers"
        const val TAG_PERMISSIONS_LEVEL: String = "permissionsLevel"
        const val TAG_PLAYER_PERMISSIONS_LEVEL: String = "playerPermissionsLevel"
        const val TAG_TELEPORT: String = "teleport"
        const val TAG_WALK_SPEED: String = "walkSpeed"
        const val TAG_WORLD_BUILDER: String = "worldbuilder"
    }
}
