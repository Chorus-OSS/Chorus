package org.chorus_oss.chorus.experimental.network.protocol.utils

import org.chorus_oss.chorus.level.GameRules
import org.chorus_oss.protocol.types.GameRule

operator fun GameRule.Companion.invoke(from: Pair<org.chorus_oss.chorus.level.GameRule, GameRules.Value<*>>): GameRule<*> {
    return GameRule(
        name = from.first.gameRuleName,
        canBeModifiedByPlayer = from.second.isCanBeChanged,
        value = from.second.value
    )
}