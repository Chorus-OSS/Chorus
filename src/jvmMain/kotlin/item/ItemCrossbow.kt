package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.entity.item.EntityCrossbowFirework
import org.chorus_oss.chorus.entity.projectile.abstract_arrow.EntityArrow
import org.chorus_oss.chorus.event.entity.EntityShootCrossbowEvent
import org.chorus_oss.chorus.event.entity.ProjectileLaunchEvent
import org.chorus_oss.chorus.inventory.Inventory
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.level.Sound
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.FloatTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.utils.Utils
import kotlin.math.cos
import kotlin.math.sin

class ItemCrossbow @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    ItemTool(ItemID.Companion.CROSSBOW, meta, count, "Crossbow") {
    private var loadTick = 0

    override val maxDurability: Int
        get() = DURABILITY_CROSSBOW

    override fun onUse(player: Player, ticksUsed: Int): Boolean {
        if (isLoaded) return true
        this.loadTick = player.level!!.tick
        var needTickUsed = 25
        val enchantment = this.getEnchantment(Enchantment.Companion.ID_CROSSBOW_QUICK_CHARGE)
        if (enchantment != null) {
            needTickUsed -= enchantment.level * 5 //0.25s
        }

        if (ticksUsed >= needTickUsed) {
            val itemArrow: Item
            val inventoryOptional = player.inventory.contents.entries.stream()
                .filter { item: Map.Entry<Int?, Item?> -> item.value is ItemArrow || item.value is ItemFireworkRocket }
                .findFirst()
            val offhandOptional = player.offhandInventory.contents.entries.stream()
                .filter { item: Map.Entry<Int?, Item?> -> item.value is ItemArrow || item.value is ItemFireworkRocket }
                .findFirst()
            var item: Item? = null
            var inventory: Inventory? = null
            if (offhandOptional.isPresent) {
                inventory = player.offhandInventory
                item = offhandOptional.get().value
            } else if (inventoryOptional.isPresent) {
                inventory = player.inventory
                item = inventoryOptional.get().value
            }
            if (inventory == null) return false
            if (!this.canLoad(item!!)) {
                if (player.isCreative) {
                    this.loadArrow(player, item)
                }

                return true
            }

            if (!this.isLoaded) {
                itemArrow = item.clone()
                itemArrow.setCount(1)

                if (!player.isCreative) {
                    if (!this.isUnbreakable) {
                        val durability = this.getEnchantment(Enchantment.Companion.ID_DURABILITY)
                        if (durability == null || durability.level <= 0 || 100 / (durability.level + 1) > Utils.random.nextInt(
                                100
                            )
                        ) {
                            this.damage = this.damage + 2
                            if (this.damage >= 385) {
                                --this.count
                            }
                            player.inventory.setItemInHand(this)
                        }
                    }

                    inventory.removeItem(itemArrow)
                }

                this.loadArrow(player, itemArrow)
            }
        }
        return true
    }

    protected fun canLoad(item: Item): Boolean {
        return when (item.id) {
            ItemID.Companion.ARROW, ItemID.Companion.FIREWORK_ROCKET -> true
            else -> false
        }
    }

    override fun onClickAir(player: Player, directionVector: Vector3): Boolean {
        if (this.isLoaded && player.level!!.tick - this.loadTick > 5) {
            val mX: Double
            val mY: Double
            val mZ: Double
            val nbt = (CompoundTag())
                .putList(
                    "Pos", ListTag<FloatTag>()
                        .add(FloatTag(player.position.x))
                        .add(FloatTag(player.position.y + player.getEyeHeight().toDouble()))
                        .add(FloatTag(player.position.z))
                )
                .putList(
                    "Motion", ListTag<FloatTag>()
                        .add(FloatTag((-sin(player.rotation.yaw / 180.0 * 3.141592653589793) * cos(player.rotation.pitch / 180.0 * 3.141592653589793)).also {
                            mX = it
                        }))
                        .add(FloatTag((-sin(player.rotation.pitch / 180.0 * 3.141592653589793)).also { mY = it }))
                        .add(FloatTag((cos(player.rotation.yaw / 180.0 * 3.141592653589793) * cos(player.rotation.pitch / 180.0 * 3.141592653589793)).also {
                            mZ = it
                        }))
                )
                .putList(
                    "Rotation", ListTag<FloatTag>()
                        .add(FloatTag((if (player.rotation.yaw > 180.0) 360 else 0).toFloat() - player.rotation.yaw.toFloat()))
                        .add(FloatTag((-player.rotation.pitch).toFloat()))
                )
            val item = get(this.namedTag!!.getCompound("chargedItem").getString("Name"))
            if (item.id == ItemID.FIREWORK_ROCKET) {
                val entity = EntityCrossbowFirework(player.chunk, nbt)
                entity.setMotion(Vector3(mX, mY, mZ))
                entity.spawnToAll()
                player.level!!.addSound(player.position, Sound.CROSSBOW_SHOOT)
                removeChargedItem(player)
            } else {
                val entity = EntityArrow(player.chunk, nbt, player, true)
                val chargedItem = this.namedTag!!.getCompound("chargedItem")
                entity.item = (
                        get(
                            chargedItem.getString("Name"),
                            chargedItem.getShort("Damage").toInt(),
                            chargedItem.getByte("Count").toInt()
                        ) as ItemArrow
                        )
                val entityShootBowEvent = EntityShootCrossbowEvent(player, this, entity)
                Server.instance.pluginManager.callEvent(entityShootBowEvent)
                if (entityShootBowEvent.cancelled) {
                    entityShootBowEvent.getProjectile(0).close()
                    player.inventory.sendContents(player)
                } else {
                    val proj = entityShootBowEvent.getProjectile(0)
                    proj.setMotion(proj.getMotion().multiply(3.5))
                    val projectile = ProjectileLaunchEvent(proj, player)
                    Server.instance.pluginManager.callEvent(projectile)
                    if (projectile.cancelled) {
                        proj.close()
                    } else {
                        proj.spawnToAll()
                        player.level!!.addSound(player.position, Sound.CROSSBOW_SHOOT)
                        removeChargedItem(player)
                    }
                }
            }
        }
        return true
    }

    fun removeChargedItem(player: Player) {
        this.setCompoundTag(this.namedTag!!.remove("chargedItem"))
        player.inventory.setItemInHand(this)
    }

    override fun onRelease(player: Player, ticksUsed: Int): Boolean {
        return true
    }


    fun loadArrow(player: Player, arrow: Item?) {
        if (arrow != null) {
            val tag = if (this.namedTag == null) CompoundTag() else this.namedTag
            tag!!.putCompound(
                "chargedItem", CompoundTag()
                    .putByte("Count", arrow.getCount())
                    .putShort("Damage", arrow.damage)
                    .putString("Name", arrow.id)
                    .putByte("WasPickedUp", 0)
            )
            this.setCompoundTag(tag)
            player.inventory.setItemInHand(this)
            player.level!!.addSound(player.position, Sound.CROSSBOW_LOADING_END)
        }
    }

    val isLoaded: Boolean
        get() {
            val itemInfo = this.getNamedTagEntry("chargedItem")
            if (itemInfo == null) {
                return false
            } else {
                val tag = itemInfo as CompoundTag
                return tag.getByte("Count") > 0 && tag.getString("Name") != null
            }
        }

    override val enchantAbility: Int
        get() = 1
}
