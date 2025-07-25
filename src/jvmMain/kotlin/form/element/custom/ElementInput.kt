package org.chorus_oss.chorus.form.element.custom

import com.google.gson.JsonObject

class ElementInput(var text: String? = null, var placeholder: String? = null, var defaultText: String? = null) :
    ElementCustom() {
    @JvmOverloads
    constructor(text: String? = "", placeholder: String? = "") : this(text, placeholder, "")

    override fun toJson(): JsonObject {
        `object`.addProperty("type", "input")
        `object`.addProperty("text", this.text)
        `object`.addProperty("placeholder", this.placeholder)
        `object`.addProperty("default", this.defaultText)

        return this.`object`
    }
}
