package org.chorus_oss.chorus.item.customitem

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.customitem.data.CreativeCategory
import org.chorus_oss.chorus.item.customitem.data.CreativeGroup
import org.chorus_oss.chorus.item.customitem.data.DigProperty
import org.chorus_oss.chorus.item.customitem.data.RenderOffsets
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.nbt.tag.StringTag
import org.chorus_oss.chorus.nbt.tag.Tag
import org.chorus_oss.chorus.tags.ItemTags
import org.chorus_oss.chorus.utils.Identifier
import org.chorus_oss.chorus.utils.Loggable
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

/**
 * CustomBlockDefinition用于获得发送给客户端的物品行为包数据。[CustomItemDefinition.SimpleBuilder]中提供的方法都是控制发送给客户端数据，如果需要控制服务端部分行为，请覆写[Item][org.chorus_oss.chorus.item.Item]中的方法。
 *
 *
 * CustomBlockDefinition is used to get the data of the item behavior_pack sent to the client. The methods provided in [CustomItemDefinition.SimpleBuilder] control the data sent to the client, if you need to control some of the server-side behavior, please override the methods in [Item][org.chorus_oss.chorus.item.Item].
 */

@JvmRecord
data class CustomItemDefinition(@JvmField val identifier: String, @JvmField val nbt: CompoundTag) : BlockID {
    val displayName: String?
        get() {
            if (!nbt.getCompound("components").contains("minecraft:display_name")) return null
            return nbt.getCompound("components").getCompound("minecraft:display_name")
                .getString("value")
        }

    val texture: String
        get() = nbt.getCompound("components")
            .getCompound("item_properties")
            .getCompound("minecraft:icon")
            .getCompound("textures")
            .getString("default")

    val runtimeId: Int
        get() = INTERNAL_ALLOCATION_ID_MAP[identifier]!!

    open class SimpleBuilder(customItem: CustomItem) {
        protected val identifier: String = (customItem as Item).id
        protected val nbt: CompoundTag = CompoundTag()
            .putCompound(
                "components", CompoundTag()
                    .putCompound(
                        "item_properties", CompoundTag()
                            .putCompound("minecraft:icon", CompoundTag())
                    )
            )
        private val item = customItem as Item
        protected var texture: String? = null
        protected var name: String? = null

        init {
            //定义最大堆叠数量
            nbt.getCompound("components")
                .getCompound("item_properties")
                .putInt("max_stack_size", item.maxStackSize)
            //定义在创造栏的分类
            nbt.getCompound("components")
                .getCompound("item_properties") //1 none
                .putInt("creative_category", CreativeCategory.NONE.ordinal + 1)
                .putString("creative_group", CreativeGroup.NONE.groupName)
        }

        fun texture(texture: String): SimpleBuilder {
            check(!texture.isBlank()) { "texture name is blank" }
            this.texture = texture
            return this
        }

        fun name(name: String): SimpleBuilder {
            check(!name.isBlank()) { "name is blank" }
            this.name = name
            return this
        }


        /**
         * 是否允许副手持有
         *
         *
         * Whether to allow the offHand to have
         */
        fun allowOffHand(allowOffHand: Boolean): SimpleBuilder {
            nbt.getCompound("components")
                .getCompound("item_properties")
                .putBoolean("allow_off_hand", allowOffHand)
            return this
        }

        /**
         * 控制第三人称手持物品的显示方式
         *
         *
         * Control how third-person handheld items are displayed
         */
        fun handEquipped(handEquipped: Boolean): SimpleBuilder {
            nbt.getCompound("components")
                .getCompound("item_properties")
                .putBoolean("hand_equipped", handEquipped)
            return this
        }

        /**
         * @param foil 自定义物品是否带有附魔光辉效果<br></br>whether or not the item has an enchanted light effect
         */
        fun foil(foil: Boolean): SimpleBuilder {
            nbt.getCompound("components")
                .getCompound("item_properties")
                .putBoolean("foil", foil)
            return this
        }

        /**
         * 控制自定义物品在创造栏的分组,例如所有的附魔书是一组
         *
         *
         * Control the grouping of custom items in the creation inventory, e.g. all enchantment books are grouped together
         *
         * @see [bedrock wiki](https://wiki.bedrock.dev/documentation/creative-categories.html.list-of-creative-categories)
         */
        fun creativeGroup(creativeGroup: String): SimpleBuilder {
            if (creativeGroup.isBlank()) {
                log.error("creativeGroup has an invalid value!")
                return this
            }
            nbt.getCompound("components")
                .getCompound("item_properties")
                .putString("creative_group", creativeGroup)
            return this
        }

        /**
         * 控制自定义物品在创造栏的分组,例如所有的附魔书是一组
         *
         *
         * Control the grouping of custom items in the creation inventory, e.g. all enchantment books are grouped together
         *
         * @see [bedrock wiki](https://wiki.bedrock.dev/documentation/creative-categories.html.list-of-creative-categories)
         */
        fun creativeGroup(creativeGroup: CreativeGroup): SimpleBuilder {
            nbt.getCompound("components")
                .getCompound("item_properties")
                .putString("creative_group", creativeGroup.groupName)
            return this
        }

        fun creativeCategory(creativeCategory: CreativeCategory): SimpleBuilder {
            nbt.getCompound("components")
                .getCompound("item_properties")
                .putInt("creative_category", creativeCategory.ordinal + 1)
            return this
        }

        /**
         * 控制自定义物品在不同视角下的渲染偏移
         *
         *
         * Control rendering offsets of custom items at different viewpoints
         */
        fun renderOffsets(renderOffsets: RenderOffsets): SimpleBuilder {
            nbt.getCompound("components")
                .putCompound("minecraft:render_offsets", renderOffsets.nbt)
            return this
        }

        /**
         * 向自定义物品添加一个tag，通常用于合成等
         *
         *
         * Add a tag to a custom item, usually used for crafting, etc.
         *
         * @param tags the tags
         * @return the simple builder
         */
        fun tag(vararg tags: String): SimpleBuilder {
            Arrays.stream(tags).forEach { id -> Identifier.assertValid(id) }
            val list = nbt.getCompound("components").getList(
                "item_tags",
                StringTag::class.java
            )
            for (s in tags) {
                list.add(StringTag(s))
            }
            nbt.getCompound("components").putList("item_tags", list)
            return this
        }

        /**
         * 控制拿该物品的玩家是否可以在创造模式挖掘方块
         *
         *
         * Control whether the player with the item can dig the block in creation mode
         *
         * @param value the value
         * @return the simple builder
         */
        fun canDestroyInCreative(value: Boolean): SimpleBuilder {
            nbt.getCompound("components")
                .getCompound("item_properties")
                .putBoolean("can_destroy_in_creative", value)
            return this
        }

