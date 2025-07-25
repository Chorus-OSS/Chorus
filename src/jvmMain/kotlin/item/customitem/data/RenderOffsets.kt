package org.chorus_oss.chorus.item.customitem.data

import org.chorus_oss.chorus.math.Vector3f
import org.chorus_oss.chorus.nbt.tag.CompoundTag
import org.chorus_oss.chorus.nbt.tag.FloatTag
import org.chorus_oss.chorus.nbt.tag.ListTag

/**
 * RenderOffsets是设置 render_offsets 项目组件。可以设置参数来偏移物品的在不同视角下的呈现方式。
 *
 *
 * RenderOffsets is the component that sets the render_offsets item. Parameters can be set to offset the rendering of items in different views.
 */
class RenderOffsets(
    mainHandFirstPerson: Offset?,
    mainHandThirdPerson: Offset?,
    offHandFirstPerson: Offset?,
    offHandThirdPerson: Offset?
) {
    val nbt: CompoundTag = CompoundTag()

    /**
     * 设置自定义物品在不同视角下的渲染偏移量
     *
     *
     * Set rendering offsets for custom items at different viewpoints
     *
     * @param mainHandFirstPerson 设置第一人称主手物品的偏移量<br></br>Set the offset of the first person main hand item
     * @param mainHandThirdPerson 设置第三人称主手物品的偏移量<br></br>Set the offset of the third person main hand item
     * @param offHandFirstPerson  设置第一人称副手物品的偏移量<br></br>Set the offset of the first person offhand item
     * @param offHandThirdPerson  设置第三人称副手物品的偏移量<br></br>Set the offset of the third person offhand item
     */
    init {
        if (mainHandFirstPerson != null || mainHandThirdPerson != null) {
            nbt.putCompound("main_hand", CompoundTag())
            if (mainHandFirstPerson != null) {
                nbt.getCompound("main_hand").putCompound(
                    "first_person",
                    xyzToCompoundTag(
                        mainHandFirstPerson.position,
                        mainHandFirstPerson.rotation,
                        mainHandFirstPerson.scale
                    )
                )
            }
            if (mainHandThirdPerson != null) {
                nbt.getCompound("main_hand").putCompound(
                    "third_person",
                    xyzToCompoundTag(
                        mainHandThirdPerson.position,
                        mainHandThirdPerson.rotation,
                        mainHandThirdPerson.scale
                    )
                )
            }
        }
        if (offHandFirstPerson != null || offHandThirdPerson != null) {
            nbt.putCompound("off_hand", CompoundTag())
            if (offHandFirstPerson != null) {
                nbt.getCompound("off_hand").putCompound(
                    "first_person",
                    xyzToCompoundTag(offHandFirstPerson.position, offHandFirstPerson.rotation, offHandFirstPerson.scale)
                )
            }
            if (offHandThirdPerson != null) {
                nbt.getCompound("off_hand").putCompound(
                    "third_person",
                    xyzToCompoundTag(offHandThirdPerson.position, offHandThirdPerson.rotation, offHandThirdPerson.scale)
                )
            }
        } else require(!(mainHandFirstPerson == null && mainHandThirdPerson == null)) { "Do not allow all parameters to be empty, if you do not want to specify, please do not use the renderOffsets method" }
    }

    private fun xyzToCompoundTag(pos: Vector3f?, rot: Vector3f?, sc: Vector3f?): CompoundTag {
        val result = CompoundTag()
        if (pos != null) {
            val position = ListTag<FloatTag>()
            position.add(FloatTag(pos.x))
            position.add(FloatTag(pos.y))
            position.add(FloatTag(pos.z))
            result.putList("position", position)
        }
        if (rot != null) {
            val rotation = ListTag<FloatTag>()
            rotation.add(FloatTag(rot.x))
            rotation.add(FloatTag(rot.y))
            rotation.add(FloatTag(rot.z))
            result.putList("rotation", rotation)
        }
        if (sc != null) {
            val scale = ListTag<FloatTag>()
            scale.add(FloatTag(sc.x))
            scale.add(FloatTag(sc.y))
            scale.add(FloatTag(sc.z))
            result.putList("scale", scale)
        }
        return result
    }

    companion object {
        /**
         * 以指定参数调整第一人称主手的scale偏移量
         *
         *
         * Adjusts the scale offset of the first-person main hand with the specified multiplier
         *
         * @param multiplier 按照指定规模缩放物品,这只会影响scale,所以物品位置可能不正确<br></br>Scaling the item to the specified scale multiplier number, which only affects the scale, so the item position may not be correct.
         * @return the render offsets
         */
        fun scaleOffset(multiplier: Double): RenderOffsets {
            var multiplier = multiplier
            if (multiplier < 0) {
                multiplier = 1.0
            }
            val scale1 = (0.075 / multiplier).toFloat()
            val scale2 = (0.125 / multiplier).toFloat()
            val scale3 = (0.075 / (multiplier * 2.4f)).toFloat()
            return RenderOffsets(
                Offset.Companion.builder().scale(scale3, scale3, scale3),
                Offset.Companion.builder().scale(scale1, scale2, scale1),
                Offset.Companion.builder().scale(scale1, scale2, scale1),
                Offset.Companion.builder().scale(scale1, scale2, scale1)
            )
        }

        /**
         * 按照指定的物品材质大小缩放为标准16x16像素物品显示
         *
         *
         * Scale to a standard 16x16 pixel item display at the specified item texture size
         *
         * @param textureSize 指定物品材质的像素大小,只能为16的倍数<br></br>Specify the pixel size of the item texture, which can only be a multiple of 16.
         * @return the render offsets
         */
        fun scaleOffset(textureSize: Int): RenderOffsets {
            val multiplier = (textureSize / 16f).toDouble()
            return scaleOffset(multiplier)
        }
    }
}
