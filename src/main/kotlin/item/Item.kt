package org.chorus_oss.chorus.item

import com.google.gson.annotations.SerializedName
import io.netty.util.internal.EmptyArrays
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockAir
import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.block.BlockState
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.item.Item.ItemJsonComponents.ItemLock
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.NBTIO
import org.chorus_oss.chorus.nbt.tag.*
import org.chorus_oss.chorus.registry.Registries
import org.chorus_oss.chorus.tags.ItemTags
import org.chorus_oss.chorus.utils.*
import org.jetbrains.annotations.ApiStatus
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.ByteOrder
import java.util.*


abstract class Item : Cloneable, ItemID, Loggable {
    var id: String
        protected set
    var identifier: Identifier
        protected set
    protected var name: String? = null
    var meta: Int = 0

    @JvmField
    var count: Int
    var netId: Int? = null
        set(value) {
            if (value != null) {
                require(value >= 0) { "stack network id cannot be negative" }
                field = value
            } else field = null
        }

    var blockState: BlockState? = null

    protected var hasMeta: Boolean = true
    var compoundTag: ByteArray = EmptyArrays.EMPTY_BYTES
        private set
    private var cachedNBT: CompoundTag? = null
    private fun idConvertToName(): String {
        if (this.name != null) {
            return name!!
        } else {
            val path = id.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            val result = StringBuilder()
            val parts = path.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (part in parts) {
                if (!part.isEmpty()) {
                    result.append(part[0].uppercaseChar()).append(part.substring(1)).append(" ")
                }
            }
            this.name = result.toString().trim { it <= ' ' }.intern()
            return name!!
        }
    }

    @JvmOverloads
    constructor(
        id: String,
        meta: Int = 0,
        count: Int = 1,
        name: String? = null,
        autoAssignStackNetworkId: Boolean = true
    ) {
        this.id = id.intern()
        this.identifier = Identifier(id)
        this.count = count
        if (name != null) {
            this.name = name.intern()
        }
        this.damage = meta
        if (autoAssignStackNetworkId) {
            this.autoAssignStackNetworkId()
        }
    }

    protected constructor(
        blockState: BlockState,
        meta: Int,
        count: Int,
        name: String?,
        autoAssignStackNetworkId: Boolean
    ) {
        this.id = blockState.identifier.intern()
        this.identifier = Identifier(id)
        this.count = count
        if (name != null) {
            this.name = name.intern()
        }
        this.blockState = blockState
        this.damage = meta
        if (autoAssignStackNetworkId) {
            this.autoAssignStackNetworkId()
        }
    }

    @ApiStatus.Internal
    open fun internalAdjust() {
    }

    fun hasMeta(): Boolean {
        return hasMeta
    }

    open fun canBeActivated(): Boolean {
        return false
    }

    fun readItemJsonComponents(components: ItemJsonComponents) {
        if (components.canPlaceOn != null) this.setCanPlaceOn(
            Arrays.stream(
                components.canPlaceOn!!.blocks
            ).map { str: String -> Block.get(if (str.startsWith("minecraft:")) str else "minecraft:$str") }
                .toList().toTypedArray())
        if (components.canDestroy != null) this.setCanDestroy(
            Arrays.stream(
                components.canDestroy!!.blocks
            ).map { str: String -> Block.get(if (str.startsWith("minecraft:")) str else "minecraft:$str") }
                .toList().toTypedArray())
        if (components.itemLock != null) itemLockMode = when (components.itemLock!!.mode) {
            ItemLock.LOCK_IN_SLOT -> ItemLockMode.LOCK_IN_SLOT
            ItemLock.LOCK_IN_INVENTORY -> ItemLockMode.LOCK_IN_INVENTORY
            else -> ItemLockMode.NONE
        }
        if (components.keepOnDeath != null) this.setKeepOnDeath(true)
    }

    fun hasCustomBlockData(): Boolean {
        if (!this.hasCompoundTag()) {
            return false
        }

        val tag = this.namedTag
        return tag!!.contains("BlockEntityTag") && tag["BlockEntityTag"] is CompoundTag
    }

    fun clearCustomBlockData(): Item {
        if (!this.hasCompoundTag()) {
            return this
        }
        val tag = this.namedTag

        if (tag!!.contains("BlockEntityTag") && tag["BlockEntityTag"] is CompoundTag) {
            tag.remove("BlockEntityTag")
            this.setNamedTag(tag)
        }

        return this
    }

    open fun setCustomBlockData(compoundTag: CompoundTag): Item {
        val tags = compoundTag.copy()
        val tag = if (!this.hasCompoundTag()) {
            CompoundTag()
        } else {
            namedTag
        }

        tag!!.putCompound("BlockEntityTag", tags)
        this.setNamedTag(tag)

        return this
    }

