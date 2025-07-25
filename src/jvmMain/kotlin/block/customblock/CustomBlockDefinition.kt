package org.chorus_oss.chorus.block.customblock

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.customblock.data.*
import org.chorus_oss.chorus.block.customblock.data.Materials.RenderMethod
import org.chorus_oss.chorus.block.property.type.BlockPropertyType
import org.chorus_oss.chorus.block.property.type.BooleanPropertyType
import org.chorus_oss.chorus.block.property.type.EnumPropertyType
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.item.customitem.data.CreativeCategory
import org.chorus_oss.chorus.item.customitem.data.CreativeGroup
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.math.Vector3f
import org.chorus_oss.chorus.nbt.tag.*
import org.chorus_oss.chorus.utils.Loggable
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

/**
 * CustomBlockDefinition用于获得发送给客户端的方块行为包数据。[CustomBlockDefinition.Builder]中提供的方法都是控制发送给客户端数据，如果需要控制服务端部分行为，请覆写[Block]中的方法。
 *
 *
 * CustomBlockDefinition is used to get the data of the block behavior_pack sent to the client. The methods provided in [CustomBlockDefinition.Builder] control the data sent to the client, if you need to control some of the server-side behavior, please override the methods in [Block].
 */

@JvmRecord
data class CustomBlockDefinition(val identifier: String, val nbt: CompoundTag) {
    val runtimeId: Int
        get() = INTERNAL_ALLOCATION_ID_MAP[identifier]!!

    class Builder(protected val customBlock: CustomBlock) {
        protected val identifier: String = customBlock.id

        protected var nbt: CompoundTag = CompoundTag()
            .putCompound("components", CompoundTag())

        init {
            val b = customBlock as Block
            val components = nbt.getCompound("components")

            //设置一些与PNX内部对应的方块属性
            components.putCompound(
                "minecraft:friction", CompoundTag()
                    .putFloat("value", 0.9.coerceAtMost(0.0.coerceAtLeast(1 - b.frictionFactor)).toFloat())
            )
                .putCompound(
                    "minecraft:destructible_by_explosion", CompoundTag()
                        .putInt("explosion_resistance", customBlock.resistance.toInt())
                )
                .putCompound(
                    "minecraft:light_dampening", CompoundTag()
                        .putByte("lightLevel", customBlock.lightFilter.toByte().toInt())
                )
                .putCompound(
                    "minecraft:light_emission", CompoundTag()
                        .putByte("emission", customBlock.lightLevel.toByte().toInt())
                )
                .putCompound(
                    "minecraft:destructible_by_mining", CompoundTag()
                        .putFloat("value", 99999f)
                ) //default server-side mining time calculate
            //设置材质
            components.putCompound(
                "minecraft:material_instances", CompoundTag()
                    .putCompound("mappings", CompoundTag())
                    .putCompound("materials", CompoundTag())
            )

            //默认单位立方体方块
            components.putCompound("minecraft:unit_cube", CompoundTag())
            //设置默认单位立方体方块的几何模型
            components.putCompound(
                "minecraft:geometry", CompoundTag()
                    .putString("identifier", "minecraft:geometry.full_block")
                    .putString("culling", "")
                    .putCompound("bone_visibility", CompoundTag())
            )

            //设置方块在创造栏的分类
            nbt.putCompound(
                "menu_category", CompoundTag()
                    .putString("category", CreativeCategory.NATURE.name)
                    .putString("group", CreativeGroup.NONE.groupName)
            )
            //molang版本
            nbt.putInt("molangVersion", 9)

            //设置方块的properties
            val propertiesNBT = propertiesNBT
            if (propertiesNBT != null) {
                nbt.putList("properties", propertiesNBT)
            }

            var blockId: Int
            if (!INTERNAL_ALLOCATION_ID_MAP.containsKey(identifier)) {
                while (INTERNAL_ALLOCATION_ID_MAP.containsValue(
                        CUSTOM_BLOCK_RUNTIME_ID.getAndIncrement().also { blockId = it })
                )
                    INTERNAL_ALLOCATION_ID_MAP[identifier] = blockId
            } else {
                blockId = INTERNAL_ALLOCATION_ID_MAP[identifier]!!
            }
            nbt.putCompound(
                "vanilla_block_data", CompoundTag().putInt("block_id", blockId) /*.putString("material", "")*/
            ) //todo Figure what is dirt, maybe that corresponds to https://wiki.bedrock.dev/documentation/materials.html
        }

        fun texture(texture: String): Builder {
            this.materials(Materials.builder().any(RenderMethod.OPAQUE, texture))
            return this
        }

        fun name(name: String): Builder {
            check(name.isNotBlank()) { "name is blank" }
            nbt.getCompound("components").putCompound(
                "minecraft:display_name", CompoundTag()
                    .putString("value", name)
            )
            return this
        }

        fun materials(materials: Materials): Builder {
            nbt.getCompound("components").putCompound(
                "minecraft:material_instances", CompoundTag()
                    .putCompound("mappings", CompoundTag())
                    .putCompound("materials", materials.toCompoundTag())
            )
            return this
        }

