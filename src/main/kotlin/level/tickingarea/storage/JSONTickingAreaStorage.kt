package org.chorus_oss.chorus.level.tickingarea.storage

import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import com.google.gson.reflect.TypeToken
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.level.tickingarea.TickingArea
import org.chorus_oss.chorus.utils.JSONUtils
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

class JSONTickingAreaStorage(path: String) : TickingAreaStorage {
    /**
     * Store the root directory of the frequently loaded area
     */
    private var filePath: Path = Paths.get(path)

    //               row     column
    //            LevelName AreaName
    private var areaMap: Table<String, String, TickingArea> = HashBasedTable.create()

    init {
        try {
            if (!Files.exists(this.filePath)) {
                Files.createDirectories(this.filePath)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun addTickingArea(area: TickingArea) {
        areaMap.put(area.levelName, area.name, area)
        save()
    }

    override fun addTickingArea(vararg areas: TickingArea) {
        for (each in areas) {
            areaMap.put(each.levelName, each.name, each)
        }
        save()
    }

    override fun readTickingArea(): MutableMap<String, TickingArea> {
        val rootDir = File(filePath.toString())
        val aMap: MutableMap<String, TickingArea> = HashMap<String, TickingArea>()
        for (each in requireNotNull(rootDir.listFiles())) {
            val jsonFile = File(each, "tickingarea.json")
            if (jsonFile.exists()) {
                try {
                    FileReader(jsonFile).use { fr ->
                        val areas: Set<TickingArea> = JSONUtils.from(fr, type)
                        for (area in areas) {
                            areaMap.put(area.levelName, area.name, area)
                            aMap[area.name] = area
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return aMap
    }

    override fun removeTickingArea(name: String) {
        areaMap.columnMap().remove(name)
        save()
    }

    override fun removeAllTickingArea() {
        areaMap.clear()
        save()
    }

    override fun containTickingArea(name: String?): Boolean {
        return areaMap.containsColumn(name)
    }

    private fun save() {
        try {
            for (level in Server.instance.levels.values) {
                if (areaMap.containsRow(level.name)) {
                    Files.writeString(
                        Path.of(filePath.toString(), level.folderName, "tickingarea.json"),
                        JSONUtils.toPretty<Array<Any>>(
                            areaMap.rowMap()[level.name]!!.values.toTypedArray()
                        )
                    )
                } else {
                    Files.deleteIfExists(Path.of(filePath.toString(), level.folderName, "tickingarea.json"))
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    companion object {
        private val type = object : TypeToken<HashSet<TickingArea>>() {}
    }
}
