package org.chorus_oss.chorus.level.generator.`object`.legacytree

import org.chorus_oss.chorus.level.generator.`object`.BlockManager
import org.chorus_oss.chorus.utils.ChorusRandom


class LegacyTallBirchTree : LegacyBirchTree() {
    override fun placeObject(level: BlockManager, x: Int, y: Int, z: Int, random: ChorusRandom) {
        this.treeHeight = random.nextInt(3) + 10
        super.placeObject(level, x, y, z, random)
    }
}
