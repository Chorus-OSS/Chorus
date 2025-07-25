package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.command.tree.node.PlayersNode
import org.chorus_oss.chorus.command.utils.CommandLogger

class HudCommand(name: String) : VanillaCommand(name, "commands.hud.description", "%commands.hud.usage") {
    init {
        this.permission = "chorus.command.hud"
        commandParameters.clear()
        this.addCommandParameters(
            "default", arrayOf(
                CommandParameter.Companion.newType("player", false, CommandParamType.TARGET, PlayersNode()),
                CommandParameter.Companion.newEnum("visible", false, arrayOf("hide", "reset")),
                CommandParameter.Companion.newEnum(
                    "hud_element",
                    false,
                    arrayOf(
                        "armor",
                        "air_bubbles_bar",
                        "crosshair",
                        "food_bar",
                        "health",
                        "hotbar",
                        "paper_doll",
                        "tool_tips",
                        "progress_bar",
                        "touch_controls",
                        "vehicle_health"
                    )
                )
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

        val players = list.getResult<List<Player>>(0)!!
        if (players.isEmpty()) {
            log.addNoTargetMatch().output()
            return 0
        }

        val visibility = when (list.getResult<Any>(1) as String) {
            "hide" -> org.chorus_oss.protocol.types.hud.HudVisibility.Hide
            "reset" -> org.chorus_oss.protocol.types.hud.HudVisibility.Reset
            else -> null
        }

        val element = when (list.getResult<Any>(2) as String) {
            "armor" -> org.chorus_oss.protocol.types.hud.HudElement.Armor
            "air_bubbles_bar" -> org.chorus_oss.protocol.types.hud.HudElement.AirBubblesBar
            "crosshair" -> org.chorus_oss.protocol.types.hud.HudElement.Crosshair
            "food_bar" -> org.chorus_oss.protocol.types.hud.HudElement.Hunger
            "health" -> org.chorus_oss.protocol.types.hud.HudElement.Health
            "hotbar" -> org.chorus_oss.protocol.types.hud.HudElement.Hotbar
            "paper_doll" -> org.chorus_oss.protocol.types.hud.HudElement.PaperDoll
            "tool_tips" -> org.chorus_oss.protocol.types.hud.HudElement.ToolTips
            "progress_bar" -> org.chorus_oss.protocol.types.hud.HudElement.ProgressBar
            "touch_controls" -> org.chorus_oss.protocol.types.hud.HudElement.TouchControls
            "vehicle_health" -> org.chorus_oss.protocol.types.hud.HudElement.VehicleHealth
            else -> null
        }

        if (visibility == null || element == null) {
            return 0
        }


        for (player in players) {
            val packet = org.chorus_oss.protocol.packets.SetHudPacket(
                elements = listOf(element),
                visibility = visibility,
            )
            player.sendPacket(packet)

            return 1
        }

        return 0
    }
}
