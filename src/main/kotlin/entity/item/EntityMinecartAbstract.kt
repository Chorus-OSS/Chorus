package org.chorus_oss.chorus.entity.item

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.block.*
import org.chorus_oss.chorus.blockentity.BlockEntityHopper
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.EntityHuman
import org.chorus_oss.chorus.entity.EntityLiving
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.event.entity.EntityDamageByEntityEvent
import org.chorus_oss.chorus.event.entity.EntityDamageEvent
import org.chorus_oss.chorus.event.vehicle.VehicleMoveEvent
import org.chorus_oss.chorus.event.vehicle.VehicleUpdateEvent
import org.chorus_oss.chorus.inventory.InventoryHolder
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemMinecart
import org.chorus_oss.chorus.level.GameRule
import org.chorus_oss.chorus.level.Transform
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.math.AxisAlignedBB
import org.chorus_oss.chorus.math.BlockVector3
import org.chorus_oss.chorus.math.SimpleAxisAlignedBB
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.NBTIO
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.utils.MinecartType
import org.chorus_oss.chorus.utils.Rail
import kotlin.math.*

abstract class EntityMinecartAbstract(chunk: IChunk?, nbt: CompoundTag) : EntityVehicle(chunk, nbt) {
    var customDisplayTile: Boolean? = null
    var displayBlock: Block? = null
    var displayOffset: Int? = null

    private val devs: Boolean = false // Avoid maintained features into production
    private var currentSpeed: Double = 0.0

    // Plugins modifiers
    private var slowWhenEmpty: Boolean = true
    private var derailedX: Double = 0.5
    private var derailedY: Double = 0.5
    private var derailedZ: Double = 0.5
    private var flyingX: Double = 0.95
    private var flyingY: Double = 0.95
    private var flyingZ: Double = 0.95
    private var maxSpeed: Double = 0.4
    private var hasUpdated: Boolean = false

    init {
        setMaxHealth(40)
        setHealthSafe(40f)

        if (nbt.contains(TAG_CUSTOM_DISPLAY_TILE)) {
            this.customDisplayTile = nbt.getBoolean(TAG_CUSTOM_DISPLAY_TILE)
        }
        if (nbt.contains(TAG_DISPLAY_BLOCK)) {
            this.displayBlock = Block.get(NBTIO.getBlockStateHelper(nbt.getCompound(TAG_DISPLAY_BLOCK)))
        }
        if (nbt.contains(TAG_DISPLAY_OFFSET)) {
            this.displayOffset = nbt.getInt(TAG_DISPLAY_OFFSET)
        }
    }

    abstract fun getType(): MinecartType

    abstract fun isRideable(): Boolean

    override fun getHeight(): Float {
        return 0.7f
    }

    override fun getWidth(): Float {
        return 0.98f
    }

    override fun getDrag(): Float {
        return 0.1f
    }

    public override fun getBaseOffset(): Float {
        return 0.35f
    }

    override fun canDoInteraction(): Boolean {
        return passengers.isEmpty() && this.displayBlock == null
    }

    public override fun initEntity() {
        super.initEntity()

        prepareDataProperty()
    }

