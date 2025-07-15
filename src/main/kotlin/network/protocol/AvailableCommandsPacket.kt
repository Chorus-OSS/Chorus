package org.chorus_oss.chorus.network.protocol

import com.google.common.base.Preconditions
import org.chorus_oss.chorus.command.data.*
import org.chorus_oss.chorus.network.DataPacket
import org.chorus_oss.chorus.network.ProtocolInfo
import org.chorus_oss.chorus.network.connection.util.HandleByteBuf
import org.chorus_oss.chorus.network.protocol.types.CommandEnumConstraintData
import org.chorus_oss.chorus.utils.SequencedHashSet
import org.chorus_oss.chorus.utils.Utils
import java.util.function.ObjIntConsumer

class AvailableCommandsPacket : DataPacket() {
    var commands: Map<String, CommandDataVersions>? = null
    val constraints: List<CommandEnumConstraintData> = listOf()

    override fun encode(byteBuf: HandleByteBuf) {
        val enumValuesSet: MutableSet<String> = mutableSetOf()
        val subCommandValues = SequencedHashSet<String>()
        val postfixSet: MutableSet<String> = mutableSetOf()
        val subCommandData = SequencedHashSet<ChainedSubCommandData>()
        val enumsSet: MutableSet<CommandEnum> = mutableSetOf()
        val softEnumsSet: MutableSet<CommandEnum> = mutableSetOf()

        // Get all enum values
        for ((_, value1) in commands!!) {
            val data = value1.versions[0]
            if (data.aliases != null) {
                enumValuesSet.addAll(data.aliases!!.getValues())
                enumsSet.add(data.aliases!!)
            }

            for (subcommand in data.subcommands) {
                if (subCommandData.contains(subcommand)) {
                    continue
                }

                subCommandData.add(subcommand)
                for (value in subcommand.values) {
                    if (subCommandValues.contains(value.first)) {
                        subCommandValues.add(value.first!!)
                    }

                    if (subCommandValues.contains(value.second)) {
                        subCommandValues.add(value.second!!)
                    }
                }
            }

            for (overload in data.overloads.values.stream()
                .map { it!!.input.parameters }.toList()) {
                for (parameter in overload) {
                    val commandEnumData = parameter.enumData
                    if (commandEnumData != null) {
                        if (commandEnumData.isSoft) {
                            softEnumsSet.add(commandEnumData)
                        } else {
                            enumValuesSet.addAll(commandEnumData.getValues())
                            enumsSet.add(commandEnumData)
                        }
                    }

                    val postfix = parameter.postFix
                    if (postfix != null) {
                        postfixSet.add(postfix)
                    }
                }
            }
        }

        // Add Constraint Enums
        // Not need it for now
        /*
        for(CommandEnum enumData : constraints.stream().map(CommandEnumConstraintData::getEnumData).collect(Collectors.toList())) {
            if (enumData.isSoft()) {
                softEnumsSet.add(enumData);
            } else {
                enumsSet.add(enumData);
            }
            enumValuesSet.addAll(Arrays.asList(String.valueOf(enumData.getValues())));
        }*/
        val enumValues: List<String> = enumValuesSet.toList()
        val postFixes: List<String> = postfixSet.toList()
        val enums: List<CommandEnum> = enumsSet.toList()
        val softEnums: List<CommandEnum> = softEnumsSet.toList()

        byteBuf.writeUnsignedVarInt(enumValues.size)
        for (enumValue in enumValues) {
            byteBuf.writeString(enumValue)
        }

        byteBuf.writeUnsignedVarInt(subCommandValues.size)
        for (subCommandValue in subCommandValues) {
            byteBuf.writeString(subCommandValue)
        }

        byteBuf.writeUnsignedVarInt(postFixes.size)
        for (postFix in postFixes) {
            byteBuf.writeString(postFix)
        }

        this.writeEnums(byteBuf, enumValues, enums)

        byteBuf.writeUnsignedVarInt(subCommandData.size)
        for (chainedSubCommandData in subCommandData) {
            byteBuf.writeString(chainedSubCommandData.name!!)
            byteBuf.writeUnsignedVarInt(chainedSubCommandData.values.size)
            for (value in chainedSubCommandData.values) {
                val first = subCommandValues.indexOf(value.first)
                Preconditions.checkArgument(first > -1, "Invalid enum value detected: " + value.first)

                val second = subCommandValues.indexOf(value.second)
                Preconditions.checkArgument(second > -1, "Invalid enum value detected: " + value.second)

                byteBuf.writeShortLE(first)
                byteBuf.writeShortLE(second)
            }
        }

        byteBuf.writeUnsignedVarInt(commands!!.size)
        for (entry in commands!!.entries) {
            this.writeCommand(byteBuf, entry, enums, softEnums, postFixes, subCommandData)
        }

        byteBuf.writeUnsignedVarInt(softEnums.size)
        for (softEnum in softEnums) {
            this.writeCommandEnum(byteBuf, softEnum)
        }

        // Constraints
        // Not need it for now
        /*helper.writeArray(buffer, packet.getConstraints(), (buf, constraint) -> {
            helper.writeCommandEnumConstraints(buf, constraint, enums, enumValues);
        });*/
        byteBuf.writeUnsignedVarInt(0)
    }

