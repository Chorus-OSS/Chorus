package org.chorus_oss.chorus.command.defaults

import org.chorus_oss.chorus.command.Command

abstract class VanillaCommand : Command {
    constructor(name: String) : super(name)

    constructor(name: String, description: String) : super(name, description)

    constructor(name: String, description: String, usageMessage: String?) : super(name, description, usageMessage)

    constructor(name: String, description: String, usageMessage: String?, aliases: Array<String>) : super(
        name,
        description,
        usageMessage,
        aliases
    )
}
