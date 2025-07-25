package org.chorus_oss.chorus.level

import org.chorus_oss.chorus.math.Vector3

class Vector3WithRuntimeId(x: Double, y: Double, z: Double, var runtimeIdLayer0: Int, var runtimeIdLayer1: Int) :
    Vector3(x, y, z)
