package org.chorus_oss.chorus.command.tree.node

/**
 * 一个用来占位的空参数节点
 */
class VoidNode : ParamNode<Void?>() {
    override fun fill(arg: String) {
    }

    override fun <E> get(): E? {
        return null
    }

    override fun reset() {
    }

    override fun hasResult(): Boolean {
        return true
    }

    override var isOptional: Boolean = true
}
