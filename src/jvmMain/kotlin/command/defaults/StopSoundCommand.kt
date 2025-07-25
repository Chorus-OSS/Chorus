package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.tree.node.PlayersNode
import org.chorus_oss.chorus.command.utils.CommandLogger
import java.util.stream.Collectors

class StopSoundCommand(name: String) : VanillaCommand(name, "commands.stopsound.description") {
    init {
        this.permission = "chorus.command.stopsound"
        commandParameters.clear()
        this.addCommandParameters(
            "default", arrayOf(
                CommandParameter.Companion.newType("player", false, CommandParamType.TARGET, PlayersNode()),
                CommandParameter.Companion.newType("sound", true, CommandParamType.STRING)
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
        val targets = list.getResult<List<Player>>(0)!!
        if (targets.isEmpty()) {
            log.addNoTargetMatch().output()
            return 0
        }
        var sound = ""

        if (list.hasResult(1)) {
            sound = list.getResult(1)!!
        }
        val packet = org.chorus_oss.protocol.packets.StopSoundPacket(
            soundName = sound,
            stopAll = sound.isEmpty(),
            stopLegacyMusic = false,
        )
        Server.broadcastPacket(targets, packet)
        val players = targets.stream().map { obj: Player -> obj.getEntityName() }.collect(Collectors.joining(" "))
        if (packet.stopAll) {
            log.addSuccess("commands.stopsound.success.all", players).output()
        } else {
            log.addSuccess("commands.stopsound.success", sound, players).output()
        }
        return 1
    }
}
