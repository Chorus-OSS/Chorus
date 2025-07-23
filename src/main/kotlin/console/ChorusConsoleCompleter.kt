package org.chorus_oss.chorus.console

import org.chorus_oss.chorus.Server
import org.jline.reader.Candidate
import org.jline.reader.Completer
import org.jline.reader.LineReader
import org.jline.reader.ParsedLine
import java.util.*
import java.util.function.Consumer

class ChorusConsoleCompleter(private val server: Server) : Completer {
    override fun complete(lineReader: LineReader, parsedLine: ParsedLine, candidates: MutableList<Candidate>) {
        if (parsedLine.wordIndex() == 0) {
            if (parsedLine.word().isEmpty()) {
                addCandidates { s: String? -> candidates.add(Candidate(s)) }
                return
            }
            val names: SortedSet<String> = TreeSet()
            addCandidates { e: String -> names.add(e) }
            for (match in names) {
                if (!match.lowercase().startsWith(parsedLine.word())) {
                    continue
                }

                candidates.add(Candidate(match))
            }
        } else if (parsedLine.wordIndex() > 0 && parsedLine.word().isNotEmpty()) {
            val word = parsedLine.word()
            val names: SortedSet<String> = TreeSet()
            server.onlinePlayers.values.forEach { p -> names.add(p.getEntityName()) }
            for (match in names) {
                if (!match.lowercase().startsWith(word.lowercase())) {
                    continue
                }

                candidates.add(Candidate(match))
            }
        }
    }

    private fun addCandidates(commandConsumer: Consumer<String>) {
        for (command in server.commandMap.commands.keys) {
            if (command == null) continue
            if (!command.contains(":")) {
                commandConsumer.accept(command)
            }
        }
    }
}
