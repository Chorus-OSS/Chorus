package org.chorus_oss.chorus.form.element.simple

import com.google.gson.JsonObject
import org.chorus_oss.chorus.form.element.Element


/**
 * The button object used to add buttons within [org.chorus_oss.chorus.form.window.SimpleForm]
 */
class ElementButton(
    /**
     * The button text shown
     */
    var text: String? = null,

    /**
     * An optional image to send with the button
     */
    var image: ButtonImage? = null,
) : Element() {
    fun text(text: String?): ElementButton {
        this.text = text
        return this
    }

    fun image(image: ButtonImage?): ElementButton {
        this.image = image
        return this
    }

    @JvmOverloads
    constructor(text: String? = "") : this(text, null)

    override fun toJson(): JsonObject {
        `object`.addProperty("text", this.text)

        if (this.image != null) {
            `object`.add("image", image!!.toJson())
        }

        return this.`object`
    }

    companion object {
        var EMPTY_LIST: Array<ElementButton?> = arrayOfNulls(0)
    }
}
