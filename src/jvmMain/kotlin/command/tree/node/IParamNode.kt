package org.chorus_oss.chorus.command.tree.node

import org.chorus_oss.chorus.command.data.CommandEnum
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.tree.ParamList
import org.chorus_oss.chorus.network.protocol.types.CommandOutputMessage
import kotlin.math.max

/**
 * 代表一个抽象的命令节点，类型T对应节点解析结果类型<br></br>
 */
interface IParamNode<T> {
    /**
     * 负责填充该参数节点,覆写该方法需要实现对接受参数arg的验证以及解析成为对应类型T的结果
     * <br></br>
     * 当验证失败或者解析失败,请调用[.error]方法标记错误.形如`this.error()`
     *
     * @param arg the arg
     */
    fun fill(arg: String)

    /**
     * 获取已被[.fill]填充后的节点值,会自动转型为接受类型E,不会判断是否能成功转型<br></br>有可能抛出[ClassCastException]
     */
    fun <E> get(): E?

    /**
     * 将节点重置回初始化状态,以待下次填充[.fill]
     */
    fun reset()

    /**
     * 该节点是否已经得到结果<br></br>
     * 该方法返回值为false时,将会一直重复对该节点执行填充[.fill]直到该方法返回true或者命令输入参数用完
     */
    fun hasResult(): Boolean

    /**
     * 该命令节点是否为可选值,可选值不一定需要被填充[.fill]
     */
    val isOptional: Boolean

    /**
     * 获取该节点所属[ParamList]
     *
     * @return the parent
     */
    val paramList: ParamList

    /**
     * 标记该节点的[.fill]出现错误，输出默认错误信息
     */
    fun error() {
        paramList.error()
    }

    /**
     * 标记该节点的[.fill]出现错误
     *
     * @param key 添加的错误信息
     */
    fun error(key: String) {
        this.error(key, *emptyArray<String>())
    }

    /**
     * 标记该节点的[.fill]出现错误
     *
     * @param key    添加的错误信息，可以填写多语言文本key
     * @param params 填充多语言文本的参数
     */
    fun error(key: String, vararg params: String) {
        val list = this.paramList
        list.error()
        list.addMessage(key, *params)
    }

    /**
     * 标记该节点的[.fill]出现错误
     *
     * @param messages 添加的错误信息[CommandOutputMessage]
     */
    fun error(vararg messages: CommandOutputMessage) {
        val list = this.paramList
        list.error()
        list.addMessage(*messages)
    }

    /**
     * 这个方法用于初始化[ParamList]和一些能从[CommandParameter][org.chorus_oss.chorus.command.data.CommandParameter]得到的参数,例如optional enumData等，插件不需要调用
     *
     * @param parent   the parent
     * @param name     the name
     * @param optional the optional
     * @param type     the type
     * @param enumData the enum data
     * @param postFix  the post fix
     * @return the param node
     */
    fun init(
        parent: ParamList,
        name: String?,
        optional: Boolean,
        type: CommandParamType?,
        enumData: CommandEnum?,
        postFix: String?
    ): IParamNode<T> {
        return this
    }

    val before: IParamNode<*>?
        /**
         * Retrieves the node before the current node.
         */
        get() {
            val index = paramList.nodeIndex
            return paramList[max(0.0, (index - 1).toDouble()).toInt()]
        }
}
