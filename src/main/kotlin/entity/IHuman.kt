package org.chorus_oss.chorus.entity

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.data.Skin
import org.chorus_oss.chorus.inventory.*
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockVector3
import org.chorus_oss.chorus.nbt.NBTIO
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import org.chorus_oss.chorus.nbt.tag.StringTag
import org.chorus_oss.chorus.utils.*
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors

interface IHuman : InventoryHolder {
    fun initHumanEntity(human: Entity) {
        human.setDataFlag(EntityFlag.HAS_GRAVITY)
        human.setDataProperty(EntityDataTypes.BED_POSITION, BlockVector3(0, 0, 0), false)

        if (human !is Player) {
            if (human.namedTag!!.contains("NameTag")) {
                human.setNameTag(human.namedTag!!.getString("NameTag"))
            }

            if (human.namedTag!!.contains("Skin") && human.namedTag!!.get("Skin") is CompoundTag) {
                val skinTag: CompoundTag = human.namedTag!!.getCompound("Skin")
                if (!skinTag.contains("Transparent")) {
                    skinTag.putBoolean("Transparent", false)
                }
                val newSkin: Skin = Skin()
                if (skinTag.contains("ModelId")) {
                    newSkin.setSkinId(skinTag.getString("ModelId"))
                }
                if (skinTag.contains("PlayFabId")) {
                    newSkin.setPlayFabId(skinTag.getString("PlayFabId"))
                }
                if (skinTag.contains("Data")) {
                    val data: ByteArray = skinTag.getByteArray("Data")
                    if (skinTag.contains("SkinImageWidth") && skinTag.contains("SkinImageHeight")) {
                        val width: Int = skinTag.getInt("SkinImageWidth")
                        val height: Int = skinTag.getInt("SkinImageHeight")
                        newSkin.setSkinData(SerializedImage(width, height, data))
                    } else {
                        newSkin.setSkinData(data)
                    }
                }
                if (skinTag.contains("CapeId")) {
                    newSkin.setCapeId(skinTag.getString("CapeId"))
                }
                if (skinTag.contains("CapeData")) {
                    val data: ByteArray = skinTag.getByteArray("CapeData")
                    if (skinTag.contains("CapeImageWidth") && skinTag.contains("CapeImageHeight")) {
                        val width: Int = skinTag.getInt("CapeImageWidth")
                        val height: Int = skinTag.getInt("CapeImageHeight")
                        newSkin.setCapeData(SerializedImage(width, height, data))
                    } else {
                        newSkin.setCapeData(data)
                    }
                }
                if (skinTag.contains("GeometryName")) {
                    newSkin.setGeometryName(skinTag.getString("GeometryName"))
                }
                if (skinTag.contains("SkinResourcePatch")) {
                    newSkin.setSkinResourcePatch(
                        String(
                            skinTag.getByteArray("SkinResourcePatch"),
                            StandardCharsets.UTF_8
                        )
                    )
                }
                if (skinTag.contains("GeometryData")) {
                    newSkin.setGeometryData(String(skinTag.getByteArray("GeometryData"), StandardCharsets.UTF_8))
                }
                if (skinTag.contains("SkinAnimationData")) {
                    newSkin.setAnimationData(
                        String(
                            skinTag.getByteArray("SkinAnimationData"),
                            StandardCharsets.UTF_8
                        )
                    )
                } else if (skinTag.contains("AnimationData")) { // backwards compatible
                    newSkin.setAnimationData(
                        String(
                            skinTag.getByteArray("AnimationData"),
                            StandardCharsets.UTF_8
                        )
                    )
                }
                if (skinTag.contains("PremiumSkin")) {
                    newSkin.setPremium(skinTag.getBoolean("PremiumSkin"))
                }
                if (skinTag.contains("PersonaSkin")) {
                    newSkin.setPersona(skinTag.getBoolean("PersonaSkin"))
                }
                if (skinTag.contains("CapeOnClassicSkin")) {
                    newSkin.setCapeOnClassic(skinTag.getBoolean("CapeOnClassicSkin"))
                }
                if (skinTag.contains("AnimatedImageData")) {
                    val list: ListTag<CompoundTag> = skinTag.getList("AnimatedImageData", CompoundTag::class.java)
                    for (animationTag: CompoundTag in list.all) {
                        val frames: Float = animationTag.getFloat("Frames")
                        val type: Int = animationTag.getInt("Type")
                        val image: ByteArray = animationTag.getByteArray("Image")
                        val width: Int = animationTag.getInt("ImageWidth")
                        val height: Int = animationTag.getInt("ImageHeight")
                        val expression: Int = animationTag.getInt("AnimationExpression")
                        newSkin.getAnimations()
                            .add(SkinAnimation(SerializedImage(width, height, image), type, frames, expression))
                    }
                }
                if (skinTag.contains("ArmSize")) {
                    newSkin.setArmSize(skinTag.getString("ArmSize"))
                }
                if (skinTag.contains("SkinColor")) {
                    newSkin.setSkinColor(skinTag.getString("SkinColor"))
                }
                if (skinTag.contains("PersonaPieces")) {
                    val pieces: ListTag<CompoundTag> = skinTag.getList("PersonaPieces", CompoundTag::class.java)
                    for (piece: CompoundTag in pieces.all) {
                        newSkin.getPersonaPieces().add(
                            PersonaPiece(
                                piece.getString("PieceId"),
                                piece.getString("PieceType"),
                                piece.getString("PackId"),
                                piece.getBoolean("IsDefault"),
                                piece.getString("ProductId")
                            )
                        )
                    }
                }
                if (skinTag.contains("PieceTintColors")) {
                    val tintColors: ListTag<CompoundTag> = skinTag.getList("PieceTintColors", CompoundTag::class.java)
                    for (tintColor: CompoundTag in tintColors.all) {
                        newSkin.getTintColors().add(
                            PersonaPieceTint(
                                tintColor.getString("PieceType"),
                                tintColor.getList("Colors", StringTag::class.java).all.stream()
                                    .map(Function { stringTag: StringTag -> stringTag.data })
                                    .collect(Collectors.toList())
                            )
                        )
                    }
                }
                if (skinTag.contains("IsTrustedSkin")) {
                    newSkin.setTrusted(skinTag.getBoolean("IsTrustedSkin"))
                }
                this.skin = (newSkin)
            }

            if (this.skin == null) {
                this.skin = (Skin())
            }
            this.setUUID(
                Utils.dataToUUID(
                    human.getRuntimeID().toString().toByteArray(StandardCharsets.UTF_8),
                    skin.getSkinData().data, human.getNameTag().toByteArray(StandardCharsets.UTF_8)
                )
            )
        }

        this.setInventories(
            arrayOf(
                HumanInventory(this),
                HumanOffHandInventory(this),
                HumanEnderChestInventory(this)
            )
        )

        if (human.namedTag!!.containsNumber("SelectedInventorySlot")) {
            inventory.setHeldItemSlot(human.namedTag!!.getInt("SelectedInventorySlot").coerceIn(0, 8))
        }

        if (human.namedTag!!.contains("Inventory") && human.namedTag!!["Inventory"] is ListTag<*>) {
            val inventory: HumanInventory = this.inventory
            val inventoryList: ListTag<CompoundTag> = human.namedTag!!.getList("Inventory", CompoundTag::class.java)
            for (item: CompoundTag in inventoryList.all) {
                val slot: Int = item.getByte("Slot").toInt()
                inventory.setItem(slot, NBTIO.getItemHelper(item)) //inventory 0-39
            }
        }
        if (human.namedTag!!.containsCompound("OffInventory")) {
            val offhandInventory: HumanOffHandInventory = offhandInventory
            val offHand: CompoundTag = human.namedTag!!.getCompound("OffInventory")
            offhandInventory!!.setItem(0, NBTIO.getItemHelper(offHand)) //offinventory index 0
        }
        if (human.namedTag!!.contains("EnderItems") && human.namedTag!!["EnderItems"] is ListTag<*>) {
            val inventoryList: ListTag<CompoundTag> = human.namedTag!!.getList("EnderItems", CompoundTag::class.java)
            for (item: CompoundTag in inventoryList.all) { //enderItems index 0-26
                (human as EntityHumanType).enderChestInventory
                    .setItem(item.getByte("Slot").toInt(), NBTIO.getItemHelper(item))
            }
        }
    }

