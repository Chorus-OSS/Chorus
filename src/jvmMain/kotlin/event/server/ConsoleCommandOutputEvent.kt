package org.chorus_oss.chorus.event.server

import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.event.Cancellable
import org.chorus_oss.chorus.event.HandlerList

class ConsoleCommandOutputEvent(val sender: CommandSender, var message: String) : ServerEvent(), Cancellable {
    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