        fun creativeGroupAndCategory(creativeGroup: CreativeGroup, creativeCategory: CreativeCategory): Builder {
            nbt.getCompound("menu_category")
                .putString("category", creativeCategory.name.lowercase())
                .putString("group", creativeGroup.groupName)
            return this
        }

        fun creativeCategory(creativeCategory: String): Builder {
            nbt.getCompound("menu_category")
                .putString("category", creativeCategory.lowercase())
            return this
        }

        fun creativeCategory(creativeCategory: CreativeCategory): Builder {
            nbt.getCompound("menu_category")
                .putString("category", creativeCategory.name.lowercase())
            return this
        }

        /**
         * 控制自定义方块客户端侧的挖掘时间(单位秒)
         *
         *
         * 自定义方块的挖掘时间取决于服务端侧和客户端侧中最小的一个
         *
         *
         * Control the digging time (in seconds) on the client side of the custom block
         *
         *
         * The digging time of a custom cube depends on the smallest of the server-side and client-side
         */
        fun breakTime(second: Double): Builder {
            nbt.getCompound("components")
                .putCompound(
                    "minecraft:destructible_by_mining", CompoundTag()
                        .putFloat("value", second.toFloat())
                )
            return this
        }

        /**
         * Control the grouping of custom blocks in the creation inventory.
         *
         * [wiki.bedrock.dev](https://wiki.bedrock.dev/documentation/creative-categories.html)
         */
        fun creativeGroup(creativeGroup: String): Builder {
            if (creativeGroup.isBlank()) {
                log.error("creativeGroup has an invalid value!")
                return this
            }
            nbt.getCompound("components")
                .getCompound("menu_category").putString("group", creativeGroup.lowercase())
            return this
        }

        /**
         * Control the grouping of custom blocks in the creation inventory.
         *
         * [wiki.bedrock.dev](https://wiki.bedrock.dev/documentation/creative-categories.html)
         */
        fun creativeGroup(creativeGroup: CreativeGroup): Builder {
            nbt.getCompound("components")
                .getCompound("menu_category").putString("group", creativeGroup.groupName)
            return this
        }

        /**
         * [wiki.bedrock.dev](https://wiki.bedrock.dev/blocks/block-components.html.crafting-table)
         */
        fun craftingTable(craftingTable: CraftingTable): Builder {
            nbt.getCompound("components").putCompound("minecraft:crafting_table", craftingTable.toCompoundTag())
            return this
        }

        /**
         * supports rotation, scaling, and translation. The component can be added to the whole block and/or to individual block permutations. Transformed geometries still have the same restrictions that non-transformed geometries have such as a maximum size of 30/16 units.
         */
        fun transformation(transformation: Transformation): Builder {
            nbt.getCompound("components").putCompound("minecraft:transformation", transformation.toCompoundTag())
            return this
        }

        /**
         * 以度为单位设置块围绕立方体中心的旋转,旋转顺序为 xyz.角度必须是90的倍数。
         *
         *
         * Set the rotation of the block around the center of the block in degrees, the rotation order is xyz. The angle must be a multiple of 90.
         */
        fun rotation(rotation: Vector3f): Builder {
            this.transformation(Transformation(Vector3(0.0, 0.0, 0.0), Vector3(1.0, 1.0, 1.0), rotation.asVector3()))
            return this
        }

        /**
         * @see .geometry
         */
        fun geometry(geometry: String): Builder {
            if (geometry.isBlank()) {
                log.error("geometry has an invalid value!")
                return this
            }
            val components = nbt.getCompound("components")
            //默认单位立方体方块，如果定义几何模型需要移除
            if (components.contains("minecraft:unit_cube")) components.remove("minecraft:unit_cube")
            //设置方块对应的几何模型
            components.putCompound(
                "minecraft:geometry", CompoundTag()
                    .putString("identifier", geometry.lowercase())
            )
            return this
        }

        /**
         * 控制自定义方块的几何模型,如果不设置默认为单位立方体
         *
         *
         * Control the geometric model of the custom block, if not set the default is the unit cube.<br></br>
         * Geometry identifier from geo file in 'RP/models/blocks' folder
         */
        fun geometry(geometry: Geometry): Builder {
            val components = nbt.getCompound("components")
            //默认单位立方体方块，如果定义几何模型需要移除
            if (components.contains("minecraft:unit_cube")) components.remove("minecraft:unit_cube")
            //设置方块对应的几何模型
            components.putCompound("minecraft:geometry", geometry.toCompoundTag())
            return this
        }

        /**
         * 控制自定义方块的变化特征，例如条件渲染，部分渲染等
         *
         *
         * Control custom block permutation features such as conditional rendering, partial rendering, etc.
         */
        fun permutation(permutation: Permutation): Builder {
            if (!nbt.contains("permutations")) {
                nbt.putList("permutations", ListTag<CompoundTag>())
            }
            val permutations = nbt.getList(
                "permutations",
                CompoundTag::class.java
            )
            permutations.add(permutation.toCompoundTag())
            nbt.putList("permutations", permutations)
            return this
        }

