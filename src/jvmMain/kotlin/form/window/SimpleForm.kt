package org.chorus_oss.chorus.form.window

import com.google.gson.JsonArray
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.form.element.simple.ButtonImage
import org.chorus_oss.chorus.form.element.simple.ElementButton
import org.chorus_oss.chorus.form.response.SimpleResponse
import java.util.function.BiConsumer
import java.util.function.Consumer

class SimpleForm : Form<SimpleResponse?> {
    protected var content: String = ""

    var buttons: MutableMap<ElementButton, Consumer<Player>?> = LinkedHashMap()

    constructor(title: String) : super(title)

    constructor(title: String, content: String) : super(title) {
        this.content = content
    }

    override fun title(title: String): SimpleForm {
        return super.title(title) as SimpleForm
    }

    fun addButton(element: ElementButton): SimpleForm {
        return this.addButton(element, null)
    }

    fun addButton(element: ElementButton, callback: Consumer<Player>?): SimpleForm {
        buttons[element] = callback
        return this
    }

    fun addButton(text: String?, callback: Consumer<Player>?): SimpleForm {
        return this.addButton(text, null, callback)
    }

    @JvmOverloads
    fun addButton(text: String?, image: ButtonImage? = null, callback: Consumer<Player>? = null): SimpleForm {
        return this.addButton(ElementButton(text, image), callback)
    }

    fun updateElement(index: Int, newElement: ElementButton): ElementButton? {
        if (buttons.size > index) {
            return buttons.keys.toTypedArray()[index]
                .text(newElement.text)
                .image(newElement.image)
        }
        return null
    }

    fun updateElement(index: Int, newElement: ElementButton, callback: Consumer<Player>?) {
        val element = this.updateElement(index, newElement)
        if (element != null) {
            buttons[element] = callback
        }
    }

    fun removeElement(index: Int) {
        this.removeElement(buttons.keys.toTypedArray()[index])
    }

    fun removeElement(elementButton: ElementButton) {
        buttons.remove(elementButton)
    }

    override fun onSubmit(submitted: BiConsumer<Player?, SimpleResponse?>?): SimpleForm {
        return super.onSubmit(submitted) as SimpleForm
    }

    override fun onClose(closed: Consumer<Player?>?): SimpleForm {
        return super.onClose(closed) as SimpleForm
    }

    override fun send(player: Player): SimpleForm {
        return super.send(player) as SimpleForm
    }

    override fun send(player: Player, id: Int): SimpleForm {
        return super.send(player, id) as SimpleForm
    }

    override fun sendUpdate(player: Player): SimpleForm {
        return super.sendUpdate(player) as SimpleForm
    }

    override fun windowType(): String {
        return "form"
    }

    override fun toJson(): String {
        `object`.addProperty("type", this.windowType())
        `object`.addProperty("title", this.title)
        `object`.addProperty("content", this.content)

        val buttons = JsonArray()
        this.buttons
            .keys
            .forEach(Consumer { element: ElementButton -> buttons.add(element.toJson()) })
        `object`.add("buttons", buttons)

        return `object`.toString()
    }

    override fun respond(player: Player, formData: String): SimpleResponse? {
        if (!super.handle(player, formData)) {
            this.supplyClosed(player)
            return null
        }


        val entries = buttons.entries.toTypedArray()

        var clickedId = -1
        try {
            clickedId = Integer.parseInt(formData)
        } catch (ignored: Exception) {
        }

        if (entries.size < clickedId || clickedId == -1) {
            return null
        }

        val entry = entries[clickedId]

        entry.value?.accept(player)

        val response = SimpleResponse(clickedId, entry.key)
        this.supplySubmitted(player, response)
        return response
    }

    override fun <M : Any> putMeta(key: String, `object`: M): SimpleForm {
        return super.putMeta(key, `object`) as SimpleForm
    }
}
