package org.chorus_oss.chorus.entity.ai.route.finder.impl

import org.chorus_oss.chorus.entity.ai.route.data.Node
import org.chorus_oss.chorus.entity.ai.route.posevaluator.IPosEvaluator
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.math.VectorMath
import kotlin.math.abs

/**
 * 务必注意，三维标准A*寻路的代价十分高昂(比原版的洪水填充低得多)，切忌将最大寻路深度设置得太大！
 * TODO: 用BA*、JPS或者势能场寻路代替
 */
open class SimpleSpaceAStarRouteFinder(blockEvaluator: IPosEvaluator?, entity: EntityMob) :
    SimpleFlatAStarRouteFinder(blockEvaluator, entity) {
    override fun getBlockMoveCostAt(level: Level, pos: Vector3): Int {
        return level.getTickCachedBlock(pos.add(0.0, -1.0, 0.0)).walkThroughExtraCost
    }

    override fun putNeighborNodeIntoOpen(node: Node) {
        val centeredNode = node.vector3.floor().add(0.5, 0.5, 0.5)
        for (dx in -1..1) {
            for (dz in -1..1) {
                for (dy in -1..1) {
                    val vec = centeredNode.add(dx.toDouble(), dy.toDouble(), dz.toDouble())
                    if (!existInCloseList(vec) && evalPos(vec)) {
                        // 计算移动1格的开销
                        val cost = when (abs(dx.toDouble()) + abs(dy.toDouble()) + abs(dz.toDouble())) {
                            1.0 -> DIRECT_MOVE_COST
                            2.0 -> OBLIQUE_2D_MOVE_COST
                            3.0 -> OBLIQUE_3D_MOVE_COST
                            else -> Int.MIN_VALUE
                        } + getBlockMoveCostAt(entity.level!!, vec) + node.cost - dy // -dy是为了倾向于从空中飞而不是贴地飞
                        if (cost < 0) continue
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
        }
    }

    /**
     * 指定两个Node之间是否有障碍物
     */
    override fun hasBarrier(pos1: Vector3, pos2: Vector3): Boolean {
        if (pos1 == pos2) return false
        return VectorMath.getPassByVector3(pos1, pos2).stream().anyMatch { pos: Vector3 ->
            !evalPos(
                entity.level!!.getTickCachedBlock(pos.add(0.0, -1.0)).position
            )
        }
    }

    companion object {
        //直接移动成本
        protected const val DIRECT_MOVE_COST: Int = 10

        //倾斜移动成本
        protected const val OBLIQUE_2D_MOVE_COST: Int = 14
        protected const val OBLIQUE_3D_MOVE_COST: Int = 17
    }
}
