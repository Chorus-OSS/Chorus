package org.chorus_oss.chorus.positiontracking

interface NamedPosition : Cloneable {
    val levelName: String

    val x: Double
    val y: Double
    val z: Double

    fun matchesNamedPosition(position: NamedPosition): Boolean {
        return x == position.x && y == position.y && z == position.z && levelName == position.levelName
    }

    public override fun clone(): NamedPosition
}
