package org.chorus_oss.chorus

import io.netty.util.ResourceLeakDetector
import io.netty.util.internal.logging.InternalLoggerFactory
import io.netty.util.internal.logging.Log4J2LoggerFactory
import joptsimple.OptionParser
import joptsimple.OptionSpec
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.chorus_oss.chorus.nbt.stream.PGZIPOutputStream
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.chorus.utils.Utils.dynamic
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*

/**
 *   _____  _
 *  / ____|| |
 * | |     | |__    ___   _ __  _   _  ___
 * | |     | '_ \  / _ \ | '__|| | | |/ __|
 * | |____ | | | || (_) || |   | |_| |\__ \
 *  \_____||_| |_| \___/ |_|    \__,_||___/
 */
/**
 * The launcher class of Chorus, including the `main` function.
 */
object Chorus : Loggable {
    val GIT_INFO: Properties? = gitInfo
    val VERSION: String = version
    val CODENAME: String = dynamic("Chorus")
    val GIT_COMMIT: String = gitCommit
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

        // Define args
        val parser = OptionParser()
        parser.allowsUnrecognizedOptions()
        val helpSpec: OptionSpec<Void> = parser.accepts("help", "Shows this page").forHelp()
        val ansiSpec: OptionSpec<Void> = parser.accepts("disable-ansi", "Disables console coloring")
        val titleSpec: OptionSpec<Void> = parser.accepts("enable-title", "Enables title at the top of the window")
        val vSpec: OptionSpec<String> = parser.accepts("v", "Set verbosity of logging").withRequiredArg().ofType(
            String::class.java
        )
        val verbositySpec: OptionSpec<String> =
            parser.accepts("verbosity", "Set verbosity of logging").withRequiredArg().ofType(
                String::class.java
            )
        val languageSpec: OptionSpec<String> =
            parser.accepts("language", "Set a predefined language").withOptionalArg().ofType(
                String::class.java
            )

        // Parse arguments
        val options = parser.parse(*args)

        if (options.has(helpSpec)) {
            try {
                // Display help page
                parser.printHelpOn(System.out)
            } catch (_: IOException) {
                // ignore
            }
            return
        }

        ANSI = !options.has(ansiSpec)
        TITLE = options.has(titleSpec)

        var verbosity = options.valueOf(vSpec)
        if (verbosity == null) {
            verbosity = options.valueOf(verbositySpec)
        }
        if (verbosity != null) {
            try {
                val level = Level.valueOf(verbosity)
                logLevel = level
            } catch (_: Exception) {
                // ignore
            }
        }

        val language = options.valueOf(languageSpec)

        try {
            if (TITLE) {
                print(0x1b.toChar().toString() + "]0;Chorus is starting up..." + 0x07.toChar())
            }
            Server(PATH, DATA_PATH, PLUGIN_PATH, language)
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

    private val gitInfo: Properties?
        get() {
            val gitFileStream: InputStream?
            try {
                gitFileStream = Chorus::class.java.module.getResourceAsStream("git.properties")
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
            if (gitFileStream == null) {
                return null
            }
            val properties = Properties()
            try {
                properties.load(gitFileStream)
            } catch (_: IOException) {
                return null
            }
            return properties
        }

    private val version: String
        get() {
            val resourceAsStream: InputStream? = try {
                Chorus::class.java.module.getResourceAsStream("git.properties")
            } catch (e: IOException) {
                throw RuntimeException(e)
            }

            if (resourceAsStream == null) {
                return "Unknown-Chorus-SNAPSHOT"
            }

            val properties = Properties()
            try {
                resourceAsStream.use { `is` ->
                    InputStreamReader(`is`).use { reader ->
                        BufferedReader(reader).use { buffered ->
                            properties.load(buffered)
                            val line = properties.getProperty("git.build.version")
                            return if ("\${project.version}".equals(line, ignoreCase = true)) {
                                "Unknown-Chorus-SNAPSHOT"
                            } else {
                                line
                            }
                        }
                    }
                }
            } catch (_: IOException) {
                return "Unknown-Chorus-SNAPSHOT"
            }
        }

    private val gitCommit: String
        get() {
            val version = StringBuilder()
            version.append("git-")
            if (GIT_INFO == null) {
                return version.append("null").toString()
            }
            val commitId = GIT_INFO.getProperty("git.commit.id.abbrev")
            return version.append(commitId).toString()
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
