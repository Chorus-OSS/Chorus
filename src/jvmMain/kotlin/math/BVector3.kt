package org.chorus_oss.chorus.math

import org.chorus_oss.chorus.level.Transform

/**
 * 向量计算工具，同时整合了yaw和pitch与坐标空间的转换功能
 *
 *
 * A vector calculation tool that integrates the conversion functions of yaw and pitch and coordinate space at the same time
 */
class BVector3 {
    /**
     * 获取未克隆的单位方向向量
     *
     * @return the direction vector
     */
    /**
     * 向量的单位向量
     *
     *
     * the unit vector of a vector
     */
    private var unclonedDirectionVector: Vector3 //标准化的方向向量,模长为1
    var yaw: Double //-90 270
        private set
    var pitch: Double //-90 90
        private set

    /**
     * 向量的模
     */
    private var length: Double

    /**
     * 通过传入的yaw、pitch和向量的模初始化BVector3
     *
     *
     * Initialize B Vector 3 by the modulus of the incoming yaw, pitch and vector
     *
     * @param yaw    the yaw
     * @param pitch  the pitch
     * @param length 向量模
     */
    private constructor(yaw: Double, pitch: Double, length: Double) {
        this.unclonedDirectionVector = getDirectionVector(yaw, pitch)
        this.yaw = getYawFromVector(unclonedDirectionVector)
        this.pitch = getPitchFromVector(unclonedDirectionVector)
        this.length = length
    }

    /**
     * 通过传入的向量坐标初始化BVector3
     *
     *
     * Initialize B Vector 3 with the vector coordinates passed in
     *
     * @param vector3 向量坐标
     */
    private constructor(vector3: Vector3) {
        this.yaw = getYawFromVector(vector3)
        this.pitch = getPitchFromVector(vector3)
        this.unclonedDirectionVector = getDirectionVector(yaw, pitch)
        this.length = vector3.length()
    }

    /**
     * 设置Yaw
     *
     * @param yaw the yaw
     * @return the yaw
     */
    fun setYaw(yaw: Double): BVector3 {
        this.unclonedDirectionVector = getDirectionVector(yaw, this.pitch)
        //重新计算在范围内的等价yaw值
        this.yaw = getYawFromVector(unclonedDirectionVector)
        return this
    }

    /**
     * 设置 pitch.
     *
     * @param pitch the pitch
     * @return the pitch
     */
    fun setPitch(pitch: Double): BVector3 {
        this.unclonedDirectionVector = getDirectionVector(this.yaw, pitch)
        //重新计算在范围内的等价pitch值
        this.pitch = getPitchFromVector(unclonedDirectionVector)
        return this
    }

    /**
     * 旋转Yaw
     *
     *
     * Rotate Yaw
     *
     * @param yaw the yaw
     * @return the b vector 3
     */
    fun rotateYaw(yaw: Double): BVector3 {
        this.yaw += yaw
        this.unclonedDirectionVector = getDirectionVector(this.yaw, this.pitch)
        //重新计算在范围内的等价yaw值
        this.yaw = getYawFromVector(unclonedDirectionVector)
        return this
    }

    /**
     * 旋转Pitch
     *
     *
     * Rotate Pitch
     *
     * @param pitch the pitch
     * @return the b vector 3
     */
    fun rotatePitch(pitch: Double): BVector3 {
        this.pitch += pitch
        this.unclonedDirectionVector = getDirectionVector(this.yaw, this.pitch)
        //重新计算在范围内的等价pitch值
        this.pitch = getPitchFromVector(unclonedDirectionVector)
        return this
    }

    /**
     * 旋转yaw和Pitch
     *
     *
     * Rotate yaw and pitch
     *
     * @param yaw   the yaw
     * @param pitch the pitch
     * @return the b vector 3
     */
    fun rotate(yaw: Double, pitch: Double): BVector3 {
        this.pitch += pitch
        this.yaw += yaw
        this.unclonedDirectionVector = getDirectionVector(this.yaw, this.pitch)
        //重新计算在范围内的等价pitch值
        this.pitch = getPitchFromVector(unclonedDirectionVector)
        this.pitch = getYawFromVector(unclonedDirectionVector)
        return this
    }

    /**
     * 向量加法
     *
     * @return 结果向量
     */
    fun add(x: Double, y: Double, z: Double): BVector3 {
        val pos = unclonedDirectionVector.multiply(this.length)
        pos.add(x, y, z)
        this.yaw = getYawFromVector(pos)
        this.pitch = getPitchFromVector(pos)
        this.unclonedDirectionVector = pos.normalize()
        this.length = pos.length()
        return this
    }

    /**
     * 向量加法
     *
     * @return 结果向量
     */
    fun add(vector3: Vector3): BVector3 {
        return add(vector3.x, vector3.y, vector3.z)
    }

    /**
     * 添加指定模长的方向向量到Vector3(0, 0, 0)<br></br>
     * 其实就是返回此向量的坐标
     *
     *
     * Adding the direction vector of the specified modulus length to Vector3(0, 0, 0)<br></br> actually returns the coordinates of this vector
     *
     * @return the vector 3
     */
    fun addToPos(): Vector3 {
        return Vector3(
            unclonedDirectionVector.x * this.length,
            unclonedDirectionVector.y * this.length, unclonedDirectionVector.z * this.length
        )
    }

