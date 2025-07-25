package org.chorus_oss.chorus.entity.mob.water_animal

import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.ai.behavior.Behavior
import org.chorus_oss.chorus.entity.ai.behavior.IBehavior
import org.chorus_oss.chorus.entity.ai.behaviorgroup.BehaviorGroup
import org.chorus_oss.chorus.entity.ai.behaviorgroup.IBehaviorGroup
import org.chorus_oss.chorus.entity.ai.controller.DiveController
import org.chorus_oss.chorus.entity.ai.controller.LookController
import org.chorus_oss.chorus.entity.ai.controller.SpaceMoveController
import org.chorus_oss.chorus.entity.ai.executor.SpaceRandomRoamExecutor
import org.chorus_oss.chorus.entity.ai.route.finder.impl.SimpleSpaceAStarRouteFinder
import org.chorus_oss.chorus.entity.ai.route.posevaluator.SwimmingPosEvaluator
import org.chorus_oss.chorus.entity.data.EntityDataTypes
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemID
import org.chorus_oss.chorus.level.format.IChunk
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.utils.DyeColor
import org.chorus_oss.chorus.utils.Utils
import java.util.concurrent.ThreadLocalRandom

class EntityTropicalfish(chunk: IChunk?, nbt: CompoundTag) : EntityWaterAnimal(chunk, nbt) {
    override fun getEntityIdentifier(): String {
        return EntityID.TROPICALFISH
    }

    override var color: Byte = 0
        set(value) {
            field = value
            this.setDataProperty(EntityDataTypes.COLOR, field)
            namedTag!!.putByte("Color", field.toInt())
        }

    override var variant: Int = 0
    private var mark_variant = 0
    override var color2: Byte = 0

    override fun getOriginalName(): String {
        return "Tropical Fish"
    }

    override fun getWidth(): Float {
        return 0.5f
    }

    override fun getHeight(): Float {
        return 0.4f
    }

    public override fun initEntity() {
        this.maxHealth = 6
        super.initEntity()
        if (namedTag!!.contains("Variant")) {
            this.variant = namedTag!!.getInt("Variant")
        } else {
            this.variant = getRandomVariant()
        }
        if (namedTag!!.contains("Mark_Variant")) {
            this.mark_variant = namedTag!!.getInt("Mark_Variant")
        } else {
            this.mark_variant = getRandomMarkVariant()
        }
        if (!namedTag!!.contains("Color")) {
            this.color = (getRandomColor().toByte())
        } else {
            this.color = (namedTag!!.getByte("Color"))
        }
        if (namedTag!!.contains("Color2")) {
            this.color2 = namedTag!!.getByte("Color2")
        } else {
            this.color2 = getRandomColor2()
        }
        this.setDataProperty(EntityDataTypes.MARK_VARIANT, this.mark_variant)
        this.setDataProperty(EntityDataTypes.VARIANT, this.variant)
        this.setDataProperty(EntityDataTypes.COLOR_2, this.color2)
    }

    private fun getRandomColor(): Int {
        return DyeColor.entries[ThreadLocalRandom.current().nextInt(0, 16)].woolData
    }

    private fun getRandomMarkVariant(): Int {
        return MARK_VARIANTS[Utils.rand(0, MARK_VARIANTS.size - 1)]
    }

    private fun getRandomColor2(): Byte {
        return COLOR2[Utils.rand(0, COLOR2.size - 1)]
    }

    private fun getRandomVariant(): Int {
        return VARIANTS[Utils.rand(0, VARIANTS.size - 1)]
    }

    override fun saveNBT() {
        super.saveNBT()

        namedTag!!.putByte("Color", this.color.toInt())
    }

    override fun getDrops(): Array<Item> {
        if (Utils.rand(0, 3) == 1) {
            return arrayOf(Item.get(ItemID.TROPICAL_FISH), Item.get(ItemID.BONE, 0, Utils.rand(1, 2)))
        }
        return arrayOf(Item.get(ItemID.TROPICAL_FISH))
    }

    public override fun requireBehaviorGroup(): IBehaviorGroup {
        return BehaviorGroup(
            this.tickSpread,
            setOf(),
            setOf<IBehavior>(
                Behavior(
                    SpaceRandomRoamExecutor(0.36f, 12, 1, 80, false, -1, false, 10),
                    { entity: EntityMob? -> true }, 1
                )
            ),
            setOf(),
            setOf(SpaceMoveController(), LookController(true, true), DiveController()),
            SimpleSpaceAStarRouteFinder(SwimmingPosEvaluator(), this),
            this
        )
    }

    companion object {
        private val VARIANTS = intArrayOf(0, 1)
        private val MARK_VARIANTS = intArrayOf(0, 1, 2, 3, 4, 5)
        private val COLOR2 = byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)
    }
}
