package org.chorus_oss.chorus.plugin

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.chorus_oss.chorus.utils.LogLevel
import org.chorus_oss.chorus.utils.Logger

class PluginLogger(context: Plugin) : Logger {
    private val pluginName: String
    private val log: org.apache.logging.log4j.Logger

    init {
        val prefix = context.description.prefix
        log = LogManager.getLogger(context.description.main)
        this.pluginName = prefix ?: context.description.name
    }

    override fun emergency(message: String?) {
        this.log(LogLevel.EMERGENCY, message)
    }

    override fun alert(message: String?) {
        this.log(LogLevel.ALERT, message)
    }

    override fun critical(message: String?) {
        this.log(LogLevel.CRITICAL, message)
    }

    override fun error(message: String?) {
        this.log(LogLevel.ERROR, message)
    }

    override fun warning(message: String?) {
        this.log(LogLevel.WARNING, message)
    }

    override fun notice(message: String?) {
        this.log(LogLevel.NOTICE, message)
    }

    override fun info(message: String?) {
        this.log(LogLevel.INFO, message)
    }

    override fun debug(message: String?) {
        this.log(LogLevel.DEBUG, message)
    }

    private fun toApacheLevel(level: LogLevel): Level {
        return when (level) {
            LogLevel.NONE -> Level.OFF
            LogLevel.EMERGENCY, LogLevel.CRITICAL -> Level.FATAL
            LogLevel.ALERT, LogLevel.WARNING, LogLevel.NOTICE -> Level.WARN
            LogLevel.ERROR -> Level.ERROR
            LogLevel.DEBUG -> Level.DEBUG
            else -> Level.INFO
        }
    }

    override fun log(level: LogLevel, message: String?) {
        log.log(toApacheLevel(level), "[{}]: {}", this.pluginName, message)
    }

    override fun emergency(message: String?, t: Throwable?) {
        this.log(LogLevel.EMERGENCY, message, t)
    }

    override fun alert(message: String?, t: Throwable?) {
        this.log(LogLevel.ALERT, message, t)
    }

    override fun critical(message: String?, t: Throwable?) {
        this.log(LogLevel.CRITICAL, message, t)
    }

    override fun error(message: String?, t: Throwable?) {
        this.log(LogLevel.ERROR, message, t)
    }

    override fun warning(message: String?, t: Throwable?) {
        this.log(LogLevel.WARNING, message, t)
    }

    override fun notice(message: String?, t: Throwable?) {
        this.log(LogLevel.NOTICE, message, t)
    }

    override fun info(message: String?, t: Throwable?) {
        this.log(LogLevel.INFO, message, t)
    }

    override fun debug(message: String?, t: Throwable?) {
        this.log(LogLevel.DEBUG, message, t)
    }

    override fun log(level: LogLevel, message: String?, t: Throwable?) {
        log.log(toApacheLevel(level), "[{}]: {}", this.pluginName, message, t)
    }
}
