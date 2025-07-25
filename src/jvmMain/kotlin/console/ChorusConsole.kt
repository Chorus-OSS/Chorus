package org.chorus_oss.chorus.console

import net.minecrell.terminalconsole.SimpleTerminalConsole
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.event.server.ServerCommandEvent
import org.chorus_oss.chorus.plugin.InternalPlugin
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

class ChorusConsole(private val server: Server) : SimpleTerminalConsole() {
    private val consoleQueue: BlockingQueue<String> = LinkedBlockingQueue()
    private val executingCommands = AtomicBoolean(false)

    override fun isRunning(): Boolean {
        return server.isRunning()
    }

    override fun runCommand(command: String) {
        if (executingCommands.get()) {
            val event = ServerCommandEvent(server.consoleSender, command)
            server.pluginManager.callEvent(event)
            if (!event.cancelled) {
                server.scheduler.scheduleTask(
                    InternalPlugin.INSTANCE
                ) { server.executeCommand(event.sender, event.command) }
            }
        } else {
            consoleQueue.add(command)
        }
    }

    fun readLine(): String {
        try {
            return consoleQueue.take()
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }

    override fun shutdown() {
        server.shutdown()
    }

    override fun buildReader(builder: LineReaderBuilder): LineReader {
        builder.completer(ChorusConsoleCompleter(server))
        builder.appName("Chorus")
        builder.option(LineReader.Option.HISTORY_BEEP, false)
        builder.option(LineReader.Option.HISTORY_IGNORE_DUPS, true)
        builder.option(LineReader.Option.HISTORY_IGNORE_SPACE, true)
        return super.buildReader(builder)
    }

    fun isExecutingCommands(): Boolean {
        return executingCommands.get()
    }

    fun setExecutingCommands(executingCommands: Boolean) {
        if (this.executingCommands.compareAndSet(!executingCommands, executingCommands) && executingCommands) {
            consoleQueue.clear()
        }
    }
}
