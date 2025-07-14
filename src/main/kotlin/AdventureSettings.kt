package org.chorus_oss.chorus

import org.chorus_oss.chorus.experimental.network.protocol.utils.Controllable
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.IntTag
import org.chorus_oss.chorus.network.protocol.types.CommandPermission
import org.chorus_oss.protocol.types.AbilitiesData
import org.chorus_oss.protocol.types.PlayerAbility
import org.chorus_oss.protocol.types.PlayerAbilitySet
import java.util.*

class AdventureSettings : Cloneable {
    val values: MutableMap<Type, Boolean> = EnumMap(
        Type::class.java
    )
    var playerPermission: org.chorus_oss.protocol.types.PlayerPermission? = null
        set(value) {
            field = value
            player.isOp = value == org.chorus_oss.protocol.types.PlayerPermission.Operator
        }

    var commandPermission: CommandPermission? = null
    var player: Player

    constructor(player: Player) {
        this.player = player
        init(null)
    }

    fun init(nbt: CompoundTag?) {
        if (nbt == null || !nbt.contains(KEY_ABILITIES)) {
            set(Type.WORLD_IMMUTABLE, player.isAdventure || player.isSpectator)
            set(Type.WORLD_BUILDER, !player.isAdventure && !player.isSpectator)
            set(Type.AUTO_JUMP, true)
            set(Type.ALLOW_FLIGHT, player.isCreative || player.isSpectator)
            set(Type.NO_CLIP, player.isSpectator)
            set(Type.FLYING, player.isSpectator)
            set(Type.OPERATOR, player.isOp)
            set(Type.TELEPORT, player.isOp)

            commandPermission = if (player.isOp) CommandPermission.GAME_DIRECTOR else CommandPermission.ANY
            playerPermission =
                if (player.isOp) org.chorus_oss.protocol.types.PlayerPermission.Operator else org.chorus_oss.protocol.types.PlayerPermission.Member
        } else {
            readNBT(nbt)
        }

        //Offline deop
        if (playerPermission == org.chorus_oss.protocol.types.PlayerPermission.Operator && !player.isOp) {
            onOpChange(false)
        }
        //Offline by op
        if (playerPermission != org.chorus_oss.protocol.types.PlayerPermission.Operator && player.isOp) {
            onOpChange(true)
        }
    }

    public override fun clone(): AdventureSettings {
        try {
            val settings = super.clone() as AdventureSettings
            settings.values.putAll(this.values)
            settings.player = this.player
            settings.playerPermission = this.playerPermission
            settings.commandPermission = this.commandPermission
            return settings
        } catch (e: CloneNotSupportedException) {
            throw AssertionError() // This should never happen.
        }
    }

    operator fun set(ability: PlayerAbility, value: Boolean): AdventureSettings {
        val type = ability2TypeMap[ability]
        if (type != null) {
            values[type] = value
        }
        return this
    }

    operator fun set(type: Type, value: Boolean): AdventureSettings {
        values[type] = value
        return this
    }

    fun get(ability: PlayerAbility): Boolean {
        val type = ability2TypeMap[ability]
        requireNotNull(type) { "Unknown ability: $ability" }
        return values.getOrDefault(type, type.defaultValue)
    }

    operator fun get(type: Type): Boolean {
        return values.getOrDefault(type, type.defaultValue)
    }

    fun update() {
        //Permission to send to all players so they can see each other
        //Make sure it will be sent to yourself (eg: there is no such player among the online players when the player enters the server)
        val players = Server.instance.onlinePlayers.values.toMutableSet()
        players.add(this.player)
        sendAbilities(players)
        updateAdventureSettings()
    }


    /**
     * This method will be called when the player's OP status changes.
     * Note that this method does not send a packet to the client to refresh the privilege information, you need to manually call the update() method to do so
     * @param op is OP or not
     */
    fun onOpChange(op: Boolean) {
        if (op) {
            for (controllableAbility in PlayerAbility.Controllable) {
                set(controllableAbility, true)
            }
        }
        //Set op-specific attributes
        set(Type.OPERATOR, op)
        set(Type.TELEPORT, op)

        commandPermission = if (op) CommandPermission.GAME_DIRECTOR else CommandPermission.ANY

        //Don't override customization/guest status
        if (op && playerPermission != org.chorus_oss.protocol.types.PlayerPermission.Operator) {
            playerPermission = org.chorus_oss.protocol.types.PlayerPermission.Operator
        }
        if (!op && playerPermission == org.chorus_oss.protocol.types.PlayerPermission.Operator) {
            playerPermission = org.chorus_oss.protocol.types.PlayerPermission.Member
        }
    }

