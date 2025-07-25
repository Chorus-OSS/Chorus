package org.chorus_oss.chorus.network.connection

fun interface BedrockSessionFactory {
    fun createSession(peer: BedrockPeer, subClientId: Int): BedrockSession?
}
