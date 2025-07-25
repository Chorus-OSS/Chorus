package org.chorus_oss.chorus.command.tree.node

class RelativeFloatNode : RelativeNumberNode<Float>() {
    override fun fill(arg: String) {
        if (arg.startsWith("~")) {
            if (arg.length == 1) {
                this.value = 0f
            } else {
                try {
                    this.value = arg.substring(1).toFloat()
                } catch (e: NumberFormatException) {
                    this.error()
                }
            }
        } else {
            try {
                this.value = arg.toFloat()
            } catch (e: NumberFormatException) {
                this.error()
            }
        }
    }

    override fun get(base: Float): Float {
        return base + value!!
    }
}
