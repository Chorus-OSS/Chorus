package org.chorus_oss.chorus.form.element.custom


import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.util.function.Consumer

class ElementStepSlider(
    var text: String = "",
    var steps: MutableList<String> = mutableListOf(),
    var defaultStep: Int = 0,
) : ElementCustom() {
    @JvmOverloads
    constructor(text: String = "", steps: List<String> = ArrayList()) : this(text, steps.toMutableList(), 0)

    fun addStep(step: String): ElementStepSlider {
        return this.addStep(step, false)
    }

    fun addStep(step: String, isDefault: Boolean): ElementStepSlider {
        if (isDefault) {
            this.defaultStep = steps.size
        }

        steps.add(step)

        return this
    }

    override fun toJson(): JsonObject {
        check(
            this.defaultStep > -1 && this.defaultStep < steps.size
        ) { "Default option not within range" }

        `object`.addProperty("type", "step_slider")
        `object`.addProperty("text", this.text)
        `object`.addProperty("default", this.defaultStep)

        val stepsArray = JsonArray()
        steps.forEach(Consumer { string: String? -> stepsArray.add(string) })
        `object`.add("steps", stepsArray)

        return this.`object`
    }
}
