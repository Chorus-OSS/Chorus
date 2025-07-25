package org.chorus_oss.chorus.command.selector

import org.chorus_oss.chorus.command.exceptions.SelectorSyntaxException


/**
 * 所有可能的选择器类型
 */
enum class SelectorType(val token: String) {
    ALL_PLAYERS("a"),
    ALL_ENTITIES("e"),

    //可通过指定type选择随机非玩家实体
    //https://zh.minecraft.wiki/w/%E7%9B%AE%E6%A0%87%E9%80%89%E6%8B%A9%E5%99%A8
    RANDOM_PLAYER("r"),
    SELF("s"),
    NEAREST_PLAYER("p"),
    NPC_INITIATOR("initiator");

    companion object {
        @Throws(SelectorSyntaxException::class)
        fun parseSelectorType(type: String): SelectorType {
            return when (type) {
                "a" -> ALL_PLAYERS
                "e" -> ALL_ENTITIES
                "r" -> RANDOM_PLAYER
                "s" -> SELF
                "p" -> NEAREST_PLAYER
                "initiator" -> NPC_INITIATOR
                else -> throw SelectorSyntaxException("Unknown selector type: $type")
            }
        }
    }
}