    fun sendAbilities(players: Collection<Player>) {
        val packet = org.chorus_oss.protocol.packets.UpdateAbilitiesPacket(
            abilitiesData = AbilitiesData(
                targetPlayerRawID = player.getRuntimeID(),
                playerPermissions = playerPermission!!,
                commandPermissions = org.chorus_oss.protocol.types.CommandPermission.entries[commandPermission!!.ordinal],
                layers = listOf(
                    org.chorus_oss.protocol.types.AbilityLayer(
                        layerType = org.chorus_oss.protocol.types.AbilityLayer.Companion.Type.Base,
                        abilitiesSet = PlayerAbilitySet(
                            flags = PlayerAbility.entries.toMutableSet()
                        ),
                        abilityValues = PlayerAbilitySet(
                            flags = listOf(
                                Type.entries
                                    .filter { it.isAbility() && get(it) }
                                    .map { it.ability!! },
                                when (player.isCreative) {
                                    true -> listOf(PlayerAbility.Instabuild)
                                    false -> emptyList()
                                },
                                listOf(
                                    PlayerAbility.WalkSpeed,
                                    PlayerAbility.FlySpeed,
                                )
                            ).flatten().toMutableSet()
                        ),
                        flySpeed = Player.DEFAULT_FLY_SPEED,
                        verticalFlySpeed = 1f,
                        walkSpeed = Player.DEFAULT_SPEED,
                    )
                )
            )
        )
        Server.broadcastPacket(players, packet)
    }

    /**
     * Save permissions to nbt
     */
    fun saveNBT() {
        val nbt = player.namedTag
        val abilityTag = CompoundTag()
        values.forEach { (type, bool) ->
            abilityTag.put(type.name, IntTag(if (bool) 1 else 0))
        }
        nbt.put(KEY_ABILITIES, abilityTag)
        nbt.putInt(KEY_PLAYER_PERMISSION, playerPermission!!.ordinal)
        nbt.putInt(KEY_COMMAND_PERMISSION, commandPermission!!.ordinal)
    }

    /**
     * Read permission data from nbt
     */
    private fun readNBT(nbt: CompoundTag) {
        val abilityTag = nbt.getCompound(KEY_ABILITIES)
        for ((key, value) in abilityTag.tags) {
            if (value is IntTag) {
                set(Type.valueOf(key), value.data == 1)
            }
        }
        playerPermission = org.chorus_oss.protocol.types.PlayerPermission.entries[nbt.getInt(KEY_PLAYER_PERMISSION)]
        commandPermission = CommandPermission.entries[nbt.getInt(KEY_COMMAND_PERMISSION)]
    }

    fun updateAdventureSettings() {
        val adventurePacket = org.chorus_oss.protocol.packets.UpdateAdventureSettingsPacket(
            noPvM = get(Type.NO_PVM),
            noMvP = get(Type.NO_MVP),
            immutableWorld = get(Type.WORLD_IMMUTABLE),
            showNameTags = get(Type.SHOW_NAME_TAGS),
            autoJump = get(Type.AUTO_JUMP)
        )
        player.sendPacket(adventurePacket)
        player.resetInAirTicks()
    }

    enum class Type {
        WORLD_IMMUTABLE(false),
        NO_PVM(false),
        NO_MVP(PlayerAbility.Invulnerable, false),
        SHOW_NAME_TAGS(false),
        AUTO_JUMP(true),
        ALLOW_FLIGHT(PlayerAbility.MayFly, false),
        NO_CLIP(PlayerAbility.NoClip, false),
        WORLD_BUILDER(PlayerAbility.WorldBuilder, false),
        FLYING(PlayerAbility.Flying, false),
        MUTED(PlayerAbility.Muted, false),
        MINE(PlayerAbility.Mine, true),
        DOORS_AND_SWITCHED(PlayerAbility.DoorsAndSwitches, true),
        OPEN_CONTAINERS(PlayerAbility.OpenContainers, true),
        ATTACK_PLAYERS(PlayerAbility.AttackPlayers, true),
        ATTACK_MOBS(PlayerAbility.AttackMobs, true),
        OPERATOR(PlayerAbility.OperatorCommands, false),
        TELEPORT(PlayerAbility.Teleport, false),
        BUILD(PlayerAbility.Build, true),
        PRIVILEGED_BUILDER(PlayerAbility.PrivilegedBuilder, false),
        VERTICAL_FLY_SPEED(PlayerAbility.VerticalFlySpeed, true);

        val ability: PlayerAbility?
        val defaultValue: Boolean

        constructor(defaultValue: Boolean) {
            this.defaultValue = defaultValue
            this.ability = null
        }

        constructor(ability: PlayerAbility?, defaultValue: Boolean) {
            this.ability = ability
            this.defaultValue = defaultValue
            if (this.ability != null) {
                ability2TypeMap[this.ability] = this
            }
        }

        fun isAbility(): Boolean {
            return this.ability != null
        }
    }

    companion object {
        const val PERMISSION_NORMAL: Int = 0
        const val PERMISSION_OPERATOR: Int = 1
        const val PERMISSION_HOST: Int = 2
        const val PERMISSION_AUTOMATION: Int = 3
        const val PERMISSION_ADMIN: Int = 4

        const val KEY_ABILITIES: String = "Abilities"
        const val KEY_PLAYER_PERMISSION: String = "PlayerPermission"
        const val KEY_COMMAND_PERMISSION: String = "CommandPermission"

        private val ability2TypeMap: MutableMap<PlayerAbility, Type> = mutableMapOf()
    }
}