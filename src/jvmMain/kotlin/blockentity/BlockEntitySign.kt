package org.chorus_oss.chorus.blockentity

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.BlockStandingSign
import org.chorus_oss.chorus.event.block.SignChangeEvent
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.utils.BlockColor
import org.chorus_oss.chorus.utils.DyeColor
import org.chorus_oss.chorus.utils.TextFormat
import kotlin.math.min


open class BlockEntitySign(chunk: IChunk, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt) {
    private lateinit var frontText: Array<String?>
    private lateinit var backText: Array<String?>

    init {
        isMovable = true
    }

    override fun loadNBT() {
        super.loadNBT()
        frontText = arrayOfNulls(4)
        backText = arrayOfNulls(4)
        if (namedTag.containsCompound(TAG_FRONT_TEXT)) {
            getLines(true)
        } else {
            frontText[0] = ""
            namedTag.putCompound(
                TAG_FRONT_TEXT,
                CompoundTag().putString(TAG_TEXT_BLOB, java.lang.String.join("\n", *arrayOf("")))
            )
        }
        if (namedTag.containsCompound(TAG_BACK_TEXT)) {
            getLines(false)
        } else {
            backText[0] = ""
            namedTag.putCompound(
                TAG_BACK_TEXT,
                CompoundTag().putString(TAG_TEXT_BLOB, java.lang.String.join("\n", *arrayOf("")))
            )
        }

        // Check old text to sanitize
        if (frontText != null) {
            sanitizeText(frontText)
        }
        if (backText != null) {
            sanitizeText(backText)
        }
        if (!namedTag.getCompound(TAG_FRONT_TEXT).containsInt(TAG_TEXT_COLOR)) {
            this.setColor(true, DyeColor.BLACK.signColor)
        }
        if (!namedTag.getCompound(TAG_BACK_TEXT).containsInt(TAG_TEXT_COLOR)) {
            this.setColor(false, DyeColor.BLACK.signColor)
        }
        if (!namedTag.getCompound(TAG_FRONT_TEXT).containsByte(TAG_GLOWING_TEXT)) {
            this.setGlowing(true, false)
        }
        if (!namedTag.getCompound(TAG_BACK_TEXT).containsByte(TAG_GLOWING_TEXT)) {
            this.setGlowing(false, false)
        }
        updateLegacyCompoundTag()
        this.editorEntityRuntimeId = null
    }

    override fun saveNBT() {
        super.saveNBT()
        namedTag.getCompound(TAG_FRONT_TEXT)
            .putString(TAG_TEXT_BLOB, frontText.filterNotNull().joinToString("\n"))
            .putByte(TAG_PERSIST_FORMATTING, 1)
        namedTag.getCompound(TAG_BACK_TEXT)
            .putString(TAG_TEXT_BLOB, backText.filterNotNull().joinToString("\n"))
            .putByte(TAG_PERSIST_FORMATTING, 1)
        namedTag.putBoolean(TAG_LEGACY_BUG_RESOLVE, true)
            .putByte(TAG_WAXED, 0)
            .putLong(TAG_LOCKED_FOR_EDITING_BY, editorEntityRuntimeId!!)
    }

    var isWaxed: Boolean
        /**
         * @return If the sign is waxed, once a sign is waxed it cannot be modified
         */
        get() = namedTag.getByte(TAG_WAXED).toInt() == 1
        /**
         * @param waxed If the sign is waxed, once a sign is waxed it cannot be modified
         */
        set(waxed) {
            namedTag.putByte(
                TAG_WAXED,
                (if (waxed) 1.toByte() else 0.toByte()).toInt()
            )
        }

    override val isBlockEntityValid: Boolean
        get() {
            val block = block
            return block is BlockStandingSign
        }

    fun setText(vararg lines: String?): Boolean {
        return setText(true, *lines)
    }

    /**
     * 设置lines文本数组到Sign对象，同时更新NBT
     *
     * @param front the front
     * @param lines the lines
     * @return the text
     */
    fun setText(front: Boolean, vararg lines: String?): Boolean {
        if (front) {
            for (i in 0..3) {
                if (i < lines.size) frontText[i] = lines[i]
                else frontText[i] = ""
            }
            namedTag.getCompound(TAG_FRONT_TEXT).putString(TAG_TEXT_BLOB, lines.filterNotNull().joinToString("\n"))
        } else {
            for (i in 0..3) {
                if (i < lines.size) backText[i] = lines[i]
                else backText[i] = ""
            }
            namedTag.getCompound(TAG_BACK_TEXT).putString(TAG_TEXT_BLOB, lines.filterNotNull().joinToString("\n"))
        }
        this.spawnToAll()
        if (this.chunk != null) {
            setDirty()
        }
        return true
    }

    val text: Array<String?>
        get() = getText(true)

    fun getText(front: Boolean): Array<String?> {
        return if (front) frontText else backText
    }

    val isEmpty: Boolean
        get() = isEmpty(true)

