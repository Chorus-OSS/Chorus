package org.chorus_oss.chorus.block.customblock.data

import org.chorus_oss.chorus.nbt.tag.CompoundTag

/**
 * 用于将方块的face(面)映射到实际的材质实例,并且设置渲染方法和参数
 *
 *
 * Used to map the face of a block to a material instance, and set the rendering method and parameters.
 */
class Materials private constructor() : NBTData {
    private val tag = CompoundTag()

    /**
     * ambientOcclusion=true, faceDimming=true
     *
     * @see .up
     */
    fun up(renderMethod: RenderMethod, texture: String): Materials {
        this.process("up", ambientOcclusion = true, faceDimming = true, renderMethod = renderMethod, texture = texture)
        return this
    }

    /**
     * 指定up面对应的渲染方法、渲染参数和材质。
     *
     *
     * Specify the corresponding rendering method, rendering parameters and texture's name for the up face.
     *
     * @param renderMethod     要使用的渲染方法<br></br>Rendering method to be used
     * @param texture          指定up方向的材质名称<br></br>Specify the texture's name of the up face
     * @param ambientOcclusion 在照明时是否应该应用环境光遮蔽?<br></br>Should I apply ambient light shielding when lighting?
     * @param faceDimming      是否应该根据它所面对的方向变暗?<br></br>Should it be dimmed according to the direction it is facing?
     * @return the materials
     */
    fun up(renderMethod: RenderMethod, ambientOcclusion: Boolean, faceDimming: Boolean, texture: String): Materials {
        this.process("up", ambientOcclusion, faceDimming, renderMethod, texture)
        return this
    }

    /**
     * ambientOcclusion=true, faceDimming=true
     *
     * @see .down
     */
    fun down(renderMethod: RenderMethod, texture: String): Materials {
        this.process(
            "down",
            ambientOcclusion = true,
            faceDimming = true,
            renderMethod = renderMethod,
            texture = texture
        )
        return this
    }

    /**
     * 指定down面对应的渲染方法、渲染参数和材质。
     *
     *
     * Specify the corresponding rendering method, rendering parameters and texture's name for the down face.
     *
     * @param renderMethod     要使用的渲染方法<br></br>Rendering method to be used
     * @param texture          指定down方向的材质名称<br></br>Specify the texture's name of the down face
     * @param ambientOcclusion 在照明时是否应该应用环境光遮蔽?<br></br>Should I apply ambient light shielding when lighting?
     * @param faceDimming      是否应该根据它所面对的方向变暗?<br></br>Should it be dimmed according to the direction it is facing?
     * @return the materials
     */
    fun down(renderMethod: RenderMethod, ambientOcclusion: Boolean, faceDimming: Boolean, texture: String): Materials {
        this.process("down", ambientOcclusion, faceDimming, renderMethod, texture)
        return this
    }

    /**
     * ambientOcclusion=true, faceDimming=true
     *
     * @see .north
     */
    fun north(renderMethod: RenderMethod, texture: String): Materials {
        this.process(
            "north",
            ambientOcclusion = true,
            faceDimming = true,
            renderMethod = renderMethod,
            texture = texture
        )
        return this
    }

    /**
     * 指定north面对应的渲染方法、渲染参数和材质。
     *
     *
     * Specify the corresponding rendering method, rendering parameters and texture's name for the north face.
     *
     * @param renderMethod     要使用的渲染方法<br></br>Rendering method to be used
     * @param texture          指定north方向的材质名称<br></br>Specify the texture's name of the north face
     * @param ambientOcclusion 在照明时是否应该应用环境光遮蔽?<br></br>Should I apply ambient light shielding when lighting?
     * @param faceDimming      是否应该根据它所面对的方向变暗?<br></br>Should it be dimmed according to the direction it is facing?
     * @return the materials
     */
    fun north(renderMethod: RenderMethod, ambientOcclusion: Boolean, faceDimming: Boolean, texture: String): Materials {
        this.process("north", ambientOcclusion, faceDimming, renderMethod, texture)
        return this
    }

    /**
     * ambientOcclusion=true, faceDimming=true
     *
     * @see .south
     */
    fun south(renderMethod: RenderMethod, texture: String): Materials {
        this.process(
            "south",
            ambientOcclusion = true,
            faceDimming = true,
            renderMethod = renderMethod,
            texture = texture
        )
        return this
    }

    /**
     * 指定south面对应的渲染方法、渲染参数和材质。
     *
     *
     * Specify the corresponding rendering method, rendering parameters and texture's name for the south face.
     *
     * @param renderMethod     要使用的渲染方法<br></br>Rendering method to be used
     * @param texture          指定south方向的材质名称<br></br>Specify the texture's name of the south face
     * @param ambientOcclusion 在照明时是否应该应用环境光遮蔽?<br></br>Should I apply ambient light shielding when lighting?
     * @param faceDimming      是否应该根据它所面对的方向变暗?<br></br>Should it be dimmed according to the direction it is facing?
     * @return the materials
     */
    fun south(renderMethod: RenderMethod, ambientOcclusion: Boolean, faceDimming: Boolean, texture: String): Materials {
        this.process("south", ambientOcclusion, faceDimming, renderMethod, texture)
        return this
    }

    /**
     * ambientOcclusion=true, faceDimming=true
     *
     * @see .east
     */
    fun east(renderMethod: RenderMethod, texture: String): Materials {
        this.process(
            "east",
            ambientOcclusion = true,
            faceDimming = true,
            renderMethod = renderMethod,
            texture = texture
        )
        return this
    }

