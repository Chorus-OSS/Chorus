package org.chorus_oss.chorus.entity.ai.route.data


import org.chorus_oss.chorus.math.Vector3
import java.util.*

class Node(
    val vector3: Vector3,
    var parent: Node?,
    var cost: Int,
    val heuristicCost: Int
) :
    Comparable<Node?> {
    private val finalCost = cost + heuristicCost

    override fun compareTo(other: Node?): Int {
        requireNotNull(other)
        if (this.finalCost != other.finalCost) {
            return this.finalCost - other.finalCost
        }
        var breaking: Double
        return if (((this.cost + (this.heuristicCost * 0.1) - (other.cost + (this.heuristicCost * 0.1))).also {
                breaking = it
            }) > 0) {
            1
        } else if (breaking < 0) {
            -1
        } else {
            0
        }
    }

    override fun toString(): String {
        return vector3.toString() + "| G:" + this.cost + " H:" + this.heuristicCost + " F" + this.finalCost + (if (this.parent != null) "\tparent:" + parent!!.vector3 else "")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val node = other as Node
        return cost == node.cost && heuristicCost == node.heuristicCost && finalCost == node.finalCost && vector3 == node.vector3 && parent == node.parent
    }

    override fun hashCode(): Int {
        return Objects.hash(vector3, parent, cost, heuristicCost, finalCost)
    }
}
