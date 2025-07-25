package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.enums.MonsterEggStoneType
import org.chorus_oss.chorus.item.Item

class BlockMonsterEgg @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockSolid(blockstate) {
    var monsterEggStoneType: MonsterEggStoneType
        get() = getPropertyValue(CommonBlockProperties.MONSTER_EGG_STONE_TYPE)
        set(value) {
            setPropertyValue(
                CommonBlockProperties.MONSTER_EGG_STONE_TYPE,
                value
            )
        }

    override val name: String
        get() = monsterEggStoneType.name + " Monster Egg"

    override val hardness: Double
        get() = 0.0

    override val resistance: Double
        get() = 0.75

    override fun getDrops(item: Item): Array<Item> {
        return Item.EMPTY_ARRAY
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties =
            BlockProperties(BlockID.MONSTER_EGG, CommonBlockProperties.MONSTER_EGG_STONE_TYPE)

    }
}
