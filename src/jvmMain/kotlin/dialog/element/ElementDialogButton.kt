package org.chorus_oss.chorus.dialog.element

import org.chorus_oss.chorus.dialog.window.Dialog

class ElementDialogButton @JvmOverloads constructor(// json if the format is required do not change it
    var name: String, var text: String, nextDialog: Dialog? = null, mode: Mode = Mode.BUTTON_MODE, type: Int = 1
) {
    private var data: List<CmdLine>

    @JvmField
    @Transient
    var nextDialog: Dialog? = null

    class CmdLine(@JvmField var cmdLine: String, var cmdVer: Int) {
        companion object {
            @Transient
            const val CMD_VER: Int = 19
        }
    }

    private var mode: Int

    var type: Int

    init {
        this.nextDialog = nextDialog
        this.data = updateButtonData()
        this.mode = mode.ordinal
        this.type = type
    }

    fun updateButtonData(): List<CmdLine> {
        val list: MutableList<CmdLine> = ArrayList()
        val split = text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (str in split) {
            list.add(CmdLine(str, CmdLine.CMD_VER))
        }
        return list
    }

    fun getData(): List<CmdLine> {
        // data will not be updated by the client
        // so we should update data with text content whenever we need it
        return updateButtonData()
    }

    fun getMode(): Mode {
        return when (mode) {
            0 -> Mode.BUTTON_MODE
            1 -> Mode.ON_EXIT
            2 -> Mode.ON_ENTER
            else -> throw IllegalStateException("Unexpected value: $mode")
        }
    }

    fun setMode(mode: Mode) {
        this.mode = mode.ordinal
    }

    enum class Mode {
        BUTTON_MODE,
        ON_EXIT,
        ON_ENTER
    }
}