    fun saveHumanEntity(human: Entity) {
        //EntityHumanType
        val inventoryTag: ListTag<CompoundTag>?
        if (this.inventory != null) {
            inventoryTag = ListTag()
            human.namedTag!!.putList("Inventory", inventoryTag)

            for (entry in inventory.contents.entries) {
                inventoryTag.add(NBTIO.putItemHelper(entry.value, entry.key))
            }

            human.namedTag!!.putInt("SelectedInventorySlot", inventory.heldItemIndex)
        }

        if (this.offhandInventory != null) {
            val item: Item = offhandInventory.getItem(0)
            human.namedTag!!.putCompound("OffInventory", NBTIO.putItemHelper(item, 0))
        }

        human.namedTag!!.putList("EnderItems", ListTag<CompoundTag>())
        if (this.enderChestInventory != null) {
            val enderItems: ListTag<CompoundTag> = human.namedTag!!.getList("EnderItems", CompoundTag::class.java)
            for (slot in 0..<enderChestInventory.size) {
                val item: Item = enderChestInventory.getItem(slot)
                if (!item.isNothing) {
                    enderItems.add(NBTIO.putItemHelper(item, slot))
                }
            }
            human.namedTag!!.putList("EnderItems", enderItems)
        }

        //EntityHuman
        val skin: Skin = skin
        if (skin != null) {
            val skinTag: CompoundTag = CompoundTag()
                .putByteArray("Data", skin.getSkinData().data)
                .putInt("SkinImageWidth", skin.getSkinData().width)
                .putInt("SkinImageHeight", skin.getSkinData().height)
                .putString("ModelId", skin.getSkinId())
                .putString("CapeId", skin.getCapeId())
                .putByteArray("CapeData", skin.getCapeData().data)
                .putInt("CapeImageWidth", skin.getCapeData().width)
                .putInt("CapeImageHeight", skin.getCapeData().height)
                .putByteArray("SkinResourcePatch", skin.getSkinResourcePatch().toByteArray(StandardCharsets.UTF_8))
                .putByteArray("GeometryData", skin.getGeometryData().toByteArray(StandardCharsets.UTF_8))
                .putByteArray("SkinAnimationData", skin.getAnimationData().toByteArray(StandardCharsets.UTF_8))
                .putBoolean("PremiumSkin", skin.isPremium())
                .putBoolean("PersonaSkin", skin.isPersona())
                .putBoolean("CapeOnClassicSkin", skin.isCapeOnClassic())
                .putString("ArmSize", skin.getArmSize()!!)
                .putString("SkinColor", skin.getSkinColor()!!)
                .putBoolean("IsTrustedSkin", skin.isTrusted())

            val animations = skin.getAnimations()
            if (animations.isNotEmpty()) {
                val animationsTag: ListTag<CompoundTag> = ListTag()
                for (animation in animations) {
                    animationsTag.add(
                        CompoundTag()
                            .putFloat("Frames", animation.frames)
                            .putInt("Type", animation.type)
                            .putInt("ImageWidth", animation.image.width)
                            .putInt("ImageHeight", animation.image.height)
                            .putInt("AnimationExpression", animation.expression)
                            .putByteArray("Image", animation.image.data)
                    )
                }
                skinTag.putList("AnimatedImageData", animationsTag)
            }

            val personaPieces = skin.getPersonaPieces()
            if (personaPieces.isNotEmpty()) {
                val piecesTag: ListTag<CompoundTag> = ListTag()
                for (piece: PersonaPiece in personaPieces) {
                    piecesTag.add(
                        CompoundTag().putString("PieceId", piece.id)
                            .putString("PieceType", piece.type)
                            .putString("PackId", piece.packId)
                            .putBoolean("IsDefault", piece.isDefault)
                            .putString("ProductId", piece.productId)
                    )
                }
                skinTag.putList("PersonaPieces", piecesTag)
            }
            val tints = skin.getTintColors()
            if (tints.isNotEmpty()) {
                val tintsTag: ListTag<CompoundTag> = ListTag()
                for (tint: PersonaPieceTint in tints) {
                    val colors: ListTag<StringTag> = ListTag()
                    colors.all = (tint.colors.stream().map { data: String? ->
                        StringTag(
                            data!!
                        )
                    }.collect(Collectors.toList()))
                    tintsTag.add(
                        CompoundTag()
                            .putString("PieceType", tint.pieceType)
                            .putList("Colors", colors)
                    )
                }
                skinTag.putList("PieceTintColors", tintsTag)
            }

            if (skin.getPlayFabId().isNotEmpty()) {
                skinTag.putString("PlayFabId", skin.getPlayFabId())
            }

            human.namedTag!!.putCompound("Skin", skinTag)
        }
    }

    var skin: Skin

    fun getUUID(): UUID

    fun setUUID(uuid: UUID)

    fun setInventories(inventory: Array<Inventory>)

    override val inventory: HumanInventory

    val offhandInventory: HumanOffHandInventory

    val enderChestInventory: HumanEnderChestInventory

    override val level: Level?

    fun getEntity(): Entity {
        return this as Entity
    }

    companion object {
        const val NETWORK_ID: Int = 257
    }
}
