package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandEnum
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.tree.node.PlayersNode
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.protocol.types.Vector3f

class PlaySoundCommand(name: String) : VanillaCommand(name, "commands.playsound.description") {
    init {
        this.permission = "chorus.command.playsound"
        commandParameters.clear()
        this.addCommandParameters(
            "default",
            arrayOf(
                CommandParameter.Companion.newEnum(
                    "sound",
                    false,
                    CommandEnum("sound", Sound.entries.map { it.sound }.toList(), true)
                ),
                CommandParameter.Companion.newType("player", true, CommandParamType.TARGET, PlayersNode()),
                CommandParameter.Companion.newType("position", true, CommandParamType.POSITION),
                CommandParameter.Companion.newType("volume", true, CommandParamType.FLOAT),
                CommandParameter.Companion.newType("pitch", true, CommandParamType.FLOAT),
                CommandParameter.Companion.newType("minimumVolume", true, CommandParamType.FLOAT)
            )
        )
        this.enableParamTree()
    }

    override fun execute(
        sender: CommandSender,
        commandLabel: String?,
        result: Map.Entry<String, ParamList>,
        log: CommandLogger
    ): Int {
        val list = result.value
        val sound = list.getResult<String>(0)!!
        var targets: List<Player>? = null
        var locator: Locator? = null
        var volume = 1f
        var pitch = 1f
        var minimumVolume = 0f
        if (list.hasResult(1)) targets = list.getResult(1)
        if (list.hasResult(2)) locator = list.getResult(2)
        if (list.hasResult(3)) volume = list.getResult(3)!!
        if (list.hasResult(4)) pitch = list.getResult(4)!!
        if (list.hasResult(5)) minimumVolume = list.getResult(5)!!
        if (minimumVolume < 0) {
            log.addNumTooSmall(5, 0).output()
            return 0
        }

        if (targets.isNullOrEmpty()) {
            if (sender.isPlayer) {
                targets = listOf(sender.asPlayer()!!)
            } else {
                log.addError("commands.generic.noTargetMatch").output()
                return 0
            }
        }
        if (locator == null) {
            locator = targets[0].locator
        }

        val maxDistance = (if (volume > 1) volume * 16 else 16f).toDouble()

        val successes: MutableList<String> = mutableListOf()
        for (player in targets) {
            val name = player.getEntityName()
            if (locator.position.distance(player.position) > maxDistance) {
                if (minimumVolume <= 0) {
                    log.addError("commands.playsound.playerTooFar", name)
                    continue
                }

                val packet = org.chorus_oss.protocol.packets.PlaySoundPacket(
                    soundName = sound,
                    position = Vector3f(player.position),
                    volume = minimumVolume,
                    pitch = pitch,
                )
                player.sendPacket(packet)
            } else {
                val packet = org.chorus_oss.protocol.packets.PlaySoundPacket(
                    soundName = sound,
                    position = Vector3f(locator.position),
                    volume = volume,
                    pitch = pitch,
                )
                player.sendPacket(packet)
            }
            successes.add(name)
        }
        log.addSuccess("commands.playsound.success", sound, java.lang.String.join(", ", successes))
            .successCount(successes.size).output()
        return successes.size
    }
}