    /**
     * 指定east面对应的渲染方法、渲染参数和材质。
     *
     *
     * Specify the corresponding rendering method, rendering parameters and texture's name for the east face.
     *
     * @param renderMethod     要使用的渲染方法<br></br>Rendering method to be used
     * @param texture          指定east方向的材质名称<br></br>Specify the texture's name of the east face
     * @param ambientOcclusion 在照明时是否应该应用环境光遮蔽?<br></br>Should I apply ambient light shielding when lighting?
     * @param faceDimming      是否应该根据它所面对的方向变暗?<br></br>Should it be dimmed according to the direction it is facing?
     * @return the materials
     */
    fun east(renderMethod: RenderMethod, ambientOcclusion: Boolean, faceDimming: Boolean, texture: String): Materials {
        this.process("east", ambientOcclusion, faceDimming, renderMethod, texture)
        return this
    }

    /**
     * ambientOcclusion=true, faceDimming=true
     *
     * @see .west
     */
    fun west(renderMethod: RenderMethod, texture: String): Materials {
        this.process(
            "west",
            ambientOcclusion = true,
            faceDimming = true,
            renderMethod = renderMethod,
            texture = texture
        )
        return this
    }

    /**
     * 指定west面对应的渲染方法、渲染参数和材质。
     *
     *
     * Specify the corresponding rendering method, rendering parameters and texture's name for the west face.
     *
     * @param renderMethod     要使用的渲染方法<br></br>Rendering method to be used
     * @param texture          指定west方向的材质名称<br></br>Specify the texture's name of the west face
     * @param ambientOcclusion 在照明时是否应该应用环境光遮蔽?<br></br>Should I apply ambient light shielding when lighting?
     * @param faceDimming      是否应该根据它所面对的方向变暗?<br></br>Should it be dimmed according to the direction it is facing?
     * @return the materials
     */
    fun west(renderMethod: RenderMethod, ambientOcclusion: Boolean, faceDimming: Boolean, texture: String): Materials {
        this.process("west", ambientOcclusion, faceDimming, renderMethod, texture)
        return this
    }

    /**
     * ambientOcclusion=true, faceDimming=true
     *
     * @see .any
     */
    fun any(renderMethod: RenderMethod, texture: String): Materials {
        this.process("*", ambientOcclusion = true, faceDimming = true, renderMethod = renderMethod, texture = texture)
        return this
    }

    /**
     * 指定所有面对应的渲染方法、渲染参数和材质。
     *
     *
     * Specify all corresponding rendering method, rendering parameters and texture name.
     *
     * @param renderMethod     要使用的渲染方法<br></br>Rendering method to be used
     * @param texture          材质名称<br></br>Specify the texture's name
     * @param ambientOcclusion 在照明时是否应该应用环境光遮蔽?<br></br>Should I apply ambient light shielding when lighting?
     * @param faceDimming      是否应该根据它所面对的方向变暗?<br></br>Should it be dimmed according to the direction it is facing?
     * @return the materials
     */
    fun any(renderMethod: RenderMethod, ambientOcclusion: Boolean, faceDimming: Boolean, texture: String): Materials {
        this.process("*", ambientOcclusion, faceDimming, renderMethod, texture)
        return this
    }

    /**
     * 指定对应对应的渲染方法、渲染参数和材质。此方法是完全自定义的，请在使用之前抓包确认参数合法性
     *
     * @param face             指定面的名称，可选值为：up, down, north, south, east, west, *
     * @param ambientOcclusion 在照明时是否应该应用环境光遮蔽?<br></br>Should it be applied ambient light shielding when lighting?
     * @param faceDimming      是否应该根据它所面对的方向变暗?<br></br>Should it be dimmed according to the direction it is facing?
     * @param renderMethodName 要使用的渲染方法<br></br>Rendering method to be used
     * @param texture          材质名称<br></br>Specify the texture's name
     */
    fun process(
        face: String,
        ambientOcclusion: Boolean,
        faceDimming: Boolean,
        renderMethodName: String,
        texture: String
    ) {
        tag.putCompound(
            face, CompoundTag()
                .putBoolean("ambient_occlusion", ambientOcclusion)
                .putBoolean("face_dimming", faceDimming)
                .putString("render_method", renderMethodName)
                .putString("texture", texture)
        )
    }

    private fun process(
        face: String,
        ambientOcclusion: Boolean,
        faceDimming: Boolean,
        renderMethod: RenderMethod,
        texture: String
    ) {
        tag.putCompound(
            face, CompoundTag()
                .putBoolean("ambient_occlusion", ambientOcclusion)
                .putBoolean("face_dimming", faceDimming)
                .putString("render_method", renderMethod.name.lowercase())
                .putString("texture", texture)
        )
    }

    override fun toCompoundTag(): CompoundTag {
        return tag
    }

    /**
     * [wiki.bedrock.dev](https://wiki.bedrock.dev/blocks/blocks-16.html.additional-notes)
     */
    enum class RenderMethod {
        OPAQUE,
        ALPHA_TEST,
        BLEND,
        DOUBLE_SIDED
    }

    companion object {
        /**
         * Builder materials.
         *
         * @return the materials
         */
        fun builder(): Materials {
            return Materials()
        }
    }
}
