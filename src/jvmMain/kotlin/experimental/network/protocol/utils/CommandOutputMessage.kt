package org.chorus_oss.chorus.experimental.network.protocol.utils

import org.chorus_oss.protocol.types.CommandOutputMessage

operator fun CommandOutputMessage.Companion.invoke(from: org.chorus_oss.chorus.network.protocol.types.CommandOutputMessage): CommandOutputMessage {
    return CommandOutputMessage(
        internal = from.internal,
        messageId = from.messageId,
        parameters = from.parameters.toList(),
    )
}