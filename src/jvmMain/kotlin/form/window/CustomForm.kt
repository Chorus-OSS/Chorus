package org.chorus_oss.chorus.form.window


import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.form.element.custom.*
import org.chorus_oss.chorus.form.response.CustomResponse
import org.chorus_oss.chorus.form.response.ElementResponse
import java.util.function.BiConsumer
import java.util.function.Consumer

class CustomForm(title: String) : Form<CustomResponse?>(title) {
    var elements: MutableList<ElementCustom> = mutableListOf()

    fun addElement(element: ElementCustom): CustomForm {
        elements.add(element)
        return this
    }

    override fun onSubmit(submitted: BiConsumer<Player?, CustomResponse?>?): CustomForm {
        return super.onSubmit(submitted) as CustomForm
    }

    override fun onClose(closed: Consumer<Player?>?): CustomForm {
        return super.onClose(closed) as CustomForm
    }

    override fun send(player: Player): CustomForm {
        return super.send(player) as CustomForm
    }

    override fun send(player: Player, id: Int): CustomForm {
        return super.send(player, id) as CustomForm
    }

    override fun sendUpdate(player: Player): CustomForm {
        return super.sendUpdate(player) as CustomForm
    }

    override fun windowType(): String {
        return "custom_form"
    }

    override fun toJson(): String {
        val `object` = JsonObject()
        `object`.addProperty("type", this.windowType())
        `object`.addProperty("title", this.title)

        val elementArray = JsonArray()
        this.elements.forEach(Consumer { element: ElementCustom -> elementArray.add(element.toJson()) })

        `object`.add("content", elementArray)
        return `object`.toString()
    }

    override fun respond(player: Player, formData: String): CustomResponse? {
        if (!super.handle(player, formData)) {
            this.supplyClosed(player)
            return null
        }

        val response = CustomResponse()

        val elementResponses = Json.parseToJsonElement(formData).jsonArray.map { it.jsonPrimitive.content }

        var i = 0
        val responseSize = elementResponses.size
        while (i < responseSize) {
            if (i >= elements.size) {
                break
            }

            val responseData = elementResponses[i]
            val element = elements[i]

            var elementResponse: Any? = null
            when (element) {
                is ElementDropdown -> {
                    val index = responseData.toInt()
                    val option: String = element.options[index]
                    elementResponse = ElementResponse(index, option)
                }

                is ElementInput -> elementResponse = responseData
                is ElementLabel -> elementResponse = element.text
                is ElementSlider -> elementResponse = responseData.toFloat()
                is ElementStepSlider -> {
                    val index = Integer.parseInt(responseData)
                    val step: String = element.steps[index]
                    elementResponse = ElementResponse(index, step)
                }

                is ElementToggle -> elementResponse = responseData.toBoolean()
                else -> Unit
            }

            response.setResponse(i, elementResponse)
            i++
        }

        this.supplySubmitted(player, response)
        return response
    }

    override fun <M : Any> putMeta(key: String, `object`: M): CustomForm {
        return super.putMeta(key, `object`) as CustomForm
    }
}
