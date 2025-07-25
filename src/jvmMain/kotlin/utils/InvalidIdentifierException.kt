package org.chorus_oss.chorus.utils

class InvalidIdentifierException : RuntimeException {
    constructor(message: String?) : super(message)

    constructor(message: String?, throwable: Throwable?) : super(message, throwable)
}

