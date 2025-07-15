package org.chorus_oss.chorus.level

import com.google.common.base.Preconditions
import org.chorus_oss.chorus.nbt.tag.*
import org.chorus_oss.chorus.network.connection.util.HandleByteBuf
import java.util.*

class GameRules private constructor() {
    private val gameRules = EnumMap<GameRule, Value<*>>(
        GameRule::class.java
    )
    var isStale: Boolean = false
        private set

    fun getGameRules(): Map<GameRule, Value<*>> {
        return gameRules.toMap()
    }

    fun refresh() {
        isStale = false
    }

    fun <V : Any> setGameRule(gameRule: GameRule, value: V, type: Type) {
        require(gameRules.containsKey(gameRule)) { "Gamerule does not exist" }
        (gameRules[gameRule]!! as Value<V>).setValue(value, type)
        isStale = true
    }

    fun setGameRule(gameRule: GameRule, value: Boolean) {
        setGameRule(gameRule, value, Type.BOOLEAN)
    }

    fun setGameRule(gameRule: GameRule, value: Int) {
        setGameRule(gameRule, value, Type.INTEGER)
    }

    fun setGameRule(gameRule: GameRule, value: Float) {
        setGameRule(gameRule, value, Type.FLOAT)
    }

    @Throws(IllegalArgumentException::class)
    fun setGameRule(gameRule: GameRule, value: String) {
        Preconditions.checkNotNull(gameRule, "gameRule")
        Preconditions.checkNotNull(value, "value")

        when (getGameRuleType(gameRule)) {
            Type.BOOLEAN -> {
                if (value.equals("true", true)) {
                    setGameRule(gameRule, true)
                } else if (value.equals("false", true)) {
                    setGameRule(gameRule, false)
                } else {
                    throw IllegalArgumentException("Was not a boolean")
                }
            }

            Type.INTEGER -> setGameRule(gameRule, Integer.parseInt(value))
            Type.FLOAT -> setGameRule(gameRule, java.lang.Float.parseFloat(value))
            Type.UNKNOWN -> {}
        }
    }

    fun getBoolean(gameRule: GameRule?): Boolean {
        return gameRules[gameRule]!!.valueAsBoolean
    }

    fun getInteger(gameRule: GameRule?): Int {
        Preconditions.checkNotNull(gameRule, "gameRule")
        return gameRules[gameRule]!!.valueAsInteger
    }

    fun getFloat(gameRule: GameRule?): Float {
        Preconditions.checkNotNull(gameRule, "gameRule")
        return gameRules[gameRule]!!.valueAsFloat
    }

    fun getString(gameRule: GameRule?): String {
        Preconditions.checkNotNull(gameRule, "gameRule")
        return gameRules[gameRule]!!.value.toString()
    }

    fun getGameRuleType(gameRule: GameRule?): Type {
        Preconditions.checkNotNull(gameRule, "gameRule")
        return gameRules[gameRule]!!.type
    }

    fun hasRule(gameRule: GameRule?): Boolean {
        return gameRules.containsKey(gameRule)
    }

    val rules: Array<GameRule>
        get() = gameRules.keys.toTypedArray()

    // TODO: This needs to be moved out since there is not a separate compound tag in the LevelDB format for Game Rules.
    fun writeNBT(): CompoundTag {
        val nbt = CompoundTag()

        for (entry in gameRules.entries) {
            nbt.putString(entry.key.gameRuleName, entry.value.value.toString())
        }

        return nbt
    }

    fun readNBT(nbt: CompoundTag) {
        Preconditions.checkNotNull(nbt)
        for (key in nbt.tags.keys) {
            val gameRule: Optional<GameRule> = GameRule.parseString(key)
            if (!gameRule.isPresent) {
                continue
            }

            setGameRule(gameRule.get(), nbt.getString(key))
        }
    }

    enum class Type {
        UNKNOWN {
            override fun write(pk: HandleByteBuf, value: Value<*>) {}
        },
        BOOLEAN {
            override fun write(pk: HandleByteBuf, value: Value<*>) {
                pk.writeBoolean(value.valueAsBoolean)
            }
        },
        INTEGER {
            override fun write(pk: HandleByteBuf, value: Value<*>) {
                pk.writeUnsignedVarInt(value.valueAsInteger)
            }
        },
        FLOAT {
            override fun write(pk: HandleByteBuf, value: Value<*>) {
                pk.writeFloatLE(value.valueAsFloat)
            }
        };

        abstract fun write(pk: HandleByteBuf, value: Value<*>)
    }

    class Value<T>(val type: Type, var value: T) {
        var isCanBeChanged: Boolean = false
            private set

        fun setValue(value: T, type: Type) {
            if (this.type !== type) {
                throw UnsupportedOperationException("Rule not of type " + type.name.lowercase(Locale.ENGLISH))
            }
            this.value = value
        }

        val tag: Tag<*>
            get() = when (this.type) {
                Type.BOOLEAN -> ByteTag(if (valueAsBoolean) 1 else 0)
                Type.INTEGER -> IntTag(valueAsInteger)
                Type.FLOAT -> FloatTag(valueAsFloat)
                Type.UNKNOWN -> throw IllegalArgumentException("unknown type")
            }

        val valueAsBoolean: Boolean
            get() {
                if (type !== Type.BOOLEAN) {
                    throw UnsupportedOperationException("Rule not of type boolean")
                }
                return value as Boolean
            }

