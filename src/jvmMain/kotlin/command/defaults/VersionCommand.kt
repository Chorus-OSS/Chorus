package org.chorus_oss.chorus.command.defaults

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.Command
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.lang.TranslationContainer
import org.chorus_oss.chorus.utils.TextFormat
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.function.Consumer

class VersionCommand(name: String) : Command(
    name,
    "%chorus.command.version.description",
    "%chorus.command.version.usage",
    arrayOf("ver", "about")
), CoreCommand {
    private val queryQueue: MutableList<Query> = LinkedList<Query>()
    private var lastUpdateTick = 0
    private var listVersionCache: JsonArray? = null

    init {
        Server.instance.scheduler.scheduleRepeatingTask(null, {
            try {
                for (query in queryQueue.toTypedArray<Query>()) {
                    if (query.jsonArrayFuture.isDone) {
                        val cores = query.jsonArrayFuture.get()
                        query.sender.sendMessage("####################")
                        var i = 0
                        val len = cores.size()
                        while (i < len) {
                            val entry = cores[i].asJsonObject

                            val infoBuilder = StringBuilder()
                            infoBuilder.append("[").append(i + 1).append("] ")
                            if (i == 0) infoBuilder.append("Name: §e").append(entry["name"].asString)
                                .append("§f, Time: §e").append(utcToLocal(entry["lastModified"].asString))
                            else infoBuilder.append("Name: §a").append(entry["name"].asString)
                                .append("§f, Time: §a").append(utcToLocal(entry["lastModified"].asString))
                            //打印相关信息
                            query.sender.sendMessage(infoBuilder.toString())
                            i++
                        }
                        query.sender.sendMessage("####################")
                        queryQueue.remove(query)
                    }
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
        }, 15)
    }

    init {
        this.permission = "chorus.command.version"
        commandParameters.clear()
        commandParameters["default"] = arrayOf(
            CommandParameter.newType("pluginName", true, CommandParamType.STRING)
        )
    }

    override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
        if (!this.testPermission(sender)) {
            return true
        }
        if (args.size == 0) {
            sender.sendMessage(
                TranslationContainer(
                    "chorus.server.info.extended", Server.instance.name,
                    Server.instance.chorusVersion,
                    Server.instance.apiVersion,
                    Server.instance.version,
                    org.chorus_oss.protocol.ProtocolInfo.VERSION.toString()
                )
            )
        } else {
            var pluginName = StringBuilder()
            for (arg in args) pluginName.append(arg).append(" ")
            pluginName = StringBuilder(pluginName.toString().trim { it <= ' ' })
            val found = booleanArrayOf(false)
            val exactPlugin = arrayOf(Server.instance.pluginManager.getPlugin(pluginName.toString()))

            if (exactPlugin[0] == null) {
                pluginName = StringBuilder(pluginName.toString().lowercase())
                val finalPluginName = pluginName.toString()
                Server.instance.pluginManager.plugins.forEach { (s, p) ->
                    if (s.lowercase().contains(finalPluginName)) {
                        exactPlugin[0] = p
                        found[0] = true
                    }
                }
            } else {
                found[0] = true
            }

            if (found[0]) {
                val desc = exactPlugin[0]!!.description
                sender.sendMessage(TextFormat.DARK_GREEN.toString() + desc.name + TextFormat.WHITE + " version " + TextFormat.DARK_GREEN + desc.version)
                if (desc.description != null) {
                    sender.sendMessage(desc.description!!)
                }
                val authors: List<String> = desc.authors
                val authorsString = arrayOf("")
                authors.forEach(Consumer { s: String -> authorsString[0] += s })
                if (authors.size == 1) {
                    sender.sendMessage("Author: " + authorsString[0])
                } else if (authors.size >= 2) {
                    sender.sendMessage("Authors: " + java.lang.String.join(", ", authors))
                }
            } else {
                sender.sendMessage(TranslationContainer("chorus.command.version.noSuchPlugin"))
            }
        }
        return true
    }

    private fun listVersion(): CompletableFuture<JsonArray> {
        return CompletableFuture.supplyAsync<JsonArray> {
            if (this.listVersionCache != null) {
                if (Server.instance.tick - this.lastUpdateTick < 7200) { //20 * 60 * 60 一小时
                    return@supplyAsync this.listVersionCache
                }
            }
            val client = HttpClient.newHttpClient()
            val builder =
                HttpRequest.newBuilder(URI.create("https://api.powernukkitx.cn/get-core-manifest?max=100"))
                    .GET()
            builder.setHeader(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36"
            )
            val request = builder.build()
            try {
                val result = JsonParser.parseString(
                    client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                    ).body()
                )
                if (result.isJsonArray) {
                    this.lastUpdateTick = Server.instance.tick
                    this.listVersionCache = result.asJsonArray
                    return@supplyAsync this.listVersionCache
                }
                return@supplyAsync JsonArray()
            } catch (e: IOException) {
                return@supplyAsync JsonArray()
            } catch (e: InterruptedException) {
                return@supplyAsync JsonArray()
            }
        }
    }

    protected fun utcToLocal(utcTime: String?): String {
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        var utcDate: Date? = null
        try {
            utcDate = sdf.parse(utcTime)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return df.format(utcDate)
    }

    @JvmRecord
    private data class Query(val sender: CommandSender, val jsonArrayFuture: CompletableFuture<JsonArray>)
}
