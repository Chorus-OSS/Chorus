package org.chorus_oss.chorus.form.element.custom

import com.google.common.base.Preconditions
import com.google.gson.JsonObject

class ElementSlider(
    var text: String? = null,
    var min: Float = 0f,
    var max: Float = 0f,
    var step: Int = 0,
    var defaultValue: Float = 0f,
) : ElementCustom() {
    @JvmOverloads
    constructor(text: String? = "", min: Float = 1f, max: Float = min.coerceAtLeast(100f), step: Int = 1) : this(
        text,
        min,
        max,
        step,
        1f
    )

    override fun toJson(): JsonObject {
        check(this.min < this.max) { "Maximum slider value must exceed the minimum value" }
        check(
            this.defaultValue >= this.min && this.defaultValue <= this.max
        ) { "Default value out of range" }

        `object`.addProperty("type", "slider")
        `object`.addProperty("text", this.text)
        `object`.addProperty("min", this.min)
        `object`.addProperty("max", this.max)
        `object`.addProperty("step", this.step)
        `object`.addProperty("default", this.defaultValue)

        return this.`object`
    }
}