        val valueAsInteger: Int
            get() {
                if (type !== Type.INTEGER) {
                    throw UnsupportedOperationException("Rule not of type integer")
                }
                return value as Int
            }

        val valueAsFloat: Float
            get() {
                if (type !== Type.FLOAT) {
                    throw UnsupportedOperationException("Rule not of type float")
                }
                return value as Float
            }

        fun write(stream: HandleByteBuf) {
            stream.writeBoolean(this.isCanBeChanged)
            stream.writeUnsignedVarInt(type.ordinal)
            type.write(stream, this)
        }
    }

    companion object {
        val default: GameRules
            get() {
                val gameRules = GameRules()

                gameRules.gameRules[GameRule.COMMAND_BLOCKS_ENABLED] =
                    Value(Type.BOOLEAN, true)
                gameRules.gameRules[GameRule.COMMAND_BLOCK_OUTPUT] =
                    Value(Type.BOOLEAN, true)
                gameRules.gameRules[GameRule.DO_DAYLIGHT_CYCLE] =
                    Value(Type.BOOLEAN, true)
                gameRules.gameRules[GameRule.DO_ENTITY_DROPS] =
                    Value(Type.BOOLEAN, true)
                gameRules.gameRules[GameRule.DO_FIRE_TICK] =
                    Value(Type.BOOLEAN, true)
                gameRules.gameRules[GameRule.DO_INSOMNIA] =
                    Value(Type.BOOLEAN, true)
                gameRules.gameRules[GameRule.DO_IMMEDIATE_RESPAWN] =
                    Value(Type.BOOLEAN, false)
                gameRules.gameRules[GameRule.DO_MOB_LOOT] =
                    Value(Type.BOOLEAN, true)
                gameRules.gameRules[GameRule.DO_MOB_SPAWNING] =
                    Value(Type.BOOLEAN, true)
                gameRules.gameRules[GameRule.DO_TILE_DROPS] =
                    Value(Type.BOOLEAN, true)
                gameRules.gameRules[GameRule.DO_WEATHER_CYCLE] =
                    Value(Type.BOOLEAN, true)
                gameRules.gameRules[GameRule.DROWNING_DAMAGE] =
                    Value(Type.BOOLEAN, true)
                gameRules.gameRules[GameRule.FALL_DAMAGE] =
                    Value(Type.BOOLEAN, true)
                gameRules.gameRules[GameRule.FIRE_DAMAGE] =
                    Value(Type.BOOLEAN, true)
                gameRules.gameRules[GameRule.FREEZE_DAMAGE] =
                    Value(Type.BOOLEAN, true)
                gameRules.gameRules[GameRule.FUNCTION_COMMAND_LIMIT] =
                    Value(Type.INTEGER, 10000)
                gameRules.gameRules[GameRule.KEEP_INVENTORY] =
                    Value(Type.BOOLEAN, false)
                gameRules.gameRules[GameRule.MAX_COMMAND_CHAIN_LENGTH] =
                    Value(Type.INTEGER, 65536)
                gameRules.gameRules[GameRule.MOB_GRIEFING] =
                    Value(Type.BOOLEAN, true)
                gameRules.gameRules[GameRule.NATURAL_REGENERATION] =
                    Value(Type.BOOLEAN, true)
                gameRules.gameRules[GameRule.PVP] =
                    Value(Type.BOOLEAN, true)
                gameRules.gameRules[GameRule.RANDOM_TICK_SPEED] =
                    Value(Type.INTEGER, 3)
                gameRules.gameRules[GameRule.SEND_COMMAND_FEEDBACK] =
                    Value(Type.BOOLEAN, true)
                gameRules.gameRules[GameRule.SHOW_COORDINATES] =
                    Value(Type.BOOLEAN, false)
                gameRules.gameRules[GameRule.SHOW_DEATH_MESSAGES] =
                    Value(Type.BOOLEAN, true)
                gameRules.gameRules[GameRule.SPAWN_RADIUS] =
                    Value(Type.INTEGER, 5)
                gameRules.gameRules[GameRule.TNT_EXPLODES] =
                    Value(Type.BOOLEAN, true)
                gameRules.gameRules[GameRule.SHOW_TAGS] =
                    Value(Type.BOOLEAN, true)
                gameRules.gameRules[GameRule.EXPERIMENTAL_GAMEPLAY] =
                    Value(Type.BOOLEAN, true)
                gameRules.gameRules[GameRule.PLAYERS_SLEEPING_PERCENTAGE] =
                    Value(Type.INTEGER, 100)
                gameRules.gameRules[GameRule.DO_LIMITED_CRAFTING] =
                    Value(Type.BOOLEAN, false)
                gameRules.gameRules[GameRule.RESPAWN_BLOCKS_EXPLODE] =
                    Value(Type.BOOLEAN, true)
                gameRules.gameRules[GameRule.SHOW_BORDER_EFFECT] =
                    Value(Type.BOOLEAN, true)
                gameRules.gameRules[GameRule.SHOW_DAYS_PLAYED] =
                    Value(Type.BOOLEAN, false)
                gameRules.gameRules[GameRule.RECIPES_UNLOCK] =
                    Value(Type.BOOLEAN, false)

                return gameRules
            }
    }
}
