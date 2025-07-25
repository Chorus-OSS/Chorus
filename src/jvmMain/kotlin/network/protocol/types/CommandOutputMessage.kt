package org.chorus_oss.chorus.network.protocol.types


data class CommandOutputMessage(
    var internal: Boolean,
    var messageId: String,
    var parameters: Array<String>
) {
    constructor(messageId: String) : this(false, messageId, arrayOf())

    constructor(messageId: String, vararg parameters: String) : this(false, messageId, arrayOf(*parameters))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CommandOutputMessage

        if (internal != other.internal) return false
        if (messageId != other.messageId) return false
        if (!parameters.contentEquals(other.parameters)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = internal.hashCode()
        result = 31 * result + messageId.hashCode()
        result = 31 * result + parameters.contentHashCode()
        return result
    }
}
