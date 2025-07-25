package org.chorus_oss.chorus.level.generator.`object`

import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.utils.ChorusRandom

abstract class ObjectGenerator {
    abstract fun generate(level: BlockManager, rand: ChorusRandom, position: Vector3): Boolean
}
