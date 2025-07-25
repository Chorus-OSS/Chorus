package org.chorus_oss.chorus.command.selector.args.impl

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.exceptions.SelectorSyntaxException
import org.chorus_oss.chorus.command.selector.ParseUtils
import org.chorus_oss.chorus.command.selector.SelectorType
import org.chorus_oss.chorus.command.selector.args.CachedSimpleSelectorArgument
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.level.Transform
import org.chorus_oss.chorus.scoreboard.scorer.EntityScorer
import org.chorus_oss.chorus.scoreboard.scorer.PlayerScorer
import java.util.function.Predicate

class Scores : CachedSimpleSelectorArgument() {
    @Throws(SelectorSyntaxException::class)
    override fun cache(
        selectorType: SelectorType?,
        sender: CommandSender?,
        basePos: Transform?,
        vararg arguments: String
    ): Predicate<Entity> {
        ParseUtils.singleArgument(arguments.toList().toTypedArray(), keyName)
        val conditions = ArrayList<ScoreCondition>()
        for (entry in arguments[0].substring(1, arguments[0].length - 1).split(SCORE_SEPARATOR)) {
            if (entry.isEmpty()) throw SelectorSyntaxException("Empty score entry is not allowed in selector!")
            val splittedEntry = entry.split(SCORE_JOINER, limit = 2)
            val objectiveName = splittedEntry[0]
            var condition = splittedEntry[1]
            val reversed: Boolean = ParseUtils.checkReversed(condition)
            if (reversed) condition = condition.substring(1)
            if (condition.contains("..")) {
                //条件为一个区间
                var min = Int.MIN_VALUE
                var max = Int.MAX_VALUE
                val splittedScoreScope = condition.split(SCORE_SCOPE_SEPARATOR)
                val minStr = splittedScoreScope[0]
                if (minStr.isNotEmpty()) {
                    min = minStr.toInt()
                }
                val maxStr = splittedScoreScope[1]
                if (maxStr.isNotEmpty()) {
                    max = maxStr.toInt()
                }
                conditions.add(ScoreCondition(objectiveName, min, max, reversed))
            } else {
                //条件为单个数字
                val score = condition.toInt()
                conditions.add(ScoreCondition(objectiveName, score, score, reversed))
            }
        }
        return Predicate { entity: Entity ->
            conditions.stream().allMatch { condition: ScoreCondition -> condition.test(entity) }
        }
    }

    override val keyName: String
        get() = "scores"

    override val priority: Int
        get() = 5

    @JvmRecord
    protected data class ScoreCondition(val objectiveName: String, val min: Int, val max: Int, val reversed: Boolean) {
        fun test(entity: Entity): Boolean {
            val scoreboard =
                Server.instance.scoreboardManager.getScoreboard(objectiveName) ?: return false
            val scorer = if (entity is Player) PlayerScorer(entity) else EntityScorer(entity)
            if (!scoreboard.containLine(scorer)) return false
            val value = scoreboard.getLine(scorer)!!.score
            return (value in min..max) != reversed
        }
    }

    companion object {
        protected const val SCORE_SEPARATOR: String = ","
        protected const val SCORE_JOINER: String = "="
        protected const val SCORE_SCOPE_SEPARATOR: String = ".."
    }
}
