package org.chorus_oss.chorus.dialog.window

import com.google.common.reflect.TypeToken
import kotlinx.coroutines.runBlocking
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.dialog.element.ElementDialogButton
import org.chorus_oss.chorus.dialog.handler.FormDialogHandler
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.generated.resources.Res
import org.chorus_oss.chorus.utils.JSONUtils
import org.chorus_oss.chorus.utils.Loggable
import java.io.IOException


class FormWindowDialog @JvmOverloads constructor(
    @JvmField var title: String?,
    @JvmField var content: String?,
    @JvmField var bindEntity: Entity?,
    private var buttons: MutableList<ElementDialogButton> = mutableListOf()
) :
    Dialog {
    @JvmField
    var skinData: String = ""

    // Please do not call this method at will, otherwise it may cause potential bugs
    // usually you shouldn't edit this
    // but here this value is used to be an identifier
    var sceneName: String = (dialogId++).toString()
        // Please do not call this method at will, otherwise it may cause potential bugs
        protected set

    @Transient
    val handlers: MutableList<FormDialogHandler> = mutableListOf()

    init {
        try {
            this.skinData = runBlocking { Res.readBytes("files/npc_data.json").decodeToString() }
        } catch (e: IOException) {
            log.error("Failed to load npc_data.json: ", e)
        }

        requireNotNull(this.bindEntity) { "bindEntity cannot be null!" }
    }

    fun getButtons(): List<ElementDialogButton> {
        return buttons
    }

    fun setButtons(buttons: MutableList<ElementDialogButton>) {
        this.buttons = buttons
    }

    fun addButton(text: String) {
        this.addButton(ElementDialogButton(text, text))
    }

    fun addButton(button: ElementDialogButton) {
        buttons.add(button)
    }

    val entityUniqueID: Long
        get() = bindEntity!!.getUniqueID()

    fun addHandler(handler: FormDialogHandler) {
        handlers.add(handler)
    }

    var buttonJSONData: String?
        get() = JSONUtils.to(this.buttons)
        set(json) {
            val buttons = JSONUtils.from<MutableList<ElementDialogButton>?>(
                json,
                object : TypeToken<List<ElementDialogButton>?>() {}.type
            ) ?: mutableListOf()
            this.setButtons(buttons)
        }

    fun updateSceneName() {
        this.sceneName = (dialogId++).toString()
    }

    override fun send(player: Player) {
        player.showDialogWindow(this)
    }

    companion object : Loggable {
        private var dialogId: Long = 0
    }
}
