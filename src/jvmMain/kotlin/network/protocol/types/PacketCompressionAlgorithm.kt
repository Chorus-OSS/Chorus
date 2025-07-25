package org.chorus_oss.chorus.network.protocol.types

enum class PacketCompressionAlgorithm : CompressionAlgorithm {
    ZLIB,
    SNAPPY,
    NONE
}