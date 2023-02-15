package dev.kosmx.geyserEmotes

import java.io.IOException
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.*


/**
 * @param runtimeEntityID it's actually an int, but to preserve legacy compatibility, long will be used
 * @param emoteID the bedrock emote UUID
 *
 * Emotecraft geyser emote packet, both directions
 */
data class JavaEmotePacket(val runtimeEntityID: Long, val emoteID: UUID) {
    companion object {
        @Throws(IOException::class)
        fun read(bytes: ByteArray): JavaEmotePacket {
            val byteBuffer = ByteBuffer.wrap(bytes)
            val str = ByteArray(byteBuffer.get().toInt())
            byteBuffer[str]
            val emoteID = UUID.fromString(String(str, StandardCharsets.UTF_8))
            return JavaEmotePacket(byteBuffer.getLong(), emoteID) // .getLong() => .long :/
        }
    }

    fun write(): ByteArray {
        val bytes = emoteID.toString().toByteArray(StandardCharsets.UTF_8)
        val byteBuffer = ByteBuffer.allocate(bytes.size + 1 + 8)
        byteBuffer.put(bytes.size.toByte())
        byteBuffer.put(bytes)
        byteBuffer.putLong(runtimeEntityID) // this is actually ignored on the other end, but that is not a problem
        return byteBuffer.array()
    }
}
