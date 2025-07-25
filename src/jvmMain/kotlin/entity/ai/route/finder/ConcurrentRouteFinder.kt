package org.chorus_oss.chorus.entity.ai.route.finder

import org.chorus_oss.chorus.entity.ai.route.data.Node
import org.chorus_oss.chorus.entity.ai.route.posevaluator.IPosEvaluator
import java.util.concurrent.CompletableFuture
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * 并行路径查找抽象类 <br></br>
 * 实现了此类的寻路器应当提供完整的异步寻路支持 <br></br>
 * PNX中未使用此寻路方案，但保留以提供API <br></br>
 */
abstract class ConcurrentRouteFinder(blockEvaluator: IPosEvaluator?) : SimpleRouteFinder(blockEvaluator) {
    //同步访问锁
    protected val lock: ReentrantReadWriteLock = ReentrantReadWriteLock()

    override fun addNode(node: Node) {
        try {
            lock.writeLock().lock()
            nodes.add(node)
        } finally {
            lock.writeLock().unlock()
        }
    }

    protected fun addNode(node: ArrayList<Node>) {
        try {
            lock.writeLock().lock()
            nodes.addAll(node)
        } finally {
            lock.writeLock().unlock()
        }
    }

    override fun resetNodes() {
        try {
            lock.writeLock().lock()
            nodes.clear()
        } finally {
            lock.writeLock().unlock()
        }
    }

    override val route: List<Node>
        /**
         * 线程安全地获取查找到的路径信息（cloned）
         */
        get() {
            val clone = mutableListOf<Node>()
            try {
                lock.writeLock().lock()
                for (node in this.nodes) {
                    clone.add(node)
                }
            } finally {
                lock.writeLock().unlock()
            }
            return clone
        }

    override val currentNode: Node?
        get() {
            try {
                lock.readLock().lock()
                if (this.hasCurrentNode()) {
                    return nodes[nodeIndex]
                }
                return null
            } finally {
                lock.readLock().unlock()
            }
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
        try {
            lock.readLock().lock()
            if (this.hasNext()) {
                return nodes[++nodeIndex]
            }
            return null
        } finally {
            lock.readLock().unlock()
        }
    }

    override fun hasCurrentNode(): Boolean {
        return nodeIndex < nodes.size
    }

    override var nodeIndex: Int = 0

    //异步查找路径
    fun asyncSearch(): CompletableFuture<Void> {
        return CompletableFuture.runAsync { this.search() }
    }
}
