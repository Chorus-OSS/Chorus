package org.chorus_oss.chorus.level.particle

import org.chorus_oss.chorus.math.Vector3

class SmokeParticle @JvmOverloads constructor(pos: Vector3, scale: Int = 0) : GenericParticle(pos, TYPE_SMOKE, scale)
