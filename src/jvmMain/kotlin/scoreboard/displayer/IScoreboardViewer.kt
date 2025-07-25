package org.chorus_oss.chorus.scoreboard.displayer

import org.chorus_oss.chorus.scoreboard.IScoreboard
import org.chorus_oss.chorus.scoreboard.IScoreboardLine
import org.chorus_oss.chorus.scoreboard.data.DisplaySlot

/**
 * 计分板观察者 (eg: Player)
 * 此接口用于抽象服务端到客户端的协议层方法
 */
interface IScoreboardViewer {
    /**
     * 在指定槽位显示计分板
     * @param scoreboard 目标计分板
     * @param slot 目标槽位
     */
    fun display(scoreboard: IScoreboard, slot: DisplaySlot?)

    /**
     * 清除指定槽位的显示内容
     * @param slot 目标槽位
     */
    fun hide(slot: DisplaySlot?)

    /**
     * 通知观察者计分板已删除（若计分板在任意显示槽位中，则会一并清除槽位）
     * @param scoreboard 目标计分板
     */
    fun removeScoreboard(scoreboard: IScoreboard)

    /**
     * 通知观察者指定计分板上的指定行已删除
     * @param line 目标行
     */
    fun removeLine(line: IScoreboardLine)

    /**
     * 向观察者发送指定行的新分数
     * @param line 目标行
     */
    fun updateScore(line: IScoreboardLine)
}
