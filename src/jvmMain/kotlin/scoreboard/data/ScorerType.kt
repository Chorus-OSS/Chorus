package org.chorus_oss.chorus.scoreboard.data

/**
 * 追踪对象类型 <br></br>
 * 除了INVALID，其他枚举都有对应[org.chorus_oss.chorus.scoreboard.scorer.IScorer]的实现类 <br></br>
 * 对于插件来说，使用FAKE类型即可
 */
enum class ScorerType {
    //未知类型
    INVALID,

    //玩家
    PLAYER,

    //实体（非玩家）
    ENTITY,

    //虚拟的（常用于显示信息）
    FAKE
}
