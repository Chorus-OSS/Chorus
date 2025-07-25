package org.chorus_oss.chorus.utils


interface Logger {
    fun emergency(message: String?)

    fun alert(message: String?)

    fun critical(message: String?)

    fun error(message: String?)

    fun warning(message: String?)

    fun notice(message: String?)

    fun info(message: String?)

    fun debug(message: String?)

    fun log(level: LogLevel, message: String?)

    fun emergency(message: String?, t: Throwable?)

    fun alert(message: String?, t: Throwable?)

    fun critical(message: String?, t: Throwable?)

    fun error(message: String?, t: Throwable?)

    fun warning(message: String?, t: Throwable?)

    fun notice(message: String?, t: Throwable?)

    fun info(message: String?, t: Throwable?)

    fun debug(message: String?, t: Throwable?)

    fun log(level: LogLevel, message: String?, t: Throwable?)
}
