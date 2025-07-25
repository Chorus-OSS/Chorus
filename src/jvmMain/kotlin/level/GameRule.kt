package org.chorus_oss.chorus.level

import java.util.*

enum class GameRule {
    COMMAND_BLOCKS_ENABLED("commandBlocksEnabled"),
    COMMAND_BLOCK_OUTPUT("commandBlockOutput"),
    DO_DAYLIGHT_CYCLE("doDaylightCycle"),
    DO_ENTITY_DROPS("doEntityDrops"),
    DO_FIRE_TICK("doFireTick"),
    DO_INSOMNIA("doInsomnia"),
    DO_IMMEDIATE_RESPAWN("doImmediateRespawn"),
    DO_MOB_LOOT("doMobLoot"),
    DO_MOB_SPAWNING("doMobSpawning"),
    DO_TILE_DROPS("doTileDrops"),
    DO_WEATHER_CYCLE("doWeatherCycle"),
    DROWNING_DAMAGE("drowningDamage"),
    FALL_DAMAGE("fallDamage"),
    FIRE_DAMAGE("fireDamage"),
    FREEZE_DAMAGE("freezeDamage"),
    FUNCTION_COMMAND_LIMIT("functionCommandLimit"),
    KEEP_INVENTORY("keepInventory"),
    MAX_COMMAND_CHAIN_LENGTH("maxCommandChainLength"),
    MOB_GRIEFING("mobGriefing"),
    NATURAL_REGENERATION("naturalRegeneration"),
    PVP("pvp"),
    RANDOM_TICK_SPEED("randomTickSpeed"),
    SEND_COMMAND_FEEDBACK("sendCommandFeedback"),
    SHOW_COORDINATES("showCoordinates"),
    SHOW_DEATH_MESSAGES("showDeathMessages"),
    SHOW_DEATH_MESSAGE(SHOW_DEATH_MESSAGES.gameRuleName, true),
    SPAWN_RADIUS("spawnRadius"),
    TNT_EXPLODES("tntExplodes"),
    EXPERIMENTAL_GAMEPLAY("experimentalGameplay"),
    SHOW_TAGS("showTags"),
    PLAYERS_SLEEPING_PERCENTAGE("playersSleepingPercentage"),
    DO_LIMITED_CRAFTING("dolimitedcrafting"),
    RESPAWN_BLOCKS_EXPLODE("respawnBlocksExplode"),
    SHOW_BORDER_EFFECT("showBorderEffect"),
    RECIPES_UNLOCK("recipesUnlock"),
    SHOW_DAYS_PLAYED("showDaysPlayed");

    val gameRuleName: String
    val isDeprecated: Boolean

    constructor(name: String) {
        this.gameRuleName = name
        this.isDeprecated = false
    }

    constructor(name: String, deprecated: Boolean) {
        this.gameRuleName = name
        this.isDeprecated = deprecated
    }

    companion object {
        val EMPTY_ARRAY: Array<GameRule?> = arrayOfNulls(0)
        fun parseString(gameRuleString: String?): Optional<GameRule> {
            // Backward compatibility
            var gameRuleString1 = gameRuleString
            if ("showDeathMessage".equals(gameRuleString1, true)) {
                gameRuleString1 = "showDeathMessages"
            }

            for (gameRule in entries) {
                if (gameRule.gameRuleName.equals(gameRuleString1, true)) {
                    return Optional.of(gameRule)
                }
            }
            return Optional.empty()
        }

        val names: Array<String>
            get() {
                return entries.map { it.gameRuleName }.toTypedArray()
            }
    }
}
