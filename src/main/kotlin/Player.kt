package org.chorus_oss.chorus

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.google.common.base.Preconditions
import com.google.common.base.Strings
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.common.collect.Sets
import io.netty.util.internal.PlatformDependent
import kotlinx.io.bytestring.ByteString
import org.chorus_oss.chorus.block.*
import org.chorus_oss.chorus.block.customblock.CustomBlock
import org.chorus_oss.chorus.block.property.CommonBlockProperties
import org.chorus_oss.chorus.block.property.type.IntPropertyType
import org.chorus_oss.chorus.blockentity.BlockEntitySign
import org.chorus_oss.chorus.blockentity.BlockEntitySpawnable
import org.chorus_oss.chorus.camera.data.CameraPreset.Companion.presets
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.utils.RawText
import org.chorus_oss.chorus.dialog.window.FormWindowDialog
import org.chorus_oss.chorus.entity.*
import org.chorus_oss.chorus.entity.Attribute.Companion.getAttribute
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.data.EntityFlag
import org.chorus_oss.chorus.entity.data.PlayerFlag
import org.chorus_oss.chorus.entity.data.Skin
import org.chorus_oss.chorus.entity.effect.EffectType
import org.chorus_oss.chorus.entity.item.EntityFishingHook
import org.chorus_oss.chorus.entity.item.EntityItem
import org.chorus_oss.chorus.entity.item.EntityXpOrb
import org.chorus_oss.chorus.entity.mob.animal.EntityHorse
import org.chorus_oss.chorus.entity.mob.monster.EntityBoss
import org.chorus_oss.chorus.entity.projectile.EntityProjectile
import org.chorus_oss.chorus.entity.projectile.abstract_arrow.EntityArrow
import org.chorus_oss.chorus.entity.projectile.abstract_arrow.EntityThrownTrident
import org.chorus_oss.chorus.event.block.WaterFrostEvent
import org.chorus_oss.chorus.event.entity.*
import org.chorus_oss.chorus.event.entity.EntityDamageEvent.DamageCause
import org.chorus_oss.chorus.event.entity.EntityPortalEnterEvent.PortalType
import org.chorus_oss.chorus.event.inventory.InventoryPickupArrowEvent
import org.chorus_oss.chorus.event.inventory.InventoryPickupItemEvent
import org.chorus_oss.chorus.event.inventory.InventoryPickupTridentEvent
import org.chorus_oss.chorus.event.player.*
import org.chorus_oss.chorus.event.player.PlayerTeleportEvent.TeleportCause
import org.chorus_oss.chorus.event.server.DataPacketSendEvent
import org.chorus_oss.chorus.experimental.network.MigrationPacket
import org.chorus_oss.chorus.experimental.network.protocol.utils.FLAG_ALL_PRIORITY
import org.chorus_oss.chorus.experimental.network.protocol.utils.invoke
import org.chorus_oss.chorus.form.window.Form
import org.chorus_oss.chorus.inventory.*
import org.chorus_oss.chorus.item.*
import org.chorus_oss.chorus.item.enchantment.Enchantment
import org.chorus_oss.chorus.lang.CommandOutputContainer
import org.chorus_oss.chorus.lang.LangCode
import org.chorus_oss.chorus.lang.LangCode.Companion.from
import org.chorus_oss.chorus.lang.TextContainer
import org.chorus_oss.chorus.lang.TranslationContainer
import org.chorus_oss.chorus.level.*
import org.chorus_oss.chorus.level.GameRule
import org.chorus_oss.chorus.level.Level.Companion.chunkHash
import org.chorus_oss.chorus.level.Level.Companion.generateChunkLoaderId
import org.chorus_oss.chorus.level.Level.Companion.getHashX
import org.chorus_oss.chorus.level.Level.Companion.getHashZ
import org.chorus_oss.chorus.level.Locator.Companion.fromObject
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.level.particle.PunchBlockParticle
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationType
import org.chorus_oss.chorus.math.*
import org.chorus_oss.chorus.nbt.tag.*
import org.chorus_oss.chorus.network.DataPacket
import org.chorus_oss.chorus.network.connection.BedrockDisconnectReasons
import org.chorus_oss.chorus.network.connection.BedrockSession
import org.chorus_oss.chorus.network.process.SessionState
import org.chorus_oss.chorus.network.protocol.LevelEventPacket
import org.chorus_oss.chorus.network.protocol.types.GameType
import org.chorus_oss.chorus.network.protocol.types.PlayerInfo
import org.chorus_oss.chorus.network.protocol.types.SpawnPointType
import org.chorus_oss.chorus.permission.PermissibleBase
import org.chorus_oss.chorus.permission.Permission
import org.chorus_oss.chorus.permission.PermissionAttachment
import org.chorus_oss.chorus.plugin.InternalPlugin
import org.chorus_oss.chorus.plugin.Plugin
import org.chorus_oss.chorus.scheduler.AsyncTask
import org.chorus_oss.chorus.scheduler.Task
import org.chorus_oss.chorus.scheduler.TaskHandler
import org.chorus_oss.chorus.scoreboard.IScoreboard
import org.chorus_oss.chorus.scoreboard.IScoreboardLine
import org.chorus_oss.chorus.scoreboard.data.DisplaySlot
import org.chorus_oss.chorus.scoreboard.displayer.IScoreboardViewer
import org.chorus_oss.chorus.scoreboard.scorer.PlayerScorer
import org.chorus_oss.chorus.utils.*
import org.chorus_oss.chorus.utils.PortalHelper.moveToTheEnd
import org.chorus_oss.chorus.utils.TextFormat.Companion.clean
import org.chorus_oss.protocol.core.Packet
import org.chorus_oss.protocol.packets.*
import org.chorus_oss.protocol.types.*
import org.chorus_oss.protocol.types.Vector3f
import org.chorus_oss.protocol.types.camera.preset.CameraPreset
import org.chorus_oss.protocol.types.scoreboard.ScoreboardRemoveEntry
import org.chorus_oss.protocol.types.scoreboard.ScoreboardSlot
import org.chorus_oss.protocol.types.scoreboard.ScoreboardSlotOrder
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.UnmodifiableView
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer
import kotlin.concurrent.Volatile
import kotlin.math.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Game player object, representing the controlled character
 */
