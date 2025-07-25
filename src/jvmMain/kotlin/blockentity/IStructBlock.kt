package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.inventory.InventoryHolder

interface IStructBlock : InventoryHolder {
    companion object {
        const val TAG_CUSTOM_NAME: String = "CustomName"
        const val TAG_ANIMATION_MODE: String = "animationMode"
        const val TAG_ANIMATION_SECONDS: String = "animationSeconds"
        const val TAG_DATA: String = "data"
        const val TAG_DATA_FIELD: String = "dataField"
        const val TAG_IGNORE_ENTITIES: String = "ignoreEntities"
        const val TAG_INCLUDE_PLAYERS: String = "includePlayers"
        const val TAG_INTEGRITY: String = "integrity"
        const val TAG_MIRROR: String = "mirror"
        const val TAG_IS_POWERED: String = "isPowered"
        const val TAG_REDSTONE_SAVEMODE: String = "redstoneSaveMode"
        const val TAG_REMOVE_BLOCKS: String = "removeBlocks"
        const val TAG_ROTATION: String = "rotation"
        const val TAG_SEED: String = "seed"
        const val TAG_SHOW_BOUNDING_BOX: String = "showBoundingBox"
        const val TAG_STRUCTURE_NAME: String = "structureName"
        const val TAG_X_STRUCTURE_OFFSET: String = "xStructureOffset"
        const val TAG_Y_STRUCTURE_OFFSET: String = "yStructureOffset"
        const val TAG_Z_STRUCTURE_OFFSET: String = "zStructureOffset"
        const val TAG_X_STRUCTURE_SIZE: String = "xStructureSize"
        const val TAG_Y_STRUCTURE_SIZE: String = "yStructureSize"
        const val TAG_Z_STRUCTURE_SIZE: String = "zStructureSize"
    }
}
