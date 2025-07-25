package org.chorus_oss.chorus.command.tree.node

abstract class RelativeNumberNode<T : Number?> : ParamNode<T>() {
    override fun <E> get(): E? {
        throw UnsupportedOperationException()
    }

    abstract fun get(base: T): T
}