    fun isEmpty(front: Boolean): Boolean {
        return if (front) {
            (frontText[0] == null || frontText[0]!!.isEmpty()) && frontText[1] == null && frontText[2] == null && frontText[3] == null
        } else {
            (backText[0] == null || backText[0]!!.isEmpty()) && backText[1] == null && backText[2] == null && backText[3] == null
        }
    }

    var editorEntityRuntimeId: Long?
        /**
         * 设置编辑此告示牌的玩家的运行时实体 ID。只有此玩家才能编辑告示牌。这用于防止多个玩家同时编辑同一告示牌，并防止玩家编辑他们未放置的告示牌。
         *
         *
         * Sets the runtime entity ID of the player editing this sign. Only this player will be able to edit the sign.
         * This is used to prevent multiple players from editing the same sign at the same time, and to prevent players
         * from editing signs they didn't place.
         */
        get() = namedTag.getLong(TAG_LOCKED_FOR_EDITING_BY)
        set(editorEntityRuntimeId) {
            namedTag.putLong(
                TAG_LOCKED_FOR_EDITING_BY,
                editorEntityRuntimeId ?: -1L
            )
        }

    var color: BlockColor
        get() = getColor(true)
        set(color) {
            setColor(true, color)
        }

    fun getColor(front: Boolean): BlockColor {
        return if (front) {
            BlockColor(
                namedTag.getCompound(TAG_FRONT_TEXT)
                    .getInt(TAG_TEXT_COLOR), true
            )
        } else {
            BlockColor(
                namedTag.getCompound(TAG_BACK_TEXT)
                    .getInt(TAG_TEXT_COLOR), true
            )
        }
    }

    fun setColor(front: Boolean, color: BlockColor) {
        if (front) {
            namedTag.getCompound(TAG_FRONT_TEXT).putInt(TAG_TEXT_COLOR, color.argb)
        } else {
            namedTag.getCompound(TAG_BACK_TEXT).putInt(TAG_TEXT_COLOR, color.argb)
        }
    }

    var isGlowing: Boolean
        get() = isGlowing(true)
        set(glowing) {
            setGlowing(true, glowing)
        }

    fun isGlowing(front: Boolean): Boolean {
        return if (front) {
            namedTag.getCompound(TAG_FRONT_TEXT)
                .getBoolean(TAG_GLOWING_TEXT)
        } else {
            namedTag.getCompound(TAG_BACK_TEXT)
                .getBoolean(TAG_GLOWING_TEXT)
        }
    }

    fun setGlowing(front: Boolean, glowing: Boolean) {
        if (front) {
            namedTag.getCompound(TAG_FRONT_TEXT).putBoolean(TAG_GLOWING_TEXT, glowing)
        } else {
            namedTag.getCompound(TAG_BACK_TEXT).putBoolean(TAG_GLOWING_TEXT, glowing)
        }
    }

    override fun updateCompoundTag(nbt: CompoundTag, player: Player): Boolean {
        if (nbt.getString("id") != BlockEntityID.SIGN && nbt.getString("id") != BlockEntityID.HANGING_SIGN) {
            return false
        }
        if (player.isOpenSignFront == null) return false
        if (!nbt.containsCompound(TAG_FRONT_TEXT) || !nbt.containsCompound(TAG_BACK_TEXT)) {
            return false
        }

        val lines = arrayOfNulls<String>(4)
        val splitLines = if (player.isOpenSignFront!!)
            nbt.getCompound(TAG_FRONT_TEXT).getString(TAG_TEXT_BLOB).split("\n".toRegex(), limit = 4)
                .toTypedArray() else
            nbt.getCompound(TAG_BACK_TEXT).getString(TAG_TEXT_BLOB).split("\n".toRegex(), limit = 4).toTypedArray()
        System.arraycopy(splitLines, 0, lines, 0, splitLines.size)

        sanitizeText(lines)

        val signChangeEvent = SignChangeEvent(this.block, player, lines)

        if (!namedTag.contains(TAG_LOCKED_FOR_EDITING_BY) || player.getRuntimeID() != this.editorEntityRuntimeId) {
            signChangeEvent.cancelled = true
        }

        if (player.removeFormat) {
            for (i in lines.indices) {
                lines[i] = TextFormat.clean(lines[i]!!)
            }
        }

        Server.instance.pluginManager.callEvent(signChangeEvent)

        if (!signChangeEvent.cancelled && player.isOpenSignFront != null) {
            this.setText(player.isOpenSignFront!!, *signChangeEvent.lines)
            this.editorEntityRuntimeId = null
            player.isOpenSignFront = null
            return true
        }
        player.isOpenSignFront = null
        return false
    }

