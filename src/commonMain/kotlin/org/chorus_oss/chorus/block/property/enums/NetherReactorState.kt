package org.chorus_oss.chorus.block.property.enums

enum class NetherReactorState {
    READY,

    INITIALIZED,

    FINISHED;

    companion object {
        private val values = entries.toTypedArray()

        fun getFromData(data: Int): NetherReactorState {
            return values[data]
        }
    }
}
