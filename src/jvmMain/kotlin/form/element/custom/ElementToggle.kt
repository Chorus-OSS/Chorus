package org.chorus_oss.chorus.form.element.custom

import com.google.gson.JsonObject

class ElementToggle(
    var text: String? = null,
    var defaultValue: Boolean = false,
) : ElementCustom() {
    @JvmOverloads
    constructor(text: String? = "") : this(text, false)

    override fun toJson(): JsonObject {
        `object`.addProperty("type", "toggle")
        `object`.addProperty("text", this.text)
        `object`.addProperty("default", this.defaultValue)

        return this.`object`
    }
}
