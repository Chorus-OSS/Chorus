package org.chorus_oss.chorus.config

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option

class ChorusOpts : CliktCommand() {
    val disableAnsi: Boolean by option("--disable-ansi", help = "Disables console coloring").flag()
    val enableTitle: Boolean by option("--enable-title", help = "Enables title at the top of the window").flag()
    val verbosity: String? by option("--verbosity", "-v", help = "Set verbosity of logging")
    val language: String? by option("--language", help = "Set a predefined language")

    override fun run() = Unit
}