package org.chorus_oss.chorus.registry

import org.chorus_oss.chorus.utils.exception.FormativeException

class RegisterException : FormativeException {
    constructor(msg: String?) : super(msg)

    constructor(format: String, vararg arguments: Any) : super(format, *arguments)

    constructor(e: Exception?) : super(e)
}
