package dev.kosmx.geyserEmotes


fun main() {
    val data = listOf("asd", "minecraft:register", "moar strings", "somestring").fold(ByteArray(0)) { acc, s ->
        (if(acc.isEmpty()) acc else acc + ByteArray(1).apply { this[0] = 0 }) + s.encodeToByteArray()
    }

    println(data.contentToString())
    println(DataUtil.readCStrings(data))

}