    val customBlockData: CompoundTag?
        get() {
            if (!this.hasCompoundTag()) {
                return null
            }

            val tag = this.namedTag

            if (tag!!.contains("BlockEntityTag")) {
                val bet = tag["BlockEntityTag"]
                if (bet is CompoundTag) {
                    return bet
                }
            }

            return null
        }

    /**
     * 该物品是否可以应用附魔效果
     */
    open fun applyEnchantments(): Boolean {
        return true
    }

    fun hasEnchantments(): Boolean {
        if (!this.hasCompoundTag()) {
            return false
        }

        val tag = this.namedTag

        if (tag!!.contains("ench")) {
            val enchTag = tag["ench"]
            return enchTag is ListTag<*>
        } else if (tag.contains("custom_ench")) {
            val enchTag = tag["custom_ench"]
            return enchTag is ListTag<*>
        }

        return false
    }

    /**
     * 通过附魔id来查找对应附魔的等级
     *
     *
     * Find the enchantment level by the enchantment id.
     *
     * @param id The enchantment ID from [Enchantment] constants.
     * @return `0` if the item don't have that enchantment or the current level of the given enchantment.
     */
    fun getEnchantmentLevel(id: Int): Int {
        if (!this.hasEnchantments()) {
            return 0
        }

        for (entry in namedTag!!.getList("ench", CompoundTag::class.java).all) {
            if (entry.getShort("id").toInt() == id) {
                return entry.getShort("lvl").toInt()
            }
        }

        return 0
    }

    /**
     * 通过附魔id来查找对应附魔的等级
     *
     *
     * Find the enchantment level by the enchantment id.
     *
     * @param id 要查询的附魔标识符
     * @return `0` if the item don't have that enchantment or the current level of the given enchantment.
     */
    fun getCustomEnchantmentLevel(id: String): Int {
        if (!this.hasEnchantments()) {
            return 0
        }
        for (entry in namedTag!!.getList("custom_ench", CompoundTag::class.java).all) {
            if (entry.getString("id") == id) {
                return entry.getShort("lvl").toInt()
            }
        }
        return 0
    }

    /**
     * @param id 要查询的附魔标识符
     */
    fun getCustomEnchantment(id: String): Enchantment? {
        if (!this.hasEnchantments()) {
            return null
        }

        for (entry in namedTag!!.getList<CompoundTag>("custom_ench", CompoundTag::class.java).all) {
            if (entry.getString("id") == id) {
                val e = Enchantment.getEnchantment(entry.getString("id"))
                if (e != null) {
                    e.setLevel(entry.getShort("lvl").toInt(), false)
                    return e
                }
            }
        }

        return null
    }

    /**
     * 检测该物品是否有该附魔
     *
     *
     * Detect if the item has the enchantment
     *
     * @param id 要查询的附魔标识符
     */
    fun hasCustomEnchantment(id: String): Boolean {
        return this.getCustomEnchantmentLevel(id) > 0
    }

    /**
     * @param id 要查询的附魔标识符
     */
    fun getCustomEnchantmentLevel(id: Identifier): Int {
        return getCustomEnchantmentLevel(id.toString())
    }

    /**
     * @param id 要查询的附魔标识符
     */
    fun hasCustomEnchantment(id: Identifier): Boolean {
        return hasCustomEnchantment(id.toString())
    }

    /**
     * @param id 要查询的附魔标识符
     */
    fun getCustomEnchantment(id: Identifier): Enchantment? {
        return getCustomEnchantment(id.toString())
    }

    /**
     * 从给定的附魔id查找该物品是否存在对应的附魔效果，如果查找不到返回null
     *
     *
     * Get the id of the enchantment
     */
    fun getEnchantment(id: Int): Enchantment? {
        return getEnchantment((id and 0xffff).toShort())
    }

    fun getEnchantment(id: Short): Enchantment? {
        if (!this.hasEnchantments()) {
            return null
        }

        for (entry in namedTag!!.getList("ench", CompoundTag::class.java).all) {
            if (entry.getShort("id") == id) {
                val e = Enchantment.getEnchantment(entry.getShort("id").toInt())
                if (e != null) {
                    e.setLevel(entry.getShort("lvl").toInt(), false)
                    return e
                }
            }
        }

        return null
    }

