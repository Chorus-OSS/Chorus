package org.chorus_oss.chorus.lang

import org.chorus_oss.chorus.network.protocol.types.CommandOutputMessage

class CommandOutputContainer : Cloneable {
    @JvmField
    val messages: MutableList<CommandOutputMessage>

    @JvmField
    var successCount: Int

    constructor() {
        this.messages = ArrayList()
        this.successCount = 0
    }

    constructor(
        messageId: String,
        parameters: Array<String>,
        successCount: Int
    ) : this(mutableListOf<CommandOutputMessage>(CommandOutputMessage(false, messageId, parameters)), successCount)

    constructor(messages: MutableList<CommandOutputMessage>, successCount: Int) {
        this.messages = messages
        this.successCount = successCount
    }

    fun incrementSuccessCount() {
        successCount++
    }

    @Throws(CloneNotSupportedException::class)
    override fun clone(): CommandOutputContainer {
        return super.clone() as CommandOutputContainer
    }

    companion object {
        val EMPTY_STRING: Array<String> = arrayOf()
    }
}
