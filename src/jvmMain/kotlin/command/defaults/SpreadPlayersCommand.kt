package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.utils.CommandLogger
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.math.Vector3
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.floor

class SpreadPlayersCommand(name: String) : VanillaCommand(name, "commands.spreadplayers.description") {
    private val random: ThreadLocalRandom

    init {
        this.permission = "chorus.command.spreadplayers"
        commandParameters.clear()
        this.addCommandParameters(
            "default", arrayOf(
                CommandParameter.Companion.newType("x", false, CommandParamType.VALUE),
                CommandParameter.Companion.newType("z", false, CommandParamType.VALUE),
                CommandParameter.Companion.newType("spreadDistance", false, CommandParamType.FLOAT),
                CommandParameter.Companion.newType("maxRange", false, CommandParamType.FLOAT),
                CommandParameter.Companion.newType("victim", false, CommandParamType.TARGET)
            )
        )
        this.random = ThreadLocalRandom.current()
        this.enableParamTree()
    }

    override fun execute(
        sender: CommandSender,
        commandLabel: String?,
        result: Map.Entry<String, ParamList>,
        log: CommandLogger
    ): Int {
        val list = result.value
        val x = list.getResult<Double>(0)!!
        val z = list.getResult<Double>(1)!!
        val spreadDistance = list.getResult<Float>(2)!!
        val maxRange = list.getResult<Float>(3)!!
        val targets = list.getResult<List<Entity>>(4)!!
        if (targets.isEmpty()) {
            log.addNoTargetMatch().output()
            return 0
        }

        if (spreadDistance < 0) {
            log.addDoubleTooSmall(3, 0.0).output()
            return 0
        } else if (maxRange < spreadDistance) {
            log.addDoubleTooSmall(4, (spreadDistance + 1).toDouble()).output()
            return 0
        }
        for (target in targets) {
            val vec3 = this.nextXZ(x, z, maxRange.toInt())
            vec3.y = (target.level!!.getHighestBlockAt(vec3.floorX, vec3.floorZ) + 1).toDouble()
            target.teleport(vec3)
        }
        log.addSuccess(
            "commands.spreadplayers.success.players",
            targets.size.toString(), floor(x).toString(), floor(z).toString()
        ).output()
        return 1
    }

    private fun nextXZ(centerX: Double, centerZ: Double, maxRange: Int): Vector3 {
        val vec3 = Vector3(centerX, 0.0, centerZ)
        vec3.x = Math.round(vec3.x) + random.nextInt(-maxRange, maxRange) + 0.5
        vec3.z = Math.round(vec3.z) + random.nextInt(-maxRange, maxRange) + 0.5
        return vec3
    }
}
