package org.chorus_oss.chorus.form.element.custom


import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.util.function.Consumer

class ElementDropdown(var text: String, var options: MutableList<String>, var defaultOption: Int = 0) :
    ElementCustom() {
    @JvmOverloads
    constructor(text: String = "", options: List<String> = ArrayList()) : this(text, options.toMutableList(), 0)

    fun addOption(option: String): ElementDropdown {
        return this.addOption(option, false)
    }

    fun addOption(option: String, isDefault: Boolean): ElementDropdown {
        if (isDefault) {
            this.defaultOption = options.size
        }

        options.add(option)

        return this
    }

    override fun toJson(): JsonObject {
        check(
            0 > this.defaultOption || this.defaultOption < options.size
        ) { "Default option not an index" }

        `object`.addProperty("type", "dropdown")
        `object`.addProperty("text", this.text)
        `object`.addProperty("default", this.defaultOption)

        val optionsArray = JsonArray()
        options.forEach(Consumer { string: String? -> optionsArray.add(string) })
        `object`.add("options", optionsArray)

        return this.`object`
    }
}