        /**
         * 对要发送给客户端的物品ComponentNBT进行自定义处理，这里包含了所有对自定义物品的定义。在符合条件的情况下，你可以任意修改。
         *
         *
         * Custom processing of the item to be sent to the client ComponentNBT, which contains all definitions for custom item. You can modify them as much as you want, under the right conditions.
         */
        fun customBuild(nbt: Consumer<CompoundTag>): CustomItemDefinition {
            val def = this.build()
            nbt.accept(def.nbt)
            return def
        }

        open fun build(): CustomItemDefinition {
            return calculateID()
        }

        protected fun calculateID(): CustomItemDefinition {
            checkNotNull(texture) { "You must define the texture through SimpleBuilder#texture method!" }
            //定义材质
            nbt.getCompound("components")
                .getCompound("item_properties")
                .getCompound("minecraft:icon")
                .putCompound(
                    "textures", CompoundTag()
                        .putString("default", texture!!)
                )

            if (name != null) {
                //定义显示名
                nbt.getCompound("components")
                    .putCompound("minecraft:display_name", CompoundTag().putString("value", name!!))
            }

            val result = CustomItemDefinition(identifier, nbt)
            var id: Int
            if (!INTERNAL_ALLOCATION_ID_MAP.containsKey(result.identifier)) {
                while (INTERNAL_ALLOCATION_ID_MAP.containsValue(nextRuntimeId.getAndIncrement().also { id = it })) {
                }
                INTERNAL_ALLOCATION_ID_MAP[result.identifier] = id
            } else {
                id = INTERNAL_ALLOCATION_ID_MAP[result.identifier]!!
            }
            result.nbt.putString("name", result.identifier)
            result.nbt.putInt("id", id)
            return result
        }

        /**
         * 添加一个可修理该物品的物品
         *
         *
         * Add an item that can repair the item
         *
         * @param repairItemNames the repair item names
         * @param molang          the molang
         * @return the simple builder
         */
        protected fun addRepairs(repairItemNames: List<String>, molang: String): SimpleBuilder {
            if (molang.isBlank()) {
                log.error("repairAmount has an invalid value!")
                return this
            }

            if (nbt.getCompound("components").contains("minecraft:repairable")) {
                val repair_items = nbt
                    .getCompound("components")
                    .getCompound("minecraft:repairable")
                    .getList("repair_items", CompoundTag::class.java)

                val items = ListTag<CompoundTag>()
                for (name in repairItemNames) {
                    items.add(CompoundTag().putString("name", name))
                }

                repair_items.add(
                    CompoundTag()
                        .putList("items", items)
                        .putCompound(
                            "repair_amount", CompoundTag()
                                .putString("expression", molang)
                                .putInt("version", 1)
                        )
                )
            } else {
                val repair_items = ListTag<CompoundTag>()
                val items = ListTag<CompoundTag>()
                for (name in repairItemNames) {
                    items.add(CompoundTag().putString("name", name))
                }
                repair_items.add(
                    CompoundTag()
                        .putList("items", items)
                        .putCompound(
                            "repair_amount", CompoundTag()
                                .putString("expression", molang)
                                .putInt("version", 1)
                        )
                )
                nbt.getCompound("components")
                    .putCompound(
                        "minecraft:repairable", CompoundTag()
                            .putList("repair_items", repair_items)
                    )
            }
            return this
        }
    }

    class ToolBuilder(private val item: ItemCustomTool) : SimpleBuilder(item) {
        private val blocks: MutableList<CompoundTag> = ArrayList()
        private val blockTags: MutableList<String> = ArrayList()
        private val diggerRoot: CompoundTag = CompoundTag()
            .putBoolean("use_efficiency", true)
            .putList("destroy_speeds", ListTag<CompoundTag>(Tag.TAG_COMPOUND.toInt()))
        private var speed: Int? = null

        init {
            nbt.getCompound("components")
                .getCompound("item_properties")
                .putInt("enchantable_value", item.enchantAbility)

            nbt.getCompound("components")
                .getCompound("item_properties")
                .putFloat("mining_speed", 1f)
                .putBoolean("can_destroy_in_creative", true)
        }

        fun addRepairItemName(repairItemName: String, molang: String): ToolBuilder {
            super.addRepairs(listOf(repairItemName), molang)
            return this
        }

        fun addRepairItemName(repairItemName: String, repairAmount: Int): ToolBuilder {
            super.addRepairs(listOf(repairItemName), repairAmount.toString())
            return this
        }

        fun addRepairItems(repairItems: List<Item>, molang: String): ToolBuilder {
            super.addRepairs(repairItems.stream().map { obj: Item -> obj.id }.toList(), molang)
            return this
        }

        fun addRepairItems(repairItems: List<Item>, repairAmount: Int): ToolBuilder {
            super.addRepairs(repairItems.stream().map { obj: Item -> obj.id }.toList(), repairAmount.toString())
            return this
        }

        /**
         * 控制采集类工具的挖掘速度
         *
         * @param speed 挖掘速度
         */
        fun speed(speed: Int): ToolBuilder {
            if (speed < 0) {
                CustomItemDefinition.log.error("speed has an invalid value!")
                return this
            }
            if (item.isPickaxe || item.isShovel || item.isHoe || item.isAxe || item.isShears) {
                this.speed = speed
            }
            return this
        }

        /**
         * 给工具添加可挖掘的方块，及挖掘它的速度
         *
         *
         * Add a diggable block to the tool and define dig speed
         *
         * @param blockName the block name
         * @param speed     挖掘速度
         * @return the tool builder
         */
        fun addExtraBlock(blockName: String, speed: Int): ToolBuilder {
            if (speed < 0) {
                CustomItemDefinition.log.error("speed has an invalid value!")
                return this
            }
            blocks.add(
                CompoundTag()
                    .putCompound(
                        "block", CompoundTag()
                            .putString("name", blockName)
                            .putCompound("states", CompoundTag())
                            .putString("tags", "")
                    )
                    .putInt("speed", speed)
            )
            return this
        }

        /**
         * 给工具添加可挖掘的方块，及挖掘它的速度
         *
         *
         * Add a diggable block to the tool and define dig speed
         *
         * @param blocks the blocks
         * @return the tool builder
         */
        fun addExtraBlocks(blocks: Map<String?, Int?>): ToolBuilder {
            blocks.forEach { (blockName: String?, speed: Int?) ->
                if (speed!! < 0) {
                    CustomItemDefinition.log.error("speed has an invalid value!")
                    return@forEach
                }
                this.blocks.add(
                    CompoundTag()
                        .putCompound(
                            "block", CompoundTag()
                                .putString("name", blockName!!)
                                .putCompound("states", CompoundTag())
                                .putString("tags", "")
                        )
                        .putInt("speed", speed)
                )
            }
            return this
        }