    private fun writeEnums(byteBuf: HandleByteBuf, values: List<String?>, enums: List<CommandEnum>) {
        // Determine width of enum index
        val indexWriter: ObjIntConsumer<HandleByteBuf>
        val valuesSize = values.size
        indexWriter = if (valuesSize < 0x100) { //256
            WRITE_BYTE
        } else if (valuesSize < 0x10000) { //65536
            WRITE_SHORT
        } else {
            WRITE_INT
        }
        // Write enums
        byteBuf.writeUnsignedVarInt(enums.size)
        for (commandEnum in enums) {
            byteBuf.writeString(commandEnum.name)

            byteBuf.writeUnsignedVarInt(commandEnum.getValues().size)
            for (value in commandEnum.getValues()) {
                val index = values.indexOf(value)
                Preconditions.checkArgument(index > -1, "Invalid enum value detected: $value")
                indexWriter.accept(byteBuf, index)
            }
        }
    }

    private fun writeCommand(
        byteBuf: HandleByteBuf,
        commandEntry: Map.Entry<String?, CommandDataVersions>,
        enums: List<CommandEnum?>,
        softEnums: List<CommandEnum?>,
        postFixes: List<String>,
        subCommands: List<ChainedSubCommandData>
    ) {
        val commandData = commandEntry.value.versions[0]
        byteBuf.writeString(commandEntry.key!!)
        byteBuf.writeString(commandData.description)
        var flags = 0
        for (flag in commandData.flags) {
            flags = flags or flag.bit
        }
        byteBuf.writeShortLE(flags)
        byteBuf.writeByte(commandData.permission.toByte().toInt())

        byteBuf.writeIntLE(if (commandData.aliases == null) -1 else enums.indexOf(commandData.aliases))

        byteBuf.writeUnsignedVarInt(subCommands.size)
        for (subcommand in subCommands) {
            val index = subCommands.indexOf(subcommand)
            Preconditions.checkArgument(index > -1, "Invalid subcommand index: $subcommand")
            byteBuf.writeShortLE(index)
        }

        val overloads: Collection<CommandOverload> = commandData.overloads.values
        byteBuf.writeUnsignedVarInt(overloads.size)
        for (overload in overloads) {
            byteBuf.writeBoolean(overload.chaining)
            byteBuf.writeUnsignedVarInt(overload.input.parameters.size)
            for (param in overload.input.parameters) {
                this.writeParameter(byteBuf, param, enums, softEnums, postFixes)
            }
        }
    }