    override val spawnCompound: CompoundTag
        get() = super.spawnCompound
            .putBoolean("isMovable", isMovable)
            .putCompound(
                TAG_FRONT_TEXT, CompoundTag()
                    .putString(
                        TAG_TEXT_BLOB,
                        namedTag.getCompound(TAG_FRONT_TEXT)
                            .getString(TAG_TEXT_BLOB)
                    )
                    .putInt(TAG_TEXT_COLOR, getColor(true).argb)
                    .putBoolean(TAG_GLOWING_TEXT, this.isGlowing)
                    .putBoolean(TAG_PERSIST_FORMATTING, true)
                    .putBoolean(TAG_HIDE_GLOW_OUTLINE, false)
                    .putString(TAG_TEXT_OWNER, "")
            )
            .putCompound(
                TAG_BACK_TEXT, CompoundTag()
                    .putString(
                        TAG_TEXT_BLOB,
                        namedTag.getCompound(TAG_BACK_TEXT)
                            .getString(TAG_TEXT_BLOB)
                    )
                    .putInt(TAG_TEXT_COLOR, getColor(false).argb)
                    .putBoolean(TAG_GLOWING_TEXT, this.isGlowing(false))
                    .putBoolean(TAG_PERSIST_FORMATTING, true)
                    .putBoolean(TAG_HIDE_GLOW_OUTLINE, false)
                    .putString(TAG_TEXT_OWNER, "")
            )
            .putBoolean(TAG_LEGACY_BUG_RESOLVE, true)
            .putByte(TAG_WAXED, 0)
            .putLong(TAG_LOCKED_FOR_EDITING_BY, editorEntityRuntimeId!!)

    //读取指定面的NBT到Sign对象字段
    private fun getLines(front: Boolean) {
        if (front) {
            val lines = namedTag.getCompound(TAG_FRONT_TEXT).getString(TAG_TEXT_BLOB).split("\n".toRegex(), limit = 4)
                .toTypedArray()
            for (i in frontText.indices) {
                if (i < lines.size) frontText[i] = lines[i]
                else frontText[i] = ""
            }
        } else {
            val lines = namedTag.getCompound(TAG_BACK_TEXT).getString(TAG_TEXT_BLOB).split("\n".toRegex(), limit = 4)
                .toTypedArray()
            for (i in backText.indices) {
                if (i < lines.size) backText[i] = lines[i]
                else backText[i] = ""
            }
        }
    }

    //在1.19.80以后,sign变成了双面显示，NBT结构也有所改变，这个方法将1.19.70以前的NBT更新至最新NBT结构
    private fun updateLegacyCompoundTag() {
        if (namedTag.contains(TAG_TEXT_BLOB)) {
            val lines = namedTag.getString(TAG_TEXT_BLOB).split("\n".toRegex(), limit = 4).toTypedArray()
            for (i in frontText.indices) {
                if (i < lines.size) frontText[i] = lines[i]
                else frontText[i] = ""
            }
            namedTag.getCompound(TAG_FRONT_TEXT).putString(TAG_TEXT_BLOB, frontText.filterNotNull().joinToString("\n"))
            namedTag.remove(TAG_TEXT_BLOB)
        } else {
            var count = 0
            for (i in 1..4) {
                val key = TAG_TEXT_BLOB + i
                if (namedTag.contains(key)) {
                    val line = namedTag.getString(key)
                    frontText[i - 1] = line
                    namedTag.remove(key)
                    count++
                }
            }
            if (count == 4) {
                namedTag.getCompound(TAG_FRONT_TEXT)
                    .putString(TAG_TEXT_BLOB, frontText.filterNotNull().joinToString("\n"))
            }
        }
        if (namedTag.contains(TAG_GLOWING_TEXT)) {
            this.setGlowing(true, namedTag.getBoolean(TAG_GLOWING_TEXT))
            namedTag.remove(TAG_GLOWING_TEXT)
        }
        if (namedTag.contains(TAG_TEXT_COLOR)) {
            this.setColor(true, BlockColor(namedTag.getInt(TAG_TEXT_COLOR), true))
            namedTag.remove(TAG_TEXT_COLOR)
        }
        namedTag.remove("Creator")
    }

    companion object {
        const val TAG_TEXT_BLOB: String = "Text"
        const val TAG_TEXT_LINE: String = "Text%d"
        const val TAG_HIDE_GLOW_OUTLINE: String = "HideGlowOutline"
        const val TAG_TEXT_OWNER: String = "TextOwner"
        const val TAG_TEXT_COLOR: String = "SignTextColor"
        const val TAG_GLOWING_TEXT: String = "IgnoreLighting"
        const val TAG_PERSIST_FORMATTING: String = "PersistFormatting"
        const val TAG_LEGACY_BUG_RESOLVE: String = "TextIgnoreLegacyBugResolved"
        const val TAG_FRONT_TEXT: String = "FrontText"
        const val TAG_BACK_TEXT: String = "BackText"
        const val TAG_WAXED: String = "IsWaxed"
        const val TAG_LOCKED_FOR_EDITING_BY: String = "LockedForEditingBy"

        //验证Line Text是否符合要求
        private fun sanitizeText(lines: Array<String?>) {
            for (i in lines.indices) {
                // Don't allow excessive text per line.
                if (lines[i] != null) {
                    lines[i] = lines[i]!!.substring(0, min(255.0, lines[i]!!.length.toDouble()).toInt())
                }
            }
        }
    }
}
