package org.chorus_oss.chorus.form.response


/**
 * The response of a [org.chorus_oss.chorus.form.window.ModalForm]
 */
class ModalResponse(
    /**
     * The ordinal of the selected option:
     * -1 if invalid
     * 0 if accepted
     * 1 if rejected
     */
    var buttonId: Int = -1,
    var yes: Boolean = false,
) : Response()
