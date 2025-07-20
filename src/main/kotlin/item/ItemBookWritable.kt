package org.chorus_oss.chorus.item

import com.google.common.base.Preconditions
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.ListTag

abstract class ItemBookWritable : Item {
    protected constructor(id: String) : super(id)

    protected constructor(id: String, meta: Int) : super(id, meta)

    protected constructor(id: String, meta: Int, count: Int) : super(id, meta, count)

    protected constructor(id: String, meta: Int, count: Int, name: String?) : super(id, meta, count, name)

    /**
     * Returns whether the given page exists in this book.
     */
    fun pageExists(pageId: Int): Boolean {
        check(
            pageId >= 0 && pageId < 50
        ) { "Page number $pageId is out of range" }
        if (this.hasCompoundTag()) {
            val tag = this.namedTag
            if (tag!!.contains("pages") && tag["pages"] is ListTag<*>) {
                return tag.getList("pages", CompoundTag::class.java).size() > pageId
            }
        }
        return false
    }

    /**
     * Returns a string containing the content of a page (which could be empty), or null if the page doesn't exist.
     */
    fun getPageText(pageId: Int): String? {
        check(
            pageId >= 0 && pageId < 50
        ) { "Page number $pageId is out of range" }
        if (this.hasCompoundTag()) {
            val tag = this.namedTag
            if (tag!!.contains("pages") && tag["pages"] is ListTag<*>) {
                val pages = tag.getList("pages", CompoundTag::class.java)
                if (pages.size() > pageId) {
                    return pages[pageId].getString("text")
                }
            }
        }
        return null
    }

    /**
     * Sets the text of a page in the book. Adds the page if the page does not yet exist.
     * @return boolean indicating success
     */
    fun setPageText(pageId: Int, pageText: String): Boolean {
        check(
            pageId >= 0 && pageId < 50
        ) { "Page number $pageId is out of range" }
        check(pageText.length <= 256) { "Text length ${pageText.length} is out of range" }
        val tag = if (this.hasCompoundTag()) {
            this.namedTag
        } else if (pageText.isEmpty()) {
            return false
        } else {
            CompoundTag()
        }
        val pages: ListTag<CompoundTag>
        if (!tag!!.contains("pages") || tag["pages"] !is ListTag<*>) {
            pages = ListTag()
            tag.putList("pages", pages)
        } else {
            pages = tag.getList("pages", CompoundTag::class.java)
        }
        if (pages.size() <= pageId) {
            for (current in pages.size()..pageId) {
                pages.add(createPageTag())
            }
        }

        pages[pageId].putString("text", pageText)
        this.setCompoundTag(tag)
        return true
    }

    /**
     * Adds a new page with the given page ID.
     * Creates a new page for every page between the given ID and existing pages that doesn't yet exist.
     * @return boolean indicating success
     */
    fun addPage(pageId: Int): Boolean {
        check(
            pageId >= 0 && pageId < 50
        ) { "Page number $pageId is out of range" }
        val tag = if (this.hasCompoundTag()) this.namedTag else CompoundTag()
        val pages: ListTag<CompoundTag>
        if (!tag!!.contains("pages") || tag["pages"] !is ListTag<*>) {
            pages = ListTag()
            tag.putList("pages", pages)
        } else {
            pages = tag.getList("pages", CompoundTag::class.java)
        }

        for (current in pages.size()..pageId) {
            pages.add(createPageTag())
        }
        this.setCompoundTag(tag)
        return true
    }

    /**
     * Deletes an existing page with the given page ID.
     * @return boolean indicating success
     */
    fun deletePage(pageId: Int): Boolean {
        check(
            pageId >= 0 && pageId < 50
        ) { "Page number $pageId is out of range" }
        if (this.hasCompoundTag()) {
            val tag = this.namedTag
            if (tag!!.contains("pages") && tag["pages"] is ListTag<*>) {
                val pages = tag.getList("pages", CompoundTag::class.java)
                if (pages.size() > pageId) {
                    pages.remove(pageId)
                    this.setCompoundTag(tag)
                }
            }
        }
        return true
    }

    /**
     * Inserts a new page with the given text and moves other pages upwards.
     * @return boolean indicating success
     */
    /**
     * Inserts a new page with the given text and moves other pages upwards.
     * @return boolean indicating success
     */
    @JvmOverloads
    fun insertPage(pageId: Int, pageText: String = ""): Boolean {
        check(
            pageId >= 0 && pageId < 50
        ) { "Page number $pageId is out of range" }
        check(pageText.length <= 256) { "Text length ${pageText.length} is out of range" }
        val tag = if (this.hasCompoundTag()) this.namedTag else CompoundTag()
        val pages: ListTag<CompoundTag>
        if (!tag!!.contains("pages") || tag["pages"] !is ListTag<*>) {
            pages = ListTag()
            tag.putList("pages", pages)
        } else {
            pages = tag.getList("pages", CompoundTag::class.java)
        }

        if (pages.size() <= pageId) {
            for (current in pages.size()..pageId) {
                pages.add(createPageTag())
            }
            pages[pageId].putString("text", pageText)
        } else {
            pages.add(pageId, createPageTag(pageText))
        }
        this.setCompoundTag(tag)
        return true
    }

    /**
     * Switches the text of two pages with each other.
     * @return boolean indicating success
     */
    fun swapPages(pageId1: Int, pageId2: Int): Boolean {
        check(
            pageId1 >= 0 && pageId1 < 50
        ) { "Page number $pageId1 is out of range" }
        check(
            pageId2 >= 0 && pageId2 < 50
        ) { "Page number $pageId2 is out of range" }
        if (this.hasCompoundTag()) {
            val tag = this.namedTag
            if (tag!!.contains("pages") && tag["pages"] is ListTag<*>) {
                val pages = tag.getList("pages", CompoundTag::class.java)
                if (pages.size() > pageId1 && pages.size() > pageId2) {
                    val pageContents1 = pages[pageId1].getString("text")
                    val pageContents2 = pages[pageId2].getString("text")
                    pages[pageId1].putString("text", pageContents2)
                    pages[pageId2].putString("text", pageContents1)
                    return true
                }
            }
        }
        return false
    }

    val pages: List<Any>
        /**
         * Returns an list containing all pages of this book.
         */
        get() {
            val tag =
                if (this.hasCompoundTag()) this.namedTag else CompoundTag()
            val pages: ListTag<CompoundTag>
            if (!tag!!.contains("pages") || tag["pages"] !is ListTag<*>) {
                pages = ListTag()
                tag.putList("pages", pages)
            } else {
                pages = tag.getList("pages", CompoundTag::class.java)
            }
            return pages.parseValue()
        }

    companion object {
        fun createPageTag(pageText: String = ""): CompoundTag {
            return CompoundTag()
                .putString("text", pageText)
                .putString("photoname", "")
        }
    }
}
