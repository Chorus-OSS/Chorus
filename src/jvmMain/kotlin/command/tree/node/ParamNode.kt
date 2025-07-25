package org.chorus_oss.chorus.command.tree.node

import org.chorus_oss.chorus.command.data.CommandEnum
import org.chorus_oss.chorus.command.data.CommandParamType
import org.chorus_oss.chorus.command.tree.ParamList

/**
 * 一个通用的命令节点抽象类实现，插件想实现自己的命令参数节点应该继承这个类实现。
 */
abstract class ParamNode<T> : IParamNode<T> {
    protected var value: T? = null
    override var isOptional: Boolean = false
        protected set
    override lateinit var paramList: ParamList
        protected set

    override fun <E> get(): E? {
        return if (value == null) null
        else value as E
    }

    override fun hasResult(): Boolean {
        return value != null
    }

    override fun reset() {
        if (this.value != null) this.value = null
    }

    override fun init(
        parent: ParamList,
        name: String?,
        optional: Boolean,
        type: CommandParamType?,
        enumData: CommandEnum?,
        postFix: String?
    ): IParamNode<T> {
        this.paramList = parent
        this.isOptional = optional
        return this
    }
}