    override fun onUpdate(currentTick: Int): Boolean {
        if (this.closed) {
            return false
        }

        if (!this.isAlive()) {
            this.despawnFromAll()
            this.close()
            return false
        }

        val tickDiff: Int = currentTick - this.lastUpdate

        if (tickDiff <= 0) {
            return false
        }

        this.lastUpdate = currentTick

        if (isAlive()) {
            super.onUpdate(currentTick)

            // The damage token
            if (health < 20) {
                setHealthSafe(health + 1)
            }

            // Entity variables
            prevPosition.x = position.x
            prevPosition.y = position.y
            prevPosition.z = position.z
            motion.y -= 0.03999999910593033
            val dx: Int = floor(position.x).toInt()
            var dy: Int = floor(position.y).toInt()
            val dz: Int = floor(position.z).toInt()

            // Some hack to check rails
            if (Rail.isRailBlock(level!!.getBlockIdAt(dx, dy - 1, dz))) {
                --dy
            }

            val block: Block = level!!.getBlock(Vector3(dx.toDouble(), dy.toDouble(), dz.toDouble()))

            // Ensure that the block is a rail
            if (Rail.isRailBlock(block)) {
                processMovement(dx, dy, dz, block as BlockRail)
                // Activate the minecart/TNT
                if (block is BlockActivatorRail && block.isActive()) {
                    activate(dx, dy, dz, block.isActive())
                    if (this.isRideable() && this.getRiding() != null) {
                        this.dismountEntity(getRiding()!!)
                    }
                }
                if (block is BlockDetectorRail && !block.isActive()) {
                    block.updateState(true)
                }
            } else {
                setFalling()
            }
            checkBlockCollision()

            // Minecart head
            rotation.pitch = 0.0
            val diffX: Double = prevPosition.x - position.x
            val diffZ: Double = prevPosition.z - position.z
            var yawToChange: Double = rotation.yaw
            if (diffX * diffX + diffZ * diffZ > 0.001) {
                yawToChange = (atan2(diffZ, diffX) * 180 / Math.PI)
            }

            // Reverse yaw if yaw is below 0
            if (yawToChange < 0) {
                // -90-(-90)-(-90) = 90
                yawToChange -= 0.0
            }

            setRotation(yawToChange, rotation.pitch)

            val from: Transform = Transform(
                prevPosition.x,
                prevPosition.y, prevPosition.z, prevRotation.yaw,
                prevRotation.pitch,
                level!!
            )
            val to: Transform = Transform(
                position.x,
                position.y, position.z, rotation.yaw, rotation.pitch,
                level!!
            )

            Server.instance.pluginManager.callEvent(VehicleUpdateEvent(this))

            if (from != to) {
                Server.instance.pluginManager.callEvent(VehicleMoveEvent(this, from, to))
            }

            // Collisions
            for (entity: Entity in level!!.getNearbyEntities(boundingBox.grow(0.2, 0.0, 0.2), this)) {
                if (!passengers.contains(entity) && entity is EntityMinecartAbstract) {
                    entity.applyEntityCollision(this)
                }
            }

            val linkedIterator: MutableIterator<Entity?> = passengers.iterator()

            while (linkedIterator.hasNext()) {
                val linked: Entity? = linkedIterator.next()

                if (!linked!!.isAlive()) {
                    if (linked.riding === this) {
                        linked.riding = null
                    }

                    linkedIterator.remove()
                }
            }

            //使矿车通知漏斗更新而不是漏斗来检测矿车
            //通常情况下，矿车的数量远远少于漏斗，所以说此举能大福提高性能
            if (this is InventoryHolder) {
                val pickupArea: SimpleAxisAlignedBB = SimpleAxisAlignedBB(
                    position.x,
                    position.y - 1,
                    position.z, position.x + 1, position.y, position.z + 1
                )
                checkPickupHopper(pickupArea, this)
                //漏斗矿车会自行拉取物品!
                if (this !is EntityHopperMinecart) {
                    val pushArea: SimpleAxisAlignedBB = SimpleAxisAlignedBB(
                        position.x,
                        position.y,
                        position.z, position.x + 1, position.y + 2, position.z + 1
                    )
                    checkPushHopper(pushArea, this)
                }
            }

            // No need to onGround or Motion diff! This always have an update
            return true
        }

        return false
    }

    override fun attack(source: EntityDamageEvent): Boolean {
        if (invulnerable) {
            return false
        } else {
            source.damage = (source.damage * 15)

            val attack: Boolean = super.attack(source)

            if (isAlive()) {
                performHurtAnimation()
            }

            return attack
        }
    }

    open fun dropItem() {
        if (lastDamageCause is EntityDamageByEntityEvent) {
            val damager: Entity = (lastDamageCause as EntityDamageByEntityEvent).damager
            if (damager is Player && damager.isCreative) {
                return
            }
        }
        level!!.dropItem(this.position, ItemMinecart())
    }

    override fun kill() {
        if (!isAlive()) {
            return
        }
        super.kill()

        if (level!!.gameRules.getBoolean(GameRule.DO_ENTITY_DROPS)) {
            dropItem()
        }
    }

    override fun close() {
        super.close()

        for (passenger: Entity? in ArrayList(this.passengers)) {
            dismountEntity(passenger!!)
        }
    }

    override fun onInteract(p: Player, item: Item, clickedPos: Vector3): Boolean {
        if (!passengers.isEmpty() && isRideable()) {
            return false
        }

        if (displayBlock == null) {
            mountEntity(p)
        }

        return super.onInteract(p, item, clickedPos)
    }

