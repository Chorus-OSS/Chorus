package org.chorus_oss.chorus.command.data

import org.chorus_oss.chorus.command.tree.node.IParamNode

class CommandParameter private constructor(
    @JvmField val name: String,
    @JvmField val optional: Boolean,
    @JvmField val type: CommandParamType?,
    @JvmField val enumData: CommandEnum?,
    @JvmField val postFix: String?,
    val paramNode: IParamNode<*>? = null
) {
    @JvmField
    var paramOptions: List<CommandParamOption>? = null

    companion object {
        val EMPTY_ARRAY: Array<CommandParameter> = emptyArray()

        /**
         * optional = false
         *
         * @see .newType
         */
        fun newType(name: String, type: CommandParamType): CommandParameter {
            return newType(name, false, type)
        }

        /**
         * optional = false,CommandParamOption=[]
         *
         * @see .newType
         */
        fun newType(name: String, type: CommandParamType?, paramNode: IParamNode<*>?): CommandParameter {
            return newType(name, false, type, paramNode)
        }

        /**
         * paramNode = null , CommandParamOption=[]
         *
         * @see .newType
         */
        fun newType(name: String, optional: Boolean, type: CommandParamType): CommandParameter {
            return newType(name, optional, type, null)
        }

        /**
         * paramNode = null
         *
         * @see .newType
         */
        fun newType(
            name: String,
            optional: Boolean,
            type: CommandParamType?,
            vararg options: CommandParamOption
        ): CommandParameter {
            return newType(name, optional, type, null, *options)
        }

        /**
         * 创建一个命令参数
         *
         * @param name      参数名
         * @param optional  该参数是否为可选参数
         * @param type      类型[CommandParamType]
         * @param paramNode 用于解析该参数的参数节点
         * @param options   the options
         * @return the command parameter
         */
        fun newType(
            name: String,
            optional: Boolean,
            type: CommandParamType?,
            paramNode: IParamNode<*>?,
            vararg options: CommandParamOption
        ): CommandParameter {
            val result = CommandParameter(name, optional, type, null, null, paramNode)
            if (options.isNotEmpty()) {
                result.paramOptions = listOf(*options)
            }
            return result
        }

        /**
         * optional = false
         *
         * @see .newEnum
         */
        fun newEnum(name: String, values: Array<String>): CommandParameter {
            return newEnum(name, false, values)
        }

        /**
         * [CommandEnum.getName]为 `name+"Enums"`
         *
         *
         * isSoft = false
         *
         * @see .newEnum
         */
        fun newEnum(name: String, optional: Boolean, values: Array<String>): CommandParameter {
            return newEnum(name, optional, CommandEnum(name + "Enums", *values))
        }

        /**
         * @see .newEnum
         */
        fun newEnum(name: String, optional: Boolean, values: Array<String>, soft: Boolean): CommandParameter {
            return newEnum(name, optional, CommandEnum(name + "Enums", listOf(*values), soft))
        }

        /**
         * optional = false
         *
         * @see .newEnum
         */
        fun newEnum(name: String, type: String): CommandParameter {
            return newEnum(name, false, type)
        }

        /**
         * optional = false
         *
         * @see .newEnum
         */
        fun newEnum(name: String, optional: Boolean, type: String): CommandParameter {
            return newEnum(name, optional, CommandEnum(type, ArrayList()))
        }

        /**
         * optional = false
         *
         * @see .newEnum
         */
        fun newEnum(name: String, data: CommandEnum?): CommandParameter {
            return newEnum(name, false, data)
        }

        /**
         * optional = false
         *
         * @see .newEnum
         */
        fun newEnum(name: String, optional: Boolean, data: CommandEnum?): CommandParameter {
            return CommandParameter(name, optional, null, data, null)
        }

        /**
         * optional = false
         *
         * @see .newEnum
         */
        fun newEnum(
            name: String,
            optional: Boolean,
            data: CommandEnum,
            vararg options: CommandParamOption
        ): CommandParameter {
            return newEnum(name, optional, data, null, *options)
        }

        /**
         * optional = false
         *
         * @see .newEnum
         */
        fun newEnum(name: String, optional: Boolean, data: CommandEnum?, paramNode: IParamNode<*>?): CommandParameter {
            return newEnum(name, optional, data, paramNode, *arrayOf())
        }

        /**
         * 创建一个枚举参数
         *
         * @param name      参数名称
         * @param optional  改参数是否可选
         * @param data      枚举数据[CommandEnum],其中的[CommandEnum.getName]才是真正的枚举参数名
         * @param paramNode 该参数对应的[IParamNode]
         * @param options   the options
         * @return the command parameter
         */
        fun newEnum(
            name: String,
            optional: Boolean,
            data: CommandEnum?,
            paramNode: IParamNode<*>?,
            vararg options: CommandParamOption
        ): CommandParameter {
            val result = CommandParameter(name, optional, null, data, null, paramNode)
            if (options.isNotEmpty()) {
                result.paramOptions = listOf(*options)
            }
            return result
        }
    }
}
