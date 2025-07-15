package org.chorus_oss.chorus.entity.mob

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.NPCCommandSender
import org.chorus_oss.chorus.dialog.element.ElementDialogButton
import org.chorus_oss.chorus.dialog.window.FormWindowDialog
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.EntityInteractable
import org.chorus_oss.chorus.entity.ai.behavior.Behavior
import org.chorus_oss.chorus.entity.ai.behavior.IBehavior
import org.chorus_oss.chorus.entity.ai.behaviorgroup.BehaviorGroup
import org.chorus_oss.chorus.entity.ai.behaviorgroup.IBehaviorGroup
import org.chorus_oss.chorus.entity.ai.controller.IController
import org.chorus_oss.chorus.entity.ai.controller.LookController
import org.chorus_oss.chorus.entity.ai.evaluator.ProbabilityEvaluator
import org.chorus_oss.chorus.entity.ai.executor.LookAtTargetExecutor
import org.chorus_oss.chorus.entity.ai.memory.CoreMemoryTypes
import org.chorus_oss.chorus.entity.ai.sensor.ISensor
import org.chorus_oss.chorus.entity.ai.sensor.NearestPlayerSensor
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.StringTag
import org.chorus_oss.protocol.packets.NPCRequestPacket

class EntityNPC(chunk: IChunk?, nbt: CompoundTag) : EntityMob(chunk, nbt), EntityInteractable {
    override fun getEntityIdentifier(): String {
        return EntityID.NPC
    }

    lateinit var dialog: FormWindowDialog

    init {
        nbt.putIfNull(TAG_RAWTEXT_NAME, StringTag("NPC"))
    }


    override fun getWidth(): Float {
        return 0.6f
    }

    override fun getHeight(): Float {
        return 2.1f
    }

    override fun canDoInteraction(): Boolean {
        return true
    }

    override fun getInteractButtonText(player: Player): String {
        return if (player.isCreative) "action.interact.edit" else "action.interact.talk"
    }

    override fun getOriginalName(): String {
        return "NPC"
    }

    override fun canCollide(): Boolean {
        return false
    }

    override fun requireBehaviorGroup(): IBehaviorGroup {
        return BehaviorGroup(
            this.tickSpread,
            setOf<IBehavior>(),
            setOf<IBehavior>(
                Behavior(
                    LookAtTargetExecutor(CoreMemoryTypes.NEAREST_PLAYER, 100),
                    ProbabilityEvaluator(2, 100)
                )
            ),
            setOf<ISensor>(NearestPlayerSensor(6.0, 0.0, 20)),
            setOf<IController>(LookController(true, false)),
            null,
            this
        )
    }

    override fun getExperienceDrops(): Int {
        return 0
    }

    public override fun initEntity() {
        super.initEntity()
        this.setMaxHealth(Int.MAX_VALUE) // Should be Float max value
        this.setHealthSafe(20f)
        this.setNameTagVisible(true)
        this.setNameTagAlwaysVisible(true)
        this.setMovementSpeedF(0.5f)

        this.dialog = FormWindowDialog(
            if (namedTag.contains(TAG_RAWTEXT_NAME)) namedTag.getString(TAG_RAWTEXT_NAME) else this.getNameTag(),
            if (namedTag.contains(TAG_INTERACTIVE_TEXT)) namedTag.getString(TAG_INTERACTIVE_TEXT) else "",
            this
        )

        if (!namedTag.getString(TAG_ACTIONS).isEmpty()) dialog.buttonJSONData =
            namedTag.getString(TAG_ACTIONS)

        dialog.addHandler { player, response ->
            when (response.requestType) {
                NPCRequestPacket.Companion.RequestType.SetActions -> {
                    if (!response.data.isEmpty()) {
                        this.dialog.buttonJSONData = response.data
                        this.setDataProperty(EntityDataTypes.ACTIONS, response.data)
                    }
                }

                NPCRequestPacket.Companion.RequestType.SetInteractText -> {
                    this.dialog.content = response.data
                    this.setDataProperty(EntityDataTypes.INTERACT_TEXT, response.data)
                }

                NPCRequestPacket.Companion.RequestType.SetName -> {
                    this.dialog.title = response.data
                    this.setNameTag(response.data)
                }

                NPCRequestPacket.Companion.RequestType.SetSkin -> {
                    this.variant = response.skinType
                }

                NPCRequestPacket.Companion.RequestType.ExecuteAction -> {
                    val clickedButton = response.clickedButton
                    if (clickedButton != null) {
                        for (line in clickedButton.getData()) {
                            Server.instance.executeCommand(NPCCommandSender(this, player), line.cmdLine)
                        }
                    }
                }

                NPCRequestPacket.Companion.RequestType.ExecuteOpeningCommands -> {
                    for (button in this.dialog.getButtons()) {
                        if (button.getMode() == ElementDialogButton.Mode.ON_ENTER) {
                            for (line in button.getData()) {
                                Server.instance.executeCommand(NPCCommandSender(this, player), line.cmdLine)
                            }
                        }
                    }
                }

                NPCRequestPacket.Companion.RequestType.ExecuteClosingCommands -> {
                    for (button in this.dialog.getButtons()) {
                        if (button.getMode() == ElementDialogButton.Mode.ON_EXIT) {
                            for (line in button.getData()) {
                                Server.instance.executeCommand(NPCCommandSender(this, player), line.cmdLine)
                            }
                        }
                    }
                }
            }
        }
        dialog.bindEntity = this
    }

    override fun saveNBT() {
        super.saveNBT()

        namedTag.putString(
            TAG_RAWTEXT_NAME,
            dialog.title!!
        )
        namedTag.putString(
            TAG_INTERACTIVE_TEXT,
            dialog.content!!
        )
        namedTag.putString(
            TAG_ACTIONS,
            dialog.buttonJSONData!!
        )
    }

    override fun onInteract(player: Player, item: Item, clickedPos: Vector3): Boolean {
        // For creative mode players, the NPC's dialog sent must have an empty sceneName; otherwise, the client will not allow the dialog box content to be modified.
        // Additionally, we do not need to record the dialog box sent to creative mode players. Firstly, because we cannot clear it, and secondly, there is no need to do so.
        player.showDialogWindow(this.dialog, !player.isCreative)
        return false
    }

    override fun kill() {
        this.health = 0f
        this.scheduleUpdate()

        for (passenger: Entity? in ArrayList(this.passengers)) {
            dismountEntity(passenger!!)
        }
    }

    override fun attack(source: EntityDamageEvent): Boolean {
        if (source is EntityDamageByEntityEvent && source.damager is Player && source.damager.isCreative) {
            this.kill()
        }
        return false
    }

    companion object {
        const val TAG_ACTIONS: String = "Actions"
        const val TAG_INTERACTIVE_TEXT: String = "InterativeText"
        const val TAG_PLAYER_SCENE_MAPPING: String = "PlayerSceneMapping"
        const val TAG_RAWTEXT_NAME: String = "RawtextName"
    }
}
