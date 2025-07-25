package org.chorus_oss.chorus.plugin

enum class PluginLoadOrder {
    /// Plugin will be loaded at startup.
    Startup,

    /// Plugin will be loaded after the first/default world was created.
    PostWorld
}
