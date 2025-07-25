package org.chorus_oss.chorus.utils


class PluginException : ServerException {
    constructor(message: String?) : super(message)

    constructor(message: String?, cause: Throwable?) : super(message, cause)
}