    override fun applyEntityCollision(entity: Entity) {
        if (entity !== riding && !(entity is Player && entity.isSpectator)) {
            if (entity is EntityLiving
                && (entity !is EntityHuman) && motion.x * motion.x + motion.z * motion.z > 0.01 && passengers.isEmpty()
                && entity.riding == null && displayBlock == null
            ) {
                if (riding == null && devs) {
                    mountEntity(entity) // TODO: rewrite (weird riding)
                }
            }

            var motiveX: Double = entity.position.x - position.x
            var motiveZ: Double = entity.position.z - position.z
            var square: Double = motiveX * motiveX + motiveZ * motiveZ

            if (square >= 9.999999747378752E-5) {
                square = sqrt(square)
                motiveX /= square
                motiveZ /= square
                var next: Double = 1 / square

                if (next > 1) {
                    next = 1.0
                }

                motiveX *= next
                motiveZ *= next
                motiveX *= 0.10000000149011612
                motiveZ *= 0.10000000149011612
                motiveX *= 1 + entityCollisionReduction
                motiveZ *= 1 + entityCollisionReduction
                motiveX *= 0.5
                motiveZ *= 0.5
                if (entity is EntityMinecartAbstract) {
                    val desinityX: Double = entity.position.x - position.x
                    val desinityZ: Double = entity.position.z - position.z
                    val vector: Vector3 = Vector3(desinityX, 0.0, desinityZ).normalize()
                    val vec: Vector3 = Vector3(
                        cos(rotation.yaw.toFloat() * 0.017453292f).toDouble(), 0.0, sin(
                            rotation.yaw.toFloat() * 0.017453292f
                        ).toDouble()
                    ).normalize()
                    val desinityXZ: Double = abs(vector.dot(vec))

                    if (desinityXZ < 0.800000011920929) {
                        return
                    }

                    var motX: Double = entity.motion.x + motion.x
                    var motZ: Double = entity.motion.z + motion.z

                    if (entity.getType().id == 2 && getType().id != 2) {
                        motion.x *= 0.20000000298023224
                        motion.z *= 0.20000000298023224
                        motion.x += entity.motion.x - motiveX
                        motion.z += entity.motion.z - motiveZ
                        entity.motion.x *= 0.949999988079071
                        entity.motion.z *= 0.949999988079071
                    } else if (entity.getType().id != 2 && getType().id == 2) {
                        entity.motion.x *= 0.20000000298023224
                        entity.motion.z *= 0.20000000298023224
                        motion.x += entity.motion.x + motiveX
                        motion.z += entity.motion.z + motiveZ
                        motion.x *= 0.949999988079071
                        motion.z *= 0.949999988079071
                    } else {
                        motX /= 2.0
                        motZ /= 2.0
                        motion.x *= 0.20000000298023224
                        motion.z *= 0.20000000298023224
                        motion.x += motX - motiveX
                        motion.z += motZ - motiveZ
                        entity.motion.x *= 0.20000000298023224
                        entity.motion.z *= 0.20000000298023224
                        entity.motion.x += motX + motiveX
                        entity.motion.z += motZ + motiveZ
                    }
                } else {
                    motion.x -= motiveX
                    motion.z -= motiveZ
                }
            }
        }
    }

    override fun saveNBT() {
        super.saveNBT()

        if (this.customDisplayTile != null) {
            namedTag!!.putBoolean(
                TAG_CUSTOM_DISPLAY_TILE,
                customDisplayTile!!
            )
        }
        if (this.displayBlock != null) {
            namedTag!!.putCompound(
                TAG_DISPLAY_BLOCK,
                displayBlock!!.blockState.blockStateTag
            )
        }
        if (this.displayOffset != null) {
            namedTag!!.putInt(TAG_DISPLAY_OFFSET, displayOffset!!)
        }
    }

    fun getMaxSpeed(): Double {
        return maxSpeed
    }

    protected open fun activate(x: Int, y: Int, z: Int, flag: Boolean) {
    }

