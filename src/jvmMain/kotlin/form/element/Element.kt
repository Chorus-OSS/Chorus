package org.chorus_oss.chorus.form.element

import com.google.gson.JsonObject

/**
 * The element base class, featuring serializable utilities
 */
abstract class Element {
    /**
     * The object used to add an element parameters
     * Since a form might be updated by the code, we do not recreate the object everytime.
     */
    protected var `object`: JsonObject = JsonObject()

    /**
     * @return Serialized element for the Minecraft client
     */
    abstract fun toJson(): JsonObject?
}
