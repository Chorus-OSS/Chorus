package org.chorus_oss.chorus.inventory

import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.Vector3

interface InventoryHolder {
    val inventory: Inventory

    val level: Level?

    val vector3: Vector3
}
