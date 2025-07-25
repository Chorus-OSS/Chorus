package org.chorus_oss.chorus.block.customblock.data

import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.math.Vector3f
import org.chorus_oss.chorus.nbt.tag.CompoundTag


import kotlin.math.min


class Component : NBTData {
    private val result = CompoundTag()
    var collisionBox: CollisionBox? = null
    var selectionBox: SelectionBox? = null
    var craftingTable: CraftingTable? = null
    var destructibleByMining: Float? = null
    var destructibleByExplosion: Int? = null
    var displayName: String? = null
    var lightEmission: Int? = null
    var lightDampening: Int? = null
    var friction: Float? = null
    var geometry: Geometry? = null
    var materialInstances: Materials? = null
    var transformation: Transformation? = null
    var unitCube: Boolean? = null
    var rotation: Vector3f? = null

    override fun toCompoundTag(): CompoundTag {
        if (unitCube != null) {
            result.putCompound("minecraft:unit_cube", CompoundTag())
        }
        if (collisionBox != null) {
            result.putCompound("minecraft:collision_box", collisionBox!!.toCompoundTag())
        }
        if (selectionBox != null) {
            result.putCompound("minecraft:selection_box", selectionBox!!.toCompoundTag())
        }
        if (craftingTable != null) {
            result.putCompound("minecraft:crafting_table", craftingTable!!.toCompoundTag())
        }
        if (destructibleByMining != null) {
            result.putCompound(
                "minecraft:destructible_by_mining", CompoundTag()
                    .putFloat("value", destructibleByMining!!)
            )
        }
        if (destructibleByExplosion != null) {
            result.putCompound(
                "minecraft:destructible_by_explosion", CompoundTag()
                    .putInt("explosion_resistance", destructibleByExplosion!!)
            )
        }
        if (displayName != null) {
            result.putCompound(
                "minecraft:display_name", CompoundTag()
                    .putString("value", displayName!!)
            ) //todo 验证
        }
        if (lightEmission != null) {
            result.putCompound(
                "minecraft:light_emission", CompoundTag()
                    .putByte("emission", lightEmission!!.toByte().toInt())
            )
        }
        if (lightDampening != null) {
            result.putCompound(
                "minecraft:light_dampening", CompoundTag()
                    .putByte("lightLevel", lightDampening!!.toByte().toInt())
            )
        }
        if (friction != null) {
            result.putCompound(
                "minecraft:friction", CompoundTag()
                    .putFloat("value", min(friction!!.toDouble(), 0.9).toFloat())
            )
        }
        if (this.geometry != null) {
            result.putCompound("minecraft:geometry", geometry!!.toCompoundTag())
            result.remove("minecraft:unit_cube")
        }
        if (materialInstances != null) {
            result.putCompound(
                "minecraft:material_instances", CompoundTag()
                    .putCompound("mappings", CompoundTag())
                    .putCompound("materials", materialInstances!!.toCompoundTag())
            )
        }
        if (transformation != null) {
            result.putCompound("minecraft:transformation", transformation!!.toCompoundTag())
        }
        if (rotation != null) {
            result.putCompound(
                "minecraft:transformation",
                Transformation(Vector3(0.0, 0.0, 0.0), Vector3(1.0, 1.0, 1.0), rotation!!.asVector3()).toCompoundTag()
            )
        }
        return this.result
    }
}
