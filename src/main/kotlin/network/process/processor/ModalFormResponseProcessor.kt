package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.event.player.PlayerFormRespondedEvent
import org.chorus_oss.chorus.event.player.PlayerSettingsRespondedEvent
import org.chorus_oss.chorus.experimental.network.MigrationPacket
import org.chorus_oss.chorus.form.element.Element
import org.chorus_oss.chorus.form.element.custom.*
import org.chorus_oss.chorus.form.response.CustomResponse
import org.chorus_oss.chorus.form.response.ElementResponse
import org.chorus_oss.chorus.form.window.CustomForm
import org.chorus_oss.chorus.network.process.DataPacketProcessor
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.protocol.packets.ModalFormResponsePacket

class ModalFormResponseProcessor : DataPacketProcessor<MigrationPacket<ModalFormResponsePacket>>() {
    override fun handle(player: Player, pk: MigrationPacket<ModalFormResponsePacket>) {
        val packet = pk.packet
        val data = packet.responseData ?: "null"

        val player = player.player
        if (!player.spawned || !player.isAlive()) {
            return
        }

        if (data.length > 1024) {
            player.close("§cPacket handling error")
            return
        }

        if (player.player.formWindows.containsKey(packet.formID.toInt())) {
            val window = player.player.formWindows.remove(packet.formID.toInt())!!

            val response = window.respond(player, data.trim { it <= ' ' })

            val event = PlayerFormRespondedEvent(player, packet.formID.toInt(), window, response!!)
            Server.instance.pluginManager.callEvent(event)
        } else if (player.player.serverSettings.containsKey(packet.formID.toInt())) {
            val window = player.player.serverSettings[packet.formID.toInt()]!!

            val response = window.respond(player, data.trim { it <= ' ' })

            val event = PlayerSettingsRespondedEvent(
                player, packet.formID.toInt(), window,
                response!!
            )
            Server.instance.pluginManager.callEvent(event)

            // Apply responses as default settings
            if (!event.cancelled && window is CustomForm) {
                (response as CustomResponse).responses.forEach { (i, res) ->
                    when (val e: Element = window.elements[i]) {
                        is ElementDropdown -> e.defaultOption = ((res as ElementResponse).elementId)
                        is ElementInput -> e.defaultText = (res.toString())
                        is ElementSlider -> e.defaultValue = (res as Float)
                        is ElementToggle -> e.defaultValue = (res as Boolean)
                        is ElementStepSlider -> e.defaultStep = ((res as ElementResponse).elementId)
                        else -> log.warn("Illegal element {} within ServerSettings", e)
                    }
                }
            }
        } else log.warn("{} sent unknown form id {}", player.getEntityName(), packet.formID.toInt())
    }

    override val packetId: Int = ModalFormResponsePacket.id

    companion object : Loggable
}
