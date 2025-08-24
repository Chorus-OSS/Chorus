package org.chorus_oss.chorus

import com.github.ajalt.clikt.core.main
import io.netty.util.ResourceLeakDetector
import io.netty.util.internal.logging.InternalLoggerFactory
import io.netty.util.internal.logging.Log4J2LoggerFactory
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.chorus_oss.chorus.config.ChorusOpts
import org.chorus_oss.chorus.nbt.stream.PGZIPOutputStream
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.chorus.utils.Utils.dynamic

/**
 *   _____  _
 *  / ____|| |
 * | |     | |__    ___   _ __  _   _  ___
 * | |     | '_ \  / _ \ | '__|| | | |/ __|
 * | |____ | | | || (_) || |   | |_| |\__ \
 *  \_____||_| |_| \___/ |_|    \__,_||___/
 */
object Chorus : Loggable {
    const val VERSION: String = "1.0-SNAPSHOT"
    val API_VERSION: String = dynamic("0.0.1")
    val PATH: String = System.getProperty("user.dir") + "/"
    val DATA_PATH: String = System.getProperty("user.dir") + "/"
    val PLUGIN_PATH: String = DATA_PATH + "plugins"
    val START_TIME: Long = System.currentTimeMillis()
    var ANSI: Boolean = true
    var TITLE: Boolean = false
    var shortTitle: Boolean = requiresShortTitle()

    @JvmStatic
    fun main(args: Array<String>) {
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED)

//        val disableSentry = AtomicBoolean(false)
//        disableSentry.set(System.getProperty("disableSentry", "false").toBoolean())
//
//        val propertiesPath = Paths.get(DATA_PATH, "server.properties")
//        if (!disableSentry.get() && Files.isRegularFile(propertiesPath)) {
//            val properties = Properties()
//            try {
//                FileReader(propertiesPath.toFile()).use { reader ->
//                    properties.load(reader)
//                    var value = properties.getProperty("disable-auto-bug-report", "false")
//                    if (value.equals("on", ignoreCase = true) || value == "1") {
//                        value = "true"
//                    }
//                    disableSentry.set(value.lowercase().toBoolean())
//                }
//            } catch (e: IOException) {
//                log.error("Failed to load server.properties to check disable-auto-bug-report.", e)
//            }
//        }

        // Force IPv4 since Chorus is not compatible with IPv6
        System.setProperty("java.net.preferIPv4Stack", "true")
        System.setProperty("log4j.skipJansi", "false")
        System.getProperties()
            .putIfAbsent("io.netty.allocator.type", "unpooled") // Disable memory pooling unless specified

        // Force Mapped ByteBuffers for LevelDB till fixed.
        System.setProperty("leveldb.mmap", "true")

        // Netty logger for debug info
        InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE)

        val opts = ChorusOpts().also { it.main(args) }

        // Define args
        ANSI = !opts.disableAnsi
        TITLE = opts.enableTitle

        if (opts.verbosity != null) {
            try {
                val level = Level.valueOf(opts.verbosity)
                logLevel = level
            } catch (_: Exception) {
                // ignore
            }
        }

        try {
            if (TITLE) {
                print(0x1b.toChar().toString() + "]0;Chorus is starting up..." + 0x07.toChar())
            }
            Server(PATH, DATA_PATH, PLUGIN_PATH, opts.language)
        } catch (t: Throwable) {
            log.error("", t)
        }

        if (TITLE) {
            print(0x1b.toChar().toString() + "]0;Stopping Server..." + 0x07.toChar())
        }
        log.info("Stopping other threads")

        PGZIPOutputStream.sharedThreadPool.shutdownNow()

        if (TITLE) {
            print(0x1b.toChar().toString() + "]0;Server Stopped" + 0x07.toChar())
        }
        LogManager.shutdown()
        Runtime.getRuntime().halt(0) // force exit
    }

    private fun requiresShortTitle(): Boolean {
        //Shorter title for Windows 8/2012
        val osName = System.getProperty("os.name").lowercase()
        return osName.contains("windows") && (osName.contains("windows 8") || osName.contains("2012"))
    }

    var logLevel: Level?
        get() {
            val ctx =
                LogManager.getContext(false) as LoggerContext
            val log4jConfig = ctx.configuration
            val loggerConfig =
                log4jConfig.getLoggerConfig(LogManager.ROOT_LOGGER_NAME)
            return loggerConfig.level
        }
        set(level) {
            val ctx = LogManager.getContext(false) as LoggerContext
            val log4jConfig = ctx.configuration
            val loggerConfig = log4jConfig.getLoggerConfig(LogManager.ROOT_LOGGER_NAME)
            loggerConfig.level = level
            ctx.updateLoggers()
        }
}