open class Player(
    val session: BedrockSession,
    val playerInfo: PlayerInfo
) :
    EntityHuman(null, CompoundTag()), CommandSender, ChunkLoader, IPlayer, IScoreboardViewer, Loggable {
    var abilities: EntityAbilities = EntityAbilities()
    var timer: EntityTimer = EntityTimer()
    var agentId: Long = 0L
    var dimensionId: Int = 0
    var enchantmentSeed: Int = 0
    var fogCommandStack: List<String> = ArrayList()
    var formatVersion: String = ""

    override val senderName
        get() = getEntityName()

    //    @NotNull public Boolean hasSeenCredits = false;
    //    @NotNull public List<EntityItemSlot> inventory = new ArrayList<>();
    var leftShoulderRiderID: Long = 0L
    var mapIndex: Int = 0
    var playerGameMode: Int = 0
    var playerLevel: Int = 0
    var playerLevelProgress: Float = 0.0f
    var playerUIItems: List<EntityItemSlot> = ArrayList()
    var recipeUnlocking: CompoundTag = CompoundTag()
    var RideID: Long = 0L
    var rightShoulderRiderID: Long = 0L
    var selectedContainerId: Int = 0
    var selectedInventorySlot: Int = 0

    //    @NotNull public Boolean sleeping = false;
    var sleepTimer: Short = 0

    //    var sneaking: Boolean = false
    var spawnBlockPositionX: Int = 0
    var spawnBlockPositionY: Int = 0
    var spawnBlockPositionZ: Int = 0
    var spawnDimension: Int = 0
    var spawnX: Int = 0
    var spawnY: Int = 0
    var spawnZ: Int = 0

    //    @NotNull public Integer timeSinceRest = 0;
    var wardenThreatDecreaseTimer: Int = 0
    var wardenThreatLevel: Int = 0
    var wardenThreatLevelIncreaseCooldown: Int = 0


    /** static fields */
    var playedBefore: Boolean = false
    var spawned: Boolean = false

    @Volatile
    var locallyInitialized: Boolean = false
    var loggedIn: Boolean = false
    val achievements: HashSet<String?> = HashSet()

    /**
     * 得到gamemode。
     *
     *
     * Get gamemode.
     *
     * @return int
     */
    @JvmField
    var gamemode: Int
    var lastBreak: Long
    var speed: Vector3? = null
    var creationTime: Long = 0

    /**
     * 正在挖掘的方块
     *
     *
     * block being dig
     */
    var breakingBlock: Block? = null

    /**
     * 正在挖掘的方向
     *
     *
     * direction of dig
     */
    var breakingBlockFace: BlockFace? = null
    var pickedXPOrb: Int = 0
    var fishing: EntityFishingHook? = null
    var lastSkinChange: Long
    var breakingBlockTime: Long = 0
    var blockBreakProgress: Double = 0.0

    /**
     * 得到原始套接字地址
     *
     * @return [InetSocketAddress]
     */
    val rawSocketAddress: InetSocketAddress?
    val hiddenPlayers: MutableMap<UUID, Player> = HashMap()
    val chunkSendCountPerTick: Int
    val spawnThreshold: Int
    var messageLimitCounter: Int = 2
    var connected: AtomicBoolean = AtomicBoolean(true)

    /**
     * 得到套接字地址,如果开启waterdogpe兼容，该套接字地址是被修改为兼容waterdogpe型的，反正则与[.rawSocketAddress] 一样
     *
     *
     * If waterdogpe compatibility is enabled, the address is modified to be waterdogpe compatible, otherwise it is the same as [.rawSocketAddress]
     *
     * @return [InetSocketAddress]
     */
    var socketAddress: InetSocketAddress?
    /**
     * 得到[.removeFormat]
     *
     *
     * get [.removeFormat]
     *
     * @return boolean
     */
    /**
     * 设置[.removeFormat]为指定值
     *
     * @param remove 是否清楚格式化字符<br></br>Whether remove the formatting character
     */
    /**
     * 是否移除改玩家聊天中的颜色字符如 §c §1
     *
     *
     * Whether to remove the color character in the chat of the changed player as §c §1
     */
    var removeFormat: Boolean = true
    var displayName: String = ""
        set(value) {
            field = value
            if (this.spawned) {
                Server.instance.updatePlayerListData(
                    getUUID(), this.getRuntimeID(), field,
                    skin,
                    loginChainData.xuid
                )
            }
        }
    var sleeping: Vector3? = null
    var chunkLoadCount: Int = 0
    var nextChunkOrderRun: Int = 1
    var newPosition: Vector3? = null
    var chunkRadius: Int

    @JvmField
    var viewDistance: Int
    var spawnPoint: Locator?
    protected var spawnPointType: SpawnPointType? = null
    /**
     * @return [.inAirTicks]
     */
    /**
     * 代表玩家悬浮空中所经过的tick数.
     *
     *
     * Represents the number of ticks the player has passed through the air.
     */
    var inAirTicks: Int = 0
    var startAirTicks: Int = 5
    var adventureSettings: AdventureSettings = AdventureSettings(this)
        set(value) {
            field = value.clone()
            field.update()
        }

    /**
     * @since 1.2.1.0-PN
     */
    var isCheckingMovement: Boolean = true

    /**
     * 获取玩家的[PlayerFood]
     *
     *
     * Get the player's [PlayerFood]
     *
     * @return the food data
     */
    var foodData: PlayerFood? = null
    protected var enableClientCommand: Boolean = true
        private set(value) {
            field = value
            session.setEnableClientCommand(value)
        }
    var formWindowCount: Int = 0
    var formWindows: MutableMap<Int, Form<*>> = HashMap()
    var serverSettings: MutableMap<Int, Form<*>> = HashMap()

    /**
     * 我们使用google的cache来存储NPC对话框发送信息
     * 原因是发送过去的对话框客户端有几率不响应，在特定情况下我们无法清除这些对话框，这会导致内存泄漏
     * 5分钟后未响应的对话框会被清除
     *
     *
     * We use Google's cache to store NPC dialogs to send messages
     * The reason is that there is a chance that the client will not respond to the dialogs sent, and in certain cases we cannot clear these dialogs, which can lead to memory leaks
     * Unresponsive dialogs will be cleared after 5 minutes
     */
    var dialogWindows: Cache<String, FormWindowDialog?> =
        Caffeine.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build()
    var dummyBossBars: MutableMap<Long, DummyBossBar?> = LinkedHashMap()
    var lastRightClickTime: Double = 0.0
    var lastRightClickPos: Vector3? = null

    /**
     * 返回[Player.lastInAirTick]的值,代表玩家上次在空中的server tick
     *
     *
     * Returns the value of [Player.lastInAirTick],represent the last server tick the player was in the air
     *
     * @return int
     */
    @JvmField
    var lastInAirTick: Int = 0
    private val clientMovements: Queue<Transform> = PlatformDependent.newMpscQueue(4)

    @get:Synchronized
    @set:Synchronized
    var locale: AtomicReference<Locale?> = AtomicReference(null)
    /**
     * How many ticks have passed since the player last sleeped, 1 tick = 0.05 s
     *
     * @return the ticks
     */
    /**
     * Set the timeSinceRest ticks
     *
     * @see .getTimeSinceRest
     */
    var timeSinceRest: Int = 0

    /**
     * 获得移动设备玩家面对载具时出现的交互按钮的语言硬编码。
     *
     *
     * Get the language hardcoded for the interaction buttons that appear when mobile device players face the carrier.
     */
    var buttonText: String = "Button"
        /**
         * 设置移动设备玩家面对载具时出现的交互按钮的语言硬编码。
         *
         *
         * Set the language hardcoded for the interaction buttons that appear when mobile device players face the carrier.
         */
        set(text) {
            field = text
            this.setDataProperty(EntityDataTypes.INTERACT_TEXT, field)
        }
    private var perm: PermissibleBase?
    private var hash = 0
    private var exp = 0

    /**
     * 得到该玩家的等级。
     *
     *
     * Get the level of the player.
     *
     * @return int
     */
    var experienceLevel: Int = 0
        private set
    var enchSeed = 0
    private var loadingScreenId = 0
    override val loaderId: Int = generateChunkLoaderId(this)
    private var lastBreakPosition = BlockVector3()
    private var hasSeenCredits = false
    private var wasInSoulSandCompatible = false

    /**
     * 返回灵魂急行带来的速度增加倍速
     *
     *
     * Return to the speed increase multiplier brought by SOUL_SPEED Enchantment
     */
    var soulSpeedMultiplier: Float = 1f
        private set

    /**
     * 获取击杀该玩家的实体
     *
     *
     * Get the entity that killed this player
     *
     * @return Entity | null
     */
    var killer: Entity? = null
        private set
    private var delayedPosTrackingUpdate: TaskHandler? = null
    var showingCredits: Boolean = false
        set(value) {
            field = value
            if (value) {
                val pk = ShowCreditsPacket(
                    playerRuntimeID = this.getRuntimeID().toULong(),
                    statusType = ShowCreditsPacket.Companion.StatusType.Start
                )
                this.sendPacket(pk)
            }
        }
    var lastBlockAction: PlayerBlockActionData? = null
    var preLoginEventTask: AsyncTask? = null

    /**
     * 获取该玩家的登录链数据
     *
     *
     * Get the login chain data of this player
     *
     * @return the login chain data
     */
    var loginChainData: LoginChainData

    /**
     * 玩家升级时播放音乐的时间
     *
     *
     * Time to play sound when player upgrades
     */
    var lastPlayerdLevelUpSoundTime: Int = 0
    /**
     * @return [.lastAttackEntity]
     */
    /**
     * 玩家最后攻击的实体.
     *
     *
     * The entity that the player attacked last.
     */
    var lastAttackEntity: Entity? = null

    /**
     * Gets fog stack.
     */
    /**
     * Set the fog stack, if you want to client effect,you need [.sendFogStack]
     *
     * @param fogStack the fog stack
     */
    /**
     * 玩家迷雾设置
     *
     *
     * Player Fog Settings
     */
    var fogStack: MutableList<Pair<String, String>> = mutableListOf()
    /**
     * @return [.lastBeAttackEntity]
     */
    /**
     * 最后攻击玩家的实体.
     *
     *
     * The entity that the player is attacked last.
     */
    var lastBeAttackEntity: Entity? = null
    val playerChunkManager: PlayerChunkManager

    /**
     * Set the status of the current player opening sign
     *
     * @param frontSide true means open sign front, vice versa. If it is null, it means that the player has not opened sign
     */
    var isOpenSignFront: Boolean? = null
    var isFlySneaking: Boolean = false

    /** lastUseItem System and item cooldown */
    protected val cooldownTickMap: HashMap<String, Int> = HashMap()
    protected val lastUseItemMap: HashMap<String, Int> = HashMap(1)

    /** */
    /** inventory system */
    protected var windowsCnt: Int = 1

    /**
     * 获取上一个关闭窗口对应的id
     *
     *
     * Get the id corresponding to the last closed window
     */
    var closingWindowId: Int = Int.MIN_VALUE
    val windows: BiMap<Inventory, Int> = HashBiMap.create()
    val windowIndex: BiMap<Int, Inventory> = windows.inverse()
    protected val permanentWindows: MutableSet<Int?> = HashSet()

    /**
     * 获取该玩家的[CraftingGridInventory]
     *
     *
     * Gets crafting grid of the player.
     */
    lateinit var craftingGrid: CraftingGridInventory
        protected set

    /**
     * 获取该玩家的[PlayerCursorInventory]
     *
     *
     * Gets cursor inventory of the player.
     */
    lateinit var cursorInventory: PlayerCursorInventory
        protected set
    lateinit var creativeOutputInventory: CreativeOutputInventory
        protected set

    /**
     * Player opens it own inventory
     */
    var inventoryOpen: Boolean = false

    /**
     * Player open it own ender chest inventory
     */
    var enderChestOpen: Boolean = false

    /** */
    /**todo hack for receive an error position after teleport */
    private var lastTeleportMessage: Pair<Transform, Long>? = null

    /**
     * Get the player info.
     */

    @OptIn(ExperimentalUuidApi::class)
    override var skin: Skin
        get() = super.skin
        set(value) {
            super.skin = value
            if (this.spawned) {
//            this.Server.instance.updatePlayerListData(this.getUniqueId(), this.getId(), this.getDisplayName(), skin, this.getLoginChainData().getXUID());
                val skinPacket = PlayerSkinPacket(
                    uuid = Uuid(this.getUUID()),
                    skin = org.chorus_oss.protocol.types.skin.Skin(this.skin),
                    newSkinName = skin.getSkinId(),
                    oldSkinName = "",
                    isTrusted = skin.isTrusted() || Server.instance.settings.playerSettings.forceSkinTrusted,
                )
                Server.broadcastPacket(Server.instance.onlinePlayers.values, skinPacket)
            }
        }

    init {
        this.perm = PermissibleBase(this)
        this.lastBreak = -1
        this.socketAddress = session.address
        this.rawSocketAddress = socketAddress
        this.chunkSendCountPerTick = Server.instance.settings.chunkSettings.perTickSend
        this.spawnThreshold = Server.instance.settings.chunkSettings.spawnThreshold
        this.spawnPoint = null
        this.gamemode = Server.instance.gamemode
        this.level = Server.instance.defaultLevel
        this.viewDistance = Server.instance.viewDistance
        this.chunkRadius = viewDistance
        this.boundingBox = SimpleAxisAlignedBB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        this.lastSkinChange = -1
        this.playerChunkManager = PlayerChunkManager(this)
        this.creationTime = System.currentTimeMillis()
        this.displayName = playerInfo.username
        this.loginChainData = playerInfo.data
        this.uuid = playerInfo.uuid
        this.skin = (playerInfo.skin)
    }

    fun onBlockBreakContinue(pos: Vector3, face: BlockFace?) {
        if (this.isBreakingBlock()) {
            val time = System.currentTimeMillis()
            val block = level!!.getBlock(pos, false)
            val tempBreakingBlock = breakingBlock
            val miningTimeRequired = if (tempBreakingBlock is CustomBlock) {
                tempBreakingBlock.breakTime(inventory.itemInHand, this)
            } else breakingBlock!!.calculateBreakTime(
                inventory.itemInHand,
                this
            )

            if (miningTimeRequired > 0) {
                val breakTick = ceil(miningTimeRequired * 20).toInt()
                val pk = LevelEventPacket()
                pk.evid = LevelEventPacket.EVENT_BLOCK_UPDATE_BREAK
                pk.x = breakingBlock!!.position.x.toFloat()
                pk.y = breakingBlock!!.position.y.toFloat()
                pk.z = breakingBlock!!.position.z.toFloat()
                pk.data = 65535 / breakTick
                level!!.addChunkPacket(
                    breakingBlock!!.position.floorX shr 4,
                    breakingBlock!!.position.floorZ shr 4, pk
                )
                level!!.addParticle(PunchBlockParticle(pos, block))
                if (breakingBlock is CustomBlock) {
                    val timeDiff = time - breakingBlockTime
                    blockBreakProgress += timeDiff / (miningTimeRequired * 1000)
                    if (blockBreakProgress > 0.99) {
                        this.onBlockBreakAbort(pos)
                        this.onBlockBreakComplete(pos.asBlockVector3(), face)
                    }
                    breakingBlockTime = time
                }
            }
        }
    }

    fun onBlockBreakStart(pos: Vector3, face: BlockFace) {
        val blockPos = pos.asBlockVector3()
        val currentBreak = System.currentTimeMillis()
        // HACK: Client spams multiple left clicks so we need to skip them.
        if ((lastBreakPosition.equals(blockPos) && (currentBreak - this.lastBreak) < 10) || pos.distanceSquared(this.position) > 1000) {
            return
        }

        val target = level!!.getBlock(pos)
        val playerInteractEvent = PlayerInteractEvent(
            this,
            inventory.itemInHand, target.position, face,
            if (target.isAir) PlayerInteractEvent.Action.LEFT_CLICK_AIR else PlayerInteractEvent.Action.LEFT_CLICK_BLOCK
        )
        Server.instance.pluginManager.callEvent(playerInteractEvent)
        if (playerInteractEvent.cancelled) {
            inventory.sendHeldItem(this)
            level!!.sendBlocks(
                arrayOf(this),
                arrayOf<Block?>(target),
                UpdateBlockPacket.FLAG_ALL_PRIORITY,
                false
            )
            if (target.getLevelBlockAtLayer(1) is BlockLiquid) {
                level!!.sendBlocks(
                    arrayOf(this), arrayOf(
                        target.getLevelBlockAtLayer(1)
                    ), UpdateBlockPacket.FLAG_ALL_PRIORITY, 1
                )
            }
            return
        }

        target.onTouch(
            pos,
            inventory.itemInHand, face, 0f, 0f, 0f, this, playerInteractEvent.action
        )

        val block = target.getSide(face)
        if (block.id == BlockID.FIRE || block.id == BlockID.SOUL_FIRE) {
            level!!.setBlock(block.position, Block.get(BlockID.AIR), true)
            level!!.addLevelSoundEvent(
                block.position,
                LevelSoundEventPacket.Companion.SoundType.ExtinguishFire
            )
            return
        }

        if (block.id == BlockID.SWEET_BERRY_BUSH && block.isDefaultState) {
            val oldItem = playerInteractEvent.item
            val i =
                level!!.useBreakOn(block.position, oldItem, this, true)
            if (this.isSurvival || this.isAdventure) {
                foodData?.exhaust(0.005)
                if (i!! != oldItem || i.getCount() != oldItem.getCount()) {
                    inventory.setItemInHand(i)
                    inventory.sendHeldItem(viewers.values)
                }
            }
            return
        }

        val canChangeBlock = target.isBlockChangeAllowed(this)
        if (!canChangeBlock) {
            return
        }

        if (this.isSurvival || (this.isAdventure && canChangeBlock)) {
            this.breakingBlockTime = currentBreak
            val miningTimeRequired = if (target is CustomBlock) {
                target.breakTime(inventory.itemInHand, this)
            } else target.calculateBreakTime(inventory.itemInHand, this)
            val breakTime = ceil(miningTimeRequired * 20).toInt()
            if (breakTime > 0) {
                val pk = LevelEventPacket()
                pk.evid = LevelEventPacket.EVENT_BLOCK_START_BREAK
                pk.x = pos.x.toFloat()
                pk.y = pos.y.toFloat()
                pk.z = pos.z.toFloat()
                pk.data = 65535 / breakTime
                level!!.addChunkPacket(pos.floorX shr 4, pos.floorZ shr 4, pk)

                if (level!!.isAntiXrayEnabled && level!!.antiXraySystem!!.isPreDeObfuscate) {
                    level!!.antiXraySystem!!.deObfuscateBlock(this, face, target)
                }
            }
        }

        this.breakingBlock = target
        this.breakingBlockFace = face
        this.lastBreak = currentBreak
        this.lastBreakPosition = blockPos
    }

    fun onBlockBreakAbort(pos: Vector3) {
        if (pos.distanceSquared(this.position) < 1000) { // same as with ACTION_START_BREAK
            val pk = LevelEventPacket()
            pk.evid = LevelEventPacket.EVENT_BLOCK_STOP_BREAK
            pk.x = pos.x.toFloat()
            pk.y = pos.y.toFloat()
            pk.z = pos.z.toFloat()
            pk.data = 0
            level!!.addChunkPacket(pos.floorX shr 4, pos.floorZ shr 4, pk)
        }
        this.blockBreakProgress = 0.0
        this.breakingBlock = null
        this.breakingBlockFace = null
    }

    fun onBlockBreakComplete(blockPos: BlockVector3, face: BlockFace?) {
        if (!this.spawned || !this.isAlive()) {
            return
        }

        var handItem: Item? = inventory.itemInHand
        val clone: Item = handItem!!.clone()

        val canInteract = this.canInteract(blockPos.add(0.5, 0.5, 0.5), (if (this.isCreative) 13 else 7).toDouble())
        if (canInteract) {
            handItem =
                level!!.useBreakOn(blockPos.asVector3(), face, handItem, this, true)
            if (handItem != null && this.isSurvival) {
                foodData?.exhaust(0.005)
                if (handItem.equals(clone) && handItem.getCount() == clone.getCount()) {
                    return
                }

                if (clone.id == handItem.id || handItem.isNothing) {
                    inventory.setItemInHand(handItem, false)
                } else {
                    log.debug("Tried to set item " + handItem.id + " but " + this.getEntityName() + " had item " + clone.id + " in their hand slot")
                }
                inventory.sendHeldItem(viewers.values)
            } else if (handItem == null) level!!.sendBlocks(
                arrayOf(this), arrayOf(
                    level!!.getBlock(blockPos.asVector3())
                ), UpdateBlockPacket.FLAG_ALL_PRIORITY, 0
            )
            return
        }

        inventory.sendContents(this)
        inventory.sendHeldItem(this)

        if (blockPos.distanceSquared(this.position) < 100) {
            val target = level!!.getBlock(blockPos.asVector3())
            level!!.sendBlocks(
                arrayOf(this),
                arrayOf<Block?>(target),
                UpdateBlockPacket.FLAG_ALL_PRIORITY
            )

            val blockEntity = level!!.getBlockEntity(blockPos.asVector3())
            if (blockEntity is BlockEntitySpawnable) {
                blockEntity.spawnTo(this)
            }
        }
    }

    private fun setTitle(text: String) {
        val packet = SetTitlePacket(
            actionType = SetTitlePacket.Companion.ActionType.SetTitle,
            text = text,
            fadeInDuration = 0,
            remainDuration = 0,
            fadeOutDuration = 0,
            xuid = "",
            platformOnlineID = "",
            filteredMessage = ""
        )
        this.sendPacket(packet)
    }

    //todo a lot on dimension
    private fun setDimension(dimension: Int) {
        this.sendPacket(
            ChangeDimensionPacket(
                dimension = dimension,
                position = Vector3f(position),
                respawn = false,
                loadingScreenID = null
            )
        )

        level!!.sendChunks(this)

        val playerActionPacket = PlayerActionPacket(
            entityRuntimeID = this.getRuntimeID().toULong(),
            actionType = PlayerActionType.ChangeDimensionACK,
            blockPosition = BlockPos(0, 0, 0),
            resultPosition = BlockPos(0, 0, 0),
            blockFace = 0,
        )
        this.sendPacket(playerActionPacket)
    }

    private fun updateBlockingFlag() {
        val shouldBlock = this.isItemCoolDownEnd("shield")
                && (this.isSneaking() || getRiding() != null)
                && (inventory.itemInHand is ItemShield || offhandInventory.getItem(0) is ItemShield)

        if (isBlocking() != shouldBlock) {
            this.setBlocking(shouldBlock)
        }
    }

    public override fun initEntity() {
        super.initEntity()
        this.uniqueId = this.uuid.leastSignificantBits

        val level = if (namedTag.containsString("SpawnLevel")) {
            Server.instance.getLevelByName(namedTag.getString("SpawnLevel")) ?: Server.instance.defaultLevel
        } else Server.instance.defaultLevel
        if (namedTag.containsInt("SpawnX") && namedTag.containsInt("SpawnY") && namedTag.containsInt("SpawnZ")) {
            this.spawnPoint = Locator(
                namedTag.getInt("SpawnX").toDouble(),
                namedTag.getInt("SpawnY").toDouble(),
                namedTag.getInt("SpawnZ").toDouble(), level!!
            )
        } else {
            this.spawnPoint = level!!.safeSpawn
            log.info(
                "Player {} cannot find the saved spawnpoint, reset the spawnpoint to {} {} {} / {}",
                this.getEntityName(),
                spawnPoint!!.position.x,
                spawnPoint!!.position.y,
                spawnPoint!!.position.z,
                spawnPoint!!.level.getLevelName()
            )
        }
        setDataFlag(EntityFlag.HIDDEN_WHEN_INVISIBLE)
        setDataFlag(EntityFlag.PUSH_TOWARDS_CLOSEST_SPACE)
        this.addDefaultWindows()
        this.loggedIn = true

        //todo remove these
        if (namedTag!!.containsString("SpawnBlockLevel")) {
            namedTag!!.remove("SpawnBlockLevel")
        }
        if (namedTag!!.containsInt("SpawnBlockPositionX") && namedTag!!.containsInt("SpawnBlockPositionY") && namedTag!!.containsInt(
                "SpawnBlockPositionZ"
            )
        ) {
            namedTag!!.remove("SpawnBlockPositionX")
            namedTag!!.remove("SpawnBlockPositionY")
            namedTag!!.remove("SpawnBlockPositionZ")
        }
    }

    /**
     * 完成completeLoginSequence后执行
     */
    fun doFirstSpawn() {
        this.spawned = true

        session.syncCraftingData()
        session.syncInventory()
        this.resetInventory()

        this.enableClientCommand = true

        val setTimePacket = SetTimePacket(
            time = level!!.getTime()
        )
        this.sendPacket(setTimePacket)

        this.noDamageTicks = 60

        for (index in playerChunkManager.usedChunks) {
            val chunkX = getHashX(index)
            val chunkZ = getHashZ(index)
            for (entity in level!!.getChunkEntities(chunkX, chunkZ).values) {
                if (this !== entity && !entity.closed && entity.isAlive()) {
                    entity.spawnTo(this)
                }
            }
        }

        val experience = this.experience
        if (experience != 0) {
            this.sendExperience(experience)
        }

        val level = this.experienceLevel
        if (level != 0) {
            this.sendExperienceLevel(this.experienceLevel)
        }

        //Weather
        this.level!!.sendWeather(this)

        //FoodLevel
        val food = this.foodData
        if (food?.isHungry == true) {
            food?.sendFood()
        }

        val scoreboardManager = Server.instance.scoreboardManager
        scoreboardManager.onPlayerJoin(this)

        if (spawn.second == null || spawn.second == SpawnPointType.WORLD) {
            this.setSpawn(this.level!!.safeSpawn, SpawnPointType.WORLD)
        } else {
            //update compass
            val pk = SetSpawnPositionPacket(
                spawnType = SetSpawnPositionPacket.Companion.SpawnType.World,
                position = BlockPos(spawn.first!!.position),
                dimension = spawn.first!!.level.dimension,
                spawnPosition = BlockPos(spawn.first!!.position)
            )
            this.sendPacket(pk)
        }

        this.sendFogStack()
        this.sendCameraPresets()

        log.debug("Send Player Spawn Status Packet to {},wait init packet", getEntityName())
        this.sendPlayStatus(PlayStatusPacket.Companion.Status.PlayerSpawn)

        //客户端初始化完毕再传送玩家，避免下落 (x)
        //已经设置immobile了所以不用管下落了
        val pos = if (Server.instance.settings.baseSettings.safeSpawn && (this.gamemode and 0x01) == 0
        ) {
            Transform(
                this.level!!.getSafeSpawn(this.position).position,
                rotation,
                headYaw,
                this.level!!
            )
        } else {
            Transform(
                this.position,
                rotation,
                headYaw,
                this.level!!
            )
        }
        this.teleport(pos, TeleportCause.PLAYER_SPAWN)

        if (this.health < 1) {
            this.setHealthSafe(0f)
        } else setHealthSafe(health) //sends health to player


        this.level!!.scheduler.scheduleDelayedTask(InternalPlugin.INSTANCE, {
            session.machine.fire(SessionState.InGame)
        }, 5)
    }

    public override fun checkGroundState(movX: Double, movY: Double, movZ: Double, dx: Double, dy: Double, dz: Double) {
        if (!this.onGround || movX != 0.0 || movY != 0.0 || movZ != 0.0) {
            var onGround = false

            val realBB = boundingBox.clone()
            realBB.maxY = realBB.minY
            realBB.minY = realBB.minY - 0.5

            val b1 = level!!.getTickCachedBlock(
                position.floorX,
                position.floorY - 1, position.floorZ
            )
            val b2 = level!!.getTickCachedBlock(
                position.floorX,
                position.floorY, position.floorZ
            )
            val blocks = arrayOf<Block>(
                level!!.getTickCachedBlock(
                    position.floorX - 1,
                    position.floorY - 1, position.floorZ
                ),
                level!!.getTickCachedBlock(
                    position.floorX + 1,
                    position.floorY - 1, position.floorZ
                ),
                level!!.getTickCachedBlock(
                    position.floorX,
                    position.floorY - 1, position.floorZ + 1
                ),
                level!!.getTickCachedBlock(
                    position.floorX,
                    position.floorY - 1, position.floorZ - 1
                ),
                level!!.getTickCachedBlock(
                    position.floorX - 1,
                    position.floorY - 1, position.floorZ - 1
                ),
                level!!.getTickCachedBlock(
                    position.floorX + 1,
                    position.floorY - 1, position.floorZ - 1
                ),
                level!!.getTickCachedBlock(
                    position.floorX + 1,
                    position.floorY - 1, position.floorZ + 1
                ),
                level!!.getTickCachedBlock(
                    position.floorX - 1,
                    position.floorY - 1, position.floorZ + 1
                )
            )
            if ((!b1.canPassThrough() && b1.collidesWithBB(realBB)) || (!b2.canPassThrough() && b2.collidesWithBB(
                    realBB
                ))
            ) {
//                level.addParticle(new BlockForceFieldParticle(b1.add(0.5, 0, 0.5)));
                onGround = true
            }
            for (block in blocks) {
//                level.addParticle(new BlockForceFieldParticle(block.add(0.5, 0, 0.5)));
                if (!block.canPassThrough() && block.collidesWithBB(realBB)) {
                    onGround = true
                }
            }
            this.onGround = onGround
        }

        this.isCollided = this.onGround
    }

    public override fun checkBlockCollision() {
        var portal = false
        var scaffolding = false
        var endPortal = false
        for (block in getCollisionBlocks()!!) {
            if (this.isSpectator) {
                continue
            }

            when (block.id) {
                BlockID.PORTAL -> portal = true
                BlockID.SCAFFOLDING -> scaffolding = true
                BlockID.END_PORTAL -> endPortal = true
            }

            block.onEntityCollide(this)
            block.getLevelBlockAtLayer(1).onEntityCollide(this)
        }
        val scanBoundingBox = boundingBox.getOffsetBoundingBox(0.0, -0.125, 0.0)
        scanBoundingBox.maxY = boundingBox.minY
        val scaffoldingUnder = level!!.getCollisionBlocks(
            scanBoundingBox,
            targetFirst = true, ignoreCollidesCheck = true
        ) { b: Block -> b.id == BlockID.SCAFFOLDING }

        setDataFlagExtend(EntityFlag.IN_SCAFFOLDING, scaffolding)
        setDataFlagExtend(EntityFlag.OVER_SCAFFOLDING, scaffoldingUnder.isNotEmpty())
        setDataFlagExtend(EntityFlag.IN_ASCENDABLE_BLOCK, scaffolding)
        setDataFlagExtend(EntityFlag.OVER_DESCENDABLE_BLOCK, scaffoldingUnder.isNotEmpty())

        if (endPortal) { //handle endPortal teleport
            if (!inEndPortal) {
                inEndPortal = true
                if (this.getRiding() == null && passengers.isEmpty()) {
                    val ev = EntityPortalEnterEvent(this, PortalType.END)
                    Server.instance.pluginManager.callEvent(ev)

                    if (!ev.cancelled) {
                        val newPos = moveToTheEnd(this.locator)
                        if (newPos != null) {
                            if (newPos.level.dimension == Level.DIMENSION_THE_END) {
                                if (teleport(newPos, TeleportCause.END_PORTAL)) {
                                    newPos.level.scheduler.scheduleDelayedTask(object : Task() {
                                        override fun onRun(currentTick: Int) {
                                            // dirty hack to make sure chunks are loaded and generated before spawning player
                                            teleport(newPos, TeleportCause.END_PORTAL)
                                            BlockEndPortal.spawnObsidianPlatform(newPos)
                                        }
                                    }, 5)
                                }
                            } else {
                                if (!this.hasSeenCredits && !this.showingCredits) {
                                    val playerShowCreditsEvent = PlayerShowCreditsEvent(this)
                                    Server.instance.pluginManager.callEvent(playerShowCreditsEvent)
                                    if (!playerShowCreditsEvent.cancelled) {
                                        this.showCredits()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            inEndPortal = false
        }

        if (portal) {
            if ((this.isCreative || this.isSpectator) && this.inPortalTicks < 80) {
                this.inPortalTicks = 80
            } else {
                inPortalTicks++
            }
        } else {
            this.inPortalTicks = 0
        }
    }

    fun checkNearEntities() {
        for (entity in level!!.getNearbyEntities(boundingBox.grow(1.0, 0.5, 1.0), this)) {
            if (entity == null) continue
            entity.scheduleUpdate()

            if (!entity.isAlive() || !this.isAlive()) {
                continue
            }

            this.pickupEntity(entity, true)
        }
    }

    fun handleMovement(clientPos: Transform) {
        if (this.firstMove) this.firstMove = false
        var invalidMotion = false
        val revertPos = transform.clone()
        val distance = clientPos.position.distanceSquared(this.position)
        //before check
        if (distance > 128) {
            invalidMotion = true
        } else if (this.chunk == null || !chunk!!.chunkState.canSend()) {
            val chunk =
                level!!.getChunk(clientPos.position.chunkX, clientPos.position.chunkZ, false)
            this.chunk = chunk
            if (this.chunk == null || !chunk.chunkState.canSend()) {
                invalidMotion = true
                this.nextChunkOrderRun = 0
                if (this.chunk != null) {
                    this.chunk!!.removeEntity(this)
                }
            }
        }

        if (invalidMotion) {
            this.revertClientMotion(revertPos)
            this.resetClientMovement()
            return
        }

        //update server-side position and rotation and aabb
        val diffX = clientPos.x - position.x
        val diffY = clientPos.y - position.y
        val diffZ = clientPos.z - position.z
        this.setRotation(clientPos.yaw, clientPos.pitch, clientPos.headYaw)
        this.fastMove(diffX, diffY, diffZ)

        //after check
        val corrX = position.x - clientPos.x
        val corrY = position.y - clientPos.y
        val corrZ = position.z - clientPos.z
        if (this.isCheckingMovement && (abs(corrX) > 0.5 || abs(corrY) > 0.5 || abs(corrZ) > 0.5) && this.riding == null && !this.hasEffect(
                EffectType.LEVITATION
            ) && !this.hasEffect(EffectType.SLOW_FALLING) && !Server.instance.allowFlight
        ) {
            val diff = corrX * corrX + corrZ * corrZ
            //这里放宽了判断，否则对角穿过脚手架会判断非法移动。
            if (diff > 1.2) {
                val event = PlayerInvalidMoveEvent(this, true)
                Server.instance.pluginManager.callEvent(event)
                if (!event.cancelled && (event.isRevert.also { invalidMotion = it })) {
                    log.warn(Server.instance.lang.tr("chorus.player.invalidMove", this.getEntityName()))
                }
            }
            if (invalidMotion) {
                this.setPositionAndRotation(
                    revertPos.position.asVector3f().asVector3(),
                    revertPos.yaw,
                    revertPos.pitch,
                    revertPos.headYaw
                )
                this.revertClientMotion(revertPos)
                this.resetClientMovement()
                return
            }
        }

        //update server-side position and rotation and aabb
        val last = Transform(
            prevPosition.x,
            prevPosition.y,
            prevPosition.z, prevRotation.yaw, prevRotation.pitch, this.prevHeadYaw,
            level!!
        )
        val now = this.transform
        prevPosition.x = now.position.x
        prevPosition.y = now.position.y
        prevPosition.z = now.position.z
        prevRotation.yaw = now.rotation.yaw
        prevRotation.pitch = now.rotation.pitch
        this.prevHeadYaw = now.headYaw

        var blocksAround: MutableList<Block>? = null
        var collidingBlocks: MutableList<Block>? = null
        if (this.blocksAround != null && this.collisionBlocks != null) {
            blocksAround = ArrayList(this.blocksAround!!)
            collidingBlocks = ArrayList(this.collisionBlocks!!)
        }

        if (!this.firstMove) {
            if (clientMovements.isEmpty()) {
                this.blocksAround = null
                this.collisionBlocks = null
            }
            val ev = PlayerMoveEvent(this, last, now)
            Server.instance.pluginManager.callEvent(ev)

            if (!(ev.cancelled.also { invalidMotion = it })) { //Yes, this is intended
                if (now != ev.to && this.riding == null) { //If plugins modify the destination
                    if (this.gamemode != SPECTATOR) level!!.vibrationManager.callVibrationEvent(
                        VibrationEvent(
                            this, ev.to.clone().position, VibrationType.TELEPORT
                        )
                    )
                    this.teleport(ev.to, null)
                } else {
                    if (this.gamemode != SPECTATOR && (last.position.x != now.position.x || last.position.y != now.position.y || last.position.z != now.position.z)) {
                        if (this.isOnGround() && this.isGliding()) {
                            level!!.vibrationManager.callVibrationEvent(
                                VibrationEvent(
                                    this,
                                    this.vector3, VibrationType.ELYTRA_GLIDE
                                )
                            )
                        } else if (this.isOnGround() && (locator
                                .getSide(BlockFace.DOWN).levelBlock !is BlockWool) && !this.isSneaking()
                        ) {
                            level!!.vibrationManager.callVibrationEvent(
                                VibrationEvent(
                                    this,
                                    this.vector3, VibrationType.STEP
                                )
                            )
                        } else if (this.isTouchingWater()) {
                            level!!.vibrationManager.callVibrationEvent(
                                VibrationEvent(
                                    this,
                                    transform.clone().position, VibrationType.SWIM
                                )
                            )
                        }
                    }
                    this.broadcastMovement(false)
                }
            } else {
                this.blocksAround = blocksAround
                this.collisionBlocks = collidingBlocks
            }
        }

        //update speed
        if (this.speed == null) {
            this.speed = Vector3(
                last.position.x - now.position.x,
                last.position.y - now.position.y,
                last.position.z - now.position.z
            )
        } else {
            speed!!.setComponents(
                last.position.x - now.position.x,
                last.position.y - now.position.y,
                last.position.z - now.position.z
            )
        }

        handleLogicInMove(invalidMotion, distance)

        //if plugin cancel move
        if (invalidMotion) {
            this.setPositionAndRotation(
                revertPos.position.asVector3f().asVector3(),
                revertPos.yaw,
                revertPos.pitch,
                revertPos.headYaw
            )
            this.revertClientMotion(revertPos)
            this.resetClientMovement()
        } else {
            if (distance != 0.0 && this.nextChunkOrderRun > 20) {
                this.nextChunkOrderRun = 20
            }
        }
    }

    fun offerMovementTask(newPosition: Transform) {
        val distance = newPosition.position.distance(this.position)
        val updatePosition = distance > MOVEMENT_DISTANCE_THRESHOLD //sqrt distance
        val updateRotation =
            abs(rotation.pitch - newPosition.rotation.pitch).toFloat() > ROTATION_UPDATE_THRESHOLD || abs(
                rotation.yaw - newPosition.rotation.yaw
            ).toFloat() > ROTATION_UPDATE_THRESHOLD || abs(
                rotation.yaw - newPosition.headYaw
            ).toFloat() > ROTATION_UPDATE_THRESHOLD
        val isHandle = this.isAlive() && this.spawned && !this.isSleeping() && (updatePosition || updateRotation)
        if (isHandle) {
            //todo hack for receive a error position after teleport
            val now = System.currentTimeMillis()
            if (lastTeleportMessage != null && (now - lastTeleportMessage!!.second) < 200) {
                val dis = newPosition.position.distance(lastTeleportMessage!!.first.position)
                if (dis < MOVEMENT_DISTANCE_THRESHOLD) return
            }
            this.newPosition = newPosition.position
            clientMovements.offer(newPosition)
        }
    }


    fun handleLogicInMove(invalidMotion: Boolean, distance: Double) {
        if (!invalidMotion) {
            //处理饱食度更新
            if (foodData?.isEnabled == true && Server.instance.getDifficulty() > 0) {
                //UpdateFoodExpLevel
                if (distance >= 0.05) {
                    var jump = 0.0
                    val swimming = if (this.isInsideOfWater()) 0.01 * distance else 0.0
                    var distance2 = distance
                    if (swimming != 0.0) distance2 = 0.0
                    if (this.isSprinting()) {  //Running
                        if (this.inAirTicks == 3 && swimming == 0.0) {
                            jump = 0.2
                        }
                        foodData?.exhaust(0.1 * distance2 + jump + swimming)
                    } else {
                        if (this.inAirTicks == 3 && swimming == 0.0) {
                            jump = 0.05
                        }
                        foodData?.exhaust(jump + swimming)
                    }
                }
            }

            //处理冰霜行者附魔
            val frostWalker = inventory.boots.getEnchantment(Enchantment.ID_FROST_WALKER)
            if (frostWalker != null && frostWalker.level > 0 && !this.isSpectator && position.y >= 1 && position.y <= 255) {
                val radius = 2 + frostWalker.level
                for (coX in position.floorX - radius..<position.floorX + radius + 1) {
                    for (coZ in position.floorZ - radius..<position.floorZ + radius + 1) {
                        var block = level!!.getBlock(coX, position.floorY - 1, coZ)
                        var layer = 0
                        if ((block.id != BlockID.WATER && (block.id != BlockID.FLOWING_WATER ||
                                    block.getPropertyValue<Int, IntPropertyType>(CommonBlockProperties.LIQUID_DEPTH) != 0)) || !block.up().isAir
                        ) {
                            block = block.getLevelBlockAtLayer(1)
                            layer = 1
                            if ((block.id != BlockID.WATER && (block.id != BlockID.FLOWING_WATER ||
                                        block.getPropertyValue(CommonBlockProperties.LIQUID_DEPTH) != 0)) || !block.up().isAir
                            ) {
                                continue
                            }
                        }
                        val ev = WaterFrostEvent(block, this)
                        Server.instance.pluginManager.callEvent(ev)
                        if (!ev.cancelled) {
                            level!!.setBlock(block.position, layer, Block.get(BlockID.FROSTED_ICE), true, false)
                            level!!.scheduleUpdate(
                                level!!.getBlock(block.position, layer),
                                ThreadLocalRandom.current().nextInt(20, 40)
                            )
                        }
                    }
                }
            }

            //处理灵魂急行附魔
            //Handling Soul Speed Enchantment
            val soulSpeedLevel = inventory.boots.getEnchantmentLevel(Enchantment.ID_SOUL_SPEED)
            if (soulSpeedLevel > 0) {
                val levelBlock = locator.levelBlock
                this.soulSpeedMultiplier = (soulSpeedLevel * 0.105f) + 1.3f

                // levelBlock check is required because of soul sand being 1 pixel shorter than normal blocks
                val isSoulSandCompatible = levelBlock.isSoulSpeedCompatible || levelBlock.down().isSoulSpeedCompatible

                if (this.wasInSoulSandCompatible && !isSoulSandCompatible) {
                    this.wasInSoulSandCompatible = false
                    this.setMovementSpeedF(this.movementSpeed / this.soulSpeedMultiplier)
                } else if (!this.wasInSoulSandCompatible && isSoulSandCompatible) {
                    this.wasInSoulSandCompatible = true
                    this.setMovementSpeedF(this.movementSpeed * this.soulSpeedMultiplier)
                }
            }
        }
    }

    fun resetClientMovement() {
        this.newPosition = null
        this.positionChanged = false
    }

    fun revertClientMotion(originalPos: Transform) {
        prevPosition.x = originalPos.x
        prevPosition.y = originalPos.y
        prevPosition.z = originalPos.z
        prevRotation.yaw = originalPos.yaw
        prevRotation.pitch = originalPos.pitch

        val syncPos = originalPos.position.add(0.0, 0.00001, 0.0)
        this.sendPosition(
            syncPos,
            originalPos.yaw,
            originalPos.pitch,
            MovePlayerPacket.Companion.Mode.Reset
        )

        if (this.speed == null) {
            this.speed = Vector3(0.0, 0.0, 0.0)
        } else {
            speed!!.setComponents(0.0, 0.0, 0.0)
        }
    }

    /**
     * 处理LOGIN_PACKET中执行
     */
    fun processLogin() {
        if (this.hasPermission(Server.BROADCAST_CHANNEL_USERS)) {
            Server.instance.pluginManager.subscribeToPermission(Server.BROADCAST_CHANNEL_USERS, this)
        }
        if (this.hasPermission(Server.BROADCAST_CHANNEL_ADMINISTRATIVE)) {
            Server.instance.pluginManager.subscribeToPermission(
                Server.BROADCAST_CHANNEL_ADMINISTRATIVE,
                this
            )
        }

        var oldPlayer: Player? = null
        for (p in Server.instance.onlinePlayers.values) {
            if (p === this) continue
            if (p.getEntityName() == this.getEntityName() || this.getUniqueID() == p.getUniqueID()) {
                oldPlayer = p
                break
            }
        }
        val nbt: CompoundTag?
        if (oldPlayer != null) {
            oldPlayer.saveNBT()
            nbt = oldPlayer.namedTag
            oldPlayer.close("disconnectionScreen.loggedinOtherLocation")
        } else {
            val existData: Boolean = Server.instance.hasOfflinePlayerData(this.getEntityName())
            nbt = if (existData) {
                Server.instance.getOfflinePlayerData(this.getEntityName(), false)
            } else {
                Server.instance.getOfflinePlayerData(this.uuid, true)
            }
        }

        if (nbt == null) {
            this.close(this.leaveMessage, "Invalid data")
            return
        }

        Server.instance.updateName(this.playerInfo)

        this.playedBefore = (nbt.getLong("lastPlayed") - nbt.getLong("firstPlayed")) > 1


        nbt.putString("NameTag", this.getEntityName())

        val exp = nbt.getInt("EXP")
        val expLevel = nbt.getInt("expLevel")
        this.setExperience(exp, expLevel)

        this.gamemode = nbt.getInt("playerGameType") and 0x03
        if (Server.instance.forceGamemode) {
            this.gamemode = Server.instance.gamemode
            nbt.putInt("playerGameType", this.gamemode)
        }

        adventureSettings.init(nbt)

        val level: Level?
        if (Server.instance.getLevelByName(nbt.getString("Level")).also { level = it } == null) {
            this.level = Server.instance.defaultLevel
            nbt.putString("Level", this.level!!.getLevelName())
            val spawnLocation = this.level!!.safeSpawn
            nbt.getList("Pos", FloatTag::class.java)
                .add(FloatTag(spawnLocation.position.x))
                .add(FloatTag(spawnLocation.position.y))
                .add(FloatTag(spawnLocation.position.z))
        } else {
            this.level = level
        }

        for ((key, value) in nbt.getCompound("Achievements").entrySet) {
            if (value !is ByteTag) {
                continue
            }

            if (value.getData() > 0) {
                achievements.add(key)
            }
        }

        nbt.putLong("lastPlayed", System.currentTimeMillis() / 1000)

        val uuid = getUUID()
        nbt.putLong("UUIDLeast", uuid.leastSignificantBits)
        nbt.putLong("UUIDMost", uuid.mostSignificantBits)

        if (Server.instance.getAutoSave()) {
            Server.instance.saveOfflinePlayerData(this.uuid, nbt, true)
        }

        val posList = nbt.getList("Pos", FloatTag::class.java)

        super.init(
            this.level!!.getChunk(
                posList[0].data.toInt() shr 4,
                posList[2].data.toInt() shr 4,
                true
            ), nbt
        )

        if (!namedTag!!.contains("foodLevel")) {
            namedTag!!.putInt("foodLevel", 20)
        }
        val foodLevel = namedTag!!.getInt("foodLevel")
        if (!namedTag!!.contains("foodSaturationLevel")) {
            namedTag!!.putFloat("foodSaturationLevel", 20f)
        }
        val foodSaturationLevel = namedTag!!.getFloat("foodSaturationLevel")
        this.foodData = PlayerFood(this, foodLevel, foodSaturationLevel)

        if (this.isSpectator) {
            this.onGround = false
        }

        if (namedTag!!.contains("enchSeed")) {
            this.enchSeed = namedTag!!.getInt("enchSeed")
        } else {
            this.regenerateEnchantmentSeed()
            namedTag!!.putInt("enchSeed", this.enchSeed)
        }

        if (!namedTag!!.contains("TimeSinceRest")) {
            namedTag!!.putInt("TimeSinceRest", 0)
        }
        this.timeSinceRest = namedTag!!.getInt("TimeSinceRest")

        if (!namedTag!!.contains("HasSeenCredits")) {
            namedTag!!.putBoolean("HasSeenCredits", false)
        }
        this.hasSeenCredits = namedTag!!.getBoolean("HasSeenCredits")

        //以下两个List的元素一一对应
        if (!namedTag!!.contains("fogIdentifiers")) {
            namedTag!!.putList("fogIdentifiers", ListTag<StringTag>())
        }
        if (!namedTag!!.contains("userProvidedFogIds")) {
            namedTag!!.putList("userProvidedFogIds", ListTag<StringTag>())
        }
        val fogIdentifiers = namedTag.getList(
            "fogIdentifiers",
            StringTag::class.java
        )
        val userProvidedFogIds = namedTag.getList(
            "userProvidedFogIds",
            StringTag::class.java
        )
        for (i in 0..<fogIdentifiers.size()) {
            fogStack.add(i, Pair(userProvidedFogIds[i].data, fogIdentifiers[i].data))
        }

        if (!Server.instance.settings.playerSettings.checkMovement) {
            this.isCheckingMovement = false
        }

        log.info(
            Server.instance.lang.tr(
                "chorus.player.logIn",
                TextFormat.AQUA.toString() + this.getEntityName() + TextFormat.WHITE,
                this.address,
                port.toString(),
                getRuntimeID().toString(),
                this.level!!.getLevelName(),
                round(position.x, 4).toString(),
                round(position.y, 4).toString(),
                round(position.z, 4).toString()
            )
        )
    }

    val safeSpawn: Vector3
        get() {
            val worldSpawnPoint = if (this.spawnPoint == null) {
                Server.instance.defaultLevel!!.safeSpawn.position
            } else {
                spawnPoint!!.position
            }
            return worldSpawnPoint
        }

    /**
     * 玩家客户端初始化完成后调用
     */
    fun onPlayerLocallyInitialized() {
        if (locallyInitialized) return
        locallyInitialized = true

        //init entity data property
        this.setDataProperty(EntityDataTypes.NAME, playerInfo.username, false)
        this.setDataProperty(EntityDataTypes.NAMETAG_ALWAYS_SHOW, 1, false)

        val playerJoinEvent = PlayerJoinEvent(
            this,
            TranslationContainer(
                TextFormat.YELLOW.toString() + "%multiplayer.player.joined", *arrayOf(
                    this.displayName
                )
            )
        )

        Server.instance.pluginManager.callEvent(playerJoinEvent)

        if (playerJoinEvent.joinMessage.toString().trim { it <= ' ' }.isNotEmpty()) {
            Server.instance.broadcastMessage(playerJoinEvent.joinMessage)
        }

        /*
          我们在玩家客户端初始化后才发送游戏模式，以解决观察者模式疾跑速度不正确的问题
          只有在玩家客户端进入游戏显示后再设置观察者模式，疾跑速度才正常
          强制更新游戏模式以确保客户端会收到模式更新包
          After initializing the player client, we send the game mode to address the issue of incorrect
          sprint speed in spectator mode. Only after the player client enters the game display is spectator mode set,
          and the sprint speed behaves normally. We force an update of the game mode to ensure that the client receives the
          mode update packet.
         */
        this.setGamemode(this.gamemode, false, null, true)
        this.sendData(hasSpawned.values.toTypedArray(), entityDataMap)
        this.spawnToAll()
        level!!.getEntities()
            .filter { it.viewers.containsKey(this.loaderId) && it is EntityBoss }
            .forEach { (it as EntityBoss).addBossbar(this) }
        this.refreshBlockEntity(1)
    }

    /**
     * 判断重生锚是否有效如果重生锚有效则在重生锚上重生或者在床上重生
     * 如果玩家以上2种都没有则在服务器重生点重生
     *
     *
     * Determine if the respawn anchor is valid if the respawn anchor is valid then the anchor is respawned at the respawn anchor or reborn in bed
     * If the player has none of the above 2 then respawn at the server respawn point
     *
     * @param block
     * @return
     */
    fun isValidRespawnBlock(block: Block): Boolean {
        if (block.id == BlockID.RESPAWN_ANCHOR && block.level.dimension == Level.DIMENSION_NETHER) {
            val anchor = block as BlockRespawnAnchor
            return anchor.charge > 0
        }
        if (block.id == BlockID.BED && block.level.dimension == Level.DIMENSION_OVERWORLD) {
            val bed = block as BlockBed
            return bed.isBedValid
        }

        return false
    }

    override var isBanned: Boolean
        get() = Server.instance.bannedPlayers.isBanned(this.getEntityName())
        set(value) {
            if (value) {
                Server.instance.bannedPlayers.addBan(this.getEntityName(), null, null, null)
                this.kick(PlayerKickEvent.Reason.NAME_BANNED, "Banned by admin")
            } else {
                Server.instance.bannedPlayers.remove(this.getEntityName())
            }
        }

    fun respawn() {
        //the player can't respawn if the server is hardcore
        if (Server.instance.isHardcore) {
            this.isBanned = true
            return
        }

        this.resetInventory()

        //level spawn point < block spawn = self spawn
        val spawnPair = this.spawn
        val playerRespawnEvent = PlayerRespawnEvent(this, spawnPair)
        if (spawnPair.second == SpawnPointType.BLOCK) { //block spawn
            val spawnBlock = playerRespawnEvent.respawnPosition.first!!.levelBlock
            if (isValidRespawnBlock(spawnBlock)) {
                // handle RESPAWN_ANCHOR state change when consume charge is true
                if (spawnBlock.id == BlockID.RESPAWN_ANCHOR) {
                    val respawnAnchor = spawnBlock as BlockRespawnAnchor
                    val charge = respawnAnchor.charge
                    if (charge > 0) {
                        respawnAnchor.charge = charge - 1
                        respawnAnchor.level.setBlock(respawnAnchor.position, spawnBlock)
                        respawnAnchor.level.scheduleUpdate(respawnAnchor, 10)
                        respawnAnchor.level.addSound(this.position, Sound.RESPAWN_ANCHOR_DEPLETE, 1f, 1f, this)
                    }
                }
            } else { //block not available
                val defaultSpawn = Server.instance.defaultLevel!!.spawnLocation
                this.setSpawn(defaultSpawn, SpawnPointType.WORLD)
                playerRespawnEvent.respawnPosition = Pair(defaultSpawn, SpawnPointType.WORLD)
                // handle spawn point change when block spawn not available
                sendMessage(TranslationContainer(TextFormat.GRAY.toString() + "%tile." + (if (level!!.dimension == Level.DIMENSION_OVERWORLD) "bed" else "respawn_anchor") + ".notValid"))
            }
        }

        Server.instance.pluginManager.callEvent(playerRespawnEvent)
        val respawnPos = playerRespawnEvent.respawnPosition.first

        this.sendExperience()
        this.sendExperienceLevel()

        this.setSprinting(false)
        this.setSneaking(false)

        this.setDataProperty(EntityDataTypes.AIR_SUPPLY, 400, false)
        this.fireTicks = 0
        this.collisionBlocks = null
        this.noDamageTicks = 60

        this.removeAllEffects()
        this.setHealthSafe(getMaxHealth().toFloat())
        foodData?.setFood(20, 20f)

        this.sendData(this)

        this.setMovementSpeedF(DEFAULT_SPEED)

        adventureSettings.update()
        inventory.sendContents(this)
        inventory.sendArmorContents(this)
        offhandInventory.sendContents(this)
        this.teleport(
            Transform.fromObject(
                respawnPos!!.position.add(
                    0.0,
                    getEyeHeight().toDouble(), 0.0
                ), respawnPos.level
            ), TeleportCause.PLAYER_SPAWN
        )
        this.spawnToAll()
    }

    public override fun checkChunks() {
        if (this.chunk == null || (chunk!!.x != (position.x.toInt() shr 4) || chunk!!.z != (position.z.toInt() shr 4))) {
            if (this.chunk != null) {
                chunk!!.removeEntity(this)
            }
            this.chunk = level!!.getChunk(
                position.x.toInt() shr 4,
                position.z.toInt() shr 4, true
            )

            if (!this.justCreated) {
                val newChunk =
                    level!!.getChunkPlayers(position.x.toInt() shr 4, position.z.toInt() shr 4)
                newChunk.remove(this.loaderId)

                //List<Player> reload = new ArrayList<>();
                for (player in ArrayList<Player>(hasSpawned.values)) {
                    if (!newChunk.containsKey(player.loaderId)) {
                        this.despawnFrom(player)
                    } else {
                        newChunk.remove(player.loaderId)
                        //reload.add(player);
                    }
                }

                for (player in newChunk.values) {
                    this.spawnTo(player)
                }
            }

            if (this.chunk == null) {
                return
            }

            chunk!!.addEntity(this)
        }
    }

    protected fun sendPlayStatus(status: PlayStatusPacket.Companion.Status, immediate: Boolean = false) {
        val pk = PlayStatusPacket(
            status = status
        )
        if (immediate) {
            this.sendPacketImmediately(pk)
        } else {
            this.sendPacket(pk)
        }
    }

    fun forceSendEmptyChunks() {
        val chunkPositionX = position.floorX shr 4
        val chunkPositionZ = position.floorZ shr 4
        for (x in -chunkRadius..<chunkRadius) {
            for (z in -chunkRadius..<chunkRadius) {
                val chunk = LevelChunkPacket(
                    position = ChunkPos(
                        chunkPositionX + x,
                        chunkPositionZ + z
                    ),
                    dimension = this.level!!.dimension,
                    subChunkCount = 0u,
                    subChunkLimit = 0u,
                    cacheEnabled = false,
                    blobHashes = emptyList(),
                    data = ByteString(),
                )
                this.sendPacket(chunk)
            }
        }
    }

    fun addDefaultWindows() {
        this.craftingGrid = CraftingGridInventory(this)
        this.cursorInventory = PlayerCursorInventory(this)
        this.creativeOutputInventory = CreativeOutputInventory(this)

        this.addWindow(this.inventory, SpecialWindowId.PLAYER.id)
        //addDefaultWindows when the player doesn't have a spawn yet,
        // so we need to manually open it to add the player to the viewer
        inventory.open(this)
        permanentWindows.add(SpecialWindowId.PLAYER.id)

        this.addWindow(creativeOutputInventory, SpecialWindowId.CREATIVE.id)
        creativeOutputInventory.open(this)
        permanentWindows.add(SpecialWindowId.CREATIVE.id)

        this.addWindow(offhandInventory, SpecialWindowId.OFFHAND.id)
        offhandInventory.open(this)
        permanentWindows.add(SpecialWindowId.OFFHAND.id)

        this.addWindow(craftingGrid, SpecialWindowId.NONE.id)
        craftingGrid.open(this)
        permanentWindows.add(SpecialWindowId.NONE.id)

        this.addWindow(cursorInventory, SpecialWindowId.CURSOR.id)
        cursorInventory.open(this)
        permanentWindows.add(SpecialWindowId.CURSOR.id)
    }

    public override fun getBaseOffset(): Float {
        return super.getBaseOffset()
    }

    override fun onBlock(entity: Entity?, event: EntityDamageEvent?, animate: Boolean) {
        super.onBlock(entity, event, animate)
        if (event!!.isBreakShield) {
            this.setItemCoolDown(event.shieldBreakCoolDown, "shield")
        }
        if (animate) {
            this.setDataFlag(EntityFlag.BLOCKED_USING_DAMAGED_SHIELD, true)
            level!!.scheduler.scheduleTask(InternalPlugin.INSTANCE) {
                if (this.isOnline) {
                    this.setDataFlag(EntityFlag.BLOCKED_USING_DAMAGED_SHIELD, false)
                }
            }
        }
    }

    public override fun getStepHeight(): Double {
        return 0.6
    }

    val leaveMessage: TranslationContainer
        /**
         * 获取玩家离开的消息
         *
         * @return [TranslationContainer]
         */
        get() = TranslationContainer(
            TextFormat.YELLOW.toString() + "%multiplayer.player.left",
            displayName
        )

    override var isWhitelisted: Boolean
        get() = Server.instance.isWhitelisted(getEntityName().lowercase())
        set(value) {
            if (value) {
                Server.instance.addWhitelist(getEntityName().lowercase())
            } else {
                Server.instance.removeWhitelist(getEntityName().lowercase())
            }
        }

    override val player: Player
        get() = this

    override val firstPlayed: Long?
        get() = if (this.namedTag != null) namedTag!!.getLong("firstPlayed") else null

    override val lastPlayed: Long?
        get() = if (this.namedTag != null) namedTag!!.getLong("lastPlayed") else null

    override fun hasPlayedBefore(): Boolean {
        return this.playedBefore
    }


    /**
     * 设置[.inAirTicks]为0
     *
     *
     * Set [.inAirTicks] to 0
     */
    fun resetInAirTicks() {
        this.inAirTicks = 0
    }

    var allowFlight: Boolean
        get() = adventureSettings[AdventureSettings.Type.ALLOW_FLIGHT]
        set(value) {
            adventureSettings[AdventureSettings.Type.ALLOW_FLIGHT] = value
            adventureSettings.update()
        }


    /**
     * 设置允许修改世界(未知原因设置完成之后，玩家不允许挖掘方块，但是可以放置方块)
     *
     *
     * Set allow to modify the world (after the unknown reason setting is completed, the player is not allowed to dig the blocks, but can place them)
     *
     * @param value 是否允许修改世界<br></br>Whether to allow modification of the world
     */
    fun setAllowModifyWorld(value: Boolean) {
        adventureSettings[AdventureSettings.Type.WORLD_IMMUTABLE] = !value
        adventureSettings[AdventureSettings.Type.BUILD] = value
        adventureSettings[AdventureSettings.Type.WORLD_BUILDER] = value
        adventureSettings.update()
    }

    fun setAllowInteract(value: Boolean) {
        setAllowInteract(value, value)
    }

    /**
     * 设置允许交互世界/容器
     *
     * @param value      是否允许交互世界
     * @param containers 是否允许交互容器
     */
    fun setAllowInteract(value: Boolean, containers: Boolean) {
        adventureSettings[AdventureSettings.Type.WORLD_IMMUTABLE] = !value
        adventureSettings[AdventureSettings.Type.DOORS_AND_SWITCHED] = value
        adventureSettings[AdventureSettings.Type.OPEN_CONTAINERS] = containers
        adventureSettings.update()
    }

    fun setAutoJump(value: Boolean) {
        adventureSettings[AdventureSettings.Type.AUTO_JUMP] = value
        adventureSettings.update()
    }

    fun hasAutoJump(): Boolean {
        return adventureSettings[AdventureSettings.Type.AUTO_JUMP]
    }

    override fun spawnTo(player: Player) {
        if (player.spawned && this.isAlive() && player.level == this.level && player.canSee(this) /* && !this.isSpectator()*/) {
            super.spawnTo(player)

            if (this.isSpectator) {
                //发送旁观者的游戏模式给对方，使得对方客户端正确渲染玩家实体
                val pk = UpdatePlayerGameTypePacket(
                    gameType = org.chorus_oss.protocol.types.GameType.Spectator,
                    playerUniqueID = this.getUniqueID(),
                    tick = 0uL
                )
                player.sendPacket(pk)
            }
        }
    }

    fun setRemoveFormat() {
        this.removeFormat = true
    }

    /**
     * @param player 玩家
     * @return 是否可以看到该玩家<br></br>Whether the player can be seen
     */
    fun canSee(player: Player): Boolean {
        return !hiddenPlayers.containsKey(player.getUUID())
    }

    /**
     * 从当前玩家实例的视角中隐藏指定玩家player
     *
     *
     * Hide the specified player from the view of the current player instance
     *
     * @param player 要隐藏的玩家<br></br>Players who want to hide
     */
    fun hidePlayer(player: Player) {
        if (this === player) {
            return
        }
        hiddenPlayers[player.getUUID()] = player
        player.despawnFrom(this)
    }

    /**
     * 从当前玩家实例的视角中显示指定玩家player
     *
     *
     * Show the specified player from the view of the current player instance
     *
     * @param player 要显示的玩家<br></br>Players who want to show
     */
    fun showPlayer(player: Player) {
        if (this === player) {
            return
        }
        hiddenPlayers.remove(player.getUUID())
        if (player.isOnline) {
            player.spawnTo(this)
        }
    }

    override fun canCollideWith(entity: Entity): Boolean {
        return false
    }

    override fun canCollide(): Boolean {
        return gamemode != SPECTATOR
    }

    override fun resetFallDistance() {
        super.resetFallDistance()
        if (this.inAirTicks != 0) {
            this.startAirTicks = 5
        }
        this.inAirTicks = 0
        this.highestPosition = position.y
    }

    override val isOnline: Boolean
        get() = connected.get() && this.loggedIn

    override var isOp: Boolean
        get() = Server.instance.isOp(this.getEntityName())
        set(value) {
            when (value) {
                isOp -> return
                true -> Server.instance.addOp(this.getEntityName())
                false -> Server.instance.removeOp(this.getEntityName())
            }
        }

    override fun isPermissionSet(name: String): Boolean {
        return perm!!.isPermissionSet(name)
    }

    override fun isPermissionSet(permission: Permission): Boolean {
        return perm!!.isPermissionSet(permission)
    }

    override fun hasPermission(name: String): Boolean {
        return this.perm != null && perm!!.hasPermission(name)
    }

    override fun hasPermission(permission: Permission): Boolean {
        return perm!!.hasPermission(permission)
    }

    override fun addAttachment(plugin: Plugin): PermissionAttachment {
        return this.addAttachment(plugin, null)
    }

    override fun addAttachment(plugin: Plugin, name: String?): PermissionAttachment {
        return this.addAttachment(plugin, name, null)
    }

    override fun addAttachment(plugin: Plugin, name: String?, value: Boolean?): PermissionAttachment {
        return perm!!.addAttachment(plugin, name, value)
    }

    override fun removeAttachment(attachment: PermissionAttachment) {
        perm!!.removeAttachment(attachment)
    }

    override fun recalculatePermissions() {
        Server.instance.pluginManager.unsubscribeFromPermission(Server.BROADCAST_CHANNEL_USERS, this)
        Server.instance.pluginManager.unsubscribeFromPermission(
            Server.BROADCAST_CHANNEL_ADMINISTRATIVE,
            this
        )

        if (this.perm == null) {
            return
        }

        perm!!.recalculatePermissions()

        if (this.hasPermission(Server.BROADCAST_CHANNEL_USERS)) {
            Server.instance.pluginManager.subscribeToPermission(Server.BROADCAST_CHANNEL_USERS, this)
        }

        if (this.hasPermission(Server.BROADCAST_CHANNEL_ADMINISTRATIVE)) {
            Server.instance.pluginManager.subscribeToPermission(
                Server.BROADCAST_CHANNEL_ADMINISTRATIVE,
                this
            )
        }

        if (this.isEnableClientCommand() && spawned) {
            session.syncAvailableCommands()
        }
    }

    fun isEnableClientCommand(): Boolean {
        return this.enableClientCommand
    }

    override fun asPlayer(): Player {
        return this
    }

    override val isEntity: Boolean
        get() = true

    override fun asEntity(): Entity {
        return this
    }

    fun removeAchievement(achievementId: String?) {
        achievements.remove(achievementId)
    }

    fun hasAchievement(achievementId: String?): Boolean {
        return achievements.contains(achievementId)
    }

    fun isConnected(): Boolean {
        return connected.get()
    }

    val rawAddress: String
        /**
         * 得到原始地址
         *
         * @return [String]
         */
        get() = rawSocketAddress!!.address.hostAddress

    val rawPort: Int
        /**
         * 得到原始端口
         *
         * @return int
         */
        get() = rawSocketAddress!!.port


    /**
     * Close all form windows
     */
    fun closeFormWindows() {
        formWindows.clear()
        this.sendPacket(ClientboundCloseFormPacket())
    }

    val address: String
        /**
         * 得到地址,如果开启waterdogpe兼容，该地址是被修改为兼容waterdogpe型的，反之则与[.rawSocketAddress] 一样
         *
         *
         * If waterdogpe compatibility is enabled, the address is modified to be waterdogpe compatible, otherwise it is the same as [.rawSocketAddress]
         *
         * @return [String]
         */
        get() = socketAddress!!.address.hostAddress

    val port: Int
        /**
         * @see .getRawPort
         */
        get() = socketAddress!!.port

    val nextPosition: Locator
        /**
         * 获得下一个tick客户端玩家将要移动的位置
         *
         *
         * Get the position where the next tick client player will move
         *
         * @return the next position
         */
        get() = if (this.newPosition != null) Locator(
            newPosition!!.x,
            newPosition!!.y,
            newPosition!!.z,
            level!!
        ) else locator

    /**
     * 玩家是否在睡觉
     *
     *
     * Whether the player is sleeping
     *
     * @return boolean
     */
    fun isSleeping(): Boolean {
        return this.sleeping != null
    }


    /**
     * 返回玩家当前是否正在使用某项物品（右击并按住）。
     *
     *
     * Returns whether the player is currently using an item (right-click and hold).
     */
    fun isUsingItem(itemId: String): Boolean {
        return getLastUseTick(itemId) != -1 && this.getDataFlag(EntityFlag.USING_ITEM)
    }

    /**
     * Sets the cooldown time for the specified item to use
     *
     * @param coolDownTick the cool down tick
     * @param itemId       the item id
     */
    fun setItemCoolDown(coolDownTick: Int, itemId: Identifier) {
        val pk = PlayerStartItemCoolDownPacket(
            category = itemId.toString(),
            duration = coolDownTick,
        )
        cooldownTickMap[itemId.toString()] = level!!.tick + coolDownTick
        this.sendPacket(pk)
    }

    /**
     * the cooldown of specified item is end
     *
     * @param itemId the item
     * @return the boolean
     */
    fun isItemCoolDownEnd(itemId: Identifier): Boolean {
        val tick = cooldownTickMap.getOrDefault(itemId.toString(), 0)
        val result = level!!.tick - tick > 0
        if (result) {
            cooldownTickMap.remove(itemId.toString())
        }
        return result
    }

    fun setItemCoolDown(coolDown: Int, category: String) {
        val pk = PlayerStartItemCoolDownPacket(
            category = category,
            duration = coolDown,
        )
        cooldownTickMap[category] = level!!.tick + coolDown
        this.sendPacket(pk)
    }

    fun isItemCoolDownEnd(category: String): Boolean {
        val tick = cooldownTickMap.getOrDefault(category, 0)
        val result = level!!.tick - tick > 0
        if (result) {
            cooldownTickMap.remove(category)
        }
        return result
    }

    /**
     * Start last use tick for an item(right-click).
     *
     * @param itemId the item id
     */
    fun setLastUseTick(itemId: String, tick: Int) {
        lastUseItemMap[itemId] = tick
        this.setDataFlag(EntityFlag.USING_ITEM, true)
    }

    fun removeLastUseTick(itemId: String) {
        lastUseItemMap.remove(itemId)
        this.setDataFlag(EntityFlag.USING_ITEM, false)
    }

    fun getLastUseTick(itemId: String): Int {
        return lastUseItemMap.getOrDefault(itemId, -1)
    }

    @JvmOverloads
    fun unloadChunk(x: Int, z: Int, level: Level? = null) {
        var level = level
        level = level ?: this.level
        val index = chunkHash(x, z)
        if (level!!.unregisterChunkLoader(this, x, z, false)) {
            if (playerChunkManager.usedChunks.contains(index)) {
                for (entity in level.getChunkEntities(x, z).values) {
                    if (entity !== this) {
                        entity.despawnFrom(this)
                    }
                }
                playerChunkManager.usedChunks.remove(index)
            }
        }
    }

    val isInOverWorld: Boolean
        /**
         * @return 玩家是否在主世界(维度为0)<br></br>Is the player in the world(Dimension equal 0)
         */
        get() = level!!.dimension == 0

    val spawn: Pair<Locator?, SpawnPointType?>
        /**
         * 获取该玩家的可用重生点,
         *
         *
         * Get the player's Spawn point
         *
         * @return [Locator]
         */
        get() = Pair(spawnPoint, spawnPointType)

    /**
     * 设置玩家的出生点/复活点。
     *
     *
     * Set the player's birth point.
     *
     * @param pos 出生点位置
     */
    fun setSpawn(pos: Locator, spawnPointType: SpawnPointType?) {
        Preconditions.checkNotNull(pos)
        Preconditions.checkNotNull(pos.level)
        this.spawnPoint = Locator(
            pos.position.x, pos.position.y, pos.position.z,
            level!!
        )
        this.spawnPointType = spawnPointType
        val pk = SetSpawnPositionPacket(
            spawnType = SetSpawnPositionPacket.Companion.SpawnType.Player,
            position = BlockPos(spawnPoint!!.position),
            dimension = spawnPoint!!.level.dimension,
            spawnPosition = BlockPos(spawnPoint!!.position),
        )
        this.sendPacket(pk)
    }

    fun sendChunk(x: Int, z: Int, packet: DataPacket) {
        if (!this.isConnected()) {
            return
        }

        chunkLoadCount++
        playerChunkManager.usedChunks.add(chunkHash(x, z))
        this.dataPacket(packet)

        if (this.spawned) {
            for (entity in level!!.getChunkEntities(x, z).values) {
                if (this !== entity && !entity.closed && entity.isAlive()) {
                    entity.spawnTo(this)
                }
            }
        }
    }

    fun sendChunk(x: Int, z: Int, packet: Packet) {
        sendChunk(x, z, MigrationPacket(packet))
    }


    @JvmOverloads
    fun updateTrackingPositions(delayed: Boolean = false) {
        Server.instance
        if (delayed) {
            if (delayedPosTrackingUpdate != null) {
                delayedPosTrackingUpdate!!.cancel()
            }
            val scheduler = if (level == null) Server.instance.scheduler else level!!.scheduler
            delayedPosTrackingUpdate = scheduler.scheduleDelayedTask(
                null,
                { this.updateTrackingPositions() }, 10
            )
            return
        }
        val positionTrackingService = Server.instance.getPositionTrackingService()
        positionTrackingService.forceRecheck(this)
    }


    /**
     * @param packet 发送的数据包<br></br>packet to send
     */
    fun dataPacket(packet: DataPacket) {
        session.sendPacket(packet)
    }

    fun sendPacket(packet: Packet) {
        session.sendPacket(MigrationPacket(packet))
    }

    val ping: Int
        /**
         * 得到该玩家的网络延迟。
         *
         *
         * Get the network latency of the player.
         *
         * @return int
         */
        get() = session.ping.toInt()

    fun sleepOn(pos: Vector3): Boolean {
        if (!this.isOnline) {
            return false
        }

        for (p in level!!.getNearbyEntities(boundingBox.grow(2.0, 1.0, 2.0), this)) {
            if (p is Player) {
                if (p.sleeping != null && pos.distance(p.sleeping!!) <= 0.1) {
                    return false
                }
            }
        }

        val ev: PlayerBedEnterEvent
        Server.instance.pluginManager.callEvent(
            PlayerBedEnterEvent(
                this,
                level!!.getBlock(pos)
            ).also { ev = it })
        if (ev.cancelled) {
            return false
        }

        this.sleeping = pos.clone()
        this.teleport(
            Transform(
                pos.x + 0.5, pos.y + 0.5, pos.z + 0.5,
                rotation.yaw, rotation.pitch,
                level!!
            ), null
        )

        this.setDataProperty(
            EntityDataTypes.BED_POSITION,
            BlockVector3(pos.x.toInt(), pos.y.toInt(), pos.z.toInt())
        )
        this.setPlayerFlag(PlayerFlag.SLEEP)
        this.setSpawn(fromObject(pos, level!!), SpawnPointType.BLOCK)
        level!!.sleepTicks = 75

        this.timeSinceRest = 0

        return true
    }

    fun stopSleep() {
        if (this.sleeping != null) {
            Server.instance.pluginManager.callEvent(
                PlayerBedLeaveEvent(
                    this,
                    level!!.getBlock(
                        sleeping!!
                    )
                )
            )

            this.sleeping = null
            this.setDataProperty(EntityDataTypes.BED_POSITION, BlockVector3(0, 0, 0))
            this.setPlayerFlag(PlayerFlag.SLEEP)


            level!!.sleepTicks = 0

            this.sendPacket(
                AnimatePacket(
                    action = AnimatePacket.Action.WakeUp,
                    targetRuntimeID = this.getRuntimeID().toULong(),
                    actionData = null,
                )
            )
        }
    }

    fun setGamemode(gamemode: Int): Boolean {
        return this.setGamemode(gamemode, false, null)
    }

    /**
     * AdventureSettings=null
     *
     * @see .setGamemode
     */
    fun setGamemode(gamemode: Int, serverSide: Boolean): Boolean {
        return this.setGamemode(gamemode, serverSide, null)
    }

    fun setGamemode(gamemode: Int, serverSide: Boolean, newSettings: AdventureSettings?): Boolean {
        return this.setGamemode(gamemode, serverSide, newSettings, false)
    }

    /**
     * Set GameMode
     *
     * @param gamemode    The player game mode to set
     * @param serverSide  Whether to update only the game mode of players on the server side. If true, the game mode update package will not be sent to the client
     * @param newSettings New Adventure Settings
     * @param forceUpdate Whether to force an update. If true, the check for the parameter 'gamemode' will be canceled
     * @return gamemode
     */
    fun setGamemode(
        gamemode: Int,
        serverSide: Boolean,
        newSettings: AdventureSettings?,
        forceUpdate: Boolean
    ): Boolean {
        var newSettings = newSettings
        if (!forceUpdate && (gamemode < 0 || gamemode > 3 || this.gamemode == gamemode)) {
            return false
        }

        if (newSettings == null) {
            newSettings = adventureSettings.clone()
            newSettings.set(AdventureSettings.Type.WORLD_IMMUTABLE, (gamemode and 0x02) > 0)
            newSettings.set(AdventureSettings.Type.BUILD, (gamemode and 0x02) <= 0)
            newSettings.set(AdventureSettings.Type.WORLD_BUILDER, (gamemode and 0x02) <= 0)
            newSettings.set(AdventureSettings.Type.ALLOW_FLIGHT, (gamemode and 0x01) > 0)
            newSettings.set(AdventureSettings.Type.NO_CLIP, gamemode == SPECTATOR)
            newSettings[AdventureSettings.Type.FLYING] = when (gamemode) {
                SURVIVAL -> false
                CREATIVE -> newSettings[AdventureSettings.Type.FLYING]
                ADVENTURE -> false
                SPECTATOR -> true
                else -> throw IllegalStateException("Unexpected game mode: $gamemode")
            }
        }

        val ev: PlayerGameModeChangeEvent
        Server.instance.pluginManager.callEvent(PlayerGameModeChangeEvent(this, gamemode, newSettings).also { ev = it })

        if (ev.cancelled) {
            return false
        }

        this.gamemode = gamemode

        if (this.isSpectator) {
            this.onGround = false
            this.setDataFlag(EntityFlag.HAS_COLLISION, false)
        } else {
            this.setDataFlag(EntityFlag.HAS_COLLISION, true)
        }

        namedTag.putInt("playerGameType", this.gamemode)

        this.adventureSettings = (ev.newAdventureSettings)

        if (!serverSide) {
            val networkGamemode = toNetworkGamemode(gamemode)
            val pk = UpdatePlayerGameTypePacket(
                gameType = org.chorus_oss.protocol.types.GameType.entries[networkGamemode],
                playerUniqueID = this.getUniqueID(),
                tick = 0uL,
            )
            val players: HashSet<Player> =
                Sets.newHashSet<Player>(Server.instance.onlinePlayers.values)
            //不向自身发送UpdatePlayerGameTypePacket，我们将使用SetPlayerGameTypePacket
            players.remove(this)
            //我们需要给所有玩家发送此包，来使玩家客户端能正确渲染玩家实体
            //eg: 观察者模式玩家对于gm 0 1 2的玩家不可见
            Server.broadcastPacket(players, pk)
            //对于自身，我们使用SetPlayerGameTypePacket来确保与WaterDog的兼容
            val pk2 = SetPlayerGameTypePacket(
                gameType = org.chorus_oss.protocol.types.GameType.entries[networkGamemode],
            )
            this.sendPacket(pk2)
        }

        this.resetFallDistance()

        return true
    }

    fun sendSettings() {
        adventureSettings.update()
    }

    val isSurvival: Boolean
        /**
         * 该玩家是否为生存模式。
         *
         *
         * Whether the player is in survival mode?
         *
         * @return boolean
         */
        get() = this.gamemode == SURVIVAL

    val isCreative: Boolean
        /**
         * 该玩家是否为创造模式。
         *
         *
         * Whether the player is in creative mode?
         *
         * @return boolean
         */
        get() = this.gamemode == CREATIVE

    val isSpectator: Boolean
        /**
         * 该玩家是否为观察者模式。
         *
         *
         * Whether the player is in spectator mode?
         *
         * @return boolean
         */
        get() = this.gamemode == SPECTATOR

    val isAdventure: Boolean
        /**
         * 该玩家是否为冒险模式。
         *
         *
         * Whether the player is in adventure mode?
         *
         * @return boolean
         */
        get() = this.gamemode == ADVENTURE

    override fun getDrops(): Array<Item> {
        if (!this.isCreative && !this.isSpectator) {
            return super.getDrops()
        }

        return Item.EMPTY_ARRAY
    }

    @ApiStatus.Internal
    fun fastMove(dx: Double, dy: Double, dz: Double): Boolean {
        position.x += dx
        position.y += dy
        position.z += dz
        this.recalculateBoundingBox(true)

        this.checkChunks()

        if (!this.isSpectator) {
            this.checkGroundState(dx, dy, dz, dx, dy, dz)
            this.updateFallState(this.onGround)
        }
        return true
    }


    @ApiStatus.Internal
    fun reCalcOffsetBoundingBox(): AxisAlignedBB {
        val dx = this.getWidth() / 2
        val dz = this.getWidth() / 2
        return offsetBoundingBox.setBounds(
            position.x - dx,
            position.y,
            position.z - dz,
            position.x + dx, position.y + this.getHeight(), position.z + dz
        )
    }

    override fun moveDelta() {
        this.sendPosition(
            this.position,
            rotation.yaw,
            rotation.pitch,
            MovePlayerPacket.Companion.Mode.Normal,
            viewers.values.toTypedArray()
        )
    }

    /**
     * 每次调用此方法都会向客户端发送motion包。若多次调用，motion将在客户端叠加
     *
     *
     *
     * @param motion 运动向量<br></br>a motion vector
     * @return 调用是否成功
     */
    override fun setMotion(motion: Vector3): Boolean {
        if (super.setMotion(motion)) {
            if (this.chunk != null) {
                this.addMotion(this.motion.x, this.motion.y, this.motion.z) //Send to others
                val pk = SetActorMotionPacket(
                    actorRuntimeID = this.getRuntimeID().toULong(),
                    motion = Vector3f(motion),
                    tick = 0uL,
                )
                this.sendPacket(pk) //Send to self
            }
            if (this.motion.y > 0) {
                // TODO: check this
                this.startAirTicks =
                    ((-(ln(this.getGravity() / (this.getGravity() + this.getDrag() * this.motion.y))) / this.getDrag()) * 2 + 5).toInt()
            }

            return true
        }

        return false
    }

    /**
     * Send attributes to client
     */
    fun sendAttributes() {
        val pk = UpdateAttributesPacket(
            actorRuntimeID = this.getRuntimeID().toULong(),
            attributes = listOf(
                getAttribute(Attribute.MAX_HEALTH)
                    .setMaxValue(getMaxHealth().toFloat())
                    .setValue(if (health > 0) (if (health < getMaxHealth()) health else getMaxHealth().toFloat()) else 0f),
                getAttribute(Attribute.MAX_HUNGER)
                    .setValue(foodData!!.getFood().toFloat()),
                getAttribute(Attribute.MOVEMENT_SPEED).setValue(this.movementSpeed),
                getAttribute(Attribute.EXPERIENCE_LEVEL).setValue(
                    experienceLevel.toFloat()
                ),
                getAttribute(Attribute.EXPERIENCE).setValue(
                    (experience.toFloat()) / calculateRequireExperience(
                        experienceLevel
                    )
                )
            ).map(org.chorus_oss.protocol.types.attribute.Attribute::invoke),
            tick = 0u,
        )
        this.sendPacket(pk)
    }

    /**
     * Send the fog settings to the client
     */
    fun sendFogStack() {
        val pk = PlayerFogPacket(
            stack = this.fogStack.map { it.second }
        )
        this.sendPacket(pk)
    }

    /**
     * Send camera presets to cilent
     */
    fun sendCameraPresets() {
        sendPacket(
            CameraPresetsPacket(
                presets = presets.values.map(CameraPreset::invoke)
            )
        )
    }

    override fun getHeight(): Float {
        if (riding is EntityHorse) {
            return 1.1f
        }
        return super.getHeight()
    }

    override fun setSwimming(value: Boolean) {
        //Stopping a swim at a height of 1 block will still send a STOPSWIMMING ACTION from the client, but the player will still be swimming height,so skip the action
        if (!value && level!!.getBlock(position.up()).isSolid && level!!.getBlock(
                position.down()
            ).isSolid
        ) {
            return
        }
        super.setSwimming(value)
    }

    override fun onUpdate(currentTick: Int): Boolean {
        if (!this.loggedIn) {
            return false
        }

        val tickDiff = currentTick - this.lastUpdate

        if (tickDiff <= 0) {
            return true
        }

        this.messageLimitCounter = 2

        this.lastUpdate = currentTick

        if (this.fishing != null && level!!.tick % 20 == 0) {
            if (position.distance(fishing!!.position) > 33) {
                this.stopFishing(false)
            }
        }

        if (!this.isAlive() && this.spawned) {
            if (level!!.gameRules.getBoolean(GameRule.DO_IMMEDIATE_RESPAWN)) {
                this.despawnFromAll()
                return true
            }
            level!!.scheduler.scheduleDelayedTask(InternalPlugin.INSTANCE, { this.despawnFromAll() }, 10)
            return true
        }

        if (this.spawned) {
            if (motion.x != 0.0 || motion.y != 0.0 || motion.z != 0.0) {
                this.setMotion(this.motion)
                motion.setComponents(0.0, 0.0, 0.0)
            }

            while (!clientMovements.isEmpty()) {
                this.positionChanged = true
                this.handleMovement(clientMovements.poll())
            }

            if (!this.isSpectator) {
                this.checkNearEntities()
            }

            this.entityBaseTick(tickDiff)

            if (Server.instance.getDifficulty() == 0 && level!!.gameRules.getBoolean(GameRule.NATURAL_REGENERATION)) {
                if (this.health < this.getMaxHealth() && this.ticksLived % 20 == 0) {
                    this.heal(1f)
                }
            }

            if (this.isOnFire() && this.lastUpdate % 10 == 0) {
                if (this.isCreative && !this.isInsideOfFire()) {
                    this.extinguish()
                } else if (level!!.isRaining) {
                    if (level!!.canBlockSeeSky(this.position)) {
                        this.extinguish()
                    }
                }
            }

            if (!this.isSpectator && this.speed != null) {
                if (this.onGround) {
                    if (this.inAirTicks != 0) {
                        this.startAirTicks = 5
                    }
                    this.inAirTicks = 0
                    this.highestPosition = position.y
                    if (this.isGliding()) {
                        this.setGliding(false)
                    }
                } else {
                    this.lastInAirTick = level!!.tick
                    //check fly for player
                    if (this.isCheckingMovement && !this.isGliding() && !Server.instance.allowFlight && !adventureSettings[AdventureSettings.Type.ALLOW_FLIGHT] && this.inAirTicks > 20 && !this.isSleeping() && !this.isImmobile() && !this.isSwimming() && this.riding == null && !this.hasEffect(
                            EffectType.LEVITATION
                        ) && !this.hasEffect(EffectType.SLOW_FALLING)
                    ) {
                        val expectedVelocity =
                            (-this.getGravity()) / (getDrag().toDouble()) - ((-this.getGravity()) / (getDrag().toDouble())) * exp(
                                -(getDrag().toDouble()) * ((this.inAirTicks - this.startAirTicks).toDouble())
                            )
                        val diff = (speed!!.y - expectedVelocity) * (speed!!.y - expectedVelocity)

                        val block = level!!.getBlock(this.position)
                        val blockId = block.id
                        val ignore = blockId == BlockID.LADDER || blockId == BlockID.VINE || blockId == BlockID.WEB
                                || blockId == BlockID.SCAFFOLDING // || (blockId == Block.SWEET_BERRY_BUSH && block.getDamage() > 0);

                        if (!this.hasEffect(EffectType.JUMP_BOOST) && diff > 0.6 && expectedVelocity < speed!!.y && !ignore) {
                            if (this.inAirTicks < 150) {
                                val ev = PlayerInvalidMoveEvent(this, true)
                                Server.instance.pluginManager.callEvent(ev)

                                if (!ev.cancelled) {
                                    this.setMotion(Vector3(0.0, expectedVelocity, 0.0))
                                }
                            } else if (this.kick(
                                    PlayerKickEvent.Reason.FLYING_DISABLED,
                                    "Flying is not enabled on this server"
                                )
                            ) {
                                return false
                            }
                        }
                        if (ignore) {
                            this.resetFallDistance()
                        }
                    }

                    if (position.y > highestPosition) {
                        this.highestPosition = position.y
                    }

                    // Wiki: 使用鞘翅滑翔时在垂直高度下降率低于每刻 0.5 格的情况下，摔落高度被重置为 1 格。
                    // Wiki: 玩家在较小的角度和足够低的速度上着陆不会受到坠落伤害。着陆时临界伤害角度为50°，伤害值等同于玩家从滑行的最高点直接摔落到着陆点受到的伤害。
                    if (this.isGliding() && abs(speed!!.y) < 0.5 && rotation.pitch <= 40) {
                        this.resetFallDistance()
                    }

                    ++this.inAirTicks
                }

                if (this.foodData != null) {
                    foodData!!.tick(tickDiff)
                }

                //鞘翅检查和耐久计算
                if (this.isGliding()) {
                    val playerInventory = this.inventory
                    if (playerInventory != null) {
                        val chestplate = playerInventory.chestplate
                        if ((chestplate == null || chestplate.id != ItemID.ELYTRA)) {
                            this.setGliding(false)
                        } else if (this.age % (20 * (chestplate.getEnchantmentLevel(Enchantment.ID_DURABILITY) + 1)) == 0 && !isCreative) {
                            val newDamage = chestplate.damage + 1
                            if (newDamage < chestplate.maxDurability) {
                                chestplate.damage = newDamage
                                playerInventory.setChestplate(chestplate)
                            } else {
                                this.setGliding(false)
                            }
                        }
                    }
                }
            }

            if (!this.isSleeping()) {
                timeSinceRest++
            }

            if (Server.instance.getServerAuthoritativeMovement() > 0 && this.breakingBlock != null) { //仅服务端权威使用，因为客户端权威continue break是正常的
                onBlockBreakContinue(breakingBlock!!.position, this.breakingBlockFace)
            }

            //reset move status
            this.newPosition = null
            this.positionChanged = false
            if (this.speed == null) {
                this.speed = Vector3(0.0, 0.0, 0.0)
            } else {
                speed!!.setComponents(0.0, 0.0, 0.0)
            }
        }

        if (currentTick % 10 == 0) {
            this.checkInteractNearby()
        }

        if (this.spawned && !dummyBossBars.isEmpty() && currentTick % 100 == 0) {
            dummyBossBars.values.forEach(Consumer { obj: DummyBossBar? -> obj!!.updateBossEntityPosition() })
        }

        updateBlockingFlag()

        val foodData = foodData
        if (this.ticksLived % 40 == 0 && foodData != null) {
            foodData.sendFood()
        }

        return true
    }

    /**
     * 检查附近可交互的实体(插件一般不使用)
     *
     *
     * Check for nearby interactable entities (not generally used by plugins)
     */
    fun checkInteractNearby() {
        val interactDistance = if (isCreative) 5 else 3
        if (canInteract(this.position, interactDistance.toDouble())) {
            if (getEntityPlayerLookingAt(interactDistance) != null) {
                val onInteract = getEntityPlayerLookingAt(interactDistance)
                buttonText = onInteract!!.getInteractButtonText(this)
            } else {
                buttonText = ""
            }
        } else {
            buttonText = ""
        }
    }

    private fun getEntityAtPosition(nearbyEntities: Array<Entity>, x: Int, y: Int, z: Int): EntityInteractable? {
        for (nearestEntity in nearbyEntities) {
            if (nearestEntity.position.floorX == x && nearestEntity.position.floorY == y && nearestEntity.position.floorZ == z && nearestEntity is EntityInteractable
                && (nearestEntity as EntityInteractable).canDoInteraction()
            ) {
                return nearestEntity
            }
        }
        return null
    }

    /**
     * 返回玩家目前正在看的实体。
     *
     *
     * Returns the Entity the player is looking at currently
     *
     * @param maxDistance 检查实体的最大距离<br></br>the maximum distance to check for entities
     * @return Entity|null    如果没有找到实体，则为NULL，或者是实体的一个实例。<br></br>either NULL if no entity is found or an instance of the entity
     */
    fun getEntityPlayerLookingAt(maxDistance: Int): EntityInteractable? {
        var entity: EntityInteractable? = null


        val nearbyEntities = level!!.getNearbyEntities(
            boundingBox.grow(maxDistance.toDouble(), maxDistance.toDouble(), maxDistance.toDouble()),
            this
        )

        // get all blocks in looking direction until the max interact distance is reached (it's possible that startblock isn't found!)
        try {
            val itr = BlockIterator(
                level!!,
                locator.position, getDirectionVector(), getEyeHeight().toDouble(), maxDistance
            )
            if (itr.hasNext()) {
                var block: Block
                while (itr.hasNext()) {
                    block = itr.next()
                    entity = getEntityAtPosition(
                        nearbyEntities.toTypedArray(),
                        block.position.floorX,
                        block.position.floorY,
                        block.position.floorZ
                    )
                    if (entity != null) {
                        break
                    }
                }
            }
        } catch (ex: Exception) {
            // nothing to log here!
        }

        return entity
    }

    fun regenerateEnchantmentSeed() {
        this.enchSeed = ThreadLocalRandom.current().nextInt(Int.MAX_VALUE)
    }

    fun checkNetwork() {
        if (!this.isOnline) {
            return
        }

        if (nextChunkOrderRun-- <= 0 || this.chunk == null) {
            playerChunkManager.tick()
        }

        if (this.chunkLoadCount >= this.spawnThreshold && !this.spawned && loggedIn) {
            session.notifyTerrainReady()
        }
    }

    @JvmOverloads
    fun canInteract(pos: Vector3, maxDistance: Double, maxDiff: Double = 6.0): Boolean {
        if (position.distanceSquared(pos) > maxDistance * maxDistance) {
            return false
        }

        val dV = this.getDirectionPlane()
        val dot = dV.dot(Vector2(position.x, position.z))
        val dot1 = dV.dot(Vector2(pos.x, pos.z))
        return (dot1 - dot) >= -maxDiff
    }

    /**
     * 以该玩家的身份发送一条聊天信息。如果消息以/（正斜杠）开头，它将被视为一个命令。
     *
     *
     * Sends a chat message as this player. If the message begins with a / (forward-slash) it will be treated
     * as a command.
     *
     * @param message 发送的信息<br></br>message to send
     * @return successful
     */
    fun chat(message: String): Boolean {
        var message = message
        if (!this.spawned || !this.isAlive()) {
            return false
        }

        this.resetInventory()

        if (this.removeFormat) {
            message = clean(message, true)
        }

        for (msg in message.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            if (msg.trim { it <= ' ' }.isNotEmpty() && msg.length <= 512 && messageLimitCounter-- > 0) {
                val chatEvent = PlayerChatEvent(this, msg)
                Server.instance.pluginManager.callEvent(chatEvent)
                if (!chatEvent.cancelled) {
                    Server.instance.broadcastMessage(
                        Server.instance.lang.tr(
                            chatEvent.format, *arrayOf<String>(
                                chatEvent.player.displayName, chatEvent.message!!
                            )
                        ), chatEvent.recipients
                    )
                }
            }
        }

        return true
    }

    /**
     * [PlayerKickEvent.Reason] = [PlayerKickEvent.Reason.UNKNOWN]
     *
     * @see .kick
     */
    fun kick(reason: String, isAdmin: Boolean): Boolean {
        return this.kick(PlayerKickEvent.Reason.UNKNOWN, reason, isAdmin)
    }

    /**
     * [PlayerKickEvent.Reason] = [PlayerKickEvent.Reason.UNKNOWN]
     *
     * @see .kick
     */
    /**
     * reason=empty string
     *
     * @see .kick
     */
    @JvmOverloads
    fun kick(reason: String = ""): Boolean {
        return kick(PlayerKickEvent.Reason.UNKNOWN, reason)
    }

    /**
     * @see .kick
     */
    /**
     * @see .kick
     */
    @JvmOverloads
    fun kick(reason: PlayerKickEvent.Reason, isAdmin: Boolean = true): Boolean {
        return this.kick(reason, reason.toString(), isAdmin)
    }

    /**
     * 踢出该玩家
     *
     *
     * Kick out the player
     *
     * @param reason       原因枚举<br></br>Cause Enumeration
     * @param reasonString 原因字符串<br></br>Reason String
     * @param isAdmin      是否来自管理员踢出<br></br>Whether from the administrator kicked out
     * @return boolean
     */
    /**
     * @see .kick
     */
    @JvmOverloads
    fun kick(reason: PlayerKickEvent.Reason, reasonString: String, isAdmin: Boolean = true): Boolean {
        val ev: PlayerKickEvent
        Server.instance.pluginManager.callEvent(
            PlayerKickEvent(
                this, reason,
                leaveMessage
            ).also { ev = it })
        if (!ev.cancelled) {
            val message = if (isAdmin) {
                if (!Server.instance.bannedPlayers.isBanned(getEntityName())) {
                    "Kicked by admin." + (if (!reasonString.isEmpty()) " Reason: $reasonString" else "")
                } else {
                    reasonString
                }
            } else {
                if (reasonString.isEmpty()) {
                    "disconnectionScreen.noReason"
                } else {
                    reasonString
                }
            }

            this.close(ev.quitMessage, message)

            return true
        }

        return false
    }

    /**
     * 设置玩家的可视距离(范围 0--[Server.getViewDistance])
     *
     *
     * Set the player's viewing distance (range 0--[Server.getViewDistance])
     *
     * @param distance 可视距离
     */
    fun setViewDistance(distance: Int) {
        this.chunkRadius = distance

        this.sendPacket(
            ChunkRadiusUpdatedPacket(
                radius = distance
            )
        )
    }

    /**
     * 得到玩家的可视距离
     *
     *
     * Get the player's viewing distance
     *
     * @return int
     */
    fun getViewDistance(): Int {
        return this.chunkRadius
    }

    override fun sendMessage(message: String) {
        val pk = TextPacket(
            textType = TextPacket.Companion.TextType.Raw,
            needsTranslation = false,
            sourceName = null,
            message = Server.instance.lang.tr(message),
            parameters = emptyList(),
            xuid = "",
            platformChatID = "",
            filteredMessage = ""
        )
        this.sendPacket(pk)
    }

    override fun sendMessage(message: TextContainer) {
        if (message is TranslationContainer) {
            this.sendTranslation(message.text, message.parameters)
            return
        }
        this.sendMessage(message.text)
    }


    @OptIn(ExperimentalUuidApi::class)
    override fun sendCommandOutput(container: CommandOutputContainer) {
        if (level!!.gameRules.getBoolean(GameRule.SEND_COMMAND_FEEDBACK)) {
            val pk = CommandOutputPacket(
                originData = CommandOriginData(
                    CommandOriginData.Companion.Origin.Player,
                    Uuid(getUUID()), "", null
                ),
                outputType = CommandOutputType.AllOutput,
                successCount = container.successCount.toUInt(),
                outputMessages = container.messages.map(CommandOutputMessage::invoke),
            )
            this.sendPacket(pk)
        }
    }

    /**
     * 在玩家聊天栏发送一个JSON文本
     *
     *
     * Send a JSON text in the player chat bar
     *
     * @param text JSON文本<br></br>Json text
     */
    fun sendRawTextMessage(text: RawText) {
        val pk = TextPacket(
            textType = TextPacket.Companion.TextType.Object,
            needsTranslation = false,
            sourceName = null,
            message = text.toRawText(),
            parameters = emptyList(),
            xuid = "",
            platformChatID = "",
            filteredMessage = ""
        )
        this.sendPacket(pk)
    }

    /**
     * 在玩家聊天栏发送一个多语言翻译文本，示例:<br></br>`message="Test Message {%0} {%1}" parameters=["Hello","World"]`<br></br>
     * 实际消息内容`"Test Message Hello World"`
     *
     *
     * Send a multilingual translated text in the player chat bar, example:<br></br> `message="Test Message {%0} {%1}" parameters=["Hello", "World"]`<br></br>
     * actual message content `"Test Message Hello World"`
     *
     * @param message    消息
     * @param parameters 参数
     */
    /**
     * @see .sendTranslation
     */
    @JvmOverloads
    fun sendTranslation(message: String, parameters: Array<String> = emptyArray()) {
        val packet = TextPacket(

            textType = when (Server.instance.settings.baseSettings.forceServerTranslate) {
                true -> TextPacket.Companion.TextType.Raw
                false -> TextPacket.Companion.TextType.Translation
            },

            needsTranslation = !Server.instance.settings.baseSettings.forceServerTranslate,
            sourceName = null,
            message = when (Server.instance.settings.baseSettings.forceServerTranslate) {
                true -> Server.instance.lang.tr(message, *parameters)
                false -> Server.instance.lang.tr(message, parameters, "nukkit.", true)
            },

            parameters = when (Server.instance.settings.baseSettings.forceServerTranslate) {
                true -> null
                false -> parameters.map { Server.instance.lang.tr(it, parameters, "nukkit.", true) }
            },

            xuid = "",
            platformChatID = "",
            filteredMessage = ""
        )

        this.sendPacket(packet)
    }

    /**
     * @see .sendChat
     */
    fun sendChat(message: String) {
        this.sendChat(source = "", message)
    }

    /**
     * 在玩家聊天栏发送一个文本
     *
     *
     * Send a text in the player chat bar
     *
     * @param message 消息
     */
    fun sendChat(source: String, message: String) {

        val pk = TextPacket(
            textType = TextPacket.Companion.TextType.Chat,
            needsTranslation = false,
            sourceName = source,
            message = Server.instance.lang.tr(message),
            parameters = emptyList(),
            xuid = "",
            platformChatID = "",
            filteredMessage = ""
        )
        this.sendPacket(packet = pk)
    }

    /**
     * 在玩家物品栏上方发送一个弹出式的文本
     *
     *
     * Send a pop-up text above the player's item bar
     *
     * @param message 消息
     */
    // TODO: Support Translation Parameters
    /**
     * @see .sendPopup
     */
    @JvmOverloads
    fun sendPopup(message: String, subtitle: String? = "") {

        val pk = TextPacket(
            textType = TextPacket.Companion.TextType.Popup,
            needsTranslation = false,
            sourceName = null,
            message = message,
            parameters = emptyList(),
            xuid = "",
            platformChatID = "",
            filteredMessage = ""
        )

        this.sendPacket(pk)
    }

    /**
     * 在玩家物品栏上方发送一个提示文本
     *
     *
     * Send a tip text above the player's item bar
     *
     * @param message 消息
     */
    fun sendTip(message: String) {

        val pk = TextPacket(
            textType = TextPacket.Companion.TextType.Tip,
            needsTranslation = false,
            sourceName = null,
            message = message,
            parameters = emptyList(),
            xuid = "",
            platformChatID = "",
            filteredMessage = ""
        )

        this.sendPacket(pk)
    }


    /**
     * 清除掉玩家身上正在显示的标题信息。
     *
     *
     * Clears away the title info being displayed on the player.
     */
    fun clearTitle() {
        val pk = SetTitlePacket(
            actionType = SetTitlePacket.Companion.ActionType.Clear,
            text = "",
            fadeInDuration = 0,
            remainDuration = 0,
            fadeOutDuration = 0,
            xuid = "",
            platformOnlineID = "",
            filteredMessage = ""
        )
        this.sendPacket(pk)
    }

    /**
     * 为下一个显示的标题重新设置标题动画时间和副标题。
     *
     *
     * Resets both title animation times and subtitle for the next shown title.
     */
    fun resetTitleSettings() {
        val pk = SetTitlePacket(
            actionType = SetTitlePacket.Companion.ActionType.Reset,
            text = "",
            fadeInDuration = 0,
            remainDuration = 0,
            fadeOutDuration = 0,
            xuid = "",
            platformOnlineID = "",
            filteredMessage = ""
        )
        this.sendPacket(pk)
    }

    /**
     * 设置副标题，在主标题下方显示。
     *
     *
     * Set subtitle to be displayed below the main title.
     *
     * @param subtitle 副标题
     */
    fun setSubtitle(subtitle: String) {
        val pk = SetTitlePacket(
            actionType = SetTitlePacket.Companion.ActionType.SetSubtitle,
            text = subtitle,
            fadeInDuration = 0,
            remainDuration = 0,
            fadeOutDuration = 0,
            xuid = "",
            platformOnlineID = "",
            filteredMessage = ""
        )
        this.sendPacket(pk)
    }

    /**
     * 设置一个JSON文本副标题。
     *
     *
     * Set a JSON text subtitle.
     *
     * @param text JSON文本<br></br>JSON text
     */
    fun setRawTextSubTitle(text: RawText) {
        val pk = SetTitlePacket(
            actionType = SetTitlePacket.Companion.ActionType.SetSubtitleJSON,
            text = text.toRawText(),
            fadeInDuration = 0,
            remainDuration = 0,
            fadeOutDuration = 0,
            xuid = "",
            platformOnlineID = "",
            filteredMessage = ""
        )
        this.sendPacket(pk)
    }

    /**
     * 设置标题动画时间
     *
     *
     * Set title animation time
     *
     * @param fadein   淡入时间
     * @param duration 持续时间
     * @param fadeout  淡出时间
     */
    fun setTitleAnimationTimes(fadein: Int, duration: Int, fadeout: Int) {
        val pk = SetTitlePacket(
            actionType = SetTitlePacket.Companion.ActionType.SetDurations,
            text = "",
            fadeInDuration = fadein,
            remainDuration = duration,
            fadeOutDuration = fadeout,
            xuid = "",
            platformOnlineID = "",
            filteredMessage = ""
        )
        this.sendPacket(pk)
    }

    /**
     * 设置一个JSON文本标题。
     *
     *
     * Set a JSON text title.
     *
     * @param text JSON文本<br></br>JSON text
     */
    fun setRawTextTitle(text: RawText) {
        val pk = SetTitlePacket(
            actionType = SetTitlePacket.Companion.ActionType.SetTitleJSON,
            text = text.toRawText(),
            fadeInDuration = 0,
            remainDuration = 0,
            fadeOutDuration = 0,
            xuid = "",
            platformOnlineID = "",
            filteredMessage = ""
        )
        this.sendPacket(pk)
    }


    /**
     * 在玩家视角正中央发送一个标题文本。
     *
     *
     * Send a title text in the center of the player's view.
     *
     * @param title    标题
     * @param subtitle 副标题
     * @param fadeIn   淡入时间<br></br>fadeIn time(tick)
     * @param stay     持续时间<br></br>stay time
     * @param fadeOut  淡出时间<br></br>fadeOut time
     */
    /**
     * `subtitle=null,fadeIn=20,stay=20,fadeOut=5`
     *
     * @see .sendTitle
     */
    /**
     * `fadeIn=20,stay=20,fadeOut=5`
     *
     * @see .sendTitle
     */
    @JvmOverloads
    fun sendTitle(title: String?, subtitle: String? = null, fadeIn: Int = 20, stay: Int = 20, fadeOut: Int = 5) {
        this.setTitleAnimationTimes(fadeIn, stay, fadeOut)
        if (!Strings.isNullOrEmpty(subtitle)) {
            this.setSubtitle(subtitle!!)
        }
        // title won't send if an empty string is used.
        this.setTitle((if (Strings.isNullOrEmpty(title)) " " else title)!!)
    }

    /**
     * 在玩家物品栏上方发送一个ActionBar消息。
     *
     *
     * Send a ActionBar text above the player's item bar.
     *
     * @param title    消息
     * @param fadein   淡入时间
     * @param duration 持续时间
     * @param fadeout  淡出时间
     */
    /**
     * fadein=1,duration=0,fadeout=1
     *
     * @see .sendActionBar
     */
    @JvmOverloads
    fun sendActionBar(title: String, fadein: Int = 1, duration: Int = 0, fadeout: Int = 1) {
        val pk = SetTitlePacket(
            actionType = SetTitlePacket.Companion.ActionType.SetActionbar,
            text = title,
            fadeInDuration = fadein,
            remainDuration = duration,
            fadeOutDuration = fadeout,
            xuid = "",
            platformOnlineID = "",
            filteredMessage = ""
        )
        this.sendPacket(pk)
    }

    /**
     * fadein=1,duration=0,fadeout=1
     *
     * @see .setRawTextActionBar
     */
    fun setRawTextActionBar(text: RawText) {
        this.setRawTextActionBar(text, 1, 0, 1)
    }

    /**
     * 设置一个JSON ActionBar消息。
     *
     *
     * Set a JSON ActionBar text.
     *
     * @param text     JSON文本<br></br>JSON text
     * @param fadein   淡入时间
     * @param duration 持续时间
     * @param fadeout  淡出时间
     */
    fun setRawTextActionBar(text: RawText, fadein: Int, duration: Int, fadeout: Int) {
        val pk = SetTitlePacket(
            actionType = SetTitlePacket.Companion.ActionType.SetActionbarJSON,
            text = text.toRawText(),
            fadeInDuration = fadein,
            remainDuration = duration,
            fadeOutDuration = fadeout,
            xuid = "",
            platformOnlineID = "",
            filteredMessage = ""
        )
        this.sendPacket(pk)
    }


    override fun close() {
        this.close("")
    }

    /**
     * `notify=true`
     *
     * @see .close
     */
    fun close(reason: String) {
        this.close(this.leaveMessage, reason)
    }

    fun close(message: String, reason: String) {
        this.close(TextContainer(message), reason)
    }

    /**
     * 关闭该玩家的连接及其一切活动，和[.kick]差不多效果，区别在于[.kick]是基于`close`实现的。
     *
     *
     * Closing the player's connection and all its activities is almost as function as [.kick], the difference is that [.kick] is implemented based on `close`.
     *
     * @param message PlayerQuitEvent事件消息<br></br>PlayerQuitEvent message
     * @param reason  登出原因<br></br>Reason for logout
     */
    /**
     * `reason="generic",notify=true`
     *
     * @see .close
     */
    @JvmOverloads
    fun close(message: TextContainer, reason: String = "generic") {
        var reason1 = reason
        if (!connected.compareAndSet(true, false) && this.closed) {
            return
        }
        //output logout infomation
        log.info(
            Server.instance.lang.tr(
                "chorus.player.logOut",
                TextFormat.AQUA.toString() + this.getEntityName() + TextFormat.WHITE,
                this.address,
                port.toString(),
                Server.instance.lang.tr(reason1)
            )
        )

        resetInventory()
        for (inv in windows.keys) {
            if (permanentWindows.contains(windows[inv])) {
                val windowId = this.getWindowId(inv)
                this.closingWindowId = windowId
                inv.close(this)
                updateTrackingPositions(true)
            }
        }

        //handle scoreboardManager#beforePlayerQuit
        val scoreboardManager = Server.instance.scoreboardManager
        scoreboardManager.beforePlayerQuit(this)

        //dismount horse
        if (riding is EntityRideable) {
            riding!!.dismountEntity(this)
        }

        unloadAllUsedChunk()

        //send disconnection packet
        val hideDisconnectionScreen = reason1.isBlank()
        if (hideDisconnectionScreen) {
            reason1 = BedrockDisconnectReasons.DISCONNECTED
        }

        val packet = DisconnectPacket(
            reason = DisconnectFailReason.Unknown,
            hideDisconnectionScreen,
            message = reason1,
            filteredMessage = reason1
        )
        session.sendPacketSync(packet)

        //call quit event
        var ev: PlayerQuitEvent? = null
        if (getEntityName().isNotEmpty()) {
            Server.instance.pluginManager.callEvent(PlayerQuitEvent(this, message, true, reason1).also { ev = it })
            if (this.fishing != null) {
                this.stopFishing(false)
            }
        }
        // Close the temporary windows first, so they have chance to change all inventories before being disposed
        if (ev != null && ev.autoSave) {
            this.save()
        }
        super.close()

        windows.clear()
        hiddenPlayers.clear()
        //remove player from playerlist
        Server.instance.removeOnlinePlayer(this)
        //remove player from players map
        Server.instance.removePlayer(this)

        Server.instance.pluginManager.unsubscribeFromPermission(Server.BROADCAST_CHANNEL_USERS, this)
        Server.instance.pluginManager.unsubscribeFromPermission(Server.BROADCAST_CHANNEL_ADMINISTRATIVE, this)
        // broadcast disconnection message
        if (ev != null && (this.getEntityName() != "") && this.spawned && (ev.quitMessage.toString() != "")) {
            Server.instance.broadcastMessage(ev.quitMessage!!)
        }

        hasSpawned.clear()
        this.loggedIn = false
        this.spawned = false
        this.spawnPoint = null
        this.riding = null
        this.chunk = null

        if (this.perm != null) {
            perm!!.clearPermissions()
            this.perm = null
        }

        // close player network session
        log.debug("Closing player network session")
        log.debug(reason1)
        session.close(null)
    }

    @Synchronized
    fun unloadAllUsedChunk() {
        //save player data
        //unload chunk for the player
        val iterator = playerChunkManager.usedChunks.iterator()
        try {
            while (iterator.hasNext()) {
                val l = iterator.next()
                val chunkX = getHashX(l)
                val chunkZ = getHashZ(l)
                if (level!!.unregisterChunkLoader(this, chunkX, chunkZ, false)) {
                    val pk = LevelChunkPacket(
                        position = ChunkPos(
                            chunkX,
                            chunkZ,
                        ),
                        dimension = level!!.dimension,
                        subChunkCount = 0u,
                        subChunkLimit = 0u,
                        cacheEnabled = false,
                        blobHashes = emptyList(),
                        data = ByteString(),
                    )
                    this.sendChunk(chunkX, chunkZ, pk)
                    for (entity in level!!.getChunkEntities(chunkX, chunkZ).values) {
                        if (entity !== this) {
                            entity.despawnFrom(this)
                        }
                    }
                    iterator.remove()
                }
            }
        } catch (e: Exception) {
            log.error("Failed to unload all used chunks.", e)
        } finally {
            playerChunkManager.usedChunks.clear()
        }
    }

    override fun saveNBT() {
        super.saveNBT()
        if (spawnPoint == null) {
            namedTag!!.remove("SpawnX")
                .remove("SpawnY")
                .remove("SpawnZ")
                .remove("SpawnLevel")
                .remove("SpawnDimension")
        } else {
            namedTag!!.putInt("SpawnX", spawnPoint!!.position.floorX)
                .putInt("SpawnY", spawnPoint!!.position.floorY)
                .putInt("SpawnZ", spawnPoint!!.position.floorZ)
            namedTag!!.putString("SpawnLevel", spawnPoint!!.level.getLevelName())
            namedTag!!.putInt("SpawnDimension", spawnPoint!!.level.dimension)
        }

        adventureSettings.saveNBT()
    }

    @JvmOverloads
    fun save(async: Boolean = false) {
        check(!this.closed) { "Tried to save closed player" }

        saveNBT()

        if (this.level != null) {
            namedTag!!.putString("Level", level!!.getLevelName())

            val achievements = CompoundTag()
            for (achievement in this.achievements) {
                achievements.putByte(achievement!!, 1)
            }

            namedTag!!.putCompound("Achievements", achievements)
            namedTag!!.putInt("playerGameType", this.gamemode)
            namedTag!!.putLong("lastPlayed", System.currentTimeMillis() / 1000)
            namedTag!!.putString("lastIP", this.address)
            namedTag!!.putInt("EXP", this.experience)
            namedTag!!.putInt("expLevel", this.experienceLevel)
            namedTag!!.putInt("foodLevel", foodData!!.getFood())
            namedTag!!.putFloat("foodSaturationLevel", foodData!!.getSaturation())
            namedTag!!.putInt("enchSeed", this.enchSeed)

            val fogIdentifiers = ListTag<StringTag>()
            val userProvidedFogIds = ListTag<StringTag>()
            fogStack.forEach { fog ->
                userProvidedFogIds.add(StringTag(fog.first))
                fogIdentifiers.add(StringTag(fog.second))
            }
            namedTag!!.putList("fogIdentifiers", fogIdentifiers)
            namedTag!!.putList("userProvidedFogIds", userProvidedFogIds)

            namedTag!!.putInt("TimeSinceRest", this.timeSinceRest)

            if (getEntityName().isNotBlank() && this.namedTag != null) {
                Server.instance.saveOfflinePlayerData(
                    uuid,
                    namedTag!!, async
                )
            }
        }
    }

    override fun getOriginalName(): String {
        return "Player"
    }

    override fun getEntityName(): String {
        return playerInfo.username
    }


    val languageCode: LangCode
        get() {
            val code = from(loginChainData.languageCode!!)
            return code ?: LangCode.en_US
        }

    override fun kill() {
        if (!this.spawned) {
            return
        }

        val showMessages = level!!.gameRules.getBoolean(GameRule.SHOW_DEATH_MESSAGES)
        var message = ""
        val params: MutableList<String> = ArrayList()
        val cause = this.getLastDamageCause()

        if (showMessages) {
            params.add(this.displayName)

            run switch@{
                when (if (cause == null) DamageCause.CUSTOM else cause.cause) {
                    DamageCause.ENTITY_ATTACK -> if (cause is EntityDamageByEntityEvent) {
                        val e = cause.damager
                        killer = e
                        when (e) {
                            is Player -> {
                                message = "death.attack.player"
                                params.add(e.displayName)
                                return@switch
                            }

                            is EntityLiving -> {
                                message = "death.attack.mob"
                                params.add(if (e.getNameTag() != "") e.getNameTag() else e.getEntityName())
                                return@switch
                            }

                            else -> params.add("Unknown")
                        }
                    }

                    DamageCause.PROJECTILE -> if (cause is EntityDamageByEntityEvent) {
                        val e = cause.damager
                        killer = e
                        when (e) {
                            is Player -> {
                                message = "death.attack.arrow"
                                params.add(e.displayName)
                            }

                            is EntityLiving -> {
                                message = "death.attack.arrow"
                                params.add(if (e.getNameTag() != "") e.getNameTag() else e.getEntityName())
                                return@switch
                            }

                            else -> params.add("Unknown")
                        }
                    }

                    DamageCause.VOID -> message = "death.attack.outOfWorld"
                    DamageCause.FALL -> {
                        if (cause!!.finalDamage > 2) {
                            message = "death.fell.accident.generic"
                            return@switch
                        }
                        message = "death.attack.fall"
                    }

                    DamageCause.SUFFOCATION -> message = "death.attack.inWall"
                    DamageCause.LAVA -> {
                        message = "death.attack.lava"

                        if (killer is EntityProjectile) {
                            val shooter = (killer as EntityProjectile).shootingEntity
                            if (shooter != null) {
                                killer = shooter
                            }
                            if (killer is EntityHuman) {
                                message += ".player"
                                params.add(if (shooter!!.getNameTag() != "") shooter.getNameTag() else shooter.getEntityName())
                            }
                        }
                    }

                    DamageCause.FIRE -> message = "death.attack.onFire"
                    DamageCause.FIRE_TICK -> message = "death.attack.inFire"
                    DamageCause.DROWNING -> message = "death.attack.drown"
                    DamageCause.CONTACT -> if (cause is EntityDamageByBlockEvent) {
                        val id = cause.damager.id
                        if (id === BlockID.CACTUS) {
                            message = "death.attack.cactus"
                        } else if (id === BlockID.ANVIL) {
                            message = "death.attack.anvil"
                        }
                    }

                    DamageCause.BLOCK_EXPLOSION, DamageCause.ENTITY_EXPLOSION -> if (cause is EntityDamageByEntityEvent) {
                        val e = cause.damager
                        killer = e
                        when (e) {
                            is Player -> {
                                message = "death.attack.explosion.player"
                                params.add(e.displayName)
                            }

                            is EntityLiving -> {
                                message = "death.attack.explosion.player"
                                params.add(if (e.getNameTag() != "") e.getNameTag() else e.getEntityName())
                                return@switch
                            }

                            else -> message = "death.attack.explosion"
                        }
                    } else {
                        message = "death.attack.explosion"
                    }

                    DamageCause.MAGIC -> message = "death.attack.magic"
                    DamageCause.LIGHTNING -> message = "death.attack.lightningBolt"
                    DamageCause.HUNGER -> message = "death.attack.starve"
                    DamageCause.HOT_FLOOR -> message = "death.attack.magma"
                    else -> message = "death.attack.generic"
                }
            }
        }

        val ev: PlayerDeathEvent = PlayerDeathEvent(
            this,
            this.getDrops(), TranslationContainer(message, *params.toTypedArray()),
            this.experienceLevel
        )
        ev.keepExperience = level!!.gameRules.getBoolean(GameRule.KEEP_INVENTORY)
        ev.keepInventory = ev.keepExperience

        Server.instance.pluginManager.callEvent(ev)

        if (!ev.cancelled) {
            if (this.fishing != null) {
                this.stopFishing(false)
            }

            this.health = 0f
            this.extinguish()
            this.scheduleUpdate()
            if (!ev.keepInventory && level!!.gameRules.getBoolean(GameRule.DO_ENTITY_DROPS)) {
                for (item in ev.getDrops()) {
                    if (!item.hasEnchantment(Enchantment.ID_VANISHING_CURSE) && item.applyEnchantments()) {
                        level!!.dropItem(this.position, item, null, true, 40)
                    }
                }

                inventory.contents.forEach { (slot, item) ->
                    if (!item.keepOnDeath()) {
                        inventory.clear(slot)
                    }
                }

                if (this.offhandInventory != null) {
                    offhandInventory.contents.forEach { (slot, item) ->
                        if (!item.keepOnDeath()) {
                            offhandInventory.clear(slot)
                        }
                    }
                }
            }

            if (!ev.keepExperience && level!!.gameRules.getBoolean(GameRule.DO_ENTITY_DROPS)) {
                if (this.isSurvival || this.isAdventure) {
                    var exp = ev.experience * 7
                    if (exp > 100) exp = 100
                    level!!.dropExpOrb(this.position, exp)
                }
                this.setExperience(0, 0)
            }

            this.timeSinceRest = 0

            val deathInfo = DeathInfoPacket(
                cause = ev.translationDeathMessage.text,
                messages = ev.translationDeathMessage.parameters.toList()
            )
            this.sendPacket(deathInfo)

            if (showMessages && ev.deathMessage.toString().isNotEmpty()) {
                Server.instance.broadcast(ev.deathMessage, Server.BROADCAST_CHANNEL_USERS)
            }
            this.setDataProperty(
                EntityDataTypes.PLAYER_LAST_DEATH_POS, BlockVector3(
                    position.floorX, position.floorY, position.floorZ
                )
            )

            val pk = RespawnPacket(
                position = Vector3f(spawn.first!!.position),
                state = RespawnPacket.Companion.State.SearchingForSpawn,
                entityRuntimeID = this.getRuntimeID().toULong(),
            )
            this.sendPacket(pk)
        }
    }

    override fun setHealthSafe(health: Float) {
        var health1 = health
        if (health1 < 1) {
            health1 = 0f
        }
        super.setHealthSafe(health1)
        val attribute = attributes.computeIfAbsent(Attribute.MAX_HEALTH) { getAttribute(it) }
        attribute.setMaxValue((if (this.getAbsorption() % 2 != 0f) this.getMaxHealth() + 1 else this.getMaxHealth()).toFloat())
            .setValue(if (health1 > 0) (if (health1 < getMaxHealth()) health1 else getMaxHealth().toFloat()) else 0f)
        if (this.spawned) {
            val pk = UpdateAttributesPacket(
                actorRuntimeID = this.getRuntimeID().toULong(),
                attributes = listOf(attribute).map(org.chorus_oss.protocol.types.attribute.Attribute::invoke),
                tick = 0u
            )
            this.sendPacket(pk)
        }
    }

    override fun setMaxHealth(maxHealth: Int) {
        super.setMaxHealth(maxHealth)

        val attribute: Attribute =
            attributes.computeIfAbsent(Attribute.MAX_HEALTH) { getAttribute(it) }
        attribute.setMaxValue((if (this.getAbsorption() % 2 != 0f) this.getMaxHealth() + 1 else this.getMaxHealth()).toFloat())
            .setValue(if (health > 0) (if (health < getMaxHealth()) health else getMaxHealth().toFloat()) else 0f)
        if (this.spawned) {
            val pk = UpdateAttributesPacket(
                actorRuntimeID = this.getRuntimeID().toULong(),
                attributes = listOf(attribute).map(org.chorus_oss.protocol.types.attribute.Attribute::invoke),
                tick = 0u,
            )
            this.sendPacket(pk)
        }
    }

    var experience: Int
        /**
         * 得到该玩家的经验值(并不会显示玩家从的经验值总数，而仅仅显示当前等级所在的经验值，即经验条)。
         *
         *
         * Get the experience value of the player (it does not show the total experience value of the player from, but only the experience value where the current level is, i.e. the experience bar).
         *
         * @return int
         */
        get() = this.exp
        /**
         * `level = this.getExperienceLevel(),playLevelUpSound=false`
         *
         * @see .setExperience
         */
        set(exp) {
            setExperience(exp, this.experienceLevel)
        }


    /**
     * 增加该玩家的经验值
     *
     *
     * Increase the experience value of the player
     *
     * @param add              经验值的数量
     * @param playLevelUpSound 有无升级声音
     */
    /**
     * playLevelUpSound=false
     *
     * @see .addExperience
     */
    @JvmOverloads
    fun addExperience(add: Int, playLevelUpSound: Boolean = false) {
        if (add == 0) return
        val now = this.experience
        var added = now + add
        var level = this.experienceLevel
        var most = calculateRequireExperience(level)
        while (added >= most) {  //Level Up!
            added = added - most
            level++
            most = calculateRequireExperience(level)
        }
        this.setExperience(added, level, playLevelUpSound)
    }


    /**
     * playLevelUpSound=false
     *
     * @see .setExperience
     */
    fun setExperience(exp: Int, level: Int) {
        setExperience(exp, level, false)
    }

    /**
     * 设置该玩家的经验值和等级
     *
     *
     * set the experience value and level of the player
     *
     * @param playLevelUpSound 有无升级声音
     * @param exp              经验值
     * @param level            等级
     */
    //todo something on performance, lots of exp orbs then lots of packets, could crash client
    fun setExperience(exp: Int, level: Int, playLevelUpSound: Boolean) {
        var exp = exp
        var level = level
        val expEvent = PlayerExperienceChangeEvent(
            this,
            experience,
            experienceLevel, exp, level
        )
        Server.instance.pluginManager.callEvent(expEvent)
        if (expEvent.cancelled) {
            return
        }
        exp = expEvent.newExperience
        level = expEvent.newExperienceLevel

        val levelBefore = this.experienceLevel
        this.exp = exp
        this.experienceLevel = level

        this.sendExperienceLevel(level)
        this.sendExperience(exp)
        if (playLevelUpSound && levelBefore < level && levelBefore / 5 != level / 5 && this.lastPlayerdLevelUpSoundTime < this.age - 100) {
            this.lastPlayerdLevelUpSoundTime = this.age
            this.level!!.addLevelSoundEvent(
                this.position,
                LevelSoundEventPacket.Companion.SoundType.Levelup,
                min(7.0, (level / 5).toDouble()).toInt() shl 28,
                "",
                isBaby = false, isGlobal = false
            )
        }
    }

    /**
     * setExperience的实现部分，用来设置当前等级所对应的经验值，即经验条
     *
     *
     * The implementation of setExperience is used to set the experience value corresponding to the current level, i.e. the experience bar
     *
     * @param exp 经验值
     */
    /**
     * @see .sendExperience
     */
    @JvmOverloads
    fun sendExperience(exp: Int = this.experience) {
        if (this.spawned) {
            var percent = (exp.toFloat()) / calculateRequireExperience(
                experienceLevel
            )
            percent = max(0.0, min(1.0, percent.toDouble())).toFloat()
            val attribute: Attribute =
                attributes.computeIfAbsent(Attribute.EXPERIENCE) { getAttribute(it) }
            attribute.setValue(percent)
            this.syncAttribute(attribute)
        }
    }

    /**
     * setExperience的实现部分，用来设置当前等级
     *
     *
     * The implementation of setExperience is used to set the level
     *
     * @param level 等级
     */
    /**
     * @see .sendExperienceLevel
     */
    @JvmOverloads
    fun sendExperienceLevel(level: Int = this.experienceLevel) {
        if (this.spawned) {
            val attribute: Attribute =
                attributes.computeIfAbsent(Attribute.EXPERIENCE_LEVEL) { getAttribute(it) }
            attribute.setValue(level.toFloat())
            this.syncAttribute(attribute)
        }
    }

    /**
     * 以指定[Attribute]发送UpdateAttributesPacket数据包到该玩家。
     *
     *
     * Send UpdateAttributesPacket packets to this player with the specified [Attribute].
     *
     * @param attribute the attribute
     */
    override fun syncAttribute(attribute: Attribute) {
        val pk = UpdateAttributesPacket(
            actorRuntimeID = this.getRuntimeID().toULong(),
            attributes = listOf(attribute).map(org.chorus_oss.protocol.types.attribute.Attribute::invoke),
            tick = 0u,
        )
        this.sendPacket(pk)
    }

    override fun syncAttributes() {
        val pk = UpdateAttributesPacket(
            actorRuntimeID = this.getRuntimeID().toULong(),
            attributes = attributes.values.filter(Attribute::isSyncable)
                .map(org.chorus_oss.protocol.types.attribute.Attribute::invoke),
            tick = 0u,
        )
        this.sendPacket(pk)
    }

    override fun setAbsorption(absorption: Float) {
        if (absorption != this.absorption) {
            this.absorption = absorption
            val attribute: Attribute =
                attributes.computeIfAbsent(Attribute.ABSORPTION) { getAttribute(it) }
            attribute.setValue(absorption)
            this.syncAttribute(attribute)
        }
    }

    /**
     * send=true
     *
     * @see .setMovementSpeed
     */
    override fun setMovementSpeedF(speed: Float) {
        setMovementSpeed(speed, true)
    }

    /**
     * 设置该玩家的移动速度
     *
     *
     * Set the movement speed of this player.
     *
     * @param speed 速度大小，注意默认移动速度为[.DEFAULT_SPEED]<br></br>Speed value, note that the default movement speed is [.DEFAULT_SPEED]
     * @param send  是否发送数据包[UpdateAttributesPacket]到客户端<br></br>Whether to send [UpdateAttributesPacket] to the client
     */
    fun setMovementSpeed(speed: Float, send: Boolean) {
        super.setMovementSpeedF(speed)
        if (this.spawned && send) {
            this.sendMovementSpeed(speed)
        }
    }

    /**
     * 发送[Attribute.MOVEMENT_SPEED]属性到客户端
     *
     *
     * Send [Attribute.MOVEMENT_SPEED] Attribute to Client.
     *
     * @param speed 属性值<br></br>the speed value
     */
    fun sendMovementSpeed(speed: Float) {
        val attribute: Attribute = attributes.computeIfAbsent(Attribute.MOVEMENT_SPEED, Attribute::getAttribute)
        attribute.setValue(speed)
        this.syncAttribute(attribute)
    }

    override fun attack(source: EntityDamageEvent): Boolean {
        if (!this.isAlive()) {
            return false
        }

        if (this.isSpectator || this.isCreative) {
            //source.cancelled = true;
            return false
        } else if (adventureSettings[AdventureSettings.Type.ALLOW_FLIGHT] && source.cause == DamageCause.FALL) {
            //source.cancelled = true;
            return false
        } else if (source.cause == DamageCause.FALL) {
            if (level!!.getBlock(
                    locator.position.floor()
                        .add(0.5, -1.0, 0.5)
                ).id == BlockID.SLIME
            ) {
                if (!this.isSneaking()) {
                    //source.cancelled = true;
                    this.resetFallDistance()
                    return false
                }
            }
        }

        if (super.attack(source)) { //!source.isCancelled()
            if (this.getLastDamageCause() === source && this.spawned) {
                if (source is EntityDamageByEntityEvent) {
                    val damager = source.damager
                    if (damager is Player) {
                        damager.foodData?.exhaust(0.1)
                    }
                    //保存攻击玩家的实体在lastBeAttackEntity
                    this.lastBeAttackEntity = source.damager
                }
                val pk = ActorEventPacket(
                    actorRuntimeID = this.getRuntimeID().toULong(),
                    eventType = ActorEventPacket.Companion.Type.HurtAnimation,
                    eventData = 0
                )
                this.sendPacket(pk)
            }
            return true
        } else {
            return false
        }
    }

    /**
     * 在玩家面前的地面上掉落一个物品。如果物品投放成功，则返回。
     *
     *
     * Drops an item on the ground in front of the player. Returns if the item drop was successful.
     *
     * @param item 掉落的物品<br></br>to drop
     * @return 一个bool值，丢弃物品成功或该物品为空<br></br>bool if the item was dropped or if the item was null
     */
    fun dropItem(item: Item): Boolean {
        if (!this.spawned || !this.isAlive()) {
            return false
        }

        if (item.isNothing) {
            log.debug("{} attempted to drop a null item ({})", this.getEntityName(), item)
            return true
        }

        val motion = getDirectionVector().multiply(0.4)

        level!!.dropItem(position.add(0.0, 1.3, 0.0), item, motion, 40)

        this.setDataFlag(EntityFlag.USING_ITEM, false)
        return true
    }

    /**
     * 在玩家面前的地面上扔下一个物品。返回值为该掉落的物品。
     *
     *
     * Drops an item on the ground in front of the player. Returns the dropped item.
     *
     * @param item 掉落的物品<br></br>to drop
     * @return 如果物品被丢弃成功，则返回EntityItem；如果物品为空，则为null<br></br>EntityItem if the item was dropped or null if the item was null
     */
    fun dropAndGetItem(item: Item): EntityItem? {
        if (!this.spawned || !this.isAlive()) {
            return null
        }

        if (item.isNothing) {
            log.debug("{} attempted to drop a null item ({})", this.getEntityName(), item)
            return null
        }

        val motion = getDirectionVector().multiply(0.4)

        this.setDataFlag(EntityFlag.USING_ITEM, false)

        return level!!.dropAndGetItem(position.add(0.0, 1.3, 0.0), item, motion, 40)
    }

    @JvmOverloads
    fun sendPosition(
        pos: Vector3,
        yaw: Double = rotation.yaw,
        pitch: Double = rotation.pitch,
        mode: MovePlayerPacket.Companion.Mode = MovePlayerPacket.Companion.Mode.Normal,
        targets: Array<Player>? = null
    ) {
        val pk = MovePlayerPacket(
            entityRuntimeID = this.getRuntimeID().toULong(),
            position = Vector3f(
                pos.x.toFloat(),
                (pos.y + this.getEyeHeight()).toFloat(),
                pos.z.toFloat(),
            ),
            pitch = pitch.toFloat(),
            yaw = yaw.toFloat(),
            headYaw = yaw.toFloat(),
            mode = when (this.riding) {
                null -> mode
                else -> MovePlayerPacket.Companion.Mode.Rotation
            },
            onGround = this.onGround,
            riddenEntityRuntimeID = this.riding.let {
                when (it) {
                    null -> 0u
                    else -> it.getRuntimeID().toULong()
                }
            },
            teleportCause = MovePlayerPacket.Companion.TeleportCause.Unknown,
            teleportSourceEntityType = 0,
            tick = 0u,
        )
        if (targets != null) {
            Server.broadcastPacket(targets.toList(), pk)
        } else {
            this.sendPacket(pk)
        }
    }

    override fun teleport(transform: Transform, cause: TeleportCause?): Boolean {
        if (!this.isOnline) {
            return false
        }
        val from = this.transform
        this.lastTeleportMessage = Pair(from, System.currentTimeMillis())

        var to = transform
        //event
        if (cause != null) {
            val event = PlayerTeleportEvent(this, from, to, cause)
            Server.instance.pluginManager.callEvent(event)
            if (event.cancelled) return false
            to = event.to!!
        }

        //remove inventory,ride,sign editor
        for (window in ArrayList(windows.keys)) {
            if (window === this.inventory) {
                continue
            }
            this.removeWindow(window)
        }
        val currentRide = getRiding()
        if (currentRide != null && !currentRide.dismountEntity(this)) {
            return false
        }
        isOpenSignFront = null

        this.setMotion(Vector3())

        var switchLevel = false
        if (to.level != from.level) {
            switchLevel = true
            unloadAllUsedChunk()
            //unload entities for old level
            Arrays.stream(from.level.getEntities()).forEach { e: Entity -> e.despawnFrom(this) }
        }

        clientMovements.clear()
        //switch level, update pos and rotation, update aabb
        if (setPositionAndRotation(to.locator, to.yaw, to.pitch, to.headYaw)) {
            //if switch level or the distance teleported is too far
            if (switchLevel) {
                playerChunkManager.handleTeleport()
                //set nextChunkOrderRun is zero means that the next tick immediately execute the playerChunkManager#tick
                this.nextChunkOrderRun = 0
            } else if ((abs((from.position.chunkX - to.position.chunkX).toDouble()) >= this.getViewDistance())
                || (abs((from.position.chunkZ - to.position.chunkZ).toDouble()) >= this.getViewDistance())
            ) {
                playerChunkManager.handleTeleport()
                this.nextChunkOrderRun = 0
            }
            //send to client
            this.sendPosition(
                to.position,
                to.rotation.yaw,
                to.rotation.pitch,
                MovePlayerPacket.Companion.Mode.Teleport
            )
            this.newPosition = to.position
        } else {
            this.sendPosition(
                this.position,
                to.rotation.yaw,
                to.rotation.pitch,
                MovePlayerPacket.Companion.Mode.Teleport
            )
            this.newPosition = this.position
        }
        //state update
        this.positionChanged = true

        if (switchLevel) {
            refreshBlockEntity(10)
            refreshChunkRender()
        }
        this.resetFallDistance()
        //DummyBossBar
        dummyBossBars.values.forEach(Consumer { obj: DummyBossBar? -> obj!!.reshow() })
        //Weather
        level!!.sendWeather(this)
        //Update time
        level!!.sendTime(this)
        updateTrackingPositions(true)
        //Update gamemode
        if (isSpectator) {
            this.setGamemode(this.gamemode, false, null, true)
        }
        this.scheduleUpdate()
        return true
    }

    fun refreshChunkRender() {
        val origin = getViewDistance()
        this.setViewDistance(1)
        this.setViewDistance(32)
        this.setViewDistance(origin)
    }

    fun refreshBlockEntity(delay: Int) {
        level!!.scheduler.scheduleDelayedTask(InternalPlugin.INSTANCE, {
            for (b in level!!.getBlockEntities().values) {
                if (b is BlockEntitySpawnable) {
                    val setAir = UpdateBlockPacket(
                        position = BlockPos(b.position),
                        newBlockRuntimeID = BlockAir.STATE.blockStateHash().toUInt(),
                        flags = UpdateBlockPacket.FLAG_NETWORK,
                        layer = 0u
                    )
                    this.sendPacket(setAir)

                    val revertAir = UpdateBlockPacket(
                        position = BlockPos(b.position),
                        newBlockRuntimeID = b.block.runtimeId.toUInt(),
                        flags = UpdateBlockPacket.FLAG_NETWORK,
                        layer = 0u
                    )
                    this.sendPacket(revertAir)
                    b.spawnTo(this)
                }
            }
        }, delay, true)
    }

    /**
     *
     * Sends a form to a player and assigns a given ID to it
     * To open a form safely, please use [Form.send]
     *
     * @param form The form to open
     * @param id The ID to assign the form to
     * @return The id assigned to the form
     */
    /**
     *
     * Sends a form to a player and assigns the next ID to it
     * To open a form safely, please use [Form.send]
     *
     * @param form The form to open
     * @return The id assigned to the form
     */
    @JvmOverloads
    fun sendForm(form: Form<*>, id: Int = formWindowCount++): Int {
        if (formWindows.size > 10) {
            this.kick("Server sent to many forms. Please ")
            return id
        }

        if (!form.isViewer(this)) {
            form.viewers.add(this)
        }

        val packet = ModalFormRequestPacket(
            formID = id.toUInt(),
            formData = form.toJson()
        )
        formWindows[id] = form
        this.sendPacket(packet)

        return id
    }

    fun updateForm(form: Form<*>) {
        if (!form.isViewer(this)) {
            return
        }

        formWindows.entries
            .stream()
            .filter { f: Map.Entry<Int, Form<*>> -> f.value == form }
            .map { it.key }
            .findFirst()
            .ifPresent { id ->
                val packet = ServerSettingsResponsePacket(
                    formID = id,
                    formData = form.toJson(),
                ) // Exploiting some (probably unintended) protocol features here
                this.sendPacket(packet)
            }
    }

    fun checkClosedForms() {
        formWindows.entries.removeIf { entry: Map.Entry<Int, Form<*>> ->
            !entry.value.isViewer(
                this
            )
        }
    }

    /**
     * 向玩家展示一个NPC对话框.
     *
     *
     * Show dialog window to the player.
     *
     * @param dialog NPC对话框<br></br>the dialog
     * @param book   如果为true,将会立即更新该[FormWindowDialog.getSceneName]<br></br>If true, the [FormWindowDialog.getSceneName] will be updated immediately.
     */
    /**
     * book=true
     *
     * @see .showDialogWindow
     */
    @JvmOverloads
    fun showDialogWindow(dialog: FormWindowDialog, book: Boolean = true) {
        val actionJson = dialog.buttonJSONData

        if (book && dialogWindows.getIfPresent(dialog.sceneName) != null) dialog.updateSceneName()
        dialog.bindEntity!!.setDataProperty(EntityDataTypes.HAS_NPC, true)
        dialog.bindEntity!!.setDataProperty(EntityDataTypes.NPC_DATA, dialog.skinData)
        dialog.bindEntity!!.setDataProperty(EntityDataTypes.ACTIONS, actionJson!!)
        dialog.bindEntity!!.setDataProperty(EntityDataTypes.INTERACT_TEXT, dialog.content!!)

        val packet = NPCDialoguePacket(
            entityUniqueID = dialog.entityUniqueID.toULong(),
            actionType = NPCDialoguePacket.Companion.ActionType.Open,
            dialogue = dialog.content ?: "",
            sceneName = when (book) {
                true -> dialog.sceneName
                false -> ""
            },
            npcName = dialog.title ?: "",
            actionJSON = dialog.buttonJSONData ?: ""
        )
        if (book) dialogWindows.put(dialog.sceneName, dialog)
        this.sendPacket(packet)
    }

    /**
     * 在游戏设置中显示一个新的设置页面。
     * 你可以通过监听PlayerFormRespondedEvent来了解设置结果。
     *
     *
     * Shows a new setting page in game settings.
     * You can find out settings result by listening to PlayerFormRespondedEvent
     *
     * @param window to show on settings page
     * @return form id to use in [PlayerFormRespondedEvent]
     */
    fun addServerSettings(window: Form<*>): Int {
        val id = formWindowCount++

        serverSettings[id] = window
        return id
    }

    /**
     * 创建并发送一个BossBar给玩家。
     *
     *
     * Creates and sends a BossBar to the player
     *
     * @param text   BossBar信息<br></br>The BossBar message
     * @param length BossBar百分比<br></br>The BossBar percentage
     * @return bossBarId BossBar的ID，如果你想以后删除或更新BossBar，你应该存储它。<br></br>bossBarId The BossBar ID, you should store it if you want to remove or update the BossBar later
     */
    fun createBossBar(text: String, length: Int): Long {
        val bossBar = DummyBossBar.Builder(this).text(text).length(length.toFloat()).build()
        return this.createBossBar(bossBar)
    }

    /**
     * 创建并发送一个BossBar给玩家。
     *
     *
     * Creates and sends a BossBar to the player
     *
     * @param dummyBossBar DummyBossBar对象（通过[DummyBossBar.Builder]实例化）。<br></br>DummyBossBar Object (Instantiate it by the Class Builder)
     * @return bossBarId BossBar的ID，如果你想以后删除或更新BossBar，你应该储存它。<br></br>bossBarId  The BossBar ID, you should store it if you want to remove or update the BossBar later
     * @see DummyBossBar.Builder
     */
    fun createBossBar(dummyBossBar: DummyBossBar): Long {
        dummyBossBars[dummyBossBar.bossBarId] = dummyBossBar
        dummyBossBar.create()
        return dummyBossBar.bossBarId
    }

    /**
     * 获取一个DummyBossBar对象
     *
     *
     * Get a DummyBossBar object
     *
     * @param bossBarId 要查找的BossBar ID<br></br>The BossBar ID
     * @return DummyBossBar对象<br></br>DummyBossBar object
     * @see DummyBossBar.setText
     * @see DummyBossBar.setLength
     * @see DummyBossBar.setColor
     */
    fun getDummyBossBar(bossBarId: Long): DummyBossBar? {
        return dummyBossBars.getOrDefault(bossBarId, null)
    }

    /**
     * 更新一个BossBar
     *
     *
     * Updates a BossBar
     *
     * @param text      The new BossBar message
     * @param length    The new BossBar length
     * @param bossBarId The BossBar ID
     */
    fun updateBossBar(text: String, length: Int, bossBarId: Long) {
        if (dummyBossBars.containsKey(bossBarId)) {
            val bossBar = dummyBossBars[bossBarId]
            bossBar!!.setText(text)
            bossBar.setLength(length.toFloat())
        }
    }

    /**
     * 移除一个BossBar
     *
     *
     * Removes a BossBar
     *
     * @param bossBarId The BossBar ID
     */
    fun removeBossBar(bossBarId: Long) {
        if (dummyBossBars.containsKey(bossBarId)) {
            dummyBossBars[bossBarId]!!.destroy()
            dummyBossBars.remove(bossBarId)
        }
    }

    /**
     * 获取id从指定[Inventory]
     *
     *
     * Get id from the specified [Inventory]
     *
     * @param inventory the inventory
     * @return the window id
     */
    fun getWindowId(inventory: Inventory): Int {
        Preconditions.checkNotNull(inventory)
        if (windows.containsKey(inventory)) {
            return windows[inventory]!!
        }

        return -1
    }

    /**
     * 获取[Inventory]从指定id
     *
     *
     * Get [Inventory] from the specified id
     *
     * @param id 窗口id<br></br>the window id
     */
    fun getWindowById(id: Int): Inventory? {
        return windowIndex[id]
    }

    /**
     * Add inventory to the current player.
     *
     * @param inventory The Inventory object representing the window, must not be null.
     * @return The unique identifier assigned to the window if successfully added and opened; -1 if the window fails to be added.
     */
    fun addWindow(inventory: Inventory): Int {
        Preconditions.checkNotNull(inventory)
        if (windows.containsKey(inventory)) {
            return windows[inventory]!!
        }
        val cnt = max(1.0, (++this.windowsCnt % 100).toDouble()).toInt()
        this.windowsCnt = cnt
        if (windowIndex.containsKey(cnt)) {
            windowIndex[cnt]!!.close(this)
        }
        windows.forcePut(inventory, cnt)

        if (this.spawned && inventory.open(this)) {
            updateTrackingPositions(true)
            return cnt
        } else {
            this.removeWindow(inventory)
            return -1
        }
    }

    fun addWindow(inventory: Inventory, forceId: Int?): Int {
        Preconditions.checkNotNull(inventory)
        if (windows.containsKey(inventory)) {
            return windows[inventory]!!
        }
        val cnt: Int
        if (forceId == null) {
            cnt = max(1.0, (++this.windowsCnt % 101).toDouble()).toInt()
            this.windowsCnt = cnt //1-100
        } else {
            cnt = forceId
        }
        if (windowIndex.containsKey(cnt)) {
            windowIndex[cnt]!!.close(this)
        }
        windows.forcePut(inventory, cnt)

        if (this.spawned) {
            if (inventory.open(this)) {
                updateTrackingPositions(true)
                return cnt
            } else {
                this.removeWindow(inventory)
                return -1
            }
        }
        return cnt
    }

    val topWindow: Optional<Inventory>
        get() {
            for ((key, value) in this.windows) {
                if (!permanentWindows.contains(value)) {
                    return Optional.of(key)
                }
            }
            return Optional.empty()
        }

    /**
     * 移除该玩家身上的指定Inventory
     *
     *
     * Remove the specified Inventory from the player
     *
     * @param inventory the inventory
     */
    fun removeWindow(inventory: Inventory) {
        Preconditions.checkNotNull(inventory)
        if (!permanentWindows.contains(windows[inventory])) {
            val windowId = this.getWindowId(inventory)
            this.closingWindowId = windowId
            inventory.close(this)
            windows.remove(inventory)
            updateTrackingPositions(true)
        }
    }

    /**
     * 常用于刷新。
     *
     *
     * Commonly used for refreshing.
     */
    fun sendAllInventories() {
        for (inv in windows.keys) {
            inv.sendContents(this)
            if (inv is HumanInventory) {
                inv.sendArmorContents(this)
            }
        }
    }


    @ApiStatus.Internal
    fun resetInventory() {
        if (spawned) {
            val contents = craftingGrid.contents
            craftingGrid.clearAll()
            val puts: MutableList<Item> = ArrayList(contents.values)

            val contents2 =
                cursorInventory.contents
            cursorInventory.clearAll()
            puts.addAll(contents2.values)

            val topWindow = topWindow
            val value: Inventory
            if (topWindow.isPresent) {
                value = topWindow.get()
                if (value is CraftTypeInventory) {
                    puts.addAll(value.contents.values)
                    value.clearAll()
                }
                removeWindow(value)
            }
            val drops = inventory.addItem(*puts.toTypedArray())
            for (drop in drops) {
                this.dropItem(drop)
            }
        }
    }

    /**
     * 清空[.windows]
     *
     *
     * Remove all windows.
     *
     * @param permanent 如果为false则会跳过删除[.permanentWindows]里面对应的window<br></br>If false, it will skip deleting the corresponding window in [.permanentWindows]
     */
    @JvmOverloads
    fun removeAllWindows(permanent: Boolean = false) {
        for ((key, value) in ArrayList<Map.Entry<Int, Inventory>>(
            windowIndex.entries
        )) {
            if (!permanent && permanentWindows.contains(key)) {
                continue
            }
            this.removeWindow(value)
        }
    }

    override fun onChunkChanged(chunk: IChunk) {
        playerChunkManager.addSendChunk(chunk.x, chunk.z)
    }

    override fun onChunkLoaded(chunk: IChunk) {
    }


    override fun onChunkUnloaded(chunk: IChunk) {
        this.unloadChunk(chunk.x, chunk.z, chunk.provider.level)
    }

    override val isLoaderActive: Boolean
        get() = this.isConnected()

    public override fun switchLevel(targetLevel: Level): Boolean {
        if (super.switchLevel(targetLevel)) {
            clientMovements.clear()
            val spawnPosition = SetSpawnPositionPacket(
                spawnType = SetSpawnPositionPacket.Companion.SpawnType.World,
                position = BlockPos(targetLevel.spawnLocation.position),
                dimension = targetLevel.dimension,
                spawnPosition = BlockPos(targetLevel.spawnLocation.position),
            )
            this.sendPacket(spawnPosition)

            // Remove old chunks
            this.forceSendEmptyChunks()

            val setTime = SetTimePacket(
                time = targetLevel.getTime()
            )
            this.sendPacket(setTime)

            val gameRulesChanged = GameRulesChangedPacket(
                gameRules = targetLevel.gameRules.getGameRules()
                    .map { org.chorus_oss.protocol.types.GameRule(it.toPair()) }
            )
            this.sendPacket(gameRulesChanged)

            if (targetLevel.dimension == this.level!!.dimension) {
                this.sendPacket(
                    ChangeDimensionPacket(
                        dimension = when (this.level!!.dimension) {
                            Level.DIMENSION_NETHER -> Level.DIMENSION_OVERWORLD
                            else -> Level.DIMENSION_NETHER
                        },
                        position = Vector3f(0f, 0f, 0f),
                        respawn = false,
                        loadingScreenID = null
                    )
                )
            }

            this.setDimension(targetLevel.dimension)

            updateTrackingPositions(true)
            return true
        }

        return false
    }

    /**
     * 设置是否检查该玩家移动
     *
     *
     * Set whether to check for this player movement
     *
     * @param checkMovement the check movement
     */
    fun setCheckMovement(checkMovement: Boolean) {
        this.isCheckingMovement = checkMovement
    }

    val usedChunks: @UnmodifiableView MutableSet<Long>
        get() = Collections.unmodifiableSet(playerChunkManager.usedChunks)

    override fun setSprinting(value: Boolean) {
        if (value && this.getFreezingTicks() > 0) return
        if (isSprinting() != value) {
            super.setSprinting(value)
            this.setMovementSpeedF(if (value) movementSpeed * 1.3f else movementSpeed / 1.3f)

            if (this.hasEffect(EffectType.SPEED)) {
                val movementSpeed = this.movementSpeed
                this.sendMovementSpeed(if (value) movementSpeed * 1.3f else movementSpeed)
            }
        }
    }

    /**
     * 传送该玩家到另一个服务器
     *
     *
     * Teleport the player to another server
     *
     * @param address the address
     */
    fun transfer(address: InetSocketAddress) {
        val hostName = address.address.hostAddress
        val port = address.port
        val packet = TransferPacket(
            address = hostName,
            port = port.toUShort(),
            reloadWorld = false,
        )
        this.sendPacket(packet)
    }

    @ApiStatus.Internal
    fun pickupEntity(entity: Entity, near: Boolean): Boolean {
        if (!this.spawned || !this.isAlive() || !this.isOnline || this.isSpectator || entity.isClosed()) {
            return false
        }

        if (near) {
            var inventory: Inventory? = this.inventory
            if (entity is EntityArrow && entity.hadCollision) {
                val item = if (entity.getArrowItem() != null) entity.getArrowItem() else ItemArrow()
                if (!this.isCreative) {
                    // Should only collect to the offhand slot if the item matches what is already there
                    if (offhandInventory.getItem(0).id == item.id && offhandInventory.canAddItem(
                            item
                        )
                    ) {
                        inventory = this.offhandInventory
                    } else if (!inventory!!.canAddItem(item)) {
                        return false
                    }
                }

                val ev = InventoryPickupArrowEvent(inventory!!, entity)

                val pickupMode = entity.pickupMode
                if (pickupMode == EntityProjectile.PICKUP_NONE || (pickupMode == EntityProjectile.PICKUP_CREATIVE && !this.isCreative)) {
                    ev.cancelled = true
                }

                Server.instance.pluginManager.callEvent(ev)
                if (ev.cancelled) {
                    return false
                }

                val pk = TakeItemEntityPacket(
                    itemEntityRuntimeID = entity.getRuntimeID().toULong(),
                    takerEntityRuntimeID = this.getRuntimeID().toULong(),
                )
                Server.broadcastPacket(entity.viewers.values, pk)
                this.sendPacket(pk)

                if (!this.isCreative) {
                    inventory.addItem(item.clone())
                }
                entity.close()
                return true
            } else if (entity is EntityThrownTrident) {
                // Check Trident is returning to shooter
                if (!entity.hadCollision) {
                    if (entity.isNoClip()) {
                        if ((entity as EntityProjectile).shootingEntity!! != this) {
                            return false
                        }
                    } else {
                        return false
                    }
                }

                if (!entity.hasPlayer()) {
                    return false
                }

                val item = entity.getItem()
                if (!this.isCreative && !this.inventory.canAddItem(item)) {
                    return false
                }

                val ev = InventoryPickupTridentEvent(this.inventory, entity)

                val pickupMode = entity.pickupMode
                if (pickupMode == EntityProjectile.PICKUP_NONE || (pickupMode == EntityProjectile.PICKUP_CREATIVE && !this.isCreative)) {
                    ev.cancelled = true
                }

                Server.instance.pluginManager.callEvent(ev)
                if (ev.cancelled) {
                    return false
                }

                val pk = TakeItemEntityPacket(
                    itemEntityRuntimeID = entity.getRuntimeID().toULong(),
                    takerEntityRuntimeID = this.getRuntimeID().toULong(),
                )
                Server.broadcastPacket(entity.viewers.values, pk)
                this.sendPacket(pk)

                if (!entity.isCreative) {
                    if (inventory!!.getItem(entity.getFavoredSlot()).isNothing) {
                        inventory.setItem(entity.getFavoredSlot(), item.clone())
                    } else {
                        inventory.addItem(item.clone())
                    }
                }
                entity.close()
                return true
            } else if (entity is EntityItem) {
                if (entity.pickupDelay <= 0) {
                    val item = entity.item

                    if (!this.isCreative && !this.inventory.canAddItem(item)) {
                        return false
                    }

                    val ev: InventoryPickupItemEvent
                    Server.instance.pluginManager.callEvent(InventoryPickupItemEvent(inventory!!, entity).also {
                        ev = it
                    })
                    if (ev.cancelled) {
                        return false
                    }

                    val pk = TakeItemEntityPacket(
                        itemEntityRuntimeID = entity.getRuntimeID().toULong(),
                        takerEntityRuntimeID = this.getRuntimeID().toULong(),
                    )
                    Server.broadcastPacket(entity.viewers.values, pk)
                    this.sendPacket(pk)

                    this.inventory.addItem(item.clone())
                    entity.close()
                    return true
                }
            }
        }

        val tick = level!!.tick
        if (pickedXPOrb < tick && entity is EntityXpOrb && boundingBox.isVectorInside(entity.position)) {
            if (entity.getPickupDelay() <= 0) {
                var exp = entity.getExp()
                entity.kill()
                level!!.addLevelEvent(LevelEventPacket.EVENT_SOUND_EXPERIENCE_ORB_PICKUP, 0, this.position)
                pickedXPOrb = tick

                //Mending
                val itemsWithMending = ArrayList<Int>()
                for (i in 0..3) {
                    if (inventory.getArmorItem(i).hasEnchantment(Enchantment.ID_MENDING)) {
                        itemsWithMending.add(inventory.size + i)
                    }
                }
                if (inventory.itemInHand.hasEnchantment(Enchantment.ID_MENDING)) {
                    itemsWithMending.add(inventory.heldItemIndex)
                }
                if (itemsWithMending.size > 0) {
                    val rand = Random()
                    val itemToRepair = itemsWithMending[rand.nextInt(itemsWithMending.size)]
                    val toRepair = inventory.getItem(itemToRepair)
                    if (toRepair is ItemTool || toRepair is ItemArmor) {
                        if (toRepair.damage > 0) {
                            var dmg = toRepair.damage - exp
                            if (dmg < 0) {
                                exp = abs(dmg.toDouble()).toInt()
                                dmg = 0
                            }
                            toRepair.damage = dmg
                            inventory.setItem(itemToRepair, toRepair)
                        }
                    }
                }

                if (exp > 0) this.addExperience(exp, true)
                return true
            }
        }

        return false
    }

    override fun hashCode(): Int {
        if ((this.hash == 0) || (this.hash == 485)) {
            this.hash = (485 + (if (getUniqueID() != null) getUniqueID().hashCode() else 0))
        }

        return this.hash
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Player) {
            return false
        }
        return this.getUniqueID() == other.getUniqueID() && this.getRuntimeID() == other.getRuntimeID()
    }

    /**
     * 玩家是否在挖掘方块
     *
     *
     * Whether the player is digging block
     *
     * @return the boolean
     */
    fun isBreakingBlock(): Boolean {
        return this.breakingBlock != null
    }

    /**
     * 显示一个XBOX账户的资料窗口
     *
     *
     * Show a window of a XBOX account's profile
     *
     * @param xuid XUID
     */
    fun showXboxProfile(xuid: String) {
        val pk = ShowProfilePacket(
            xuid = xuid
        )
        this.sendPacket(pk)
    }

    /**
     * Start fishing
     *
     * @param fishingRod fishing rod item
     */
    fun startFishing(fishingRod: Item?) {
        val nbt = CompoundTag()
            .putList(
                "Pos", ListTag<FloatTag>()
                    .add(FloatTag(position.x))
                    .add(FloatTag(position.y + this.getEyeHeight()))
                    .add(FloatTag(position.z))
            )
            .putList(
                "Motion", ListTag<FloatTag>()
                    .add(
                        FloatTag(
                            -sin(rotation.yaw / 180 + Math.PI) * cos(
                                rotation.pitch / 180 * Math.PI
                            )
                        )
                    )
                    .add(FloatTag(-sin(rotation.pitch / 180 * Math.PI)))
                    .add(
                        FloatTag(
                            cos(rotation.yaw / 180 * Math.PI) * cos(
                                rotation.pitch / 180 * Math.PI
                            )
                        )
                    )
            )
            .putList(
                "Rotation", ListTag<FloatTag>()
                    .add(FloatTag(rotation.yaw.toFloat()))
                    .add(FloatTag(rotation.pitch.toFloat()))
            )
        val f = 1.1
        val fishingHook = EntityFishingHook(chunk, nbt, this)
        fishingHook.setMotion(
            Vector3(
                -sin(Math.toRadians(rotation.yaw)) * cos(
                    Math.toRadians(
                        rotation.pitch
                    )
                ) * f * f,
                -sin(Math.toRadians(rotation.pitch)) * f * f,
                cos(Math.toRadians(rotation.yaw)) * cos(
                    Math.toRadians(
                        rotation.pitch
                    )
                ) * f * f
            )
        )
        val ev = ProjectileLaunchEvent(fishingHook, this)
        Server.instance.pluginManager.callEvent(ev)
        if (ev.cancelled) {
            fishingHook.close()
        } else {
            this.fishing = fishingHook
            fishingHook.rod = fishingRod
            fishingHook.checkLure()
            fishingHook.spawnToAll()
        }
    }

    /**
     * Stop fishing
     *
     * @param click clicked or forced
     */
    fun stopFishing(click: Boolean) {
        if (this.fishing != null && click) {
            fishing!!.reelLine()
        } else if (this.fishing != null) {
            fishing!!.close()
        }

        this.fishing = null
    }

    override fun doesTriggerPressurePlate(): Boolean {
        return this.gamemode != SPECTATOR
    }

    override fun toString(): String {
        return "Player(name='" + getEntityName() +
                "', location=" + super.toString() +
                ')'
    }

    /**
     * 将物品添加到玩家的主要库存中，并将任何多余的物品丢在地上。
     *
     *
     * Adds the items to the main player inventory and drops on the floor any excess.
     *
     * @param items The items to give to the player.
     */
    fun giveItem(vararg items: Item) {
        for (failed in inventory.addItem(*items)) {
            level!!.dropItem(this.position, failed)
        }
    }


    // TODO: Support Translation Parameters
    fun sendPopupJukebox(message: String) {

        val pk = TextPacket(
            textType = TextPacket.Companion.TextType.JukeboxPopup,
            needsTranslation = false,
            sourceName = null,
            message = message,
            parameters = emptyList(),
            xuid = "",
            platformChatID = "",
            filteredMessage = ""
        )
        this.sendPacket(pk)
    }

    fun sendSystem(message: String) {
        val pk = TextPacket(
            textType = TextPacket.Companion.TextType.System,
            needsTranslation = false,
            sourceName = "",
            message = message,
            parameters = emptyList(),
            xuid = "",
            platformChatID = "",
            filteredMessage = ""
        )
        this.sendPacket(pk)
    }


    fun sendWhisper(message: String) {
        this.sendWhisper("", message)
    }


    fun sendWhisper(source: String, message: String) {
        val pk = TextPacket(
            textType = TextPacket.Companion.TextType.Whisper,
            needsTranslation = false,
            sourceName = source,
            message = message,
            parameters = emptyList(),
            xuid = "",
            platformChatID = "",
            filteredMessage = ""
        )
        this.sendPacket(pk)
    }


    fun sendAnnouncement(message: String) {
        this.sendAnnouncement("", message)
    }


    fun sendAnnouncement(source: String, message: String) {
        val pk = TextPacket(
            textType = TextPacket.Companion.TextType.Announcement,
            needsTranslation = false,
            sourceName = source,
            message = message,
            parameters = emptyList(),
            xuid = "",
            platformChatID = "",
            filteredMessage = ""
        )
        this.sendPacket(pk)
    }


    fun completeUsingItem(
        itemId: Short,
        action: CompletedUsingItemPacket.Companion.ItemUseMethod
    ) {
        this.sendPacket(
            packet = CompletedUsingItemPacket(
                itemID = itemId,
                itemUseMethod = action
            )
        )
    }


    fun isShowingCredits(): Boolean {
        return showingCredits
    }


    fun showCredits() {
        this.showingCredits = true
    }


    fun hasSeenCredits(): Boolean {
        return hasSeenCredits
    }

    fun setHasSeenCredits(hasSeenCredits: Boolean) {
        this.hasSeenCredits = hasSeenCredits
    }


    fun dataPacketImmediately(packet: DataPacket): Boolean {
        if (!this.isConnected()) {
            return false
        }
        val ev = DataPacketSendEvent(this, packet)
        Server.instance.pluginManager.callEvent(ev)
        if (ev.cancelled) {
            return false
        }
        session.sendPacketImmediately(packet)
        return true
    }

    fun sendPacketImmediately(packet: Packet): Boolean {
        return dataPacketImmediately(MigrationPacket(packet))
    }

    /**
     * 玩家屏幕振动效果
     *
     *
     * Player screen shake effect
     *
     * @param intensity   the intensity
     * @param duration    the duration
     * @param shakeType   the shake type
     * @param shakeAction the shake action
     */
    fun shakeCamera(
        intensity: Float,
        duration: Float,
        shakeType: CameraShakePacket.Companion.Type,
        shakeAction: CameraShakePacket.Companion.Action
    ) {
        this.sendPacket(
            CameraShakePacket(
                intensity = intensity,
                duration = duration,
                type = shakeType,
                action = shakeAction
            )
        )
    }

    /**
     * 发送一个下弹消息框给玩家
     *
     *
     * Send a Toast message box to the player
     *
     * @param title   the title
     * @param content the content
     */
    fun sendToast(title: String, content: String) {
        val pk = ToastRequestPacket(
            title = title,
            message = content,
        )
        this.sendPacket(pk)
    }

    override fun removeLine(line: IScoreboardLine) {
        val packet = SetScorePacket(
            actionType = SetScorePacket.Companion.ActionType.Remove,
            removeEntries = listOfNotNull(line.toNetworkInfo()).map {
                ScoreboardRemoveEntry(
                    entryID = it.entryID,
                    objectiveName = it.objectiveName,
                    score = it.score,
                )
            },
            modifyEntries = null
        )
        this.sendPacket(packet)

        val scorer = PlayerScorer(this)
        if (line.scorer == scorer && line.scoreboard.getViewers(DisplaySlot.BELOW_NAME).contains(this)) {
            this.setScoreTag("")
        }
    }

    override fun updateScore(line: IScoreboardLine) {
        val packet = SetScorePacket(
            actionType = SetScorePacket.Companion.ActionType.Modify,
            removeEntries = null,
            modifyEntries = listOfNotNull(line.toNetworkInfo())
        )
        this.sendPacket(packet)

        val scorer = PlayerScorer(this)
        if (line.scorer == scorer && line.scoreboard.getViewers(DisplaySlot.BELOW_NAME).contains(this)) {
            this.setScoreTag("${line.score} ${line.scoreboard.displayName}")
        }
    }

    override fun display(scoreboard: IScoreboard, slot: DisplaySlot?) {
        val pk = SetDisplayObjectivePacket(
            displaySlot = ScoreboardSlot.entries[slot!!.ordinal],
            objectiveName = scoreboard.objectiveName,
            displayName = scoreboard.displayName ?: "",
            criteriaName = scoreboard.criteriaName ?: "",
            sortOrder = ScoreboardSlotOrder.entries[scoreboard.sortOrder!!.ordinal],
        )
        this.sendPacket(pk)

        //client won't storage the score of a scoreboard,so we should send the score to client
        val pk2 = SetScorePacket(
            actionType = SetScorePacket.Companion.ActionType.Modify,
            removeEntries = null,
            modifyEntries = scoreboard.lines.values.mapNotNull { it.toNetworkInfo() }
        )
        this.sendPacket(pk2)

        val scorer = PlayerScorer(this)
        val line = scoreboard.getLine(scorer)
        if (slot == DisplaySlot.BELOW_NAME && line != null) {
            this.setScoreTag("${line.score} ${scoreboard.displayName}")
        }
    }

    override fun hide(slot: DisplaySlot?) {
        val pk = SetDisplayObjectivePacket(
            displaySlot = ScoreboardSlot.entries[slot!!.ordinal],
            objectiveName = "",
            displayName = "",
            criteriaName = "",
            sortOrder = ScoreboardSlotOrder.Ascending,
        )
        this.sendPacket(pk)

        if (slot == DisplaySlot.BELOW_NAME) {
            this.setScoreTag("")
        }
    }

    override fun removeScoreboard(scoreboard: IScoreboard) {
        val pk = RemoveObjectivePacket(
            objectiveName = scoreboard.objectiveName,
        )
        this.sendPacket(pk)
    }

    /**
     * Opens the player's sign editor GUI for the sign at the given position.
     */
    fun openSignEditor(position: Vector3, frontSide: Boolean) {
        if (isOpenSignFront == null) {
            val blockEntity = level!!.getBlockEntity(position)
            if (blockEntity is BlockEntitySign) {
                if (blockEntity.editorEntityRuntimeId == -1L) {
                    blockEntity.editorEntityRuntimeId = this.getRuntimeID()
                    val openSignPacket = OpenSignPacket(
                        position = BlockPos(position),
                        frontSide = frontSide,
                    )
                    this.sendPacket(openSignPacket)
                    isOpenSignFront = frontSide
                }
            } else {
                throw IllegalArgumentException("Block at this position is not a sign")
            }
        }
    }


    companion object {
        const val TAG_ABILITIES: String = "abilities"
        const val TAG_TIMER: String = "timer"
        const val TAG_AGENT_ID: String = "AgentID"
        const val TAG_DIMENSION_ID: String = "DimensionId"
        const val TAG_ENCHANTMENT_SEED: String = "EnchantmentSeed"
        const val TAG_ENDER_CHEST_INVENTORY: String = "EnderChestInventory"
        const val TAG_FOG_COMMAND_STACK: String = "fogCommandStack"
        const val TAG_FORMAT_VERSION: String = "format_version"
        const val TAG_HAS_SEEN_CREDITS: String = "HasSeenCredits"
        const val TAG_INVENTORY: String = "Inventory"
        const val TAG_LEFT_SHOULDER_RIDER_ID: String = "LeftShoulderRiderID"
        const val TAG_MAP_INDEX: String = "MapIndex"
        const val TAG_PLAYER_GAME_MODE: String = "PlayerGameMode"
        const val TAG_PLAYER_LEVEL: String = "PlayerLevel"
        const val TAG_PLAYER_LEVEL_PROGRESS: String = "PlayerLevelProgress"
        const val TAG_PLAYER_UI_ITEMS: String = "PlayerUIItems"
        const val TAG_RECIPE_UNLOCKING: String = "recipe_unlocking"
        const val TAG_RIDE_ID: String = "RideID"
        const val TAG_RIGHT_SHOULDER_RIDER_ID: String = "RightShoulderRiderID"
        const val TAG_SELECTED_CONTAINER_ID: String = "SelectedContainerId"
        const val TAG_SELECTED_INVENTORY_SLOT: String = "SelectedInventorySlot"
        const val TAG_SLEEPING: String = "Sleeping"
        const val TAG_SLEEP_TIMER: String = "SleepTimer"
        const val TAG_SNEAKING: String = "Sneaking"
        const val TAG_SPAWN_BLOCK_POSITION_X: String = "SpawnBlockPositionX"
        const val TAG_SPAWN_BLOCK_POSITION_Y: String = "SpawnBlockPositionY"
        const val TAG_SPAWN_BLOCK_POSITION_Z: String = "SpawnBlockPositionZ"
        const val TAG_SPAWN_DIMENSION: String = "SpawnDimension"
        const val TAG_SPAWN_X: String = "SpawnX"
        const val TAG_SPAWN_Y: String = "SpawnY"
        const val TAG_SPAWN_Z: String = "SpawnZ"
        const val TAG_TIME_SINCE_REST: String = "TimeSinceRest"
        const val TAG_WARDEN_THREAT_DECREASE_TIMER: String = "WardenThreatDecreaseTimer"
        const val TAG_WARDEN_THREAT_LEVEL: String = "WardenThreatLevel"
        const val TAG_WARDEN_THREAT_LEVEL_INCREASE_COOLDOWN: String = "WardenThreatLevelIncreaseCooldown"

        /** static fields */
        /**
         * 一个承载玩家的空数组静态常量
         *
         *
         * An empty array of static constants that host the player
         */
        @JvmField
        val EMPTY_ARRAY: Array<Player?> = arrayOfNulls(0)
        const val SURVIVAL: Int = 0
        const val CREATIVE: Int = 1
        const val ADVENTURE: Int = 2
        const val SPECTATOR: Int = 3
        const val DEFAULT_SPEED: Float = 0.1f
        const val DEFAULT_FLY_SPEED: Float = 0.05f
        const val MAXIMUM_SPEED: Float = 0.5f
        const val PERMISSION_CUSTOM: Int = 3
        const val PERMISSION_OPERATOR: Int = 2
        const val PERMISSION_MEMBER: Int = 1
        const val PERMISSION_VISITOR: Int = 0
        protected const val RESOURCE_PACK_CHUNK_SIZE: Int = 8 * 1024 // 8KB
        private const val ROTATION_UPDATE_THRESHOLD = 1f
        private const val MOVEMENT_DISTANCE_THRESHOLD = 0.1f
        const val NO_SHIELD_DELAY: Int = 10

        /**
         * 将服务端侧游戏模式转换为网络包适用的游戏模式ID
         * 此方法是为了解决NK观察者模式ID为3而原版ID为6的问题
         *
         * @param gamemode 服务端侧游戏模式
         * @return 网络层游戏模式ID
         */
        fun toNetworkGamemode(gamemode: Int): Int {
            return if (gamemode != SPECTATOR) gamemode else GameType.SPECTATOR.ordinal
        }

        /**
         * 计算玩家到达某等级所需要的经验值
         *
         *
         * Calculates the amount of experience a player needs to reach a certain level
         *
         * @param level 等级
         * @return int
         */
        fun calculateRequireExperience(level: Int): Int {
            return if (level >= 30) {
                112 + (level - 30) * 9
            } else if (level >= 15) {
                37 + (level - 15) * 5
            } else {
                7 + level * 2
            }
        }
    }
}
