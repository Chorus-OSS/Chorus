package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.block.BlockID
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.NBTIO
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.IntTag
import org.chorus_oss.chorus.utils.RedstoneComponent
import kotlin.math.min

class BlockEntityLectern(chunk: IChunk, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt) {
    var totalPages: Int = 0
        private set


    override fun initBlockEntity() {
        super.initBlockEntity()
        updateTotalPages()
    }

    override fun loadNBT() {
        super.loadNBT()
        if (namedTag["book"] !is CompoundTag) {
            namedTag.remove("book")
        }

        if (namedTag["page"] !is IntTag) {
            namedTag.remove("page")
        }
    }

    override val spawnCompound: CompoundTag
        get() {
            val c = super.spawnCompound
                .putBoolean("isMovable", this.isMovable)

            val book = book
            if (!book.isNothing) {
                c.putCompound("book", NBTIO.putItemHelper(book))
                c.putBoolean("hasBook", true)
                c.putInt("page", rawPage)
                c.putInt("totalPages", totalPages)
            } else {
                c.putBoolean("hasBook", false)
            }

            return c
        }

    override val isBlockEntityValid: Boolean
        get() = block.id === BlockID.LECTERN

    override fun onBreak(isSilkTouch: Boolean) {
        level.dropItem(this.position, book)
    }

    fun hasBook(): Boolean {
        return namedTag.contains("book") && namedTag["book"] is CompoundTag
    }

    var book: Item
        get() {
            return if (!hasBook()) {
                Item.AIR
            } else {
                NBTIO.getItemHelper(namedTag.getCompound("book"))
            }
        }
        set(item) {
            if (item.id == ItemID.WRITTEN_BOOK || item.id == ItemID.WRITABLE_BOOK) {
                namedTag.putCompound("book", NBTIO.putItemHelper(item))
            } else {
                namedTag.remove("book")
                namedTag.remove("page")
            }
            updateTotalPages()
        }

    var leftPage: Int
        get() = (rawPage * 2) + 1
        set(newLeftPage) {
            rawPage = (newLeftPage - 1) / 2
        }

    var rightPage: Int
        get() = leftPage + 1
        set(newRightPage) {
            leftPage = newRightPage - 1
        }

    var rawPage: Int
        get() = namedTag.getInt("page")
        set(page) {
            namedTag.putInt("page", min(page.toDouble(), totalPages.toDouble()).toInt())
            level.updateAround(this.position)
        }


    private fun updateTotalPages() {
        val book = book
        totalPages = if (book.isNothing || !book.hasCompoundTag()) {
            0
        } else {
            book.namedTag!!.getList("pages", CompoundTag::class.java).size()
        }
        RedstoneComponent.updateAroundRedstone(this)
    }
}
