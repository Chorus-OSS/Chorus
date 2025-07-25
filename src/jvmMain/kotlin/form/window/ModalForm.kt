package org.chorus_oss.chorus.form.window

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.form.response.ModalResponse


import java.util.function.BiConsumer
import java.util.function.Consumer

class ModalForm : Form<ModalResponse?> {
    var content: String = ""


    protected var yes: Consumer<Player> =
        Consumer { player: Player? -> }


    protected var no: Consumer<Player> =
        Consumer { player: Player? -> }

    protected var yesText: String = ""
    protected var noText: String = ""

    constructor(title: String) : super(title)

    constructor(title: String, content: String) : super(title) {
        this.content = content
    }


    override fun title(title: String): ModalForm {
        return super.title(title) as ModalForm
    }

    fun text(yes: String, no: String): ModalForm {
        this.yesText = yes
        this.noText = no
        return this
    }

    fun onYes(yes: Consumer<Player>): ModalForm {
        this.yes = yes
        return this
    }

    fun onNo(no: Consumer<Player>): ModalForm {
        this.no = no
        return this
    }

    fun yes(text: String, yes: Consumer<Player>): ModalForm {
        this.yesText = text
        this.yes = yes
        return this
    }

    fun no(text: String, no: Consumer<Player>): ModalForm {
        this.noText = text
        this.no = no
        return this
    }

    fun supplyYes(player: Player) {
        yes.accept(player)
    }

    fun supplyNo(player: Player) {
        no.accept(player)
    }

    override fun onSubmit(submitted: BiConsumer<Player?, ModalResponse?>?): ModalForm {
        return super.onSubmit(submitted) as ModalForm
    }

    override fun onClose(closed: Consumer<Player?>?): ModalForm {
        return super.onClose(closed) as ModalForm
    }

    override fun send(player: Player): ModalForm {
        return super.send(player) as ModalForm
    }

    override fun send(player: Player, id: Int): ModalForm {
        return super.send(player, id) as ModalForm
    }

    override fun sendUpdate(player: Player): ModalForm {
        return super.sendUpdate(player) as ModalForm
    }

    override fun windowType(): String {
        return "modal"
    }

    override fun toJson(): String {
        `object`.addProperty("type", this.windowType())
        `object`.addProperty("title", this.title)
        `object`.addProperty("content", this.content)
        `object`.addProperty("button1", this.yesText)
        `object`.addProperty("button2", this.noText)

        return `object`.toString()
    }

    override fun respond(player: Player, formData: String): ModalResponse? {
        val yes: Boolean
        if (!super.handle(player, formData)) {
            this.supplyClosed(player)
            return null
        } else yes = formData.trim { it <= ' ' } == "true"

        if (yes) this.supplyYes(player)
        else this.supplyNo(player)

        val response = ModalResponse(if (yes) 0 else 1, yes)
        this.supplySubmitted(player, response)
        return response
    }

    override fun <M : Any> putMeta(key: String, `object`: M): ModalForm {
        return super.putMeta(key, `object`) as ModalForm
    }
}
