package org.chorus_oss.chorus.command.function

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.CommandSender
import java.io.IOException

import java.nio.file.Files
import java.nio.file.Path

class Function private constructor(private val fullPath: Path) {
    val commands: List<String> =
        try {
            Files.readAllLines(fullPath)
                .filter { it.isNotBlank() }
                .map { s -> s.split("#".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0] }
                .filter { it.isNotEmpty() }.toList()
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }

    fun dispatch(sender: CommandSender): Boolean {
        var success = true
        for (command in commands) {
            if (Server.instance.executeCommand(sender, command) <= 0) success = false
        }
        return success
    }

    companion object {
        fun fromPath(path: Path): Function {
            return Function(path)
        }
    }
}
