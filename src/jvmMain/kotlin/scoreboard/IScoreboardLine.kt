package org.chorus_oss.chorus.scoreboard

import org.chorus_oss.chorus.scoreboard.scorer.IScorer
import org.chorus_oss.protocol.types.scoreboard.ScoreboardEntry

/**
 * 计分板上的单个行 <br></br>
 * 由[IScorer]和分数组成
 */
interface IScoreboardLine {
    /**
     * GetTrackingObject
     * @return TrackingObjects
     */
    val scorer: IScorer

    /**
     * Get the line id
     * The client identifies each row on the scoreboard through this id
     * @return line id
     */
    val lineId: Long

    /**
     * Get the scoreboard to which this branch belongs
     * @return Scoreboard
     */
    val scoreboard: IScoreboard

    /**
     * Get scores
     * @return score
     */
    val score: Int

    /**
     * Set the score
     * @param score
     * @return Whether the success (event is withdrawn will be false)
     */
    fun setScore(score: Int): Boolean

    /**
     * Increase score
     * @param addition Increase amount
     * @return Whether the success (event is withdrawn will be false)
     */
    fun addScore(addition: Int): Boolean {
        return setScore(score + addition)
    }

    /**
     * Reduce score
     * @param reduction
     * @return Whether the success (event is withdrawn will be false)
     */
    fun removeScore(reduction: Int): Boolean {
        return setScore(score - reduction)
    }

    /**
     * Internal method
     * Convert to network information
     * @return network information
     */
    fun toNetworkInfo(): ScoreboardEntry? {
        return scorer.toNetworkInfo(scoreboard, this)
    }

    /**
     * Internal method
     * Notify the scoreboard object to update this line information
     */
    fun updateScore() {
        scoreboard.updateScore(this)
    }
}
