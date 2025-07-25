package org.chorus_oss.chorus.scoreboard.storage

import org.chorus_oss.chorus.scoreboard.IScoreboard
import org.chorus_oss.chorus.scoreboard.Scoreboard
import org.chorus_oss.chorus.scoreboard.ScoreboardLine
import org.chorus_oss.chorus.scoreboard.data.DisplaySlot
import org.chorus_oss.chorus.scoreboard.data.ScorerType
import org.chorus_oss.chorus.scoreboard.data.SortOrder
import org.chorus_oss.chorus.scoreboard.scorer.EntityScorer
import org.chorus_oss.chorus.scoreboard.scorer.FakeScorer
import org.chorus_oss.chorus.scoreboard.scorer.IScorer
import org.chorus_oss.chorus.scoreboard.scorer.PlayerScorer
import org.chorus_oss.chorus.utils.Config

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*


class JSONScoreboardStorage(path: String) : IScoreboardStorage {
    protected var filePath: Path = Paths.get(path)
    protected var json: Config? = null

    init {
        try {
            if (!Files.exists(this.filePath)) {
                Files.createFile(this.filePath)
            }
            json = Config(filePath.toFile(), Config.JSON)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun saveScoreboard(scoreboard: IScoreboard) {
        json!!["scoreboard." + scoreboard.objectiveName] = serializeToMap(scoreboard)
        json!!.save()
    }

    override fun saveScoreboard(scoreboards: Collection<IScoreboard>) {
        for (scoreboard in scoreboards) saveScoreboard(scoreboard)
    }

    override fun saveDisplay(display: Map<DisplaySlot, IScoreboard?>) {
        for ((key, value) in display) {
            json!!["display." + key.name] = value?.objectiveName
        }
        json!!.save()
    }

    override fun readScoreboard(): MutableMap<String, IScoreboard> {
        val scoreboards = json!!["scoreboard"] as Map<String, Any>?
        val result: MutableMap<String, IScoreboard> = HashMap()
        if (scoreboards == null) return result
        for ((key, value) in scoreboards) result[key] =
            deserializeFromMap(value as Map<String, Any>)
        return result
    }

    override fun readScoreboard(name: String): IScoreboard? {
        return if (json!!["scoreboard.$name"] == null) null else deserializeFromMap(json!!["scoreboard.$name"] as Map<String, Any>)
    }

    override fun readDisplay(): Map<DisplaySlot, String> {
        val result: MutableMap<DisplaySlot, String> = EnumMap(DisplaySlot::class.java)
        if (json!!["display"] == null) return result
        for ((key, value) in (json!!["display"] as Map<String?, String>)) {
            val slot = DisplaySlot.valueOf(key!!)
            result[slot] = value
        }
        return result
    }

    override fun removeScoreboard(name: String) {
        json!!.remove("scoreboard.$name")
        json!!.save()
    }

    override fun removeAllScoreboard() {
        json!!.remove("scoreboard")
        json!!.save()
    }

    override fun containScoreboard(name: String): Boolean {
        return json!!.exists("scoreboard.$name")
    }

    private fun serializeToMap(scoreboard: IScoreboard): Map<String, Any?> {
        val map: MutableMap<String, Any?> = HashMap()
        map["objectiveName"] = scoreboard.objectiveName
        map["displayName"] = scoreboard.displayName
        map["criteriaName"] = scoreboard.criteriaName
        map["sortOrder"] = scoreboard.sortOrder!!.name
        val lines: MutableList<Map<String, Any>> = ArrayList()
        for (e in scoreboard.lines.values) {
            val line: MutableMap<String, Any> = HashMap()
            line["score"] = e.score
            line["scorerType"] = e.scorer.scorerType.name
            line["name"] = when (e.scorer.scorerType) {
                ScorerType.PLAYER -> (e.scorer as PlayerScorer).uuid.toString()
                ScorerType.ENTITY -> (e.scorer as EntityScorer).uniqueID.toString()
                ScorerType.FAKE -> (e.scorer as FakeScorer).name
                else -> null
            }!!
            lines.add(line)
        }
        map["lines"] = lines
        return map
    }

    private fun deserializeFromMap(map: Map<String, Any>): IScoreboard {
        val objectiveName = map["objectiveName"].toString()
        val displayName = map["displayName"].toString()
        val criteriaName = map["criteriaName"].toString()
        val sortOrder = SortOrder.valueOf(map["sortOrder"].toString())
        val scoreboard: IScoreboard = Scoreboard(objectiveName, displayName, criteriaName, sortOrder)
        for (line in (map["lines"] as List<Map<String, Any>>?)!!) {
            val score = (line["score"] as Double).toInt()
            val scorer: IScorer = when (line["scorerType"].toString()) {
                "PLAYER" -> PlayerScorer(UUID.fromString(line["name"] as String?))
                "ENTITY" -> EntityScorer(UUID.fromString(line["name"] as String?))
                "FAKE" -> FakeScorer((line["name"] as String?)!!)
                else -> throw IllegalArgumentException("Invalid scorerType: ${line["scorerType"]}")
            }
            scoreboard.addLine(ScoreboardLine(scoreboard, scorer, score))
        }
        return scoreboard
    }
}
