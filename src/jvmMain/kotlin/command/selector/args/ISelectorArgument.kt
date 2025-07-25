package org.chorus_oss.chorus.command.selector.args

import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.exceptions.SelectorSyntaxException
import org.chorus_oss.chorus.command.selector.SelectorType
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.level.Transform
import java.util.function.Function
import java.util.function.Predicate

/**
 * 此接口描述了一个选择器参数
 */
interface ISelectorArgument : Comparable<ISelectorArgument> {
    /**
     * 根据给定的参数表返回特定的`List<Predicate<Entity>>`
     * @param arguments 参数列表
     * @param selectorType 选择器类型
     * @param sender 命令发送者
     * @param basePos 若此参数解析需要用到相对坐标，则应该以此坐标为依据
     *
     *
     * 若此参数需要修改参照坐标(例如x,y,z参数)，则应当在这个参数上修改
     *
     *
     * 在一条解析链上的参数只会使用一个Location对象
     * @return `Predicate<Entity>`
     * @throws SelectorSyntaxException 当解析出错
     */
    @Throws(SelectorSyntaxException::class)
    fun getPredicate(
        selectorType: SelectorType?,
        sender: CommandSender?,
        basePos: Transform,
        vararg arguments: String
    ): Predicate<Entity>? {
        return null
    }

    /**
     * 仅当方法isFilter()返回true时调用此方法
     *
     *
     * 方法返回的实体集合将传递到下一个参数
     * @param arguments 参数列表
     * @param selectorType 选择器类型
     * @param sender 命令发送者
     * @param basePos 若此参数解析需要用到相对坐标，则应该以此坐标为依据
     *
     *
     * 若此参数需要修改参照坐标(例如x,y,z参数)，则应当在这个参数上修改
     *
     *
     * 在一条解析链上的参数只会使用一个Location对象
     * @return 实体过滤器
     * @throws SelectorSyntaxException 当解析出错
     */
    @Throws(SelectorSyntaxException::class)
    fun getFilter(
        selectorType: SelectorType?,
        sender: CommandSender?,
        basePos: Transform,
        vararg arguments: String
    ): Function<List<Entity>, List<Entity>>? {
        return null
    }

    /**
     * 获取此参数的名称
     * @return 参数名称
     */
    val keyName: String

    /**
     * 解析优先级定义了各个参数的解析顺序
     *
     *
     * 优先级越高(数字越小)的参数，其越先被解析，且其解析结果将会影响下个参数的解析
     * @return 此参数的解析优先级
     */
    val priority: Int

    val isFilter: Boolean
        /**
         * 是否启用过滤器模式
         *
         *
         * 若一个参数为过滤器模式，解析器将不会调用getPredicate()方法而是调用filter()方法
         *
         *
         * 这意味着参数将对到达此参数节点的所有实体进行检测，而不是相对于单个实体进行检测
         *
         *
         * 对于部分参数（例如"c"），需要启用此功能
         * @return 是否启用过滤器模式
         */
        get() = false

    /**
     * 若一个参数有默认值（即此方法返回非null值），则在解析时若给定参数表中没有此参数，会以此默认值参与解析
     *
     * @param values 参数列表
     * @param selectorType 选择器类型
     * @param sender 命令执行者
     * @return 此参数的默认值
     */
    fun getDefaultValue(
        values: Map<String, List<String>>,
        selectorType: SelectorType,
        sender: CommandSender?
    ): String? {
        return null
    }

    override fun compareTo(o: ISelectorArgument): Int {
        return Integer.compare(this.priority, o.priority)
    }
}
