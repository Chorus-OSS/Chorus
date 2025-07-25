package org.chorus_oss.chorus.plugin

import java.lang.reflect.Method

interface CompiledExecutor {
    /**
     *
     * @return the method to be executed when fallback
     */
    val originMethod: Method?
}