        /**
         * 给工具添加可挖掘的方块，及挖掘它的速度
         *
         *
         * Add a diggable block to the tool and define dig speed
         *
         * @param blockName the block name
         * @param property  the property
         * @return the tool builder
         */
        fun addExtraBlocks(blockName: String, property: DigProperty): ToolBuilder {
            val propertySpeed: Int?
            if ((property.getSpeed().also { propertySpeed = it }) != null && propertySpeed!! < 0) {
                CustomItemDefinition.log.error("speed has an invalid value!")
                return this
            }
            blocks.add(
                CompoundTag()
                    .putCompound(
                        "block", CompoundTag()
                            .putString("name", blockName)
                            .putCompound("states", property.states)
                            .putString("tags", "")
                    )
                    .putInt("speed", propertySpeed!!)
            )
            return this
        }

        /**
         * 给工具添加可挖掘的一类方块，用blockTag描述，挖掘它们的速度为[.speed]的速度，如果没定义则为工具TIER对应的速度
         *
         *
         * Add a class of block to the tool that can be mined, described by blockTag, and the speed to mine them is the speed of [.speed], or the speed corresponding to the tool TIER if it is not defined
         *
         * @param blockTags 挖掘速度
         * @return the tool builder
         */
        fun addExtraBlockTags(blockTags: List<String>): ToolBuilder {
            if (!blockTags.isEmpty()) {
                this.blockTags.addAll(blockTags)
            }
            return this
        }

        override fun build(): CustomItemDefinition {
            //附加耐久 攻击伤害信息
            nbt.getCompound("components")
                .putCompound("minecraft:durability", CompoundTag().putInt("max_durability", item.maxDurability))
                .getCompound("item_properties")
                .putInt("damage", item.attackDamage)

            if (speed == null) {
                speed = when (item.tier) {
                    6 -> 7
                    5 -> 6
                    4 -> 5
                    3 -> 4
                    2 -> 3
                    1 -> 2
                    else -> 1
                }
            }
            var type: String? = null
            if (item.isPickaxe) {
                //添加可挖掘方块Tags
                blockTags.addAll(
                    listOf(
                        "'stone'",
                        "'metal'",
                        "'diamond_pick_diggable'",
                        "'mob_spawner'",
                        "'rail'",
                        "'slab_block'",
                        "'stair_block'",
                        "'smooth stone slab'",
                        "'sandstone slab'",
                        "'cobblestone slab'",
                        "'brick slab'",
                        "'stone bricks slab'",
                        "'quartz slab'",
                        "'nether brick slab'"
                    )
                )
                //添加可挖掘方块
                type = ItemTags.IS_PICKAXE
                //附加附魔信息
                nbt.getCompound("components").getCompound("item_properties")
                    .putString("enchantable_slot", "pickaxe")
                this.tag("minecraft:is_pickaxe")
            } else if (item.isAxe) {
                blockTags.addAll(listOf("'wood'", "'pumpkin'", "'plant'"))
                type = ItemTags.IS_AXE
                nbt.getCompound("components").getCompound("item_properties")
                    .putString("enchantable_slot", "axe")
                this.tag("minecraft:is_axe")
            } else if (item.isShovel) {
                blockTags.addAll(listOf("'sand'", "'dirt'", "'gravel'", "'grass'", "'snow'"))
                type = ItemTags.IS_SHOVEL
                nbt.getCompound("components").getCompound("item_properties")
                    .putString("enchantable_slot", "shovel")
                this.tag("minecraft:is_shovel")
            } else if (item.isHoe) {
                nbt.getCompound("components").getCompound("item_properties")
                    .putString("enchantable_slot", "hoe")
                type = ItemTags.IS_HOE
                this.tag("minecraft:is_hoe")
            } else if (item.isSword) {
                nbt.getCompound("components").getCompound("item_properties")
                    .putString("enchantable_slot", "sword")
                type = ItemTags.IS_SWORD
            } else {
                if (nbt.getCompound("components").contains("item_tags")) {
                    val list = nbt.getCompound("components").getList(
                        "item_tags",
                        StringTag::class.java
                    ).all
                    for (tag in list) {
                        val id = tag.parseValue()
                        if (toolBlocks.containsKey(id)) {
                            type = id
                            break
                        }
                    }
                }
            }
            if (type != null) {
                toolBlocks[type]!!.forEach { (k, v) ->
                    if (v.getSpeed() == null) v.setSpeed(speed!!)
                    blocks.add(
                        CompoundTag()
                            .putCompound(
                                "block", CompoundTag()
                                    .putString("name", k)
                                    .putCompound("states", v.states)
                                    .putString("tags", "")
                            )
                            .putInt("speed", v.getSpeed()!!)
                    )
                }
            }
            //添加可挖掘的方块tags
            if (!blockTags.isEmpty()) {
                val cmp = CompoundTag()
                cmp.putCompound(
                    "block", CompoundTag()
                        .putString("name", "")
                        .putCompound("states", CompoundTag())
                        .putString("tags", "q.any_tag(" + java.lang.String.join(", ", this.blockTags) + ")")
                )
                    .putInt("speed", speed!!)
                diggerRoot.getList("destroy_speeds", CompoundTag::class.java).add(cmp)
                nbt.getCompound("components")
                    .putCompound("minecraft:digger", this.diggerRoot)
            }
            if (!blocks.isEmpty()) {
                //添加可挖掘的方块
                for (k in this.blocks) {
                    diggerRoot.getList("destroy_speeds", CompoundTag::class.java).add(k)
                }
                if (!nbt.getCompound("components").containsCompound("minecraft:digger")) {
                    nbt.getCompound("components")
                        .putCompound("minecraft:digger", this.diggerRoot)
                }
            }
            return calculateID()
        }

