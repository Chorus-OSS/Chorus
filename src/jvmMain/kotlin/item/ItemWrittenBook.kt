package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag

class ItemWrittenBook @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) :
    ItemBookWritable(ItemID.Companion.WRITTEN_BOOK, 0, count, "Written Book") {
    override val maxStackSize: Int
        get() = 16

    fun writeBook(author: String, title: String, pages: Array<String>): Item {
        val pageList = ListTag<CompoundTag>()
        for (page in pages) {
            pageList.add(createPageTag(page))
        }
        return writeBook(author, title, pageList)
    }

    fun writeBook(author: String, title: String, pages: ListTag<CompoundTag>): Item {
        if (pages.size() > 50 || pages.size() <= 0) return this //Minecraft does not support more than 50 pages

        val tag = if (this.hasCompoundTag()) this.namedTag else CompoundTag()

        tag!!.putString("author", author)
        tag.putString("title", title)
        tag.putList("pages", pages)

        tag.putInt("generation", GENERATION_ORIGINAL)
        tag.putString("xuid", "")

        return this.setNamedTag(tag)
    }

    fun signBook(title: String, author: String, xuid: String, generation: Int): Boolean {
        this.setNamedTag(
            (if (this.hasCompoundTag()) this.namedTag!! else CompoundTag())
                .putString("title", title)
                .putString("author", author)
                .putInt("generation", generation)
                .putString("xuid", xuid)
        )
        return true
    }

    var generation: Int
        /**
         * Returns the generation of the book.
         * Generations higher than 1 can not be copied.
         */
        get() = if (this.hasCompoundTag()) this.namedTag!!.getInt("generation") else -1
        /**
         * Sets the generation of a book.
         */
        set(generation) {
            this.setNamedTag(
                (if (this.hasCompoundTag()) this.namedTag!! else CompoundTag()).putInt(
                    "generation",
                    generation
                )
            )
        }

    var author: String
        /**
         * Returns the author of this book.
         * This is not a reliable way to get the name of the player who signed this book.
         * The author can be set to anything when signing a book.
         */
        get() = if (this.hasCompoundTag()) this.namedTag!!.getString("author") else ""
        /**
         * Sets the author of this book.
         */
        set(author) {
            this.setNamedTag(
                (if (this.hasCompoundTag()) this.namedTag!! else CompoundTag()).putString(
                    "author",
                    author
                )
            )
        }

    var title: String
        /**
         * Returns the title of this book.
         */
        get() = if (this.hasCompoundTag()) this.namedTag!!
            .getString("title") else "Written Book"
        /**
         * Sets the title of this book.
         */
        set(title) {
            this.setNamedTag(
                (if (this.hasCompoundTag()) this.namedTag!! else CompoundTag()).putString(
                    "title",
                    title
                )
            )
        }

    var xUID: String
        /**
         * Returns the author's XUID of this book.
         */
        get() = if (this.hasCompoundTag()) this.namedTag!!.getString("xuid") else ""
        /**
         * Sets the author's XUID of this book.
         */
        set(title) {
            this.setNamedTag(
                (if (this.hasCompoundTag()) this.namedTag!! else CompoundTag()).putString(
                    "xuid",
                    title
                )
            )
        }

    companion object {
        const val GENERATION_ORIGINAL: Int = 0
        const val GENERATION_COPY: Int = 1
        const val GENERATION_COPY_OF_COPY: Int = 2
        const val GENERATION_TATTERED: Int = 3
    }
}