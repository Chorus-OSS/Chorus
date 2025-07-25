package org.chorus_oss.chorus.command.tree.node

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.block.BlockState
import org.chorus_oss.chorus.block.property.type.BlockPropertyType

/**
 * Parse the corresponding parameter to the value of [BlockState],Must be defined one after [BlockNode]
 *
 *
 * Corresponding parameter type [org.chorus_oss.chorus.command.data.CommandParamType.BLOCK_STATES]
 */
class BlockStateNode : ParamNode<BlockState?>() {
    override fun fill(arg: String) {
        val before = before
        if (!(before is BlockNode && before.hasResult())) {
            this.error()
            return
        }
        if (!(arg.startsWith("[") && arg.endsWith("]"))) {
            this.error()
            return
        }
        val block = before.get<Block>()
        val properties = block!!.properties
        var result = properties.defaultState
        val substring = arg.substring(1, arg.length - 1)
        if (substring.isBlank()) {
            this.value = result
            return
        }

        val split = substring.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val propertyTypeSet = properties.getPropertyTypeSet()
        for (s in split) {
            val property = s.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val nameWithQuote = property[0]
            val valueWithQuote = property[1]
            val key = nameWithQuote.substring(1, nameWithQuote.length - 1)
            val value = valueWithQuote.substring(1, valueWithQuote.length - 1)
            for (propertyType in propertyTypeSet) {
                if (properties.identifier == key) {
                    if (propertyType.type == BlockPropertyType.Type.ENUM) {
                        if (propertyType.validValues.contains(value)) {
                            result = result.setPropertyValue(properties, propertyType.tryCreateValue(value)!!)
                            break
                        } else {
                            this.error()
                            return
                        }
                    } else if (propertyType.type == BlockPropertyType.Type.BOOLEAN) {
                        if (value == "true" || value == "false") {
                            result =
                                result.setPropertyValue(properties, propertyType.tryCreateValue(value.toBoolean())!!)
                            break
                        } else {
                            this.error()
                            return
                        }
                    } else if (propertyType.type == BlockPropertyType.Type.INT) {
                        try {
                            result = result.setPropertyValue(properties, propertyType.tryCreateValue(value.toInt())!!)
                            break
                        } catch (e: NumberFormatException) {
                            this.error()
                            return
                        }
                    }
                }
            }
        }
        this.value = result
    }
}
