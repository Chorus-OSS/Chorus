package org.chorus_oss.chorus.math

import kotlin.math.pow
import kotlin.math.round

fun round(value: Float, precision: Int): Float {
    val scale = 10f.pow(precision)
    return round(value * scale) / scale
}

fun round(value: Double, precision: Int): Double {
    val scale = 10.0.pow(precision)
    return round(value * scale) / scale
}