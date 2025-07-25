package org.chorus_oss.chorus.scoreboard.storage

import org.chorus_oss.chorus.scoreboard.IScoreboard
import org.chorus_oss.chorus.scoreboard.data.DisplaySlot

/**
 * 计分板存储器接口
 */
interface IScoreboardStorage {
    fun saveScoreboard(scoreboard: IScoreboard)

    fun saveScoreboard(scoreboards: Collection<IScoreboard>)

    fun saveDisplay(display: Map<DisplaySlot, IScoreboard?>)

    fun readScoreboard(): MutableMap<String, IScoreboard>

    fun readScoreboard(name: String): IScoreboard?

    fun readDisplay(): Map<DisplaySlot, String>

    fun removeScoreboard(name: String)

    fun removeAllScoreboard()

    fun containScoreboard(name: String): Boolean
}
