package org.chorus_oss.chorus.plugin

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlInputConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import org.chorus_oss.chorus.permission.Permission
import org.chorus_oss.chorus.utils.PluginException

@Serializable
class PluginTOML(
    var name: String,
    /**
     * The main class name of the plugin.
     *
     * Eg: `com.example.ExamplePlugin`
     */
    var main: String,
    var version: String,
    var api: List<String>,
    var description: String? = null,
    var authors: MutableList<String> = mutableListOf(),
    var dependencies: MutableList<String> = mutableListOf(),
    @SerialName("soft-dependencies")
    var softDependencies: MutableList<String> = mutableListOf(),
    @SerialName("load-before")
    var loadBefore: List<String> = mutableListOf(),
    /**
     * Returns the message title of this plugin.
     *
     * When a PluginLogger logs, the message title is used as the prefix of message. If prefix is null,
     * the plugin name will be used instead.
     */
    var prefix: String? = null,
    var order: PluginLoadOrder = PluginLoadOrder.PostWorld,
    var permissions: List<Permission> = mutableListOf(),
) {
    init {
        // TODO: Move into custom Serializer
        this.name = name.replace("[^A-Za-z0-9 _.-]".toRegex(), "")
        if (this.name == "") {
            throw PluginException("Invalid PluginDescription name")
        }
        this.name = name.replace(" ", "_")

        if (main.startsWith("org.chorus_oss.chorus.") && (this.main != "org.chorus_oss.chorus.plugin.InternalPlugin")) {
            throw PluginException("Invalid PluginDescription main, cannot start within the org.chorus_oss.chorus. package")
        }
    }

    /**
     * Versioned name of a plugin is `${name} v${version}`.
     *
     * Eg: `HelloWorld v1.0.0`
     */
    val versionedName: String
        get() = "${this.name} v${this.version}"

    companion object {
        fun fromString(string: String): PluginTOML {
            return Toml(
                inputConfig = TomlInputConfig(ignoreUnknownNames = true)
            ).decodeFromString<PluginTOML>(string)
        }
    }
}
