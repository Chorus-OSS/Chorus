package org.chorus_oss.chorus.level.updater

import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext

interface Updater {
    fun registerUpdaters(context: CompoundTagUpdaterContext)
}
