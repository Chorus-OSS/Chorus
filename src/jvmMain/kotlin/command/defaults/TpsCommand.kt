package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.Command
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.scheduler.Task
import org.chorus_oss.chorus.utils.TextFormat

class TpsCommand(name: String) : Command(name, "get server tps"), CoreCommand {
    init {
        this.permission = "nukkit.tps.status"
        commandParameters.clear()
        this.addCommandParameters(
            "default", arrayOf(
                CommandParameter.Companion.newType("count", true, CommandParamType.INT)
            )
        )
    }

    override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
        if (!this.testPermission(sender)) {
            return false
        }

        var count = 1
        if (args.size > 0) {
            count = args[0]!!.toInt()
        }

        if (count == 1) {
            val currentTps = Server.instance.ticksPerSecond
            sender.sendMessage(getTpsColor(currentTps).toString() + " Current TPS: " + currentTps)
        } else {
            Server.instance.scheduler.scheduleRepeatingTask(TpsTestTask(sender, count), 20)
        }
        return true
    }

    private fun getTpsColor(tps: Float): TextFormat {
        var tpsColor = TextFormat.GREEN
        if (tps < 12) {
            tpsColor = TextFormat.RED
        } else if (tps < 17) {
            tpsColor = TextFormat.GOLD
        }
        return tpsColor
    }

    private inner class TpsTestTask(private val sender: CommandSender, private val count: Int) : Task() {
        private var currentCount = 0
        private var tpsSum = 0f

        override fun onRun(currentTick: Int) {
            currentCount++
            val currentTps = Server.instance.ticksPerSecond

            sender.sendMessage("[" + currentCount + "]" + getTpsColor(currentTps) + " Current TPS: " + currentTps)
            tpsSum += currentTps
            if (currentCount >= count) {
                sender.sendMessage("Average TPS: " + (tpsSum / count))
                this.cancel()
            }
        }
    }
}
