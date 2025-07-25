package org.chorus_oss.chorus.form.window


import com.google.gson.JsonObject
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.form.response.Response
import java.util.function.BiConsumer
import java.util.function.Consumer

/**
 * Abstract class used to dynamically generate and send forms to players.
 *
 * @param <T> A response object
</T> */
abstract class Form<T : Response?>(title: String) {
    protected val `object`: JsonObject = JsonObject()
    protected val meta: MutableMap<String, Any> = HashMap()

    var viewers: MutableSet<Player> = HashSet()

    var title: String = ""


    protected var closed: Consumer<Player?>? =
        Consumer { player: Player? -> }


    protected var submitted: BiConsumer<Player?, T>? =
        BiConsumer { player: Player?, response: T -> }


    protected var response: T? = null

    init {
        this.title = title
    }

    open fun title(title: String): Form<T> {
        this.title = title
        return this
    }

    /**
     * Internally used to accept the consumer to execute when the form was closed
     * @param player The player who closed the form
     */
    fun supplyClosed(player: Player?) {
        if (this.closed != null) closed!!.accept(player)
    }

    /**
     * Internally used to accept the consumer to execute when the form was submitted
     * @param player The player who submitted the form
     * @param data The data submitted by the player
     */
    fun supplySubmitted(player: Player?, data: T) {
        this.response = data
        if (this.submitted != null) submitted!!.accept(player, data)
    }

    /**
     * @param closed The consumer executed when a player closes the form
     * @return The form
     */
    open fun onClose(closed: Consumer<Player?>?): Form<T>? {
        this.closed = closed
        return this
    }

    /**
     * @param submitted The consumer executed when a player submits the form
     * @return The form
     */
    open fun onSubmit(submitted: BiConsumer<Player?, T>?): Form<T>? {
        this.submitted = submitted
        return this
    }

    /**
     * Sends the form to a player
     * @param player The player to send the form to
     * @return The form
     */
    open fun send(player: Player): Form<T>? {
        if (this.isViewer(player)) {
            return this
        }

        viewers.add(player)

        player.sendForm(this)
        return this
    }

    /**
     * Sends the form to a player
     * @param player The player to send the form to
     * @param id The ID to use internally for the player
     * @return The form
     */
    open fun send(player: Player, id: Int): Form<T>? {
        if (this.isViewer(player)) {
            return this
        }

        viewers.add(player)

        player.sendForm(this, id)
        return this
    }

    /**
     * Update the form while the player still has the form open
     * Not recommended for scrolling content
     *
     * @param player The player to send the update to
     * @return The form
     */
    open fun sendUpdate(player: Player): Form<T>? {
        if (!this.isViewer(player)) {
            this.send(player)
            return this
        }

        player.updateForm(this)
        return this
    }

    fun handle(player: Player, formData: String?): Boolean {
        viewers.remove(player)
        player.checkClosedForms()

        return formData != null && formData != "null"
    }

    abstract fun respond(player: Player, formData: String): Response?

    fun isViewer(player: Player): Boolean {
        return viewers.contains(player)
    }

    /**
     * Get the value of a key
     *
     * @param key The key
     * @return The value
     * @param <M> Any
    </M> */
    fun <M> getMeta(key: String): M? {
        return meta[key] as M?
    }

    /**
     * Get the value of a key
     *
     * @param key The key
     * @param defaultValue The default value
     * @return If present, the value. Otherwise, returns the default value
     * @param <M> Any
    </M> */
    fun <M> getMeta(key: String, defaultValue: M): M {
        val value = this.getMeta<Any>(key)
        return if (value == null) defaultValue else value as M
    }

    /**
     * Put data inside here, e.g. to identify which form has been opened
     *
     * @param key The key
     * @param object The value
     * @param <M> Any
    </M> */
    open fun <M : Any> putMeta(key: String, `object`: M): Form<*>? {
        meta[key] = `object`
        return this
    }

    abstract fun windowType(): String

    abstract fun toJson(): String
}
