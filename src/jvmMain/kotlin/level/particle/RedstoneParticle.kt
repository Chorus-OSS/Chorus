package org.chorus_oss.chorus.level.particle

import org.chorus_oss.chorus.math.Vector3

class RedstoneParticle @JvmOverloads constructor(pos: Vector3, lifetime: Int = 1) :
    GenericParticle(pos, TYPE_RED_DUST, lifetime)
