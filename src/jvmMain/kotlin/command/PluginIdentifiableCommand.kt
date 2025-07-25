package org.chorus_oss.chorus.command

import org.chorus_oss.chorus.plugin.Plugin


interface PluginIdentifiableCommand {
    val plugin: Plugin
}
