package org.chorus_oss.chorus.command.tree.node

import org.chorus_oss.chorus.level.Locator
import org.chorus_oss.chorus.math.BVector3
import org.chorus_oss.chorus.math.Vector3
import java.util.regex.Pattern

/**
 * 坐标节点基类
 */
abstract class PositionNode(private val pattern: Pattern) : ParamNode<Locator?>() {
    protected val coordinate: DoubleArray = DoubleArray(3)
    protected val TMP: MutableList<String> = ArrayList()
    private var relative: Byte = 0
    protected var index: Byte = 0

    override fun <E> get(): E? {
        return this.get<E>(paramList.paramTree.sender!!.locator)
    }

    fun <E> get(basePos: Locator): E? {
        if (this.value == null) return null
        if (this.getRelative(0)) {
            value!!.position.setX(value!!.position.x + basePos.x)
        }
        if (this.getRelative(1)) {
            value!!.position.setY(value!!.position.y + basePos.y)
        }
        if (this.getRelative(2)) {
            value!!.position.setZ(value!!.position.z + basePos.z)
        }
        return value as E?
    }

    override fun fill(arg: String) {
        TMP.clear()
        //check
        val matcher = pattern.matcher(arg)
        while (matcher.find()) {
            TMP.add(matcher.group())
        }
        val str = TMP.stream().reduce { s1: String, s2: String -> s1 + s2 }
        if (str.isEmpty) this.error()
        else if (str.get().length != arg.length) this.error()
        else {
            //parse
            try {
                val loc = paramList.paramTree.sender!!.transform
                for (s in TMP) {
                    if (s[0] == '~') {
                        this.setRelative(index)
                        var relativeCoordinate = s.substring(1)
                        if (relativeCoordinate.isEmpty()) {
                            coordinate[index.toInt()] = 0.0
                        } else {
                            if (relativeCoordinate[0] == '+') relativeCoordinate = relativeCoordinate.substring(1)
                            coordinate[index.toInt()] = relativeCoordinate.toDouble()
                        }
                    } else if (s[0] == '^') {
                        if (index.toInt() == 0) {
                            coordinate[0] = 0.0
                            coordinate[1] = 0.0
                            coordinate[2] = 0.0
                        }
                        this.setRelative(index)
                        var relativeAngleCoordinate = s.substring(1)
                        if (!relativeAngleCoordinate.isEmpty()) {
                            val vector3: Vector3
                            if (relativeAngleCoordinate[0] == '+') relativeAngleCoordinate =
                                relativeAngleCoordinate.substring(1)
                            when (index.toInt()) {
                                0 -> {
                                    vector3 = BVector3.fromLocation(loc).rotateYaw(-90.0).setPitch(0.0)
                                        .setLength(relativeAngleCoordinate.toDouble()).addToPos()
                                    coordinate[0] += vector3.x
                                    coordinate[1] += vector3.y
                                    coordinate[2] += vector3.z
                                }

                                1 -> {
                                    vector3 = BVector3.fromLocation(loc).rotatePitch(-90.0)
                                        .setLength(relativeAngleCoordinate.toDouble()).addToPos()
                                    coordinate[0] += vector3.x
                                    coordinate[1] += vector3.y
                                    coordinate[2] += vector3.z
                                }

                                2 -> {
                                    vector3 = BVector3.fromLocation(loc).setLength(relativeAngleCoordinate.toDouble())
                                        .addToPos()
                                    coordinate[0] += vector3.x
                                    coordinate[1] += vector3.y
                                    coordinate[2] += vector3.z
                                }

                                else -> {
                                    this.error()
                                    return
                                }
                            }
                        }
                    } else {
                        coordinate[index.toInt()] = s.toDouble()
                    }
                    index++
                }
                if (index.toInt() == 3) {
                    this.value = Locator(
                        coordinate[0],
                        coordinate[1], coordinate[2], loc.level
                    )
                    index = 0
                }
            } catch (ignore: NumberFormatException) {
                this.error()
            }
        }
    }

    override fun reset() {
        super.reset()
        this.relative = 0
    }

    fun setRelative(index: Byte) {
        when (index.toInt()) {
            0 -> this.relative = (relative.toInt() or 1).toByte()
            1 -> this.relative = (relative.toInt() or 2).toByte()
            2 -> this.relative = (relative.toInt() or 4).toByte()
        }
    }

    fun getRelative(index: Int): Boolean {
        return when (index) {
            0 -> (relative.toInt() and 1) == 1
            1 -> (relative.toInt() and 2) == 2
            2 -> (relative.toInt() and 4) == 4
            else -> false
        }
    }
}
