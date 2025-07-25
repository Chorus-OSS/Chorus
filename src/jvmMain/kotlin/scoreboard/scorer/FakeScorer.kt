package org.chorus_oss.chorus.scoreboard.scorer

import org.chorus_oss.chorus.scoreboard.IScoreboard
import org.chorus_oss.chorus.scoreboard.IScoreboardLine
import org.chorus_oss.chorus.scoreboard.data.ScorerType
import org.chorus_oss.protocol.types.scoreboard.ScoreboardEntry

class FakeScorer(override val name: String) : IScorer {
    override val scorerType: ScorerType
        get() = ScorerType.FAKE

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is FakeScorer) {
            return other.name == name
        }
        return false
    }

    override fun toNetworkInfo(scoreboard: IScoreboard, line: IScoreboardLine): ScoreboardEntry {
        return ScoreboardEntry(
            entryID = line.lineId,
            objectiveName = scoreboard.objectiveName,
            score = line.score,
            identityType = ScoreboardEntry.Companion.IdentityType.FakePlayer,
            entityUniqueID = null,
            displayName = name,
        )
    }
}
