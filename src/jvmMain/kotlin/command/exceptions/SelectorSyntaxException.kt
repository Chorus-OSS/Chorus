package org.chorus_oss.chorus.command.exceptions


class SelectorSyntaxException : Exception {
    constructor()

    constructor(message: String?) : super(message)

    constructor(message: String?, cause: Throwable?) : super(message, cause)

    constructor(cause: Throwable?) : super(cause)

    protected constructor(
        message: String?,
        cause: Throwable?,
        enableSuppression: Boolean,
        writableStackTrace: Boolean
    ) : super(message, cause, enableSuppression, writableStackTrace)

    override val message: String
        get() {
            val builder = StringBuilder(super.message)
            var t: Throwable? = this
            while (t!!.cause != null) {
                //到达最底层
                t = t.cause
                builder.append("\n")
                builder.append("§cCaused by ")
                builder.append(t!!.javaClass.simpleName)
                builder.append(": ")
                builder.append(t.message)
            }
            return builder.toString()
        }
}
