package org.chorus_oss.chorus.scoreboard.scorer

import org.chorus_oss.chorus.scoreboard.IScoreboard
import org.chorus_oss.chorus.scoreboard.IScoreboardLine
import org.chorus_oss.chorus.scoreboard.data.ScorerType
import org.chorus_oss.protocol.types.scoreboard.ScoreboardEntry

/**
 * 计分板追踪对象
 */
interface IScorer {
    /**
     * Get the tracking object type
     * @return Tracking object type
     */
    val scorerType: ScorerType

    /**
     * Get the name
     * @return Tracking object type
     */
    val name: String

    /**
     * Internal method
     * Convert to network information
     * @param scoreboard
     * @param line
     * @return network information
     */
    fun toNetworkInfo(scoreboard: IScoreboard, line: IScoreboardLine): ScoreboardEntry?
}