    fun addEnchantment(vararg enchantments: Enchantment) {
        val tag = if (!this.hasCompoundTag()) {
            CompoundTag()
        } else {
            namedTag
        }

        val ench: ListTag<CompoundTag>
        if (!tag!!.contains("ench")) {
            ench = ListTag()
            tag.putList("ench", ench)
        } else {
            ench = tag.getList("ench", CompoundTag::class.java)
        }
        val customEnch: ListTag<CompoundTag>
        if (!tag.contains("custom_ench")) {
            customEnch = ListTag()
            tag.putList("custom_ench", customEnch)
        } else {
            customEnch = tag.getList("custom_ench", CompoundTag::class.java)
        }

        for (enchantment in enchantments) {
            var found = false
            if (enchantment.identifier == null) {
                for (k in 0..<ench.size()) {
                    val entry = ench[k]
                    if (entry.getShort("id").toInt() == enchantment.id) {
                        ench.add(
                            k, CompoundTag()
                                .putShort("id", enchantment.id)
                                .putShort("lvl", enchantment.level)
                        )
                        found = true
                        break
                    }
                }
                if (!found) {
                    ench.add(
                        CompoundTag()
                            .putShort("id", enchantment.id)
                            .putShort("lvl", enchantment.level)
                    )
                }
            } else {
                for (k in 0..<customEnch.size()) {
                    val entry = customEnch[k]
                    if (entry.getString("id") == enchantment.identifier.toString()) {
                        customEnch.add(
                            k, CompoundTag()
                                .putString("id", enchantment.identifier.toString())
                                .putShort("lvl", enchantment.level)
                        )
                        found = true
                        break
                    }
                }
                if (!found) {
                    customEnch.add(
                        CompoundTag()
                            .putString("id", enchantment.identifier.toString())
                            .putShort("lvl", enchantment.level)
                    )
                }
            }
        }
        if (customEnch.size() != 0) {
            val customName = setCustomEnchantDisplay(customEnch)
            if (tag.contains("display") && tag["display"] is CompoundTag) {
                tag.getCompound("display").putString("Name", customName)
            } else {
                tag.putCompound(
                    "display", CompoundTag()
                        .putString("Name", customName)
                )
            }
        }
        this.setNamedTag(tag)
    }

    private fun setCustomEnchantDisplay(customEnch: ListTag<CompoundTag>): String {
        val joiner = StringJoiner("\n", TextFormat.RESET.toString() + TextFormat.AQUA + idConvertToName() + "\n", "")
        for (ench in customEnch.all) {
            val enchantment: Enchantment = Enchantment.getEnchantment(
                ench.getString("id")
            ).setLevel(ench.getShort("lvl").toInt())
            joiner.add(enchantment.lore)
        }
        return joiner.toString()
    }

    val enchantments: Array<Enchantment>
        /**
         * 获取该物品所带有的全部附魔
         *
         *
         * Get all the enchantments that the item comes with
         *
         * @return 如果没有附魔效果返回Enchantment.EMPTY_ARRAY<br></br>If there is no enchanting effect return Enchantment.EMPTY_ARRAY
         */
        get() {
            if (!this.hasEnchantments()) {
                return Enchantment.EMPTY_ARRAY
            }
            val enchantments: MutableList<Enchantment> = ArrayList()

            val ench = namedTag!!.getList("ench", CompoundTag::class.java)
            for (entry in ench.all) {
                val e = Enchantment.getEnchantment(entry.getShort("id").toInt())
                if (e != null) {
                    e.setLevel(entry.getShort("lvl").toInt(), false)
                    enchantments.add(e)
                }
            }
            //custom ench
            val customEnch = namedTag!!.getList(
                "custom_ench",
                CompoundTag::class.java
            )
            for (entry in customEnch.all) {
                val e = Enchantment.getEnchantment(entry.getString("id"))
                if (e != null) {
                    e.setLevel(entry.getShort("lvl").toInt(), false)
                    enchantments.add(e)
                }
            }
            return enchantments.toTypedArray()
        }

    /**
     * 检测该物品是否有该附魔
     *
     *
     * Detect if the item has the enchantment
     *
     * @param id The enchantment ID from [Enchantment] constants.
     */
    fun hasEnchantment(id: Int): Boolean {
        return this.getEnchantmentLevel(id) > 0
    }

    val repairCost: Int
        get() {
            if (this.hasCompoundTag()) {
                val tag = this.namedTag
                if (tag!!.contains("RepairCost")) {
                    val repairCost = tag["RepairCost"]
                    if (repairCost is IntTag) {
                        return repairCost.data
                    }
                }
            }
            return 0
        }

    open fun setRepairCost(cost: Int): Item {
        if (cost <= 0 && this.hasCompoundTag()) {
            return this.setNamedTag(namedTag!!.remove("RepairCost"))
        }
        val tag = if (!this.hasCompoundTag()) {
            CompoundTag()
        } else {
            namedTag
        }
        return this.setNamedTag(tag!!.putInt("RepairCost", cost))
    }

    fun hasCustomName(): Boolean {
        if (!this.hasCompoundTag()) {
            return false
        }

        val tag = this.namedTag
        if (tag!!.contains("display")) {
            val tag1 = tag["display"]
            return tag1 is CompoundTag && tag1.contains("Name") && tag1["Name"] is StringTag
        }

        return false
    }

    val customName: String
        get() {
            if (!this.hasCompoundTag()) {
                return ""
            }

            val tag = this.namedTag
            if (tag!!.contains("display")) {
                val tag1 = tag["display"]
                if (tag1 is CompoundTag && tag1.contains("Name") && tag1["Name"] is StringTag) {
                    return tag1.getString("Name")
                }
            }

            return ""
        }

