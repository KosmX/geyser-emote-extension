package dev.kosmx.geyserEmotes

import java.nio.charset.StandardCharsets

object DataUtil {
    tailrec fun readCStrings(data: ByteArray, offset: Int = 0, list: List<String> = listOf()): List<String> {
        var end = offset + 1
        while (end < data.size && data[end] != 0.toByte()) {
            end++
        }
        // stop condition
        return if (offset >= data.size) list
        else readCStrings(data, end + 1, list + String(data, offset, end - offset, StandardCharsets.UTF_8))
    }
}
