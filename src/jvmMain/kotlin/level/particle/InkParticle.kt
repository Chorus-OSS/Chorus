package org.chorus_oss.chorus.level.particle

import org.chorus_oss.chorus.math.Vector3

class InkParticle @JvmOverloads constructor(pos: Vector3, scale: Int = 0) : GenericParticle(pos, TYPE_INK, scale)
