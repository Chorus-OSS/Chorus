package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.dialog.response.FormResponseDialog
import org.chorus_oss.chorus.dialog.window.FormWindowDialog
import org.chorus_oss.chorus.entity.mob.EntityNPC
import org.chorus_oss.chorus.event.player.PlayerDialogRespondedEvent
import org.chorus_oss.chorus.experimental.network.MigrationPacket
import org.chorus_oss.chorus.network.process.DataPacketProcessor
import org.chorus_oss.protocol.packets.NPCRequestPacket

class NPCRequestProcessor : DataPacketProcessor<MigrationPacket<NPCRequestPacket>>() {
    override fun handle(player: Player, pk: MigrationPacket<NPCRequestPacket>) {
        val packet = pk.packet

        val player = player.player
        //若sceneName字段为空，则为玩家在编辑NPC，我们并不需要记录对话框，直接通过entityRuntimeId获取实体即可
        val entity = player.level!!.getEntity(packet.entityRuntimeID.toLong())
        if (packet.sceneName.isEmpty() && entity is EntityNPC) {
            val dialog: FormWindowDialog = entity.dialog

            val response = FormResponseDialog(packet, dialog)
            for (handler in dialog.handlers) {
                handler.handle(player, response)
            }

            val event = PlayerDialogRespondedEvent(player, dialog, response)
            Server.instance.pluginManager.callEvent(event)
            return
        }
        if (player.player.dialogWindows.getIfPresent(packet.sceneName) != null) {
            //remove the window from the map only if the requestType is EXECUTE_CLOSING_COMMANDS
            val dialog: FormWindowDialog?
            if (packet.requestType == NPCRequestPacket.Companion.RequestType.ExecuteClosingCommands) {
                dialog = player.player.dialogWindows.getIfPresent(packet.sceneName)
                player.player.dialogWindows.invalidate(packet.sceneName)
            } else {
                dialog = player.player.dialogWindows.getIfPresent(packet.sceneName)
            }

            val response = FormResponseDialog(packet, dialog!!)
            for (handler in dialog.handlers) {
                handler.handle(player, response)
            }

            val event = PlayerDialogRespondedEvent(player, dialog, response)
            Server.instance.pluginManager.callEvent(event)

            //close dialog after clicked button (otherwise the client will not be able to close the window)
            if (response.clickedButton != null && packet.requestType == NPCRequestPacket.Companion.RequestType.ExecuteAction) {
                val closeWindowPacket = org.chorus_oss.protocol.packets.NPCDialoguePacket(
                    entityUniqueID = entity!!.getUniqueID().toULong(),
                    actionType = org.chorus_oss.protocol.packets.NPCDialoguePacket.Companion.ActionType.Close,
                    dialogue = "",
                    sceneName = response.sceneName,
                    npcName = "",
                    actionJSON = "",
                )
                player.sendPacket(closeWindowPacket)
            }
            if (response.clickedButton != null && response.requestType == NPCRequestPacket.Companion.RequestType.ExecuteAction && response.clickedButton!!.nextDialog != null) {
                response.clickedButton!!.nextDialog!!.send(player)
            }
        }
    }

    override val packetId: Int = NPCRequestPacket.id
}