    /**
     * 将此向量的坐标添加到pos上
     *
     * @param pos the pos
     * @return the vector 3
     */
    fun addToPos(pos: Vector3): Vector3 {
        return pos.add(
            unclonedDirectionVector.x * this.length,
            unclonedDirectionVector.y * this.length, unclonedDirectionVector.z * this.length
        )
    }

    /**
     * 设置该向量的模
     *
     * @param length the length
     * @return the length
     */
    fun setLength(length: Double): BVector3 {
        this.length = length
        return this
    }

    /**
     * 增加该向量的模
     *
     *
     * 当然你也可以传入负数，但请确保最终长度要大于0!
     *
     * @param length 增加/减少的模
     * @return 自身
     */
    fun extend(length: Double): BVector3 {
        require(!((this.length + length) <= 0)) { "Vector length must bigger than zero" }
        this.length += length
        return this
    }

    val directionVector: Vector3
        /**
         * 获取单位方向向量
         *
         * @return the direction vector
         */
        get() = unclonedDirectionVector.clone()

    companion object {
        /**
         * 通过传入的Location的yaw与pitch初始化BVector3<br></br>
         * 此方法返回的BVector3的模长为传入的length值
         *
         *
         * Initialize BVector3 by passing in the yaw and pitch of the Location<br></br>
         * The module length of the BVector3 returned by this method is the length value passed in
         *
         * @param transform the location
         * @return the b vector 3
         */
        /**
         * 通过传入的Location的yaw与pitch初始化BVector3<br></br>
         * 此方法返回的BVector3的模长为1
         *
         *
         * Initialize BVector3 by passing in the yaw and pitch of Location<br></br>
         * The module length of BVector3 returned by this method is 1
         *
         * @param transform the location
         * @return the b vector 3
         */
        @JvmOverloads
        fun fromLocation(transform: Transform, length: Double = 1.0): BVector3 {
            return BVector3(transform.yaw, transform.pitch, length)
        }

        /**
         * 通过传入的yaw与pitch初始化BVector3<br></br>
         * 此方法返回的BVector3的模长为1
         *
         *
         * Initialize BVector3 by passing in yaw and pitch<br></br>
         * The module length of BVector3 returned by this method is 1
         *
         * @param yaw   the yaw (-90 270)
         * @param pitch the pitch
         * @return the b vector 3
         */
        fun fromAngle(yaw: Double, pitch: Double): BVector3 {
            return BVector3(yaw, pitch, 1.0)
        }

        /**
         * 通过传入的向量坐标初始化BVector3
         *
         *
         * Initialize B Vector 3 with the vector coordinates passed in
         *
         * @param pos 向量坐标
         * @return the b vector 3
         */
        fun fromPos(pos: Vector3): BVector3 {
            return BVector3(pos)
        }

        /**
         * 通过传入的向量坐标初始化BVector3
         *
         *
         * Initialize B Vector 3 with the vector coordinates passed in
         *
         * @param x the x
         * @param y the y
         * @param z the z
         * @return the b vector 3
         */
        fun fromPos(x: Double, y: Double, z: Double): BVector3 {
            return fromPos(Vector3(x, y, z))
        }

        /**
         * 通过yaw与pitch计算出等价的Vector3方向向量
         *
         * @param yaw   yaw
         * @param pitch pitch
         * @return Vector3方向向量
         */
        fun getDirectionVector(yaw: Double, pitch: Double): Vector3 {
            val pitch0 = StrictMath.toRadians(pitch + 90)
            val yaw0 = StrictMath.toRadians(yaw + 90)
            val x = StrictMath.sin(pitch0) * StrictMath.cos(yaw0)
            val z = StrictMath.sin(pitch0) * StrictMath.sin(yaw0)
            val y = StrictMath.cos(pitch0)
            return Vector3(x, y, z).normalize()
        }

        /**
         * 通过方向向量计算出yaw
         *
         *
         * Calculate yaw from the direction vector
         *
         * @param vector 方向向量
         * @return yaw
         */
        fun getYawFromVector(vector: Vector3): Double {
            val length = vector.x * vector.x + vector.z * vector.z
            // 避免NAN
            if (length == 0.0) {
                return 0.0
            }
            val yaw = StrictMath.toDegrees(StrictMath.asin(-vector.x / StrictMath.sqrt(length)))
            return if (-vector.z > 0.0) 180.0 - yaw else if (StrictMath.abs(yaw) < 1E-10) 0.0 else yaw
        }

        /**
         * 通过方向向量计算出pitch
         *
         *
         * Calculate the pitch by the direction vector
         *
         * @param vector 方向向量
         * @return pitch
         */
        fun getPitchFromVector(vector: Vector3): Double {
            val length = vector.x * vector.x + vector.z * vector.z + vector.y * vector.y
            // 避免NAN
            if (length == 0.0) {
                return 0.0
            }
            val pitch = StrictMath.toDegrees(StrictMath.asin(-vector.y / StrictMath.sqrt(length)))
            return if (StrictMath.abs(pitch) < 1E-10) 0.0 else pitch
        }
    }
}
