package org.chorus_oss.chorus.experimental.network.protocol.utils

import org.chorus_oss.protocol.types.PlayerAbility

val PlayerAbility.Companion.Controllable: List<PlayerAbility>
    get() = listOf(
        PlayerAbility.Build,
        PlayerAbility.Mine,
        PlayerAbility.DoorsAndSwitches,
        PlayerAbility.OpenContainers,
        PlayerAbility.AttackPlayers,
        PlayerAbility.AttackMobs,
        PlayerAbility.OperatorCommands,
        PlayerAbility.Teleport,
    )