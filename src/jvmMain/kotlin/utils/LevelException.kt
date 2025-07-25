package org.chorus_oss.chorus.utils


class LevelException : ServerException {
    constructor(message: String?) : super(message)

    constructor(message: String?, cause: Throwable?) : super(message, cause)
}