        companion object : Loggable {
            var toolBlocks: MutableMap<String, Map<String, DigProperty>> = HashMap()

            init {
                val pickaxeBlocks = HashMap<String, DigProperty>()
                val axeBlocks = HashMap<String, DigProperty>()
                val shovelBlocks = HashMap<String, DigProperty>()
                val hoeBlocks = HashMap<String, DigProperty>()
                val swordBlocks = HashMap<String, DigProperty>()
                for (name in listOf<String>(
                    BlockID.ACACIA_SLAB,
                    BlockID.BAMBOO_MOSAIC_SLAB,
                    BlockID.BAMBOO_SLAB,
                    BlockID.BIRCH_SLAB,
                    BlockID.BLACKSTONE_SLAB,
                    BlockID.BRICK_SLAB,
                    BlockID.CHERRY_SLAB,
                    BlockID.COBBLED_DEEPSLATE_SLAB,
                    BlockID.COBBLESTONE_SLAB,
                    BlockID.CRIMSON_SLAB,
                    BlockID.CUT_COPPER_SLAB,
                    BlockID.DARK_OAK_SLAB,
                    BlockID.DEEPSLATE_BRICK_SLAB,
                    BlockID.DEEPSLATE_TILE_SLAB,
                    BlockID.EXPOSED_CUT_COPPER_SLAB,
                    BlockID.JUNGLE_SLAB,
                    BlockID.MANGROVE_SLAB,
                    BlockID.MUD_BRICK_SLAB,
                    BlockID.NETHER_BRICK_SLAB,
                    BlockID.OAK_SLAB,
                    BlockID.OXIDIZED_CUT_COPPER_SLAB,
                    BlockID.PETRIFIED_OAK_SLAB,
                    BlockID.POLISHED_BLACKSTONE_BRICK_SLAB,
                    BlockID.POLISHED_BLACKSTONE_SLAB,
                    BlockID.POLISHED_DEEPSLATE_SLAB,
                    BlockID.POLISHED_TUFF_SLAB,
                    BlockID.QUARTZ_SLAB,
                    BlockID.SANDSTONE_SLAB,
                    BlockID.SMOOTH_STONE_SLAB,
                    BlockID.SPRUCE_SLAB,
                    BlockID.STONE_BRICK_SLAB,
                    BlockID.TUFF_BRICK_SLAB,
                    BlockID.TUFF_SLAB,
                    BlockID.WARPED_SLAB,
                    BlockID.WAXED_CUT_COPPER_SLAB,
                    BlockID.WAXED_EXPOSED_CUT_COPPER_SLAB,
                    BlockID.WAXED_OXIDIZED_CUT_COPPER_SLAB,
                    BlockID.WAXED_WEATHERED_CUT_COPPER_SLAB,
                    BlockID.WEATHERED_CUT_COPPER_SLAB,
                    BlockID.ICE,
                    BlockID.BLUE_ICE,
                    BlockID.UNDYED_SHULKER_BOX,
                    BlockID.BLUE_SHULKER_BOX,
                    BlockID.RED_SHULKER_BOX,
                    BlockID.BLACK_SHULKER_BOX,
                    BlockID.CYAN_SHULKER_BOX,
                    BlockID.BROWN_SHULKER_BOX,
                    BlockID.LIME_SHULKER_BOX,
                    BlockID.GRAY_SHULKER_BOX,
                    BlockID.GREEN_SHULKER_BOX,
                    BlockID.LIGHT_BLUE_SHULKER_BOX,
                    BlockID.LIGHT_GRAY_SHULKER_BOX,
                    BlockID.MAGENTA_SHULKER_BOX,
                    BlockID.ORANGE_SHULKER_BOX,
                    BlockID.WHITE_SHULKER_BOX,
                    BlockID.YELLOW_SHULKER_BOX,
                    BlockID.PINK_SHULKER_BOX,
                    BlockID.PURPLE_SHULKER_BOX,
                    BlockID.PRISMARINE,
                    BlockID.PRISMARINE_BRICKS_STAIRS,
                    BlockID.PRISMARINE_STAIRS,
                    BlockID.STONE_BLOCK_SLAB4,
                    BlockID.DARK_PRISMARINE_STAIRS,
                    BlockID.ANVIL,
                    BlockID.BONE_BLOCK,
                    BlockID.IRON_TRAPDOOR,
                    BlockID.NETHER_BRICK_FENCE,
                    BlockID.CRYING_OBSIDIAN,
                    BlockID.MAGMA,
                    BlockID.SMOKER,
                    BlockID.LIT_SMOKER,
                    BlockID.HOPPER,
                    BlockID.REDSTONE_BLOCK,
                    BlockID.MOB_SPAWNER,
                    BlockID.NETHERITE_BLOCK,
                    BlockID.SMOOTH_STONE,
                    BlockID.DIAMOND_BLOCK,
                    BlockID.LAPIS_BLOCK,
                    BlockID.EMERALD_BLOCK,
                    BlockID.ENCHANTING_TABLE,
                    BlockID.END_BRICKS,
                    BlockID.CRACKED_POLISHED_BLACKSTONE_BRICKS,
                    BlockID.NETHER_BRICK,
                    BlockID.CRACKED_NETHER_BRICKS,
                    BlockID.PURPUR_BLOCK,
                    BlockID.PURPUR_STAIRS,
                    BlockID.END_BRICK_STAIRS,
                    BlockID.STONE_BLOCK_SLAB2,
                    BlockID.STONE_BLOCK_SLAB3,
                    BlockID.STONE_BRICK_STAIRS,
                    BlockID.MOSSY_STONE_BRICK_STAIRS,
                    BlockID.POLISHED_BLACKSTONE_BRICKS,
                    BlockID.POLISHED_BLACKSTONE_STAIRS,
                    BlockID.BLACKSTONE_WALL,
                    BlockID.BLACKSTONE_WALL,
                    BlockID.POLISHED_BLACKSTONE_WALL,
                    BlockID.SANDSTONE,
                    BlockID.GRINDSTONE,
                    BlockID.SMOOTH_STONE,
                    BlockID.BREWING_STAND,
                    BlockID.CHAIN,
                    BlockID.LANTERN,
                    BlockID.SOUL_LANTERN,
                    BlockID.ANCIENT_DEBRIS,
                    BlockID.QUARTZ_ORE,
                    BlockID.NETHERRACK,
                    BlockID.BASALT,
                    BlockID.POLISHED_BASALT,
                    BlockID.STONE_BRICKS,
                    BlockID.WARPED_NYLIUM,
                    BlockID.CRIMSON_NYLIUM,
                    BlockID.END_STONE,
                    BlockID.ENDER_CHEST,
                    BlockID.QUARTZ_BLOCK,
                    BlockID.QUARTZ_STAIRS,
                    BlockID.QUARTZ_BRICKS,
                    BlockID.QUARTZ_STAIRS,
                    BlockID.NETHER_GOLD_ORE,
                    BlockID.FURNACE,
                    BlockID.BLAST_FURNACE,
                    BlockID.LIT_FURNACE,
                    BlockID.BLAST_FURNACE,
                    BlockID.BLACKSTONE,
                    BlockID.BLACK_CONCRETE,
                    BlockID.BLUE_CONCRETE,
                    BlockID.BROWN_CONCRETE,
                    BlockID.CYAN_CONCRETE,
                    BlockID.GRAY_CONCRETE,
                    BlockID.GREEN_CONCRETE,
                    BlockID.LIGHT_BLUE_CONCRETE,
                    BlockID.LIME_CONCRETE,
                    BlockID.MAGENTA_CONCRETE,
                    BlockID.ORANGE_CONCRETE,
                    BlockID.PINK_CONCRETE,
                    BlockID.PURPLE_CONCRETE,
                    BlockID.RED_CONCRETE,
                    BlockID.LIGHT_GRAY_CONCRETE,
                    BlockID.WHITE_CONCRETE,
                    BlockID.YELLOW_CONCRETE,
                    BlockID.DEEPSLATE_COPPER_ORE,
                    BlockID.DEEPSLATE_LAPIS_ORE,
                    BlockID.CHISELED_DEEPSLATE,
                    BlockID.COBBLED_DEEPSLATE,
                    BlockID.COBBLED_DEEPSLATE_DOUBLE_SLAB,
                    BlockID.COBBLED_DEEPSLATE_SLAB,
                    BlockID.COBBLED_DEEPSLATE_STAIRS,
                    BlockID.COBBLED_DEEPSLATE_WALL,
                    BlockID.CRACKED_DEEPSLATE_BRICKS,
                    BlockID.CRACKED_DEEPSLATE_TILES,
                    BlockID.DEEPSLATE,
                    BlockID.DEEPSLATE_BRICK_DOUBLE_SLAB,
                    BlockID.DEEPSLATE_BRICK_SLAB,
                    BlockID.DEEPSLATE_BRICK_STAIRS,
                    BlockID.DEEPSLATE_BRICK_WALL,
                    BlockID.DEEPSLATE_BRICKS,
                    BlockID.DEEPSLATE_TILE_DOUBLE_SLAB,
                    BlockID.DEEPSLATE_TILE_SLAB,
                    BlockID.DEEPSLATE_TILE_STAIRS,
                    BlockID.DEEPSLATE_TILE_WALL,
                    BlockID.DEEPSLATE_TILES,
                    BlockID.INFESTED_DEEPSLATE,
                    BlockID.POLISHED_DEEPSLATE,
                    BlockID.POLISHED_DEEPSLATE_DOUBLE_SLAB,
                    BlockID.POLISHED_DEEPSLATE_SLAB,
                    BlockID.POLISHED_DEEPSLATE_STAIRS,
                    BlockID.POLISHED_DEEPSLATE_WALL,
                    BlockID.CALCITE,
                    BlockID.AMETHYST_BLOCK,
                    BlockID.AMETHYST_CLUSTER,
                    BlockID.BUDDING_AMETHYST,
                    BlockID.RAW_COPPER_BLOCK,
                    BlockID.RAW_GOLD_BLOCK,
                    BlockID.RAW_IRON_BLOCK,
                    BlockID.COPPER_ORE,
                    BlockID.COPPER_BLOCK,
                    BlockID.CUT_COPPER,
                    BlockID.CUT_COPPER_SLAB,
                    BlockID.CUT_COPPER_STAIRS,
                    BlockID.DOUBLE_CUT_COPPER_SLAB,
                    BlockID.EXPOSED_COPPER,
                    BlockID.EXPOSED_CUT_COPPER,
                    BlockID.EXPOSED_CUT_COPPER_SLAB,
                    BlockID.EXPOSED_CUT_COPPER_STAIRS,
                    BlockID.EXPOSED_DOUBLE_CUT_COPPER_SLAB,
                    BlockID.OXIDIZED_COPPER,
                    BlockID.OXIDIZED_CUT_COPPER,
                    BlockID.OXIDIZED_CUT_COPPER_SLAB,
                    BlockID.OXIDIZED_CUT_COPPER_STAIRS,
                    BlockID.OXIDIZED_DOUBLE_CUT_COPPER_SLAB,
                    BlockID.WEATHERED_COPPER,
                    BlockID.WEATHERED_CUT_COPPER,
                    BlockID.WEATHERED_CUT_COPPER_SLAB,
                    BlockID.WEATHERED_CUT_COPPER_STAIRS,
                    BlockID.WEATHERED_DOUBLE_CUT_COPPER_SLAB,
                    BlockID.WAXED_COPPER,
                    BlockID.WAXED_CUT_COPPER,
                    BlockID.WAXED_CUT_COPPER_SLAB,
                    BlockID.WAXED_CUT_COPPER_STAIRS,
                    BlockID.WAXED_DOUBLE_CUT_COPPER_SLAB,
                    BlockID.WAXED_EXPOSED_COPPER,
                    BlockID.WAXED_EXPOSED_CUT_COPPER,
                    BlockID.WAXED_EXPOSED_CUT_COPPER_SLAB,
                    BlockID.WAXED_EXPOSED_CUT_COPPER_STAIRS,
                    BlockID.WAXED_EXPOSED_DOUBLE_CUT_COPPER_SLAB,
                    BlockID.WAXED_OXIDIZED_COPPER,
                    BlockID.WAXED_OXIDIZED_CUT_COPPER,
                    BlockID.WAXED_OXIDIZED_CUT_COPPER_SLAB,
                    BlockID.WAXED_OXIDIZED_CUT_COPPER_STAIRS,
                    BlockID.WAXED_OXIDIZED_DOUBLE_CUT_COPPER_SLAB,
                    BlockID.WAXED_WEATHERED_COPPER,
                    BlockID.WAXED_WEATHERED_CUT_COPPER,
                    BlockID.WAXED_WEATHERED_CUT_COPPER_SLAB,
                    BlockID.WAXED_WEATHERED_CUT_COPPER_STAIRS,
                    BlockID.WAXED_WEATHERED_DOUBLE_CUT_COPPER_SLAB,
                    BlockID.DRIPSTONE_BLOCK,
                    BlockID.POINTED_DRIPSTONE,
                    BlockID.LIGHTNING_ROD,
                    BlockID.BASALT,
                    BlockID.TUFF,
                    BlockID.DOUBLE_STONE_BLOCK_SLAB,
                    BlockID.DOUBLE_STONE_BLOCK_SLAB2,
                    BlockID.DOUBLE_STONE_BLOCK_SLAB3,
                    BlockID.DOUBLE_STONE_BLOCK_SLAB4,
                    BlockID.BLACKSTONE_DOUBLE_SLAB,
                    BlockID.POLISHED_BLACKSTONE_BRICK_DOUBLE_SLAB,
                    BlockID.POLISHED_BLACKSTONE_DOUBLE_SLAB,
                    BlockID.MOSSY_COBBLESTONE_STAIRS,
                    BlockID.STONECUTTER,
                    BlockID.STONECUTTER_BLOCK,
                    BlockID.RED_NETHER_BRICK,
                    BlockID.RED_NETHER_BRICK_STAIRS,
                    BlockID.NORMAL_STONE_STAIRS,
                    BlockID.SMOOTH_BASALT,
                    BlockID.STONE,
                    BlockID.COBBLESTONE,
                    BlockID.MOSSY_COBBLESTONE,
                    BlockID.DRIPSTONE_BLOCK,
                    BlockID.BRICK_BLOCK,
                    BlockID.STONE_STAIRS,
                    BlockID.STONE_BLOCK_SLAB2,
                    BlockID.STONE_BLOCK_SLAB3,
                    BlockID.STONE_BLOCK_SLAB4,
                    BlockID.COBBLESTONE_WALL,
                    BlockID.GOLD_BLOCK,
                    BlockID.IRON_BLOCK,
                    BlockID.CAULDRON,
                    BlockID.IRON_BARS,
                    BlockID.OBSIDIAN,
                    BlockID.COAL_ORE,
                    BlockID.DEEPSLATE_COAL_ORE,
                    BlockID.DEEPSLATE_DIAMOND_ORE,
                    BlockID.DEEPSLATE_EMERALD_ORE,
                    BlockID.DEEPSLATE_GOLD_ORE,
                    BlockID.DEEPSLATE_IRON_ORE,
                    BlockID.DEEPSLATE_REDSTONE_ORE,
                    BlockID.LIT_DEEPSLATE_REDSTONE_ORE,
                    BlockID.DIAMOND_ORE,
                    BlockID.EMERALD_ORE,
                    BlockID.GOLD_ORE,
                    BlockID.IRON_ORE,
                    BlockID.LAPIS_ORE,
                    BlockID.REDSTONE_ORE,
                    BlockID.LIT_REDSTONE_ORE,
                    BlockID.RAW_IRON_BLOCK,
                    BlockID.RAW_GOLD_BLOCK,
                    BlockID.RAW_COPPER_BLOCK,
                    BlockID.MUD_BRICK_DOUBLE_SLAB,
                    BlockID.MUD_BRICK_SLAB,
                    BlockID.MUD_BRICK_STAIRS,
                    BlockID.MUD_BRICK_WALL,
                    BlockID.MUD_BRICKS,
                    BlockID.HARDENED_CLAY,
                    BlockID.BLACK_TERRACOTTA,
                    BlockID.BLUE_TERRACOTTA,
                    BlockID.BROWN_TERRACOTTA,
                    BlockID.CYAN_TERRACOTTA,
                    BlockID.GRAY_TERRACOTTA,
                    BlockID.GREEN_TERRACOTTA,
                    BlockID.LIGHT_BLUE_TERRACOTTA,
                    BlockID.LIME_TERRACOTTA,
                    BlockID.MAGENTA_TERRACOTTA,
                    BlockID.ORANGE_TERRACOTTA,
                    BlockID.PINK_TERRACOTTA,
                    BlockID.PURPLE_TERRACOTTA,
                    BlockID.RED_TERRACOTTA,
                    BlockID.LIGHT_GRAY_TERRACOTTA,
                    BlockID.WHITE_TERRACOTTA,
                    BlockID.YELLOW_TERRACOTTA,
                    BlockID.POLISHED_DIORITE_STAIRS,
                    BlockID.ANDESITE_STAIRS,
                    BlockID.POLISHED_ANDESITE_STAIRS,
                    BlockID.GRANITE_STAIRS,
                    BlockID.POLISHED_GRANITE_STAIRS,
                    BlockID.POLISHED_BLACKSTONE,
                    BlockID.CHISELED_POLISHED_BLACKSTONE,
                    BlockID.POLISHED_BLACKSTONE_BRICK_STAIRS,
                    BlockID.BLACKSTONE_STAIRS,
                    BlockID.POLISHED_BLACKSTONE_BRICK_WALL,
                    BlockID.GILDED_BLACKSTONE,
                    BlockID.COAL_BLOCK
                )) {
                    pickaxeBlocks[name] = DigProperty()
                }
                toolBlocks[ItemTags.IS_PICKAXE] = pickaxeBlocks

                for (name in listOf<String>(
                    BlockID.CHEST,
                    BlockID.BOOKSHELF,
                    BlockID.MELON_BLOCK,
                    BlockID.WARPED_STEM,
                    BlockID.CRIMSON_STEM,
                    BlockID.WARPED_STEM,
                    BlockID.CRIMSON_STEM,
                    BlockID.CRAFTING_TABLE,
                    BlockID.CRIMSON_PLANKS,
                    BlockID.WARPED_PLANKS,
                    BlockID.WARPED_STAIRS,
                    BlockID.WARPED_TRAPDOOR,
                    BlockID.CRIMSON_STAIRS,
                    BlockID.CRIMSON_TRAPDOOR,
                    BlockID.CRIMSON_DOOR,
                    BlockID.CRIMSON_DOUBLE_SLAB,
                    BlockID.WARPED_DOOR,
                    BlockID.WARPED_DOUBLE_SLAB,
                    BlockID.CRAFTING_TABLE,
                    BlockID.COMPOSTER,
                    BlockID.CARTOGRAPHY_TABLE,
                    BlockID.LECTERN,
                    BlockID.STRIPPED_CRIMSON_STEM,
                    BlockID.STRIPPED_WARPED_STEM,
                    BlockID.TRAPDOOR,
                    BlockID.SPRUCE_TRAPDOOR,
                    BlockID.BIRCH_TRAPDOOR,
                    BlockID.JUNGLE_TRAPDOOR,
                    BlockID.ACACIA_TRAPDOOR,
                    BlockID.DARK_OAK_TRAPDOOR,
                    BlockID.WOODEN_DOOR,
                    BlockID.SPRUCE_DOOR,
                    BlockID.BIRCH_DOOR,
                    BlockID.JUNGLE_DOOR,
                    BlockID.ACACIA_DOOR,
                    BlockID.DARK_OAK_DOOR,
                    BlockID.ACACIA_FENCE,
                    BlockID.DARK_OAK_FENCE,
                    BlockID.BAMBOO_FENCE,
                    BlockID.MANGROVE_FENCE,
                    BlockID.NETHER_BRICK_FENCE,
                    BlockID.OAK_FENCE,
                    BlockID.CRIMSON_FENCE,
                    BlockID.JUNGLE_FENCE,
                    BlockID.CHERRY_FENCE,
                    BlockID.BIRCH_FENCE,
                    BlockID.WARPED_FENCE,
                    BlockID.SPRUCE_FENCE,
                    BlockID.FENCE_GATE,
                    BlockID.SPRUCE_FENCE_GATE,
                    BlockID.BIRCH_FENCE_GATE,
                    BlockID.JUNGLE_FENCE_GATE,
                    BlockID.ACACIA_FENCE_GATE,
                    BlockID.DARK_OAK_FENCE_GATE,
                    BlockID.MANGROVE_LOG,
                    BlockID.OAK_LOG,
                    BlockID.JUNGLE_LOG,
                    BlockID.SPRUCE_LOG,
                    BlockID.DARK_OAK_LOG,
                    BlockID.CHERRY_LOG,
                    BlockID.ACACIA_LOG,
                    BlockID.BIRCH_LOG,
                    BlockID.ACACIA_PLANKS,
                    BlockID.BAMBOO_PLANKS,
                    BlockID.CRIMSON_PLANKS,
                    BlockID.BIRCH_PLANKS,
                    BlockID.DARK_OAK_PLANKS,
                    BlockID.CHERRY_PLANKS,
                    BlockID.WARPED_PLANKS,
                    BlockID.OAK_PLANKS,
                    BlockID.SPRUCE_PLANKS,
                    BlockID.MANGROVE_PLANKS,
                    BlockID.JUNGLE_PLANKS,
                    BlockID.ACACIA_DOUBLE_SLAB,
                    BlockID.ACACIA_SLAB,
                    BlockID.BAMBOO_DOUBLE_SLAB,
                    BlockID.BAMBOO_MOSAIC_SLAB,
                    BlockID.BIRCH_DOUBLE_SLAB,
                    BlockID.BIRCH_SLAB,
                    BlockID.OAK_STAIRS,
                    BlockID.SPRUCE_STAIRS,
                    BlockID.BIRCH_STAIRS,
                    BlockID.JUNGLE_STAIRS,
                    BlockID.ACACIA_STAIRS,
                    BlockID.DARK_OAK_STAIRS,
                    BlockID.WALL_SIGN,
                    BlockID.SPRUCE_WALL_SIGN,
                    BlockID.BIRCH_WALL_SIGN,
                    BlockID.JUNGLE_WALL_SIGN,
                    BlockID.ACACIA_WALL_SIGN,
                    BlockID.DARKOAK_WALL_SIGN,
                    BlockID.WOODEN_PRESSURE_PLATE,
                    BlockID.SPRUCE_PRESSURE_PLATE,
                    BlockID.BIRCH_PRESSURE_PLATE,
                    BlockID.JUNGLE_PRESSURE_PLATE,
                    BlockID.ACACIA_PRESSURE_PLATE,
                    BlockID.DARK_OAK_PRESSURE_PLATE,
                    BlockID.SMITHING_TABLE,
                    BlockID.FLETCHING_TABLE,
                    BlockID.BARREL,
                    BlockID.BEEHIVE,
                    BlockID.BEE_NEST,
                    BlockID.LADDER,
                    BlockID.PUMPKIN,
                    BlockID.CARVED_PUMPKIN,
                    BlockID.LIT_PUMPKIN,
                    BlockID.MANGROVE_DOOR,
                    BlockID.MANGROVE_DOUBLE_SLAB,
                    BlockID.MANGROVE_FENCE,
                    BlockID.MANGROVE_FENCE_GATE,
                    BlockID.MANGROVE_LOG,
                    BlockID.MANGROVE_PLANKS,
                    BlockID.MANGROVE_PRESSURE_PLATE,
                    BlockID.MANGROVE_SLAB,
                    BlockID.MANGROVE_STAIRS,
                    BlockID.MANGROVE_WALL_SIGN,
                    BlockID.MANGROVE_WOOD,
                    BlockID.WOODEN_BUTTON,
                    BlockID.SPRUCE_BUTTON,
                    BlockID.BIRCH_BUTTON,
                    BlockID.JUNGLE_BUTTON,
                    BlockID.ACACIA_BUTTON,
                    BlockID.DARK_OAK_BUTTON,
                    BlockID.MANGROVE_BUTTON,
                    BlockID.STRIPPED_OAK_LOG,
                    BlockID.STRIPPED_SPRUCE_LOG,
                    BlockID.STRIPPED_BIRCH_LOG,
                    BlockID.STRIPPED_JUNGLE_LOG,
                    BlockID.STRIPPED_ACACIA_LOG,
                    BlockID.STRIPPED_DARK_OAK_LOG,
                    BlockID.STRIPPED_MANGROVE_WOOD,
                    BlockID.STRIPPED_OAK_LOG,
                    BlockID.STRIPPED_SPRUCE_LOG,
                    BlockID.STRIPPED_BIRCH_LOG,
                    BlockID.STRIPPED_JUNGLE_LOG,
                    BlockID.STRIPPED_ACACIA_LOG,
                    BlockID.STRIPPED_DARK_OAK_LOG,
                    BlockID.STRIPPED_MANGROVE_LOG,
                    BlockID.STANDING_SIGN,
                    BlockID.SPRUCE_STANDING_SIGN,
                    BlockID.BIRCH_STANDING_SIGN,
                    BlockID.JUNGLE_STANDING_SIGN,
                    BlockID.ACACIA_STANDING_SIGN,
                    BlockID.DARKOAK_STANDING_SIGN,
                    BlockID.MANGROVE_STANDING_SIGN,
                    BlockID.MANGROVE_TRAPDOOR,
                    BlockID.WARPED_STANDING_SIGN,
                    BlockID.WARPED_WALL_SIGN,
                    BlockID.CRIMSON_STANDING_SIGN,
                    BlockID.CRIMSON_WALL_SIGN,
                    BlockID.MANGROVE_ROOTS
                )) {
                    axeBlocks[name] = DigProperty()
                }
                toolBlocks[ItemTags.IS_AXE] = axeBlocks

                for (name in listOf<String>(
                    BlockID.SOUL_SAND,
                    BlockID.SOUL_SOIL,
                    BlockID.DIRT_WITH_ROOTS,
                    BlockID.MYCELIUM,
                    BlockID.PODZOL,
                    BlockID.DIRT,
                    BlockID.FARMLAND,
                    BlockID.SAND,
                    BlockID.GRAVEL,
                    BlockID.GRASS_BLOCK,
                    BlockID.GRASS_PATH,
                    BlockID.SNOW,
                    BlockID.MUD,
                    BlockID.PACKED_MUD,
                    BlockID.CLAY
                )) {
                    shovelBlocks[name] = DigProperty()
                }
                toolBlocks[ItemTags.IS_SHOVEL] = shovelBlocks

                for (name in listOf<String>(
                    BlockID.NETHER_WART_BLOCK,
                    BlockID.HAY_BLOCK,
                    BlockID.TARGET,
                    BlockID.SHROOMLIGHT,
                    BlockID.ACACIA_LEAVES,
                    BlockID.AZALEA_LEAVES,
                    BlockID.BIRCH_LEAVES,
                    BlockID.AZALEA_LEAVES_FLOWERED,
                    BlockID.CHERRY_LEAVES,
                    BlockID.DARK_OAK_LEAVES,
                    BlockID.JUNGLE_LEAVES,
                    BlockID.MANGROVE_LEAVES,
                    BlockID.OAK_LEAVES,
                    BlockID.SPRUCE_LEAVES,
                    BlockID.AZALEA_LEAVES_FLOWERED,
                    BlockID.AZALEA_LEAVES,
                    BlockID.WARPED_WART_BLOCK
                )) {
                    hoeBlocks[name] = DigProperty()
                }
                toolBlocks[ItemTags.IS_HOE] = hoeBlocks

                for (name in listOf<String>(BlockID.WEB, BlockID.BAMBOO)) {
                    swordBlocks[name] = DigProperty()
                }
                toolBlocks[ItemTags.IS_SWORD] = swordBlocks
            }
        }
    }

