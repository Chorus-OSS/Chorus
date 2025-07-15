package org.chorus_oss.chorus.network.process.processor

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.event.player.PlayerEditBookEvent
import org.chorus_oss.chorus.experimental.network.MigrationPacket
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.Item.Companion.get
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.item.ItemWritableBook
import org.chorus_oss.chorus.item.ItemWrittenBook
import org.chorus_oss.chorus.network.process.DataPacketProcessor
import org.chorus_oss.chorus.utils.Loggable


class BookEditProcessor : DataPacketProcessor<MigrationPacket<org.chorus_oss.protocol.packets.BookEditPacket>>() {
    override fun handle(player: Player, pk: MigrationPacket<org.chorus_oss.protocol.packets.BookEditPacket>) {
        val packet = pk.packet

        val player = player.player

        val oldBook = player.inventory.getItem(packet.bookSlot.toInt())
        if (oldBook.id != ItemID.WRITABLE_BOOK) {
            return
        }

        var newBook: Item = oldBook.clone()
        val success: Boolean
        when (packet.action) {
            org.chorus_oss.protocol.packets.BookEditPacket.Action.ReplacePage -> {
                val actionData = packet.actionData as org.chorus_oss.protocol.packets.BookEditPacket.ReplacePageData
                if (actionData.text.length > 512) return

                success = (newBook as ItemWritableBook).setPageText(actionData.pageIndex.toInt(), actionData.text)
            }

            org.chorus_oss.protocol.packets.BookEditPacket.Action.AddPage -> {
                val actionData = packet.actionData as org.chorus_oss.protocol.packets.BookEditPacket.AddPageData
                if (actionData.text.length > 512) return

                success = (newBook as ItemWritableBook).insertPage(actionData.pageIndex.toInt(), actionData.text)
            }

            org.chorus_oss.protocol.packets.BookEditPacket.Action.DeletePage -> {
                val actionData = packet.actionData as org.chorus_oss.protocol.packets.BookEditPacket.DeletePageData
                success = (newBook as ItemWritableBook).deletePage(actionData.pageIndex.toInt())
            }

            org.chorus_oss.protocol.packets.BookEditPacket.Action.SwapPages -> {
                val actionData = packet.actionData as org.chorus_oss.protocol.packets.BookEditPacket.SwapPagesData
                success = (newBook as ItemWritableBook).swapPages(
                    actionData.pageIndexA.toInt(),
                    actionData.pageIndexB.toInt()
                )
            }

            org.chorus_oss.protocol.packets.BookEditPacket.Action.Finalize -> {
                val actionData = packet.actionData as org.chorus_oss.protocol.packets.BookEditPacket.FinalizeData
                if (actionData.title.length > 64 || actionData.author.length > 64 || actionData.xuid.length > 64) return

                newBook = get(ItemID.WRITTEN_BOOK, 0, 1, oldBook.compoundTag)
                success = (newBook as ItemWrittenBook).signBook(
                    actionData.title,
                    actionData.author,
                    actionData.xuid,
                    ItemWrittenBook.GENERATION_ORIGINAL
                )
            }
        }

        if (success) {
            val editBookEvent = PlayerEditBookEvent(player, oldBook, newBook, packet.action)
            Server.instance.pluginManager.callEvent(editBookEvent)
            if (!editBookEvent.cancelled) {
                player.inventory.setItem(packet.bookSlot.toInt(), editBookEvent.newBook)
            }
        }
    }

    override val packetId: Int = org.chorus_oss.protocol.packets.BookEditPacket.id

    companion object : Loggable
}