        /**
         * 控制自定义方块的变化特征，例如条件渲染，部分渲染等
         *
         *
         * Control custom block permutation features such as conditional rendering, partial rendering, etc.
         */
        fun permutations(vararg permutations: Permutation): Builder {
            val per = ListTag<CompoundTag>()
            for (permutation in permutations) {
                per.add(permutation.toCompoundTag())
            }
            nbt.putList("permutations", per)
            return this
        }

        /**
         * 设置此方块的客户端碰撞箱。
         *
         *
         * Set the client collision box for this block.
         *
         * @param origin 碰撞箱的原点 The origin of the collision box
         * @param size   碰撞箱的大小 The size of the collision box
         */
        fun collisionBox(origin: Vector3f, size: Vector3f): Builder {
            nbt.getCompound("components")
                .putCompound(
                    "minecraft:collision_box", CompoundTag()
                        .putBoolean("enabled", true)
                        .putList(
                            "origin", ListTag<FloatTag>()
                                .add(FloatTag(origin.x))
                                .add(FloatTag(origin.y))
                                .add(FloatTag(origin.z))
                        )
                        .putList(
                            "size", ListTag<FloatTag>()
                                .add(FloatTag(size.x))
                                .add(FloatTag(size.y))
                                .add(FloatTag(size.z))
                        )
                )
            return this
        }

        /**
         * 设置此方块的客户端选择箱。
         *
         *
         * Set the client collision box for this block.
         *
         * @param origin 选择箱的原点 The origin of the collision box
         * @param size   选择箱的大小 The size of the collision box
         */
        fun selectionBox(origin: Vector3f, size: Vector3f): Builder {
            nbt.getCompound("components")
                .putCompound(
                    "minecraft:selection_box", CompoundTag()
                        .putBoolean("enabled", true)
                        .putList(
                            "origin", ListTag<FloatTag>()
                                .add(FloatTag(origin.x))
                                .add(FloatTag(origin.y))
                                .add(FloatTag(origin.z))
                        )
                        .putList(
                            "size", ListTag<FloatTag>()
                                .add(FloatTag(size.x))
                                .add(FloatTag(size.y))
                                .add(FloatTag(size.z))
                        )
                )
            return this
        }

        fun blockTags(vararg tag: String): Builder {
            check(tag.isNotEmpty())
            val stringTagListTag = ListTag<StringTag>()
            for (s in tag) {
                stringTagListTag.add(StringTag(s))
            }
            nbt.putList("blockTags", stringTagListTag)
            return this
        }

        private val propertiesNBT: ListTag<CompoundTag>?
            /**
             * @return Block Properties in NBT Tag format
             */
            get() {
                if (customBlock is Block) {
                    val properties = customBlock.properties
                    val propertyTypeSet: Set<BlockPropertyType<*>> = properties.getPropertyTypeSet()
                    if (propertyTypeSet.isEmpty()) {
                        return null
                    }
                    val nbtList = ListTag<CompoundTag>()
                    for (each in propertyTypeSet) {
                        when (each) {
                            is BooleanPropertyType -> {
                                nbtList.add(
                                    CompoundTag().putString("name", each.name)
                                        .putList(
                                            "enum", ListTag<ByteTag>()
                                                .add(ByteTag(0))
                                                .add(ByteTag(1))
                                        )
                                )
                            }

                            is IntPropertyType -> {
                                val enumList = ListTag<IntTag>()
                                for (i in each.min..each.max) {
                                    enumList.add(IntTag(i))
                                }
                                nbtList.add(CompoundTag().putString("name", each.name).putList("enum", enumList))
                            }

                            is EnumPropertyType<*> -> {
                                val enumList = ListTag<StringTag>()
                                for (e in each.validValues) {
                                    enumList.add(StringTag(e.name.lowercase()))
                                }
                                nbtList.add(CompoundTag().putString("name", each.name).putList("enum", enumList))
                            }
                        }
                    }
                    return nbtList
                }
                return null
            }

        /**
         * 对要发送给客户端的方块ComponentNBT进行自定义处理，这里包含了所有对自定义方块的定义。在符合条件的情况下，你可以任意修改。
         *
         *
         * Custom processing of the block to be sent to the client ComponentNBT, which contains all definitions for custom block. You can modify them as much as you want, under the right conditions.
         */
        fun customBuild(nbt: Consumer<CompoundTag?>): CustomBlockDefinition {
            val def = this.build()
            nbt.accept(def.nbt)
            return def
        }

        fun build(): CustomBlockDefinition {
            return CustomBlockDefinition(this.identifier, this.nbt)
        }
    }

    companion object : Loggable {
        private val INTERNAL_ALLOCATION_ID_MAP = HashMap<String?, Int>()
        private val CUSTOM_BLOCK_RUNTIME_ID = AtomicInteger(10000)

        fun getRuntimeId(identifier: String?): Int {
            return INTERNAL_ALLOCATION_ID_MAP[identifier]!!
        }

        /**
         * Builder custom block definition.
         *
         * @param customBlock the custom block
         * @return the custom block definition builder.
         */
        fun builder(customBlock: CustomBlock): Builder {
            return Builder(customBlock)
        }
    }
}
