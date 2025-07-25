package org.chorus_oss.chorus.entity.ai.route.finder

import org.chorus_oss.chorus.entity.ai.route.data.Node
import org.chorus_oss.chorus.entity.ai.route.posevaluator.IPosEvaluator


/**
 * 非异步的路径查找抽象类 <br></br>
 * 在PowerNukkitX的生物AI架构中，不同实体的路径查找是并行的而不是异步的 <br></br>
 * 所以说我们并不需要异步路径查找
 */
abstract class SimpleRouteFinder(//方块评估器
    protected var evalPos: IPosEvaluator?
) : IRouteFinder {
    //用于存储寻路结果的List
    protected var nodes: MutableList<Node> = ArrayList()

    //索引值
    override var nodeIndex: Int = 0

    //添加寻路结果节点
    protected open fun addNode(node: Node) {
        nodes.add(node)
    }

    //批量添加寻路结果节点
    protected fun addNode(node: List<Node>) {
        nodes.addAll(node)
    }

    //重置寻路结果
    protected open fun resetNodes() {
        nodes.clear()
    }

    override val route: List<Node>
        get() = ArrayList(this.nodes)

    override val currentNode: Node?
        get() {
            if (this.hasCurrentNode()) {
                return nodes[nodeIndex]
            }
            return null
        }

    override fun hasNext(): Boolean {
        try {
            if (this.nodeIndex + 1 < nodes.size) {
                return nodes[nodeIndex + 1] != null
            }
        } catch (ignore: Exception) {
        }
        return false
    }

    override fun next(): Node? {
        if (this.hasNext()) {
            return nodes[++nodeIndex]
        }
        return null
    }

    override fun hasCurrentNode(): Boolean {
        return nodeIndex < nodes.size
    }

    override fun getNode(index: Int): Node? {
        if (index + 1 < nodes.size) {
            return nodes[index]
        }
        return null
    }
}
