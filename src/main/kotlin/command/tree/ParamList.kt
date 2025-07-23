package org.chorus_oss.chorus.command.tree

import org.chorus_oss.chorus.command.tree.node.IParamNode
import org.chorus_oss.chorus.lang.CommandOutputContainer
import org.chorus_oss.chorus.network.protocol.types.CommandOutputMessage
import org.jetbrains.annotations.ApiStatus

class ParamList(@get:ApiStatus.Internal val paramTree: ParamTree) : ArrayList<IParamNode<*>>() {
    /**
     * 获取当前的参数链解析在哪个下标发生了错误(下标从0开始)
     *
     * @return the error index
     */
    var error: Int = Int.MIN_VALUE
        private set

    /**
     * 获取当前的参数链解析了几个参数(下标从1开始)
     *
     * @return the index
     */
    var index: Int = 0
        private set

    /**
     * Get the current [ParamList] parses several [IParamNode] (start at 0)
     */
    var nodeIndex: Int = 0
    val messageContainer: CommandOutputContainer = CommandOutputContainer()

    fun reset() {
        this.error = Int.MIN_VALUE
        messageContainer.messages.clear()
        this.index = 0
        this.nodeIndex = 0
        for (node in this) {
            node.reset()
        }
    }

    val indexAndIncrement: Int
        get() = index++

    fun error() {
        this.error = index - 1
    }

    /**
     * 获取指定索引处参数节点的值。
     */
    fun <E> getResult(index: Int): E? {
        return this[index].get()
    }

    fun <E> getResult(index: Int, defaultValue: E): E {
        return this.getResult(index) ?: defaultValue
    }

    fun addMessage(key: String) {
        messageContainer.messages.add(CommandOutputMessage(key, *emptyArray<String>()))
    }

    fun addMessage(key: String, vararg params: String) {
        messageContainer.messages.add(CommandOutputMessage(key, *params))
    }

    fun addMessage(vararg messages: CommandOutputMessage) {
        for (message in messages) {
            messageContainer.messages.add(message)
        }
    }

    /**
     * 如果是可选命令[IParamNode.isOptional]节点，请在获取值[.getResult]之前调用该方法判断是否存在
     *
     * @return 指定索引处的参数节点是否存在值
     */
    fun hasResult(index: Int): Boolean {
        return index < this.size && index > -1 && this[index].hasResult()
    }

    override fun clone(): ParamList {
        val v = super.clone() as ParamList
        v.error = this.error
        v.index = this.index
        return v
    }
}