    private fun writeParameter(
        byteBuf: HandleByteBuf,
        param: CommandParameter,
        enums: List<CommandEnum?>,
        softEnums: List<CommandEnum?>,
        postFixes: List<String?>
    ) {
        byteBuf.writeString(param.name)
        val index = if (param.postFix != null) {
            postFixes.indexOf(param.postFix) or ARG_FLAG_POSTFIX
        } else if (param.enumData != null) {
            if (param.enumData.isSoft) {
                softEnums.indexOf(param.enumData) or ARG_FLAG_SOFT_ENUM or ARG_FLAG_VALID
            } else {
                enums.indexOf(param.enumData) or ARG_FLAG_ENUM or ARG_FLAG_VALID
            }
        } else if (param.type != null) {
            param.type.id or ARG_FLAG_VALID
        } else {
            throw IllegalStateException("No param type specified: $param")
        }

        byteBuf.writeIntLE(index)
        byteBuf.writeBoolean(param.optional)

        var options: Byte = 0
        if (param.paramOptions != null) {
            for (option in param.paramOptions!!) {
                options = (options.toInt() or (1 shl option.ordinal)).toByte()
            }
        }
        byteBuf.writeByte(options.toInt())
    }

    private fun writeCommandEnum(byteBuf: HandleByteBuf, enumData: CommandEnum?) {
        Preconditions.checkNotNull(enumData, "enumData")

        byteBuf.writeString(enumData!!.name)

        val values = enumData.getValues()
        byteBuf.writeUnsignedVarInt(values.size)
        for (value in values) {
            byteBuf.writeString(value)
        }
    }

    override fun pid(): Int {
        return ProtocolInfo.AVAILABLE_COMMANDS_PACKET
    }

    companion object {
        private val WRITE_BYTE =
            ObjIntConsumer { s: HandleByteBuf, v: Int -> s.writeByte(v.toByte().toInt()) }
        private val WRITE_SHORT =
            ObjIntConsumer { obj: HandleByteBuf, value: Int -> obj.writeShortLE(value) }
        private val WRITE_INT =
            ObjIntConsumer { obj: HandleByteBuf, value: Int -> obj.writeIntLE(value) }

        const val ARG_FLAG_VALID: Int = 0x100000
        const val ARG_FLAG_ENUM: Int = 0x200000
        const val ARG_FLAG_POSTFIX: Int = 0x1000000
        const val ARG_FLAG_SOFT_ENUM: Int = 0x4000000

        val ARG_TYPE_INT: Int = Utils.dynamic(1)
        val ARG_TYPE_FLOAT: Int = Utils.dynamic(3)
        val ARG_TYPE_VALUE: Int = Utils.dynamic(4)
        val ARG_TYPE_WILDCARD_INT: Int = Utils.dynamic(5)
        val ARG_TYPE_OPERATOR: Int = Utils.dynamic(6)
        val ARG_TYPE_COMPARE_OPERATOR: Int = Utils.dynamic(7)
        val ARG_TYPE_TARGET: Int = Utils.dynamic(8)
        val ARG_TYPE_WILDCARD_TARGET: Int = Utils.dynamic(10)

        val ARG_TYPE_FILE_PATH: Int = Utils.dynamic(17)

        val ARG_TYPE_FULL_INTEGER_RANGE: Int = Utils.dynamic(23)

        val ARG_TYPE_EQUIPMENT_SLOT: Int = Utils.dynamic(47)
        val ARG_TYPE_STRING: Int = Utils.dynamic(56)
        val ARG_TYPE_BLOCK_POSITION: Int = Utils.dynamic(64)
        val ARG_TYPE_POSITION: Int = Utils.dynamic(65)

        val ARG_TYPE_MESSAGE: Int = Utils.dynamic(68)
        val ARG_TYPE_RAWTEXT: Int = Utils.dynamic(70)
        val ARG_TYPE_JSON: Int = Utils.dynamic(74)
        val ARG_TYPE_BLOCK_STATES: Int = Utils.dynamic(84)
        val ARG_TYPE_COMMAND: Int = Utils.dynamic(87)
    }
}