    class ArmorBuilder(private val item: ItemCustomArmor) : SimpleBuilder(item) {
        init {
            nbt.getCompound("components")
                .getCompound("item_properties")
                .putInt("enchantable_value", item.enchantAbility)
                .putBoolean("can_destroy_in_creative", true)
        }

        fun addRepairItemName(repairItemName: String, molang: String): ArmorBuilder {
            super.addRepairs(listOf(repairItemName), molang)
            return this
        }

        fun addRepairItemName(repairItemName: String, repairAmount: Int): ArmorBuilder {
            super.addRepairs(listOf(repairItemName), repairAmount.toString())
            return this
        }

        fun addRepairItems(repairItems: List<Item>, molang: String): ArmorBuilder {
            super.addRepairs(repairItems.stream().map { obj: Item -> obj.id }.toList(), molang)
            return this
        }

        fun addRepairItems(repairItems: List<Item>, repairAmount: Int): ArmorBuilder {
            super.addRepairs(repairItems.stream().map { obj: Item -> obj.id }.toList(), repairAmount.toString())
            return this
        }

        override fun build(): CustomItemDefinition {
            nbt.getCompound("components")
                .putCompound(
                    "minecraft:armor", CompoundTag()
                        .putInt("protection", item.armorPoints)
                )
                .putCompound(
                    "minecraft:durability", CompoundTag()
                        .putInt("max_durability", item.maxDurability)
                )
            if (item.isHelmet) {
                nbt.getCompound("components").getCompound("item_properties")
                    .putString("enchantable_slot", "armor_head")
                nbt.getCompound("components")
                    .putCompound(
                        "minecraft:wearable", CompoundTag()
                            .putString("slot", "slot.armor.head")
                    )
            } else if (item.isChestplate) {
                nbt.getCompound("components").getCompound("item_properties")
                    .putString("enchantable_slot", "armor_torso")
                nbt.getCompound("components")
                    .putCompound(
                        "minecraft:wearable", CompoundTag()
                            .putString("slot", "slot.armor.chest")
                    )
            } else if (item.isLeggings) {
                nbt.getCompound("components").getCompound("item_properties")
                    .putString("enchantable_slot", "armor_legs")
                nbt.getCompound("components")
                    .putCompound(
                        "minecraft:wearable", CompoundTag()
                            .putString("slot", "slot.armor.legs")
                    )
            } else if (item.isBoots) {
                nbt.getCompound("components").getCompound("item_properties")
                    .putString("enchantable_slot", "armor_feet")
                nbt.getCompound("components")
                    .putCompound(
                        "minecraft:wearable", CompoundTag()
                            .putString("slot", "slot.armor.feet")
                    )
            }
            return calculateID()
        }
    }

