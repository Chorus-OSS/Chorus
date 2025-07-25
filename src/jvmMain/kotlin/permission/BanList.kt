package org.chorus_oss.chorus.permission

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.chorus_oss.chorus.utils.Loggable
import org.chorus_oss.chorus.utils.Utils
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*


class BanList(private val file: String) : Loggable {
    private var list = LinkedHashMap<String, BanEntry>()

    var enabled: Boolean = true

    val entries: LinkedHashMap<String, BanEntry>
        get() {
            removeExpired()
            return this.list
        }

    fun isBanned(name: String?): Boolean {
        if (!this.enabled || name == null) {
            return false
        } else {
            this.removeExpired()

            return list.containsKey(name.lowercase())
        }
    }

    fun add(entry: BanEntry) {
        list[entry.name] = entry
        this.save()
    }

    @JvmOverloads
    fun addBan(target: String, reason: String? = null, expireDate: Date? = null): BanEntry {
        return this.addBan(target, reason, expireDate, null)
    }

    fun addBan(target: String, reason: String?, expireDate: Date?, source: String?): BanEntry {
        val entry = BanEntry(target)
        entry.source = source ?: entry.source
        entry.expirationDate = expireDate
        entry.reason = reason ?: entry.reason

        this.add(entry)

        return entry
    }

    fun remove(name: String) {
        var name = name
        name = name.lowercase()
        if (list.containsKey(name)) {
            list.remove(name)
            this.save()
        }
    }

    fun removeExpired() {
        for (name in ArrayList(list.keys)) {
            val entry = list[name]
            if (entry!!.hasExpired()) {
                list.remove(name)
            }
        }
    }

    fun load() {
        this.list = LinkedHashMap()
        val file = File(this.file)
        try {
            if (!file.exists()) {
                file.createNewFile()
                this.save()
            } else {
                val list = Gson().fromJson<LinkedList<TreeMap<String, String>>>(
                    Utils.readFile(
                        this.file
                    ), object : TypeToken<LinkedList<TreeMap<String, String>>>() {
                    }.type
                )
                for (map in list) {
                    val entry: BanEntry = BanEntry.fromMap(map)
                    this.list[entry.name] = entry
                }
            }
        } catch (e: IOException) {
            log.error("Could not load ban list: ", e)
        }
    }

    fun save() {
        this.removeExpired()

        try {
            val file = File(this.file)
            if (!file.exists()) {
                file.createNewFile()
            }

            val list = LinkedList<LinkedHashMap<String, String?>>()
            for (entry in this.list.values) {
                list.add(entry.map)
            }
            Utils.writeFile(
                this.file, ByteArrayInputStream(
                    GsonBuilder().setPrettyPrinting().create().toJson(list).toByteArray(
                        StandardCharsets.UTF_8
                    )
                )
            )
        } catch (e: IOException) {
            log.error("Could not save ban list ", e)
        }
    }
}
