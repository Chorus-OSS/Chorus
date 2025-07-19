package org.chorus_oss.chorus.experimental.network.protocol.utils

import org.chorus_oss.chorus.command.data.ChainedSubCommandData
import org.chorus_oss.chorus.command.data.CommandDataVersions
import org.chorus_oss.chorus.command.data.CommandEnum
import org.chorus_oss.chorus.utils.SequencedHashSet
import org.chorus_oss.protocol.packets.AvailableCommandsPacket
import org.chorus_oss.protocol.types.CommandPermission
import org.chorus_oss.protocol.types.command.*
import kotlin.experimental.or

const val ARG_FLAG_VALID: Int = 0x100000
const val ARG_FLAG_ENUM: Int = 0x200000
const val ARG_FLAG_POSTFIX: Int = 0x1000000
const val ARG_FLAG_SOFT_ENUM: Int = 0x4000000

operator fun AvailableCommandsPacket.Companion.invoke(from: Map<String, CommandDataVersions>): AvailableCommandsPacket {
    val enumValuesSet: MutableSet<String> = mutableSetOf()
    val subCommandValues = SequencedHashSet<String>()
    val postfixSet: MutableSet<String> = mutableSetOf()
    val subCommandData = SequencedHashSet<ChainedSubCommandData>()
    val enumsSet: MutableSet<CommandEnum> = mutableSetOf()
    val softEnumsSet: MutableSet<CommandEnum> = mutableSetOf()

    // Get all enum values
    for ((_, value1) in from) {
        val data = value1.versions[0]
        val aliases = data.aliases
        if (aliases != null) {
            enumValuesSet.addAll(aliases.getValues())
            enumsSet.add(aliases)
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

        for (parameter in data.overloads.values.flatMap { it.input.parameters.toList() }) {
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

    val enums = enumsSet.toList()
    val enumValues = enumValuesSet.toList()
    val softEnums = softEnumsSet.toList()
    val suffixes = postfixSet.toList()
    val chainedSubcommandValues = subCommandValues.toList()

    return AvailableCommandsPacket(
        enumValues = enumValues,
        chainedSubcommandValues = chainedSubcommandValues,
        suffixes = suffixes,
        enums = enumsSet.toList().map {
            CommandEnum(
                type = it.name,
                valueIndices = it.getValues().map { s ->
                    enumValues.indexOf(s).toUInt()
                }
            )
        },
        chainedSubcommands = subCommandData.toList().map {
            ChainedSubcommand(
                name = it.name!!,
                values = it.values.map { v ->
                    ChainedSubcommandValue(
                        index = chainedSubcommandValues.indexOf(v.first!!)
                            .also { i -> require(i > -1) { "Invalid enum value: ${v.first}" } }.toUShort(),
                        value = chainedSubcommandValues.indexOf(v.second!!)
                            .also { i -> require(i > -1) { "Invalid enum value: ${v.second}" } }.toUShort(),
                    )
                }
            )
        },
        commands = from.entries.map {
            val version = it.value.versions.first()
            Command(
                name = it.key,
                description = version.description,
                flags = version.flags.fold(0.toUShort()) { acc, flag ->
                    acc or flag.bit.toUShort()
                },
                permissionLevel = CommandPermission.entries[version.permission],
                aliasesOffset = if (version.aliases == null) (-1).toUInt() else enums.indexOf(version.aliases).toUInt(),
                chainedSubcommandOffsets = subCommandData.map { d ->
                    subCommandData.indexOf(d).also { i -> require(i > -1) { "Invalid subcommand index: $d" } }
                        .toUShort()
                },
                overloads = version.overloads.values.map { o ->
                    CommandOverload(
                        chaining = o.chaining,
                        parameters = o.input.parameters.map { p ->
                            CommandParameter(
                                name = p.name,
                                typeFlags = when {
                                    p.postFix != null -> suffixes.indexOf(p.postFix) or ARG_FLAG_POSTFIX
                                    p.enumData != null -> {
                                        if (p.enumData.isSoft) {
                                            softEnums.indexOf(p.enumData) or ARG_FLAG_SOFT_ENUM or ARG_FLAG_VALID
                                        } else {
                                            enums.indexOf(p.enumData) or ARG_FLAG_ENUM or ARG_FLAG_VALID
                                        }
                                    }

                                    p.type != null -> p.type.id or ARG_FLAG_VALID
                                    else -> throw IllegalStateException("No param type specified: $p")
                                }.toUInt(),
                                optional = p.optional,
                                options = p.paramOptions?.fold(0.toByte()) { acc, v ->
                                    acc or (1 shl v.ordinal).toByte()
                                } ?: 0
                            )
                        }
                    )
                },
            )
        },
        dynamicEnums = softEnums.map {
            CommandDynamicEnum(
                it.name,
                values = it.getValues()
            )
        },
        constraints = emptyList(),
    )
}