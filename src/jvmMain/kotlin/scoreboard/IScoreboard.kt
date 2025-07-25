package org.chorus_oss.chorus.scoreboard

import org.chorus_oss.chorus.scoreboard.data.DisplaySlot
import org.chorus_oss.chorus.scoreboard.data.SortOrder
import org.chorus_oss.chorus.scoreboard.displayer.IScoreboardViewer
import org.chorus_oss.chorus.scoreboard.scorer.IScorer

/**
 * 计分板对象
 * 可被发送到任何实现了[IScoreboardViewer]接口的对象上
 */
interface IScoreboard {
    /**
     * @return 此计分板的标识名称
     */
    val objectiveName: String

    /**
     * @return 此计分板的显示名称
     */
    val displayName: String?

    /**
     * @return 此计分板的 “准则” (eg: dummy)
     */
    val criteriaName: String?

    /**
     * @return 此计分板的排序规则
     */
    var sortOrder: SortOrder?

    /**
     * @return 此计分板的所有观察者
     */
    val allViewers: Set<IScoreboardViewer>

    /**
     * @param slot 目标槽位
     * @return 此计分板目标槽位的观察者
     */
    fun getViewers(slot: DisplaySlot): Set<IScoreboardViewer>

    /**
     * 删除此计分板目标槽位中的某个观察者
     * @param viewer 目标观察者
     * @param slot 目标槽位
     * @return 是否删除成功
     */
    fun removeViewer(viewer: IScoreboardViewer, slot: DisplaySlot): Boolean

    /**
     * 向此计分板目标槽位中添加一个观察者
     * @param viewer 目标观察者
     * @param slot 目标槽位
     * @return 是否添加成功
     */
    fun addViewer(viewer: IScoreboardViewer, slot: DisplaySlot): Boolean

    /**
     * 检查此计分板目标槽位中是否有特定观察者
     * @param viewer 目标观察者
     * @param slot 目标槽位
     * @return 是否存在
     */
    fun containViewer(viewer: IScoreboardViewer, slot: DisplaySlot): Boolean

    /**
     * @return 此计分板的所有行
     */
    val lines: Map<IScorer, IScoreboardLine>

    /**
     * 获取追踪对象在此计分板上对应的行（如果存在）
     * @param scorer 追踪对象
     * @return 对应行
     */
    fun getLine(scorer: IScorer): IScoreboardLine?

    /**
     * 为此计分板添加一个行
     * @param line 目标行
     * @return 是否添加成功
     */
    fun addLine(line: IScoreboardLine): Boolean

    /**
     * 为此计分板添加一个行
     * @param scorer 追踪对象
     * @param score 分数
     * @return 是否添加成功
     */
    fun addLine(scorer: IScorer, score: Int): Boolean

    /**
     * 为插件提供的便捷的计分板显示接口
     * @param text FakeScorer的名称
     * @param score 分数
     * @return 是否添加成功
     */
    fun addLine(text: String, score: Int): Boolean

    /**
     * 删除追踪对象在此计分板上对应的行（如果存在）
     * @param scorer 目标追踪对象
     * @return 是否删除成功
     */
    fun removeLine(scorer: IScorer?): Boolean

    /**
     * 删除计分板所有行
     * @param send 是否发送到观察者
     * @return 是否删除成功
     */
    fun removeAllLine(send: Boolean): Boolean

    /**
     * 检查追踪对象在此计分板上是否有记录
     * @param scorer 目标追踪对象
     * @return 是否存在
     */
    fun containLine(scorer: IScorer?): Boolean

    /**
     * 向所有观察者发送新的分数 <br></br>
     * @param update 需要更新的行
     */
    fun updateScore(update: IScoreboardLine)

    /**
     * 向所有观察者重新发送此计分板以及行信息 <br></br>
     * 例如当对计分板进行了大量的更改后，调用此方法 <br></br>
     * 可节省大量带宽
     */
    fun resend()

    /**
     * 为插件提供的快捷接口 <br></br>
     * 按照List顺序设置计分板的内容 (使用FakeScorer作为追踪对象) <br></br>
     * 会覆盖之前的所有行 <br></br>
     * @param lines 需要设置的字符串内容
     */
    fun setLines(lines: List<String>)

    /**
     * 按照List顺序设置计分板的内容 <br></br>
     * 会覆盖之前的所有行 <br></br>
     * @param lines 需要设置的行内容
     */
    fun setLines(lines: Collection<IScoreboardLine>)

    /**
     * @return 对此计分板的更改是否会产生事件
     */
    fun shouldCallEvent(): Boolean
}