    /**
     * 检查邻近的漏斗并通知它输出物品
     *
     * @param pushArea 漏斗输出范围
     * @return 是否有漏斗被通知
     */
    private fun checkPushHopper(pushArea: AxisAlignedBB, holder: InventoryHolder): Boolean {
        val minX: Int = floor(pushArea.minX).toInt()
        val minY: Int = floor(pushArea.minY).toInt()
        val minZ: Int = floor(pushArea.minZ).toInt()
        val maxX: Int = ceil(pushArea.maxX).toInt()
        val maxY: Int = ceil(pushArea.maxY).toInt()
        val maxZ: Int = ceil(pushArea.maxZ).toInt()
        val tmpBV: BlockVector3 = BlockVector3()
        for (z in minZ..maxZ) {
            for (x in minX..maxX) {
                for (y in minY..maxY) {
                    tmpBV.setComponents(x, y, z)
                    val be = level!!.getBlockEntity(tmpBV)
                    if (be is BlockEntityHopper) {
                        be.minecartInvPushTo = (holder)
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * 检查邻近的漏斗并通知它获取物品
     *
     * @param pickupArea 漏斗拉取范围
     * @return 是否有漏斗被通知
     */
    private fun checkPickupHopper(pickupArea: AxisAlignedBB, holder: InventoryHolder): Boolean {
        val minX: Int = floor(pickupArea.minX).toInt()
        val minY: Int = floor(pickupArea.minY).toInt()
        val minZ: Int = floor(pickupArea.minZ).toInt()
        val maxX: Int = ceil(pickupArea.maxX).toInt()
        val maxY: Int = ceil(pickupArea.maxY).toInt()
        val maxZ: Int = ceil(pickupArea.maxZ).toInt()
        val tmpBV: BlockVector3 = BlockVector3()
        for (z in minZ..maxZ) {
            for (x in minX..maxX) {
                for (y in minY..maxY) {
                    tmpBV.setComponents(x, y, z)
                    val be = level!!.getBlockEntity(tmpBV)
                    if (be is BlockEntityHopper) {
                        be.minecartInvPickupFrom = (holder)
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun setFalling() {
        motion.x = motion.x.coerceIn(-getMaxSpeed(), getMaxSpeed())
        motion.z = motion.z.coerceIn(-getMaxSpeed(), getMaxSpeed())

        if (!hasUpdated) {
            for (linked: Entity in passengers) {
                linked.setSeatPosition(getMountedOffset(linked).add(0f, 0.35f))
                updatePassengerPosition(linked)
            }

            hasUpdated = true
        }

        if (onGround) {
            motion.x *= derailedX
            motion.y *= derailedY
            motion.z *= derailedZ
        }

        move(motion.x, motion.y, motion.z)
        if (!onGround) {
            motion.x *= flyingX
            motion.y *= flyingY
            motion.z *= flyingZ
        }
    }

    private fun processMovement(dx: Int, dy: Int, dz: Int, block: BlockRail) {
        fallDistance = 0.0f
        val vector: Vector3? = getNextRail(
            position.x,
            position.y, position.z
        )

        position.y = dy.toDouble()
        var isPowered: Boolean = false
        var isSlowed: Boolean = false

        if (block is BlockGoldenRail) {
            isPowered = block.isActive()
            isSlowed = !block.isActive()
        }

        when (Rail.Orientation.byMetadata(block.realMeta)) {
            Rail.Orientation.ASCENDING_NORTH -> {
                motion.x -= 0.0078125
                position.y += 1.0
            }

            Rail.Orientation.ASCENDING_SOUTH -> {
                motion.x += 0.0078125
                position.y += 1.0
            }

            Rail.Orientation.ASCENDING_EAST -> {
                motion.z += 0.0078125
                position.y += 1.0
            }

            Rail.Orientation.ASCENDING_WEST -> {
                motion.z -= 0.0078125
                position.y += 1.0
            }

            else -> Unit
        }

        val facing: Array<IntArray> = matrix.get(block.realMeta)
        var facing1: Double = (facing.get(1).get(0) - facing.get(0).get(0)).toDouble()
        var facing2: Double = (facing.get(1).get(2) - facing.get(0).get(2)).toDouble()
        val speedOnTurns: Double = sqrt(facing1 * facing1 + facing2 * facing2)
        val realFacing: Double = motion.x * facing1 + motion.z * facing2

        if (realFacing < 0) {
            facing1 = -facing1
            facing2 = -facing2
        }

        var squareOfFame: Double = sqrt(motion.x * motion.x + motion.z * motion.z)

        if (squareOfFame > 2) {
            squareOfFame = 2.0
        }

        motion.x = squareOfFame * facing1 / speedOnTurns
        motion.z = squareOfFame * facing2 / speedOnTurns
        var expectedSpeed: Double
        var playerYawNeg: Double // PlayerYawNegative
        var playerYawPos: Double // PlayerYawPositive
        var motion: Double

        val linked: Entity? = getPassenger()

        if (linked is EntityLiving) {
            expectedSpeed = currentSpeed
            if (expectedSpeed > 0) {
                // This is a trajectory (Angle of elevation)
                playerYawNeg = -sin(linked.rotation.yaw * Math.PI / 180.0f)
                playerYawPos = cos(linked.rotation.yaw * Math.PI / 180.0f)
                motion = this.motion.x * this.motion.x + this.motion.z * this.motion.z
                if (motion < 0.01) {
                    this.motion.x += playerYawNeg * 0.1
                    this.motion.z += playerYawPos * 0.1

                    isSlowed = false
                }
            }
        }

        //http://minecraft.wiki/w/Powered_Rail#Rail
        if (isSlowed) {
            expectedSpeed = sqrt(this.motion.x * this.motion.x + this.motion.z * this.motion.z)
            if (expectedSpeed < 0.03) {
                this.motion.x *= 0.0
                this.motion.y *= 0.0
                this.motion.z *= 0.0
            } else {
                this.motion.x *= 0.5
                this.motion.y *= 0.0
                this.motion.z *= 0.5
            }
        }

        playerYawNeg = dx.toDouble() + 0.5 + facing.get(0).get(0).toDouble() * 0.5
        playerYawPos = dz.toDouble() + 0.5 + facing.get(0).get(2).toDouble() * 0.5
        motion = dx.toDouble() + 0.5 + facing.get(1).get(0).toDouble() * 0.5
        val wallOfFame: Double = dz.toDouble() + 0.5 + facing.get(1).get(2).toDouble() * 0.5

        facing1 = motion - playerYawNeg
        facing2 = wallOfFame - playerYawPos
        var motX: Double
        var motZ: Double

        if (facing1 == 0.0) {
            position.x = dx.toDouble() + 0.5
            expectedSpeed = position.z - dz.toDouble()
        } else if (facing2 == 0.0) {
            position.z = dz.toDouble() + 0.5
            expectedSpeed = position.x - dx.toDouble()
        } else {
            motX = position.x - playerYawNeg
            motZ = position.z - playerYawPos
            expectedSpeed = (motX * facing1 + motZ * facing2) * 2
        }

        position.x = playerYawNeg + facing1 * expectedSpeed
        position.z = playerYawPos + facing2 * expectedSpeed
        setPosition(position.clone()) // Hehe, my minstake :3

        motX = this.motion.x
        motZ = this.motion.z
        if (!passengers.isEmpty()) {
            motX *= 0.75
            motZ *= 0.75
        }
        motX = motX.coerceIn(-getMaxSpeed(), getMaxSpeed())
        motZ = motZ.coerceIn(-getMaxSpeed(), getMaxSpeed())

        move(motX, 0.0, motZ)
        if (facing[0][1] != 0 && floor(position.x).toInt() - dx == facing[0][0] && floor(
                position.z
            ).toInt() - dz == facing[0][2]
        ) {
            setPosition(
                Vector3(
                    position.x,
                    position.y + facing.get(0).get(1).toDouble(), position.z
                )
            )
        } else if (facing[1][1] != 0 && floor(position.x).toInt() - dx == facing[1][0] && floor(
                position.z
            ).toInt() - dz == facing[1][2]
        ) {
            setPosition(
                Vector3(
                    position.x,
                    position.y + facing.get(1).get(1).toDouble(), position.z
                )
            )
        }

        applyDrag()
        val vector1: Vector3? = getNextRail(
            position.x,
            position.y, position.z
        )

        if (vector1 != null && vector != null) {
            val d14: Double = (vector.y - vector1.y) * 0.05

            squareOfFame = sqrt(this.motion.x * this.motion.x + this.motion.z * this.motion.z)
            if (squareOfFame > 0) {
                this.motion.x = this.motion.x / squareOfFame * (squareOfFame + d14)
                this.motion.z = this.motion.z / squareOfFame * (squareOfFame + d14)
            }

            setPosition(Vector3(position.x, vector1.y, position.z))
        }

        val floorX: Int = floor(position.x).toInt()
        val floorZ: Int = floor(position.z).toInt()

        if (floorX != dx || floorZ != dz) {
            squareOfFame = sqrt(this.motion.x * this.motion.x + this.motion.z * this.motion.z)
            this.motion.x = squareOfFame * (floorX - dx).toDouble()
            this.motion.z = squareOfFame * (floorZ - dz).toDouble()
        }

        if (isPowered) {
            val newMovie: Double = sqrt(this.motion.x * this.motion.x + this.motion.z * this.motion.z)

            if (newMovie > 0.01) {
                val nextMovie: Double = 0.06

                this.motion.x += this.motion.x / newMovie * nextMovie
                this.motion.z += this.motion.z / newMovie * nextMovie
            } else if (block.getOrientation() == Rail.Orientation.STRAIGHT_NORTH_SOUTH) {
                if (level!!.getBlock(Vector3((dx - 1).toDouble(), dy.toDouble(), dz.toDouble())).isNormalBlock) {
                    this.motion.x = 0.02
                } else if (level!!.getBlock(Vector3((dx + 1).toDouble(), dy.toDouble(), dz.toDouble()))
                        .isNormalBlock
                ) {
                    this.motion.x = -0.02
                }
            } else if (block.getOrientation() == Rail.Orientation.STRAIGHT_EAST_WEST) {
                if (level!!.getBlock(Vector3(dx.toDouble(), dy.toDouble(), (dz - 1).toDouble())).isNormalBlock) {
                    this.motion.z = 0.02
                } else if (level!!.getBlock(Vector3(dx.toDouble(), dy.toDouble(), (dz + 1).toDouble()))
                        .isNormalBlock
                ) {
                    this.motion.z = -0.02
                }
            }
        }
    }

    private fun applyDrag() {
        if (!passengers.isEmpty() || !slowWhenEmpty) {
            motion.x *= 0.996999979019165
            motion.y *= 0.0
            motion.z *= 0.996999979019165
        } else {
            motion.x *= 0.9599999785423279
            motion.y *= 0.0
            motion.z *= 0.9599999785423279
        }
    }

    private fun getNextRail(dx: Double, dy: Double, dz: Double): Vector3? {
        var dx: Double = dx
        var dy: Double = dy
        var dz: Double = dz
        val checkX: Int = floor(dx).toInt()
        var checkY: Int = floor(dy).toInt()
        val checkZ: Int = floor(dz).toInt()

        if (Rail.isRailBlock(level!!.getBlockIdAt(checkX, checkY - 1, checkZ))) {
            --checkY
        }

        val block: Block = level!!.getBlock(Vector3(checkX.toDouble(), checkY.toDouble(), checkZ.toDouble()))

        if (Rail.isRailBlock(block)) {
            val facing: Array<IntArray> = matrix.get((block as BlockRail).realMeta)
            val rail: Double
            // Genisys mistake (Doesn't check surrounding more exactly)
            val nextOne: Double = checkX.toDouble() + 0.5 + facing.get(0).get(0).toDouble() * 0.5
            val nextTwo: Double = checkY.toDouble() + 0.5 + facing.get(0).get(1).toDouble() * 0.5
            val nextThree: Double = checkZ.toDouble() + 0.5 + facing.get(0).get(2).toDouble() * 0.5
            val nextFour: Double = checkX.toDouble() + 0.5 + facing.get(1).get(0).toDouble() * 0.5
            val nextFive: Double = checkY.toDouble() + 0.5 + facing.get(1).get(1).toDouble() * 0.5
            val nextSix: Double = checkZ.toDouble() + 0.5 + facing.get(1).get(2).toDouble() * 0.5
            val nextSeven: Double = nextFour - nextOne
            val nextEight: Double = (nextFive - nextTwo) * 2
            val nextMax: Double = nextSix - nextThree

            if (nextSeven == 0.0) {
                rail = dz - checkZ.toDouble()
            } else if (nextMax == 0.0) {
                rail = dx - checkX.toDouble()
            } else {
                val whatOne: Double = dx - nextOne
                val whatTwo: Double = dz - nextThree

                rail = (whatOne * nextSeven + whatTwo * nextMax) * 2
            }

            dx = nextOne + nextSeven * rail
            dy = nextTwo + nextEight * rail
            dz = nextThree + nextMax * rail
            if (nextEight < 0) {
                ++dy
            }

            if (nextEight > 0) {
                dy += 0.5
            }

            return Vector3(dx, dy, dz)
        } else {
            return null
        }
    }

    /**
     * Used to multiply the minecart current speed
     *
     * @param speed The speed of the minecart that will be calculated
     */
    fun setCurrentSpeed(speed: Double) {
        currentSpeed = speed
    }

    private fun prepareDataProperty() {
        setRollingAmplitude(0)
        setRollingDirection(1)

        if (this.customDisplayTile != null) {
            setDataProperty(EntityDataTypes.CUSTOM_DISPLAY, if (customDisplayTile!!) 1 else 0)
        }
        if (this.displayBlock != null) {
            setDataProperty(EntityDataTypes.HORSE_FLAGS, displayBlock!!.runtimeId)
        }
        if (this.displayOffset != null) {
            setDataProperty(EntityDataTypes.DISPLAY_OFFSET, displayOffset!!)
        }
    }

    /**
     * Set the minecart display block
     *
     * @param block  The block that will changed. Set `null` for BlockAir
     * @param update Do update for the block. (This state changes if you want to show the block)
     */
    fun setDisplayBlock(block: Block?, update: Boolean) {
        if (!update) {
            displayBlock = block
            return
        }
        displayBlock = block
        if (displayBlock != null) {
            setCustomDisplayTile(true)
            setDataProperty(EntityDataTypes.HORSE_FLAGS, displayBlock!!.runtimeId)
            setDisplayBlockOffset(6)
        } else {
            setCustomDisplayTile(false)
            setDataProperty(EntityDataTypes.HORSE_FLAGS, 0)
            setDisplayBlockOffset(0)
        }
    }

    fun setCustomDisplayTile(customDisplayTile: Boolean) {
        this.customDisplayTile = customDisplayTile
        setDataProperty(EntityDataTypes.CUSTOM_DISPLAY, if (customDisplayTile) 1 else 0)
    }

    fun setDisplayBlockOffset(offset: Int) {
        this.displayOffset = offset
        setDataProperty(EntityDataTypes.DISPLAY_OFFSET, offset)
    }

    /**
     * Is the minecart can be slowed when empty?
     *
     * @return boolean
     */
    fun isSlowWhenEmpty(): Boolean {
        return slowWhenEmpty
    }

    /**
     * Set the minecart slowdown flag
     *
     * @param slow The slowdown flag
     */
    fun setSlowWhenEmpty(slow: Boolean) {
        slowWhenEmpty = slow
    }

    fun getFlyingVelocityMod(): Vector3 {
        return Vector3(flyingX, flyingY, flyingZ)
    }

    fun setFlyingVelocityMod(flying: Vector3) {
        flyingX = flying.x
        flyingY = flying.y
        flyingZ = flying.z
    }

    fun getDerailedVelocityMod(): Vector3 {
        return Vector3(derailedX, derailedY, derailedZ)
    }

    fun setDerailedVelocityMod(derailed: Vector3) {
        derailedX = derailed.x
        derailedY = derailed.y
        derailedZ = derailed.z
    }

    fun setMaximumSpeed(speed: Double) {
        maxSpeed = speed
    }

    companion object {
        const val TAG_CUSTOM_DISPLAY_TILE: String = "CustomDisplayTile"
        const val TAG_DISPLAY_BLOCK: String = "DisplayBlock"
        const val TAG_DISPLAY_OFFSET: String = "DisplayOffset"

        private val matrix: Array<Array<IntArray>> = arrayOf(
            arrayOf(intArrayOf(0, 0, -1), intArrayOf(0, 0, 1)),
            arrayOf(intArrayOf(-1, 0, 0), intArrayOf(1, 0, 0)),
            arrayOf(intArrayOf(-1, -1, 0), intArrayOf(1, 0, 0)),
            arrayOf(intArrayOf(-1, 0, 0), intArrayOf(1, -1, 0)),
            arrayOf(intArrayOf(0, 0, -1), intArrayOf(0, -1, 1)),
            arrayOf(intArrayOf(0, -1, -1), intArrayOf(0, 0, 1)),
            arrayOf(intArrayOf(0, 0, 1), intArrayOf(1, 0, 0)),
            arrayOf(intArrayOf(0, 0, 1), intArrayOf(-1, 0, 0)),
            arrayOf(intArrayOf(0, 0, -1), intArrayOf(-1, 0, 0)),
            arrayOf(intArrayOf(0, 0, -1), intArrayOf(1, 0, 0))
        )
    }
}
