package org.chorus_oss.chorus.level.generator.`object`.legacytree

import org.chorus_oss.chorus.block.property.enums.WoodType
import org.chorus_oss.chorus.level.generator.`object`.BlockManager
import org.chorus_oss.chorus.utils.ChorusRandom

class LegacyOakTree : LegacyTreeGenerator() {
    override val type: WoodType
        get() = WoodType.OAK

    override fun placeObject(level: BlockManager, x: Int, y: Int, z: Int, random: ChorusRandom) {
        this.treeHeight = random.nextInt(3) + 4
        super.placeObject(level, x, y, z, random)
    }
}