    class EdibleBuilder(item: ItemCustomFood) : SimpleBuilder(item) {
        init {
            if (nbt.getCompound("components").contains("minecraft:food")) {
                nbt.getCompound("components").getCompound("minecraft:food")
                    .putBoolean("can_always_eat", !item.isRequiresHunger)
            } else {
                nbt.getCompound("components")
                    .putCompound("minecraft:food", CompoundTag().putBoolean("can_always_eat", !item.isRequiresHunger))
            }

            val eatingtick = item.eatingTicks
            nbt.getCompound("components")
                .getCompound("item_properties")
                .putInt("use_duration", eatingtick)
                .putInt("use_animation", if (item.isDrink) 2 else 1)
                .putBoolean("can_destroy_in_creative", true)
        }
    }

    companion object : Loggable {
        private val INTERNAL_ALLOCATION_ID_MAP: HashMap<String, Int> = HashMap()
        private val nextRuntimeId = AtomicInteger(10000)

        /**
         * 自定义物品的定义构造器
         *
         *
         * Definition builder for custom simple item
         *
         * @param item the item
         * @return the custom item definition . simple builder
         */
        fun customBuilder(item: CustomItem): SimpleBuilder {
            return SimpleBuilder(item)
        }

        /**
         * 简单物品的定义构造器
         *
         *
         * Definition builder for custom simple item
         *
         * @param item the item
         */
        fun simpleBuilder(item: ItemCustom): SimpleBuilder {
            return SimpleBuilder(item)
        }

        /**
         * 自定义工具的定义构造器
         *
         *
         * Definition builder for custom tools
         *
         * @param item the item
         */
        fun toolBuilder(item: ItemCustomTool): ToolBuilder {
            return ToolBuilder(item)
        }

        /**
         * 自定义盔甲的定义构造器
         *
         *
         * Definition builder for custom armor
         *
         * @param item the item
         */
        fun armorBuilder(item: ItemCustomArmor): ArmorBuilder {
            return ArmorBuilder(item)
        }

        /**
         * 自定义食物(药水)的定义构造器
         *
         *
         * Definition builder for custom food or potion
         *
         * @param item the item
         */
        fun edibleBuilder(item: ItemCustomFood): EdibleBuilder {
            return EdibleBuilder(item)
        }

        fun getRuntimeId(identifier: String): Int {
            return INTERNAL_ALLOCATION_ID_MAP.get(identifier)
                ?: throw RuntimeException("Unknown Identifier: $identifier")
        }
    }
}
