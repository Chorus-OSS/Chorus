package org.chorus_oss.chorus.utils

class RollingFloatAverage(private val maxSize: Int) {
    private val queue: ArrayDeque<Float> = ArrayDeque()
    private var sum: Float = 0f

    fun add(value: Float) {
        queue.add(value)
        sum += value

        if (queue.size > maxSize) {
            val removed = queue.removeFirst()
            sum -= removed
        }
    }

    fun getAverage(): Float {
        return if (queue.isNotEmpty()) sum / queue.size else 0f
    }
}