    /**
     * 设置物品的自定义名字
     *
     *
     * Set custom names for items
     *
     * @param name
     * @return
     */
    open fun setCustomName(name: String?): Item {
        if (name == null || name == "") {
            this.clearCustomName()
        }
        val tag = if (!this.hasCompoundTag()) {
            CompoundTag()
        } else {
            namedTag
        }
        if (tag!!.contains("display") && tag["display"] is CompoundTag) {
            tag.getCompound("display").putString("Name", name!!)
        } else {
            tag.putCompound(
                "display", CompoundTag()
                    .putString("Name", name!!)
            )
        }
        this.setNamedTag(tag)
        return this
    }

    /**
     * 清除物品的自定义名称
     *
     *
     * Clear custom name for item
     *
     * @return
     */
    fun clearCustomName(): Item {
        if (!this.hasCompoundTag()) {
            return this
        }

        val tag = this.namedTag

        if (tag!!.contains("display") && tag["display"] is CompoundTag) {
            tag.getCompound("display").remove("Name")
            if (tag.getCompound("display").isEmpty) {
                tag.remove("display")
            }

            this.setNamedTag(tag)
        }

        return this
    }

    val lore: Array<String>
        /**
         * 定义物品的Lore信息
         *
         *
         * Get the Lore information of the item
         *
         * @return
         */
        get() {
            val tag = this.getNamedTagEntry("display")
            val lines = ArrayList<String>()

            if (tag is CompoundTag) {
                val lore = tag.getList("Lore", StringTag::class.java)

                if (lore.size() > 0) {
                    for (stringTag in lore.all) {
                        lines.add(stringTag.data)
                    }
                }
            }

            return lines.toArray(EmptyArrays.EMPTY_STRINGS)
        }

    /**
     * 设置物品的Lore信息
     *
     *
     * Set the Lore information of the item
     *
     * @param lines the lines
     * @return the lore
     */
    open fun setLore(vararg lines: String): Item {
        val tag = if (!this.hasCompoundTag()) {
            CompoundTag()
        } else {
            namedTag
        }
        val lore = ListTag<StringTag>()

        for (line in lines) {
            lore.add(StringTag(line))
        }

        if (!tag!!.contains("display")) {
            tag.putCompound("display", CompoundTag().putList("Lore", lore))
        } else {
            tag.getCompound("display").putList("Lore", lore)
        }

        this.setNamedTag(tag)
        return this
    }

    fun getNamedTagEntry(name: String): Tag<*>? {
        val tag = this.namedTag
        if (tag != null) {
            return if (tag.contains(name)) tag[name] else null
        }

        return null
    }

    open fun setNamedTag(tag: CompoundTag?): Item {
        this.cachedNBT = tag
        this.compoundTag = writeCompoundTag(tag)
        return this
    }

    val namedTag: CompoundTag?
        get() {
            if (!this.hasCompoundTag()) {
                return null
            }

            if (this.cachedNBT == null) {
                this.cachedNBT =
                    parseCompoundTag(this.compoundTag)
            }
            return this.cachedNBT
        }

    open fun setCompoundTag(tag: CompoundTag?): Item {
        return setNamedTag(tag)
    }

    open fun setCompoundTag(tags: ByteArray): Item {
        this.compoundTag = tags
        this.cachedNBT = parseCompoundTag(tags)
        return this
    }

    fun hasCompoundTag(): Boolean {
        if (compoundTag.isNotEmpty()) {
            if (cachedNBT == null) cachedNBT = parseCompoundTag(compoundTag)
            return !cachedNBT!!.isEmpty
        } else return false
    }

    fun getOrCreateNamedTag(): CompoundTag {
        if (!hasCompoundTag()) {
            setNamedTag(CompoundTag())
            return cachedNBT!!
        }
        return namedTag!!
    }

    fun clearNamedTag(): Item {
        this.compoundTag = EmptyArrays.EMPTY_BYTES
        this.cachedNBT = null
        return this
    }

