package org.chorus_oss.chorus.entity.ai.route.finder

import org.chorus_oss.chorus.entity.ai.route.data.Node
import org.chorus_oss.chorus.math.Vector3

/**
 * 此接口抽象了一个寻路器
 *
 *
 * This interface abstracts a pathfinder
 */
interface IRouteFinder {
    /**
     * @return boolean 是否正在寻路
     */
    val isSearching: Boolean

    /**
     * @return boolean 是否完成寻路（找到有效路径）
     */
    val isFinished: Boolean

    /**
     * @return boolean 寻路是否被中断了
     */
    val isInterrupt: Boolean

    /**
     * 在调用此方法前，你应该首先尝试寻路，否则此方法始将终返回`true`
     *
     * @return 终点是否可到达
     */
    val isReachable: Boolean

    /**
     * 尝试开始寻路
     *
     * @return 是否成功找到路径
     */
    fun search(): Boolean

    /**
     * @return 寻路起点
     */
    /**
     * 设置寻路起点，将会导致寻路中断
     *
     * @param vector3 寻路起点
     */
    var start: Vector3?

    /**
     * @return 寻路终点
     */
    /**
     * 设置寻路终点，将会导致寻路中断
     *
     * @param vector3 寻路终点
     */
    var target: Vector3?

    /**
     * @return 可到达的终点
     */
    val reachableTarget: Vector3?

    /**
     * 获取寻路结果
     *
     * @return 一个包含 [Node] 的列表 [List]，应已排序好，第一项为寻路起点，最后一项为寻路终点，之间的为找到的路径点
     */
    val route: List<Node>

    /**
     * @return 是否有下一个节点 [Node]
     */
    fun hasNext(): Boolean

    /**
     * 获取下一个节点[Node]（如果有的话）
     *
     * @return 下一个节点
     */
    fun next(): Node?

    /**
     * @return 当前索引所在位置是否有节点 [Node]
     */
    fun hasCurrentNode(): Boolean

    /**
     * @return 当前索引位置对应的节点 [Node]
     */
    val currentNode: Node?

    /**
     * @return 当前索引
     */
    /**
     * 设置当前索引
     *
     * @param index 索引值
     */
    var nodeIndex: Int

    /**
     * @return 指定索引位置的节点 [Node]
     */
    fun getNode(index: Int): Node?
}
