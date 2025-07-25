package org.chorus_oss.chorus.dialog.response

import org.chorus_oss.chorus.dialog.element.ElementDialogButton
import org.chorus_oss.chorus.dialog.window.FormWindowDialog
import org.chorus_oss.protocol.packets.NPCRequestPacket


class FormResponseDialog(packet: NPCRequestPacket, dialog: FormWindowDialog) {
    val entityRuntimeId = packet.entityRuntimeID
    val data: String = packet.commandString
    var clickedButton: ElementDialogButton? = dialog.getButtons().getOrNull(packet.actionType.toInt())
    val sceneName: String = packet.sceneName
    val requestType: NPCRequestPacket.Companion.RequestType = packet.requestType
    val skinType: Int = packet.actionType.toInt()
}
