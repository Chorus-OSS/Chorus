package org.chorus_oss.chorus.entity.ai.route.finder.impl


import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.entity.ai.EntityAI
import org.chorus_oss.chorus.entity.ai.route.data.Node
import org.chorus_oss.chorus.entity.ai.route.finder.SimpleRouteFinder
import org.chorus_oss.chorus.entity.ai.route.posevaluator.IPosEvaluator
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.particle.BlockForceFieldParticle
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.math.VectorMath
import java.util.*
import java.util.function.Consumer
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * 标准A*寻路实现
 */


open class SimpleFlatAStarRouteFinder(blockEvaluator: IPosEvaluator?, protected var entity: EntityMob) :
    SimpleRouteFinder(blockEvaluator) {
    protected val openList: PriorityQueue<Node> = PriorityQueue()

    protected val closeList: MutableList<Node> = ArrayList()
    protected val closeHashSet: HashSet<Vector3> = HashSet()

    override var start: Vector3? = null
        set(value) {
            field = value
            if (isInterrupt) this.interrupt = true
        }

    override var target: Vector3? = null
        set(value) {
            field = value
            if (isInterrupt) this.interrupt = true
        }

    override var reachableTarget: Vector3? = null

    protected var finished: Boolean = false

    override val isFinished: Boolean
        get() = this.finished

    override val isInterrupt: Boolean
        get() = this.interrupt

    override val isReachable: Boolean
        get() = this.reachable

    override var isSearching: Boolean = false
        protected set

    protected var interrupt: Boolean = false

    protected var reachable: Boolean = true

    protected var enableFloydSmooth: Boolean = true

    //寻路最大深度
    protected var currentSearchDepth: Int = 100

    protected var maxSearchDepth: Int = 100

    protected var lastRouteParticleSpawn: Long = 0

    override fun search(): Boolean {
        //init status
        this.finished = false
        this.isSearching = true
        this.interrupt = false
        var currentReachable = true
        //若实体未处于active状态，则关闭路径平滑
        this.enableFloydSmooth = (entity.isActive)
        //清空openList和closeList
        openList.clear()
        closeList.clear()
        closeHashSet.clear()
        //重置寻路深度
        currentSearchDepth = maxSearchDepth

        //将起点放置到closeList中，以开始寻路
        //起点没有父节点，且我们不需要计算他的代价
        var currentNode = Node(start!!, null, 0, 0)
        val tmpNode = Node(start!!, null, 0, 0)
        closeList.add(tmpNode)
        closeHashSet.add(tmpNode.vector3)

        //若当前寻路点没有到达终点
        while (!isPositionOverlap(currentNode.vector3, target!!)) {
            //检查是否被中断了
            if (this.isInterrupt) {
                currentSearchDepth = 0
                this.isSearching = false
                this.finished = true
                this.reachable = false
                return false
            }
            //将当前节点周围的有效节点放入openList中
            putNeighborNodeIntoOpen(currentNode)
            //若未超出寻路深度，则获取代价最小的一个node并将其设置为currentNode
            if (openList.peek() != null && currentSearchDepth-- > 0) {
                closeList.add(openList.poll().also { currentNode = it })
                closeHashSet.add(currentNode.vector3)
            } else {
                this.isSearching = false
                this.finished = true
                currentReachable = false
                break
            }
        }

        //因为在前面是否到达终点的检查中我们只粗略检查了坐标的floor值
        //所以说这里我们还需要将其精确指向到终点
        var targetNode = currentNode
        if (currentNode.vector3 != target) {
            targetNode = Node(target!!, currentNode, 0, 0)
        }

        //如果无法到达，则取最接近终点的一个Node作为尾节点
        var reachableNode: Node? = null
        reachableTarget = if (currentReachable) target else (getNearestNodeFromCloseList(target!!).also {
            reachableNode = it
        })!!.vector3
        var findingPath: List<Node> = if (currentReachable) getPathRoute(targetNode) else getPathRoute(reachableNode)
        //使用floyd平滑路径
        if (enableFloydSmooth) findingPath = floydSmooth(findingPath)

        //清空上次的寻路结果
        this.resetNodes()
        //重置Node指针
        this.nodeIndex = 0

        //写入结果
        this.addNode(findingPath)

        if (EntityAI.checkDebugOption(EntityAI.DebugOption.ROUTE)) {
            if (System.currentTimeMillis() - lastRouteParticleSpawn > EntityAI.getRouteParticleSpawnInterval()) {
                findingPath.forEach(Consumer { node: Node ->
                    entity.level!!.addParticle(
                        BlockForceFieldParticle(node.vector3),
                        Server.instance.onlinePlayers.values.toTypedArray()
                    )
                })
                lastRouteParticleSpawn = System.currentTimeMillis()
            }
        }

        this.reachable = currentReachable
        this.finished = true
        this.isSearching = false

        return true
    }

    /**
     * 获取指定位置的方块的移动Cost
     *
     * @param level
     * @param pos
     * @return cost
     */
    protected open fun getBlockMoveCostAt(level: Level, pos: Vector3): Int {
        return level.getTickCachedBlock(pos).walkThroughExtraCost + level.getTickCachedBlock(
            pos.add(
                0.0,
                -1.0,
                0.0
            )
        ).walkThroughExtraCost
    }

    /**
     * 将一个节点周围的有效节点放入OpenList中
     *
     * @param node 节点
     */
    protected open fun putNeighborNodeIntoOpen(node: Node) {
        val N: Boolean
        val E: Boolean
        val S: Boolean
        val W: Boolean

        val vector3 = Vector3(node.vector3.floorX + 0.5, node.vector3.y, node.vector3.floorZ + 0.5)

        var offsetY: Double

        if ((getAvailableHorizontalOffset(vector3).also { offsetY = it.toDouble() }).toDouble() != -384.0) {
            if (abs(offsetY) > 0.25) {
                val vec = vector3.add(0.0, offsetY, 0.0)
                if (!existInCloseList(vec)) {
                    val nodeNear = getOpenNode(vec)
                    if (nodeNear == null) {
                        openList.offer(
                            Node(
                                vec, node, node.cost, calH(
                                    vec,
                                    target!!
                                )
                            )
                        )
                    } else {
                        if (node.cost < nodeNear.cost) {
                            nodeNear.parent = node
                            nodeNear.cost = node.cost
                        }
                    }
                }
            }
        }

        if (((getAvailableHorizontalOffset(vector3.add(1.0, 0.0, 0.0)).also {
                offsetY = it.toDouble()
            }).toDouble() != -384.0).also { E = it }) {
            val vec = vector3.add(1.0, offsetY, 0.0)
            if (!existInCloseList(vec)) {
                val cost = getBlockMoveCostAt(entity.level!!, vec) + DIRECT_MOVE_COST + node.cost
                val nodeNear = getOpenNode(vec)
                if (nodeNear == null) {
                    openList.offer(
                        Node(
                            vec, node, cost, calH(
                                vec,
                                target!!
                            )
                        )
                    )
                } else {
                    if (cost < nodeNear.cost) {
                        nodeNear.parent = node
                        nodeNear.cost = cost
                    }
                }
            }
        }

        if (((getAvailableHorizontalOffset(vector3.add(0.0, 0.0, 1.0)).also {
                offsetY = it.toDouble()
            }).toDouble() != -384.0).also { S = it }) {
            val vec = vector3.add(0.0, offsetY, 1.0)
            if (!existInCloseList(vec)) {
                val cost = getBlockMoveCostAt(entity.level!!, vec) + DIRECT_MOVE_COST + node.cost
                val nodeNear = getOpenNode(vec)
                if (nodeNear == null) {
                    openList.offer(
                        Node(
                            vec, node, cost, calH(
                                vec,
                                target!!
                            )
                        )
                    )
                } else {
                    if (cost < nodeNear.cost) {
                        nodeNear.parent = node
                        nodeNear.cost = cost
                    }
                }
            }
        }

        if (((getAvailableHorizontalOffset(vector3.add(-1.0, 0.0, 0.0)).also {
                offsetY = it.toDouble()
            }).toDouble() != -384.0).also { W = it }) {
            val vec = vector3.add(-1.0, offsetY, 0.0)
            if (!existInCloseList(vec)) {
                val cost = getBlockMoveCostAt(entity.level!!, vec) + DIRECT_MOVE_COST + node.cost
                val nodeNear = getOpenNode(vec)
                if (nodeNear == null) {
                    openList.offer(
                        Node(
                            vec, node, cost, calH(
                                vec,
                                target!!
                            )
                        )
                    )
                } else {
                    if (cost < nodeNear.cost) {
                        nodeNear.parent = node
                        nodeNear.cost = cost
                    }
                }
            }
        }

        if (((getAvailableHorizontalOffset(vector3.add(0.0, 0.0, -1.0)).also {
                offsetY = it.toDouble()
            }).toDouble() != -384.0).also { N = it }) {
            val vec = vector3.add(0.0, offsetY, -1.0)
            if (!existInCloseList(vec)) {
                val cost = getBlockMoveCostAt(entity.level!!, vec) + DIRECT_MOVE_COST + node.cost
                val nodeNear = getOpenNode(vec)
                if (nodeNear == null) {
                    openList.offer(
                        Node(
                            vec, node, cost, calH(
                                vec,
                                target!!
                            )
                        )
                    )
                } else {
                    if (cost < nodeNear.cost) {
                        nodeNear.parent = node
                        nodeNear.cost = cost
                    }
                }
            }
        }

        //我们不允许实体在上下坡的时候斜着走，因为这容易导致实体卡脚（原版也是这个逻辑）
        //接触水的时候就不需要这么判断了
        if (N && E && (((getAvailableHorizontalOffset(vector3.add(1.0, 0.0, -1.0)).also {
                offsetY = it.toDouble()
            }).toDouble() == 0.0) || (offsetY != -384.0 && entity.isTouchingWater()))) {
            val vec = vector3.add(1.0, offsetY, -1.0)
            if (!existInCloseList(vec)) {
                val cost = getBlockMoveCostAt(entity.level!!, vec) + OBLIQUE_MOVE_COST + node.cost
                val nodeNear = getOpenNode(vec)
                if (nodeNear == null) {
                    openList.offer(
                        Node(
                            vec, node, cost, calH(
                                vec,
                                target!!
                            )
                        )
                    )
                } else {
                    if (cost < nodeNear.cost) {
                        nodeNear.parent = node
                        nodeNear.cost = cost
                    }
                }
            }
        }

        if (E && S && (((getAvailableHorizontalOffset(vector3.add(1.0, 0.0, 1.0)).also {
                offsetY = it.toDouble()
            }).toDouble() == 0.0) || (offsetY != -384.0 && entity.isTouchingWater()))) {
            val vec = vector3.add(1.0, offsetY, 1.0)
            if (!existInCloseList(vec)) {
                val cost = getBlockMoveCostAt(entity.level!!, vec) + OBLIQUE_MOVE_COST + node.cost
                val nodeNear = getOpenNode(vec)
                if (nodeNear == null) {
                    openList.offer(
                        Node(
                            vec, node, cost, calH(
                                vec,
                                target!!
                            )
                        )
                    )
                } else {
                    if (cost < nodeNear.cost) {
                        nodeNear.parent = node
                        nodeNear.cost = cost

                    }
                }
            }
        }

        if (W && S && (((getAvailableHorizontalOffset(vector3.add(-1.0, 0.0, 1.0)).also {
                offsetY = it.toDouble()
            }).toDouble() == 0.0) || (offsetY != -384.0 && entity.isTouchingWater()))) {
            val vec = vector3.add(-1.0, offsetY, 1.0)
            if (!existInCloseList(vec)) {
                val cost = getBlockMoveCostAt(entity.level!!, vec) + OBLIQUE_MOVE_COST + node.cost
                val nodeNear = getOpenNode(vec)
                if (nodeNear == null) {
                    openList.offer(
                        Node(
                            vec, node, cost, calH(
                                vec,
                                target!!
                            )
                        )
                    )
                } else {
                    if (cost < nodeNear.cost) {
                        nodeNear.parent = node
                        nodeNear.cost = cost
                    }
                }
            }
        }

        if (W && N && (((getAvailableHorizontalOffset(vector3.add(-1.0, 0.0, -1.0)).also {
                offsetY = it.toDouble()
            }).toDouble() == 0.0) || (offsetY != -384.0 && entity.isTouchingWater()))) {
            val vec = vector3.add(-1.0, offsetY, -1.0)
            if (!existInCloseList(vec)) {
                val cost = getBlockMoveCostAt(entity.level!!, vec) + OBLIQUE_MOVE_COST + node.cost
                val nodeNear = getOpenNode(vec)
                if (nodeNear == null) {
                    openList.offer(
                        Node(
                            vec, node, cost, calH(
                                vec,
                                target!!
                            )
                        )
                    )
                } else {
                    if (cost < nodeNear.cost) {
                        nodeNear.parent = node
                        nodeNear.cost = cost
                    }
                }
            }
        }
    }

    protected fun getOpenNode(vector2: Vector3): Node? {
        for (node in this.openList) {
            if (vector2 == node.vector3) {
                return node
            }
        }

        return null
    }

    protected fun existInOpenList(vector2: Vector3): Boolean {
        return getOpenNode(vector2) != null
    }

    protected fun getCloseNode(vector2: Vector3): Node? {
        for (node in this.closeList) {
            if (vector2 == node.vector3) {
                return node
            }
        }
        return null
    }

    protected fun existInCloseList(vector2: Vector3): Boolean {
        return closeHashSet.contains(vector2)
    }

    /**
     * 计算当前点到终点的代价H
     * 默认使用对角线+直线距离
     */
    protected fun calH(start: Vector3, target: Vector3): Int {
        //使用DIRECT_MOVE_COST和OBLIQUE_MOVE_COST计算代价
        //计算对角线距离
        val obliqueCost = (abs(min(target.x - start.x, target.z - start.z)) * OBLIQUE_MOVE_COST).toInt()
        //计算剩余直线距离
        val directCost = ((abs(max(target.x - start.x, target.z - start.z)) - abs(
            min(
                target.x - start.x,
                target.z - start.z
            )
        )) * DIRECT_MOVE_COST).toInt()
        return obliqueCost + directCost + (abs(target.y - start.y) * DIRECT_MOVE_COST).toInt()
    }

    /**
     * 获取目标坐标最高有效点（沿Y轴往下检查）
     */
    fun getHighestUnder(vector3: Vector3, limit: Int): Block? {
        if (limit > 0) {
            for (y in vector3.floorY downTo vector3.floorY - limit) {
                val block =
                    entity.level!!.getTickCachedBlock(vector3.floorX, y, vector3.floorZ, false)
                if (evalStandingBlock(block)) return block
            }
            return null
        }
        for (y in vector3.floorY downTo -64) {
            val block =
                entity.level!!.getTickCachedBlock(vector3.floorX, y, vector3.floorZ, false)
            if (evalStandingBlock(block)) return block
        }
        return null
    }

    /**
     * 指定位置是否可作为一个有效的节点
     */
    protected fun evalPos(pos: Vector3): Boolean {
        return evalPos!!.evalPos(entity, pos)
    }

    /**
     * 指定方块上面是否可作为一个有效的节点
     */
    protected fun evalStandingBlock(block: Block): Boolean {
        return evalPos!!.evalStandingBlock(entity, block)
    }

    /**
     * @param vector3
     * @return 指定坐标可到达的最高点 (limit=4)
     */
    protected fun getAvailableHorizontalOffset(vector3: Vector3): Int {
        val block = getHighestUnder(vector3, 4)
        if (block != null) {
            return block.position.floorY - vector3.floorY + 1
        }
        return -384
    }

    protected fun hasBarrier(node1: Node, node2: Node): Boolean {
        return hasBarrier(node1.vector3, node2.vector3)
    }

    /**
     * 指定两个Node之间是否有障碍物
     */
    protected open fun hasBarrier(pos1: Vector3, pos2: Vector3): Boolean {
        if (pos1 == pos2) return false
        return VectorMath.getPassByVector3(pos1, pos2).stream().anyMatch { pos: Vector3 ->
            !evalStandingBlock(
                entity.level!!.getTickCachedBlock(pos.add(0.0, -1.0))
            )
        }
    }

    /**
     * Smoothing A* Path Using Floyd Algorithm
     */
    protected fun floydSmooth(array: List<Node>): List<Node> {
        var current = 0
        var total = 2
        if (array.size > 2) {
            while (total < array.size) {
                if (hasBarrier(array[current], array[total]) || total == array.size - 1) {
                    array[total - 1].parent = array[current]
                    current = total - 1
                }
                total++
            }
            var temp = array[array.size - 1]
            val tempL: MutableList<Node> = ArrayList()
            tempL.add(temp)
            while (temp.parent != null && temp.parent!!.vector3 != start) {
                tempL.add((temp.parent!!.also { temp = it }))
            }
            return tempL.reversed()
        }
        return array
    }

    /**
     * 将Node链转换成List<Node>样式的路径信息
     *
     * @param end 列表尾节点
    </Node> */
    protected fun getPathRoute(end: Node?): List<Node> {
        var end1 = end
        val nodes: MutableList<Node> = ArrayList()
        if (end1 == null) end1 = closeList[closeList.size - 1]
        nodes.add(end1)
        if (end1.parent != null) {
            while (end1!!.parent!!.vector3 != start) {
                nodes.add(end1.parent!!.also { end1 = it })
            }
        } else {
            nodes.add(end1!!)
        }
        return nodes.reversed()
    }

    /**
     * 获取接近指定坐标的最近的Node
     */
    protected fun getNearestNodeFromCloseList(vector3: Vector3): Node? {
        var min = Double.MAX_VALUE
        var node: Node? = null
        for (n in closeList) {
            val distanceSquared = n.vector3.floor().distanceSquared(vector3.floor())
            if (distanceSquared < min) {
                min = distanceSquared
                node = n
            }
        }
        return node
    }

    /**
     * 坐标是否重叠了 <br></br>
     * 此方法只会比较坐标的floorX、floorY、floorZ
     */
    protected fun isPositionOverlap(vector2: Vector3, vector2_: Vector3): Boolean {
        return vector2.floorX == vector2_.floorX && vector2.floorZ == vector2_.floorZ && vector2.floorY == vector2_.floorY
    }

    companion object {
        //这些常量是为了避免开方运算而设置的
        //直接移动成本
        protected const val DIRECT_MOVE_COST: Int = 10

        //倾斜移动成本
        protected const val OBLIQUE_MOVE_COST: Int = 14
    }
}
