package org.chorus_oss.chorus.permission

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.chorus_oss.chorus.utils.Loggable

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class BanEntry(name: String) {
    @JvmField
    val name: String = name.lowercase()
    var creationDate: Date? = null
    var source: String? = "(Unknown)"
    var expirationDate: Date? = null
    var reason: String = "Banned by an operator."

    init {
        this.creationDate = Date()
    }

    fun hasExpired(): Boolean {
        val now = Date()
        return this.expirationDate != null && expirationDate!!.before(now)
    }

    val map: LinkedHashMap<String, String?>
        get() {
            val map =
                LinkedHashMap<String, String?>()
            map["name"] = name
            map["creationDate"] = SimpleDateFormat(FORMAT).format(creationDate)
            map["source"] = source
            map["expireDate"] =
                if (expirationDate != null) SimpleDateFormat(FORMAT).format(expirationDate) else "Forever"
            map["reason"] = reason
            return map
        }

    val string: String
        get() = Gson().toJson(this.map)

    companion object : Loggable {
        const val FORMAT: String = "yyyy-MM-dd HH:mm:ss Z"

        fun fromMap(map: Map<String, String>): BanEntry {
            val banEntry = BanEntry(map["name"]!!)
            try {
                banEntry.creationDate =
                    SimpleDateFormat(FORMAT).parse(map["creationDate"])
                banEntry.expirationDate =
                    if (map["expireDate"] != "Forever") SimpleDateFormat(FORMAT)
                        .parse(map["expireDate"]) else null
            } catch (e: ParseException) {
                log.error("An exception happed while loading the ban list.", e)
            }
            banEntry.source = map["source"]
            banEntry.reason = map["reason"] ?: banEntry.reason
            return banEntry
        }

        fun fromString(str: String?): BanEntry {
            val map = Gson().fromJson<Map<String, String>>(str, object : TypeToken<TreeMap<String?, String?>?>() {
            }.type)
            val banEntry = BanEntry(map["name"]!!)
            try {
                banEntry.creationDate =
                    SimpleDateFormat(FORMAT).parse(map["creationDate"])
                banEntry.expirationDate =
                    if (map["expireDate"] != "Forever") SimpleDateFormat(FORMAT)
                        .parse(map["expireDate"]) else null
            } catch (e: ParseException) {
                log.error("An exception happened while loading a ban entry from the string {}", str, e)
            }
            banEntry.source = map["source"]
            banEntry.reason = map["reason"] ?: banEntry.reason
            return banEntry
        }
    }
}
