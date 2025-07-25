package org.chorus_oss.chorus.form.response


/**
 * The response of a [org.chorus_oss.chorus.form.window.CustomForm]
 */
class CustomResponse : Response() {
    val responses: MutableMap<Int, Any?> = HashMap()

    /**
     * Set a response for an element (internal)
     *
     * @param index    The index of the response
     * @param response The value of the response
     * @param <T>      Any
    </T> */
    fun <T : Any?> setResponse(index: Int, response: T) {
        responses[index] = response
    }

    /**
     * Get an element's response by its index
     *
     * @param index The index of the element
     * @return The response corresponding to the index
     * @param <T> Any valid response
    </T> */
    fun <T : Any?> getResponse(index: Int): T {
        return responses[index] as T
    }

    /**
     * Get a dropdown's response
     *
     * @param index The index of the dropdown
     * @return The elementId and elementText of the dropdown's response
     */
    fun getDropdownResponse(index: Int): ElementResponse {
        return this.getResponse(index)
    }

    /**
     * Get an input's response
     *
     * @param index The index of the input
     * @return The input
     */
    fun getInputResponse(index: Int): String {
        return this.getResponse(index)
    }

    /**
     * Get a label
     *
     * @param index The index of the label
     * @return The label
     */
    fun getLabelResponse(index: Int): String {
        return this.getResponse(index)
    }

    /**
     * Get a slider's response
     *
     * @param index The index of the slider
     * @return The slider's float response
     */
    fun getSliderResponse(index: Int): Float {
        return this.getResponse(index)
    }

    /**
     * Get a step sliders's response
     *
     * @param index The index of the step slider
     * @return The elementId and elementText of the step slider's response
     */
    fun getStepSliderResponse(index: Int): ElementResponse {
        return this.getResponse(index)
    }

    /**
     * Get a toggle's response
     *
     * @param index The index of the toggle
     * @return Whether the toggle was turned on or not
     */
    fun getToggleResponse(index: Int): Boolean {
        return this.getResponse(index)
    }
}
