package org.chorus_oss.chorus.scoreboard

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.event.scoreboard.ScoreboardLineChangeEvent
import org.chorus_oss.chorus.scoreboard.scorer.IScorer


class ScoreboardLine @JvmOverloads constructor(
    override val scoreboard: IScoreboard,
    override val scorer: IScorer,
    override var score: Int = 0
) :
    IScoreboardLine {
    override val lineId: Long = ++staticLineId

    override fun setScore(score: Int): Boolean {
        var score1 = score
        if (scoreboard.shouldCallEvent()) {
            val event = ScoreboardLineChangeEvent(
                scoreboard,
                this,
                score1,
                this.score,
                ScoreboardLineChangeEvent.ActionType.SCORE_CHANGE
            )
            Server.instance.pluginManager.callEvent(event)
            if (event.cancelled) {
                return false
            }
            score1 = event.newValue
        }
        this.score = score1
        updateScore()
        return true
    }

    companion object {
        private var staticLineId: Long = 0
    }
}
