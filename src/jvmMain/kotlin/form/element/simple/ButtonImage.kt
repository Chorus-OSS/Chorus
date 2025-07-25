package org.chorus_oss.chorus.form.element.simple

import com.google.gson.JsonObject
import java.util.*


/**
 * The image of a [ElementButton]
 */
class ButtonImage(type: Type, data: String?) {
    protected val `object`: JsonObject = JsonObject()

    protected var type: Type? = null
    var data: String? = null

    init {
        this.type(type)
        this.data(data)
    }

    fun type(type: Type): ButtonImage {
        this.type = type
        `object`.addProperty("type", type.name.lowercase(Locale.getDefault()))
        return this
    }

    fun data(path: String?): ButtonImage {
        this.data = path
        `object`.addProperty("data", path)
        return this
    }

    /**
     * There are two types of images:
     * - PATH (image located inside a resource pack)
     * - URL (image accessed via the internet)
     */
    enum class Type {
        PATH,
        URL;

        fun of(path: String?): ButtonImage {
            return ButtonImage(this, path)
        }
    }

    fun toJson(): JsonObject {
        return this.`object`
    }
}
