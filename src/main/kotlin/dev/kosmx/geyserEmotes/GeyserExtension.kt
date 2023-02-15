package dev.kosmx.geyserEmotes

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundCustomPayloadPacket
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundCustomPayloadPacket
import com.nukkitx.protocol.bedrock.packet.EmotePacket
import org.geysermc.event.subscribe.Subscribe
import org.geysermc.geyser.api.event.ExtensionEventBus
import org.geysermc.geyser.api.event.bedrock.ClientEmoteEvent
import org.geysermc.geyser.api.event.java.ServerDefineCommandsEvent
import org.geysermc.geyser.api.event.lifecycle.GeyserPostInitializeEvent
import org.geysermc.geyser.api.extension.Extension
import org.geysermc.geyser.api.extension.ExtensionLogger
import org.geysermc.geyser.registry.Registries
import org.geysermc.geyser.session.GeyserSession
import org.geysermc.geyser.translator.protocol.PacketTranslator
import java.nio.charset.StandardCharsets
import java.util.*

open class GeyserExtension : Extension {
    private val logger: ExtensionLogger
        get() = logger()
    private val eventBus: ExtensionEventBus
        get() = eventBus()

    private val javaEmoteChannelPresence = DefaultMap<String, Boolean>(false)
    // packet contains a byte prefixed string (max 127 long), actually a UUID
    private val javaEmotePacketID = "geyser:emote"

    @Subscribe
    @Suppress("UNCHECKED_CAST", "UNUSED")
    fun onPostInitialize(event: GeyserPostInitializeEvent) {
        logger.info("Loading emotes extension")

        eventBus.subscribe(ClientEmoteEvent::class.java, this::onEmoteEvent)

        // hijack custom payload listener to add custom listener in a cascade way
        val originCustomPayloadTranslator =
            Registries.JAVA_PACKET_TRANSLATORS[ClientboundCustomPayloadPacket::class.java] as PacketTranslator<ClientboundCustomPayloadPacket>

        Registries.JAVA_PACKET_TRANSLATORS.register(
            ClientboundCustomPayloadPacket::class.java,
            object : PacketTranslator<ClientboundCustomPayloadPacket>() {
                override fun translate(session: GeyserSession, packet: ClientboundCustomPayloadPacket) {
                    if (packet.channel == "minecraft:register") {
                        val channels = DataUtil.readCStrings(data = packet.data)
                        if (javaEmotePacketID in channels) {
                            javaEmoteChannelPresence[session.remoteServer().address()] = true // look for other side opening geyser channel
                        }
                    }

                    when (packet.channel) {
                        javaEmotePacketID ->
                            onJavaEmoteEvent(session, JavaEmotePacket.read(packet.data))
                        else ->
                            originCustomPayloadTranslator.translate(session, packet)
                    }
                }
            })


        // misuse of events (again)
        eventBus.subscribe(ServerDefineCommandsEvent::class.java) { commandEvent ->
            val session = commandEvent.connection() as GeyserSession
            session.sendDownstreamPacket(ServerboundCustomPayloadPacket("minecraft:register", javaEmotePacketID.toByteArray(StandardCharsets.UTF_8))) // null separated a.k.a. no tailing null
        }

    }

    /**
     * Handle a java emote packet
     */
    fun onJavaEmoteEvent(session: GeyserSession, packet: JavaEmotePacket) {
        val bedrockEntity = session.entityCache.getEntityByJavaId(packet.runtimeEntityID.toInt())

        bedrockEntity?.let { EmotePacket().apply {
            runtimeEntityId = it.geyserId
            emoteId = packet.emoteID.toString()
        } }?.also {
            session.sendUpstreamPacket(it)
        }
    }

    //@Subscribe
    @Suppress("UNUSED")
    private fun onEmoteEvent(event: ClientEmoteEvent) {
        val session = event.connection() as GeyserSession // i'll have to send packets, hacking
        logger.info("Bedrock emote event, emotes compatible server: ${javaEmoteChannelPresence[session.remoteServer().address()]}")
        if (javaEmoteChannelPresence[session.remoteServer().address()]) {
            event.isCancelled = true

            val packet = JavaEmotePacket(session.playerEntity.entityId.toLong(), UUID.fromString(event.emoteId()))
            session.sendDownstreamPacket(ServerboundCustomPayloadPacket(
                javaEmotePacketID,
                packet.write()
            ))
        }
    }
}