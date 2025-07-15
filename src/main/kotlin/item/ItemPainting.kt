package org.chorus_oss.chorus.item

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.entity.Entity.Companion.createEntity
import org.chorus_oss.chorus.entity.EntityID
import org.chorus_oss.chorus.entity.item.EntityPainting
import org.chorus_oss.chorus.entity.item.EntityPainting.Motive
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.level.vibration.VibrationEvent
import org.chorus_oss.chorus.level.vibration.VibrationType
import org.chorus_oss.chorus.math.BlockFace
import org.chorus_oss.chorus.math.Vector3
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.FloatTag
import org.chorus_oss.chorus.nbt.tag.ListTag
import java.util.concurrent.ThreadLocalRandom


class ItemPainting @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) :
    Item(ItemID.Companion.PAINTING, 0, count, "Painting") {
    override fun canBeActivated(): Boolean {
        return true
    }

    override fun onActivate(
        level: Level,
        player: Player,
        block: Block,
        target: Block,
        face: BlockFace,
        fx: Double,
        fy: Double,
        fz: Double
    ): Boolean {
        if (player.isAdventure) {
            return false
        }

        val chunk = level.getChunk(block.x.toInt() shr 4, block.z.toInt() shr 4)

        if (target.isTransparent || face.horizontalIndex == -1 || block.isSolid) {
            return false
        }

        val validMotives: MutableList<Motive> = ArrayList()
        for (motive in EntityPainting.motives) {
            if (motive.predicate.test(target.level, face, block, target)) {
                validMotives.add(motive)
            }
        }
        if (validMotives.isEmpty()) return false

        val direction = DIRECTION[face.index - 2]
        val motive = validMotives[ThreadLocalRandom.current().nextInt(validMotives.size)]

        val position = Vector3(target.position.x + 0.5, target.position.y + 0.5, target.position.z + 0.5)
        val widthOffset = offset(motive.width)

        when (face.horizontalIndex) {
            0 -> {
                position.x += widthOffset
                position.z += OFFSET
            }

            1 -> {
                position.x -= OFFSET
                position.z += widthOffset
            }

            2 -> {
                position.x -= widthOffset
                position.z -= OFFSET
            }

            3 -> {
                position.x += OFFSET
                position.z -= widthOffset
            }
        }
        position.y += offset(motive.height)

        val nbt = CompoundTag()
            .putByte("Direction", direction)
            .putString("Motive", motive.title)
            .putList(
                "Pos", ListTag<FloatTag>()
                    .add(FloatTag(position.x))
                    .add(FloatTag(position.y))
                    .add(FloatTag(position.z))
            )
            .putList(
                "Motion", ListTag<FloatTag>()
                    .add(FloatTag(0f))
                    .add(FloatTag(0f))
                    .add(FloatTag(0f))
            )
            .putList(
                "Rotation", ListTag<FloatTag>()
                    .add(FloatTag((direction * 90).toFloat()))
                    .add(FloatTag(0f))
            )

        val entity =
            createEntity(EntityID.PAINTING, chunk, nbt) as EntityPainting?
                ?: return false

        if (player.isSurvival) {
            val item = player.inventory.itemInHand
            item.setCount(item.getCount() - 1)
            player.inventory.setItemInHand(item)
        }

        entity.spawnToAll()

        level.vibrationManager.callVibrationEvent(VibrationEvent(player, position.clone(), VibrationType.ENTITY_PLACE))

        return true
    }

    companion object {
        private val DIRECTION = intArrayOf(2, 3, 4, 5)
        private val RIGHT = intArrayOf(4, 5, 3, 2)
        private const val OFFSET = 0.53125

        private fun offset(value: Int): Double {
            if (value > 1 && value != 3) {
                return 0.5
            }
            return 0.0
        }
    }
}
