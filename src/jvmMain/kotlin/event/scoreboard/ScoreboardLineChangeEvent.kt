package org.chorus_oss.chorus.event.scoreboard

import org.chorus_oss.chorus.event.HandlerList
import org.chorus_oss.chorus.scoreboard.IScoreboard
import org.chorus_oss.chorus.scoreboard.IScoreboardLine


class ScoreboardLineChangeEvent @JvmOverloads constructor(
    scoreboard: IScoreboard,
    val line: IScoreboardLine?,
    val newValue: Int,
    val oldValue: Int,
    val actionType: ActionType = ActionType.SCORE_CHANGE
) :
    ScoreboardEvent(scoreboard) {
    enum class ActionType {
        SCORE_CHANGE,
        REMOVE_LINE,
        REMOVE_ALL_LINES,
        ADD_LINE
    }

    companion object {
        val handlers: HandlerList = HandlerList()
    }
}
