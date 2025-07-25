package org.chorus_oss.chorus.form.element.custom

import com.google.gson.JsonObject

class ElementLabel(val text: String) : ElementCustom() {
    constructor() : this("")

    override fun toJson(): JsonObject {
        `object`.addProperty("type", "label")
        `object`.addProperty("text", this.text)

        return this.`object`
    }
}