    fun writeCompoundTag(tag: CompoundTag?): ByteArray {
        if (tag == null) {
            return EmptyArrays.EMPTY_BYTES
        }
        try {
            return NBTIO.write(tag, ByteOrder.LITTLE_ENDIAN)
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }

    @get:ApiStatus.Internal
    val isUsingNetId: Boolean
        /**
         * Gets whether this item is using a net id.
         *
         * @return whether this item is using a net id
         */
        get() = netId != null

    @ApiStatus.Internal
    open fun getNetId(): Int {
        return netId!!
    }

    @ApiStatus.Internal
    fun autoAssignStackNetworkId(): Item {
        this.netId = STACK_NETWORK_ID_COUNTER++
        return this
    }

    open fun getCount(): Int {
        return count
    }

    open fun setCount(count: Int) {
        this.count = count
    }

    open val isNothing: Boolean
        get() = this.id == BlockID.AIR || this.count <= 0

    fun `is`(itemTag: String?): Boolean {
        val contains = ItemTags.getTagSet(this.id).contains(itemTag)
        if (contains) return true
        return ItemTags.getTagSet(this.blockId).contains(itemTag)
    }

    fun getItemName(): String {
        return if (this.hasCustomName()) customName else idConvertToName()
    }

    val displayName: String
        get() = if (this.hasCustomName()) customName else idConvertToName()

    fun canBePlaced(): Boolean {
        return (this.blockState != null)
    }

    open fun getSafeBlockState(): BlockState {
        return this.blockState ?: BlockAir.properties.defaultState
    }

    private val airRuntimeId: Int
        get() = Registries.ITEM_RUNTIMEID.getInt(BlockID.AIR)

    private val unknownRuntimeId: Int
        get() = Registries.ITEM_RUNTIMEID.getInt(BlockID.UNKNOWN)

    val runtimeId: Int
        get() {
            if (this.isNothing) return airRuntimeId
            var i = Registries.ITEM_RUNTIMEID.getInt(this.id)
            if (i == Int.MAX_VALUE) {
                i = Registries.ITEM_RUNTIMEID.getInt(this.blockId)
            }
            if (i == Int.MAX_VALUE) {
                log.warn(
                    "Can't find runtimeId for item {}, will return unknown itemblock!",
                    id
                )
                return unknownRuntimeId // Can't find runtimeId
            }
            return i
        }

    val fullId: Int
        get() = ((runtimeId.toShort()).toInt() shl 16) or ((meta and 0x7fff) shl 1)

    fun isBlock(): Boolean {
        return this.blockState != null
    }

    val blockId: String
        get() {
            return blockState?.identifier ?: UNKNOWN_STR
        }

    open var damage: Int
        get() = meta
        set(damage) {
            this.meta = damage and 0xffff
            this.hasMeta = true
            internalAdjust()
        }

    /**
     * 创建一个通配配方物品,即该物品可以不限制数据值应用到配方中
     *
     *
     * Create a wildcard recipe item,the item can be applied to a recipe without restriction on data(damage/meta) values
     */
    fun disableMeta() {
        this.hasMeta = false
    }

    open val maxStackSize: Int = 64

    val fuelTime: Int?
        /**
         * 获取一个可燃烧物品的燃烧时间
         *
         *
         * Get the burn time of a burnable item
         */
        get() {
            if (!Registries.FUEL.isFuel(this)) {
                return null
            }
            if (this.id != ItemID.Companion.BUCKET || this.meta == 10) {
                return Registries.FUEL.getFuelDuration(this)
            }
            return null
        }

    open fun useOn(entity: Entity?): Boolean {
        return false
    }

    open fun useOn(block: Block): Boolean {
        return false
    }

    open val isTool: Boolean
        /**
         * 定义物品是否为工具
         *
         *
         * Define if this item is a tool
         */
        get() = false

    open val maxDurability: Int
        /**
         * 定义物品最大耐久值
         *
         *
         * Define the maximum durability value of the item
         */
        get() = -1

    open val tier: Int
        /**
         * 定义物品的挖掘等级
         *
         *
         * Define the item Tier level
         */
        get() = 0

    open val isPickaxe: Boolean
        /**
         * 定义物品是否为镐子
         *
         *
         * Define if the item is a Pickaxe
         */
        get() = false

    open val isAxe: Boolean
        /**
         * 定义物品是否为斧子
         *
         *
         * Define if the item is a Axe
         */
        get() = false

    open val isSword: Boolean
        /**
         * 定义物品是否为剑
         *
         *
         * Define if the item is a Sword
         */
        get() = false

    open val isShovel: Boolean
        /**
         * 定义物品是否为铲子
         *
         *
         * Define if the item is a Shovel
         */
        get() = false

    open val isHoe: Boolean
        /**
         * 定义物品是否为锄头
         *
         *
         * Define if the item is a Hoe
         */
        get() = false

    open val isShears: Boolean
        /**
         * 定义物品是否为剪刀
         *
         *
         * Define if the item is a Shears
         */
        get() = false

    open val isArmor: Boolean
        /**
         * 定义物品是否为盔甲
         *
         *
         * Define if the item is a Armor
         */
        get() = false

    open val isHelmet: Boolean
        /**
         * 定义物品是否为头盔
         *
         *
         * Define if the item is a Helmet
         */
        get() = false

    open val isChestplate: Boolean
        /**
         * 定义物品是否为胸甲
         *
         *
         * Define if the item is a Chestplate
         */
        get() = false

    open val isLeggings: Boolean
        /**
         * 定义物品是否为护腿
         *
         *
         * Define if the item is a Leggings
         */
        get() = false

    open val isBoots: Boolean
        /**
         * 定义物品是否为靴子
         *
         *
         * Define if the item is a Boots
         */
        get() = false

    open val enchantAbility: Int
        /**
         * 定义物品的附魔
         *
         *
         * Define the enchantment of an item
         */
        get() = 0

    open val attackDamage: Int
        /**
         * 定义物品的攻击伤害
         *
         *
         * Define the attackdamage of an item
         */
        get() = 1

    open fun getAttackDamage(entity: Entity): Int {
        return attackDamage
    }

    open val armorPoints: Int
        /**
         * 定义物品的护甲值
         *
         *
         * Define the Armour value of an item
         */
        get() = 0

    open val toughness: Int
        /**
         * 定义物品的盔甲韧性
         *
         *
         * Define the Armour Toughness of an item
         */
        get() = 0

    open val isUnbreakable: Boolean
        /**
         * 定义物品是否不可损坏
         *
         *
         * Define if the item is Unbreakable
         */
        get() = false

    open val isLavaResistant: Boolean
        /**
         * 物品是否抵抗熔岩和火，并且可以像在水上一样漂浮在熔岩上。
         *
         *
         * If the item is resistant to lava and fire and can float on lava like if it was on water.
         *
         * @since 1.4.0.0-PN
         */
        get() = false

    /**
     * 定义物品是否可以打破盾牌
     *
     *
     * Define if the item can break the shield
     */
    open fun canBreakShield(): Boolean {
        return false
    }

    /**
     * Called before [.onUse],The player is right clicking use on an item
     *
     * @param player          player
     * @param directionVector 点击的方向向量<br></br>The direction vector of the click
     * @return if false is returned, calls [.onUse] will be stopped
     */
    open fun onClickAir(player: Player, directionVector: Vector3): Boolean {
        return false
    }

    /**
     * The [.onClickAir] is called only after the command is successful
     *
     * @param player    the player
     * @param ticksUsed 物品被使用了多久(右键持续时间)<br></br>How long the item has been used (right-click duration)
     * @return the boolean
     */
    open fun onUse(player: Player, ticksUsed: Int): Boolean {
        return false
    }

    /**
     * Called after [.onUse],It will only be called when onUse returns true
     */
    open fun afterUse(player: Player) {
    }

    /**
     * 当玩家在长时间右键物品后释放物品时，该函数被调用。
     *
     *
     * Allows the item to execute code when the player releases the item after long clicking it.
     *
     * @param player    The player who released the click button<br></br>松开按钮的玩家
     * @param ticksUsed How many ticks the item was held.<br></br>这个物品被使用多少ticks时间
     * @return If an inventory contents update should be sent to the player<br></br>是否要向玩家发送库存内容的更新信息
     */
    open fun onRelease(player: Player, ticksUsed: Int): Boolean {
        return false
    }

    override fun toString(): String {
        return "Item(name=\"${idConvertToName()}\", id=\"${this.id}\", meta=${this.meta}, count=${this.count}${
            if (this.hasCompoundTag()) ", tags=0x${
                Binary.bytesToHexString(
                    compoundTag
                )
            }" else ")"
        }"
    }

    /**
     * 玩家使用一个物品交互时会调用这个方法
     *
     *
     * This method is called when the player interacts with an item
     *
     * @param level  玩家所在地图 <br></br> Player location level
     * @param player 玩家实例对象 <br></br> Player instance object
     * @param block  the block
     * @param target 交互的目标方块 <br></br>Interacting target block
     * @param face   交互的方向 <br></br>Direction of Interaction
     * @param fx     the fx
     * @param fy     the fy
     * @param fz     the fz
     * @return boolean
     */
    open fun onActivate(
        level: Level,
        player: Player,
        block: Block,
        target: Block,
        face: BlockFace,
        fx: Double,
        fy: Double,
        fz: Double
    ): Boolean {
        return false
    }

    fun decrement(amount: Int): Item {
        return increment(-amount)
    }

    fun increment(amount: Int): Item {
        if (count + amount <= 0) {
            return get(BlockID.AIR)
        }
        val cloned: Item = clone()
        cloned.count += amount
        return cloned
    }

    open val isFertilizer: Boolean
        /**
         * 如果为true,这个物品可以如骨粉一样减少作物成长时间
         *
         *
         * When true, this item can be used to reduce growing times like a bone meal.
         *
         * @return `true` if it can act like a bone meal
         */
        get() = false


    /**
     * 返回物品堆叠是否与指定的物品堆叠有相同的ID,伤害,NBT和数量
     *
     *
     * Returns whether the specified item stack has the same ID, damage, NBT and count as this item stack.
     *
     * @param other item
     * @return equal
     */
    fun equalsExact(other: Item): Boolean {
        return this.equals(other, checkDamage = true, checkCompound = true) && this.count == other.count
    }

    override fun equals(other: Any?): Boolean {
        return other is Item && this.equals(other, true)
    }

    open fun equalItemBlock(item: Item): Boolean {
        if (this.isBlock() && item.isBlock()) {
            return this.blockState === item.blockState
        }
        return true
    }

    @JvmOverloads
    fun equals(item: Item, checkDamage: Boolean, checkCompound: Boolean = true): Boolean {
        if (this.id != item.id) {
            return false
        }
        if (checkDamage && this.hasMeta() && item.hasMeta() && this.damage != item.damage) {
            return false
        }
        if (checkDamage && !equalItemBlock(item)) {
            return false
        }
        if (checkCompound && (this.hasCompoundTag() || item.hasCompoundTag())) {
            return this.namedTag == item.namedTag
        }
        return true
    }

    /**
     * Same as [.equals] but the enchantment order of the items does not affect the result.
     */
    fun equalsIgnoringEnchantmentOrder(item: Item, checkDamage: Boolean): Boolean {
        if (!this.equals(item, checkDamage, false)) {
            return false
        }
        if (compoundTag.contentEquals(item.compoundTag)) {
            return true
        }

        if (!this.hasCompoundTag() || !item.hasCompoundTag()) {
            return false
        }

        val thisTags = this.namedTag
        val otherTags = item.namedTag
        if (thisTags == otherTags) {
            return true
        }

        if (!thisTags!!.contains("ench") || !otherTags!!.contains("ench") || (thisTags["ench"] !is ListTag<*>) || (otherTags["ench"] !is ListTag<*>) || thisTags.getList(
                "ench"
            ).size() != otherTags.getList("ench").size()
        ) {
            return false
        }

        val thisEnchantmentTags = thisTags.getList("ench", CompoundTag::class.java)
        val otherEnchantmentTags = otherTags.getList("ench", CompoundTag::class.java)

        val size = thisEnchantmentTags.size()
        val enchantments: MutableMap<Int, Int> = mutableMapOf()

        for (i in 0..<size) {
            val tag = thisEnchantmentTags[i]
            enchantments[tag.getShort("id").toInt()] = tag.getShort("lvl").toInt()
        }

        for (i in 0..<size) {
            val tag = otherEnchantmentTags[i]
            if ((enchantments[tag.getShort("id").toInt()] ?: Int.MIN_VALUE) != tag.getShort("lvl").toInt()) {
                return false
            }
        }

        return true
    }

    public override fun clone(): Item {
        var tags = EmptyArrays.EMPTY_BYTES
        if (this.hasCompoundTag()) {
            tags = compoundTag.clone()
        }
        val item = super.clone() as Item
        item.setCompoundTag(tags)
        return item
    }

    /**
     * 控制此方块（在冒险模式下）可以使用/放置在其上的方块类型。
     *
     *
     * Controls what block types this block may be placed on.
     */
    fun addCanPlaceOn(block: Block) {
        val tag = getOrCreateNamedTag()
        val canPlaceOn = tag.getList("CanPlaceOn", StringTag::class.java)
        tag.putList("CanPlaceOn", canPlaceOn.add(StringTag(block.toItem().id)))
        this.setCompoundTag(tag)
    }

    fun addCanPlaceOn(blocks: Array<Block>) {
        for (block in blocks) {
            addCanPlaceOn(block)
        }
    }

    open fun setCanPlaceOn(blocks: Array<Block>) {
        val tag = getOrCreateNamedTag()
        val canPlaceOn = ListTag<StringTag>()
        for (block in blocks) {
            canPlaceOn.add(StringTag(block.toItem().id))
        }
        tag.putList("CanPlaceOn", canPlaceOn)
        this.setCompoundTag(tag)
    }

    val canPlaceOn: ListTag<StringTag>
        get() {
            val tag = getOrCreateNamedTag()
            return tag.getList("CanPlaceOn", StringTag::class.java)
        }

    /**
     * 控制此方块（在冒险模式下）可以破坏的方块类型。此效果不会改变原本的破坏速度和破坏后掉落物。
     *
     *
     * Controls what block types can destroy
     */
    fun addCanDestroy(block: Block) {
        val tag = getOrCreateNamedTag()
        val canDestroy = tag.getList("CanDestroy", StringTag::class.java)
        tag.putList("CanDestroy", canDestroy.add(StringTag(block.toItem().id)))
        this.setCompoundTag(tag)
    }

    fun addCanDestroy(blocks: Array<Block>) {
        for (block in blocks) {
            addCanDestroy(block)
        }
    }

    open fun setCanDestroy(blocks: Array<Block>) {
        val tag = getOrCreateNamedTag()
        val canDestroy = ListTag<StringTag>()
        for (block in blocks) {
            canDestroy.add(StringTag(block.toItem().id))
        }
        tag.putList("CanDestroy", canDestroy)
        this.setCompoundTag(tag)
    }

    val canDestroy: ListTag<StringTag>
        get() {
            val tag = getOrCreateNamedTag()
            return tag.getList("CanDestroy", StringTag::class.java)
        }

    /**
     * 物品锁定在玩家的物品栏
     * LOCK_IN_SLOT 阻止该物品被从玩家物品栏的该槽位移动、移除、丢弃或用于合成
     * LOCK_IN_INVENTORY 阻止该物品被从玩家的物品栏移除、丢弃或用于合成
     *
     *
     * Locks the item in the player's inventory
     * LOCK_IN_SLOT Prevents the item from being removed from the player's inventory, dropped, or crafted with.
     * LOCK_IN_INVENTORY Prevents the item from being moved or removed from its slot in the player's inventory, dropped, or crafted with
     */
    enum class ItemLockMode {
        NONE,  //only used in server
        LOCK_IN_SLOT,
        LOCK_IN_INVENTORY
    }

    open var itemLockMode: ItemLockMode
        /**
         * 获取物品锁定在玩家的物品栏的模式
         *
         *
         * Get items locked mode in the player's item inventory
         *
         * @return
         */
        get() {
            val tag = getOrCreateNamedTag()
            if (tag.contains("minecraft:item_lock")) {
                return ItemLockMode.entries[tag.getByte("minecraft:item_lock").toInt()]
            }
            return ItemLockMode.NONE
        }
        set(mode) {
            val tag = getOrCreateNamedTag()
            if (mode == ItemLockMode.NONE) {
                tag.remove("minecraft:item_lock")
            } else {
                tag.putByte("minecraft:item_lock", mode.ordinal)
            }
            this.setCompoundTag(tag)
        }

    open fun setKeepOnDeath(keepOnDeath: Boolean) {
        val tag = getOrCreateNamedTag()
        if (keepOnDeath) {
            tag.putByte("minecraft:keep_on_death", 1)
        } else {
            tag.remove("minecraft:keep_on_death")
        }
        this.setCompoundTag(tag)
    }

    /**
     * 该物品是否死亡不掉落
     *
     *
     * Define if the item does not drop on death
     *
     * @return
     */
    fun keepOnDeath(): Boolean {
        val tag = getOrCreateNamedTag()
        return tag.contains("minecraft:keep_on_death")
    }

    class ItemJsonComponents private constructor() {
        class CanPlaceOn {
            lateinit var blocks: Array<String>
        }

        class CanDestory {
            lateinit var blocks: Array<String>
        }

        class ItemLock {
            var mode: String? = null

            companion object {
                const val LOCK_IN_INVENTORY: String = "lock_in_inventory"
                const val LOCK_IN_SLOT: String = "lock_in_slot"
            }
        }

        class KeepOnDeath

        @SerializedName(value = "minecraft:can_place_on", alternate = ["can_place_on"])
        var canPlaceOn: CanPlaceOn? = null

        @SerializedName(value = "minecraft:can_destroy", alternate = ["can_destroy"])
        var canDestroy: CanDestory? = null

        @SerializedName(value = "minecraft:item_lock", alternate = ["item_lock"])
        var itemLock: ItemLock? = null

        @SerializedName(value = "minecraft:keep_on_death", alternate = ["keep_on_death"])
        var keepOnDeath: KeepOnDeath? = null

        companion object {
            fun fromJson(json: String?): ItemJsonComponents {
                return JSONUtils.from(json, ItemJsonComponents::class.java)
            }
        }
    }

    companion object {
        @JvmField
        val AIR: Item = ConstAirItem()

        @JvmField
        val EMPTY_ARRAY: Array<Item> = emptyArray()

        var UNKNOWN_STR: String = "Unknown"
        private var STACK_NETWORK_ID_COUNTER = 1

        @JvmStatic
        @JvmOverloads
        fun get(id: String, meta: Int = 0, count: Int = 1, tags: ByteArray? = null): Item {
            return get(id, meta, count, tags, true)
        }

        @JvmStatic
        fun get(id: String, meta: Int, count: Int, tags: ByteArray?, autoAssignStackNetworkId: Boolean): Item {
            var id1 = id
            id1 = if (id1.contains(":")) id1 else "minecraft:$id1"
            var item = Registries.ITEM[id1, meta, count, tags]
            if (item == null) {
                val itemBlockState = getItemBlockState(id1, meta)
                if (itemBlockState === BlockAir.STATE) {
                    return AIR
                }
                item = itemBlockState.toItem()
                item.setCount(count)
                if (tags != null) {
                    item.setCompoundTag(tags)
                }
            } else if (autoAssignStackNetworkId) {
                item.autoAssignStackNetworkId()
            }
            return item
        }

        fun parseCompoundTag(tag: ByteArray?): CompoundTag? {
            if (tag == null || tag.isEmpty()) return null
            return try {
                NBTIO.read(tag, ByteOrder.LITTLE_ENDIAN)
            } catch (e: IOException) {
                try {
                    NBTIO.read(tag, ByteOrder.BIG_ENDIAN)
                } catch (ee: IOException) {
                    throw UncheckedIOException(ee)
                }
            }
        }

        protected fun getItemBlockState(id: String, aux: Int): BlockState {
            val i = Registries.BLOCKSTATE_ITEMMETA[id, aux]
            if (i == null) {
                val properties = Registries.BLOCK.getBlockProperties(id)
                return properties.defaultState
            }
            return Registries.BLOCKSTATE[i]!!
        }
    }
}
