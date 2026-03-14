package me.zpleum.hyprv;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.StateRegistry;
import me.zpleum.hyprv.hook.HandshakeHook;
import me.zpleum.hyprv.hook.LoginHook;
import io.netty.util.collection.IntObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.slf4j.Logger;
import me.zpleum.hyprv.hook.PacketHook;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Plugin(id = "hyprv", name = "hyprv", version = "1.0", description = "Logs all commands executed in the proxy")
public class Hyprv {

    @Inject
    public static Logger LOGGER;

    @Inject
    public static ProxyServer PROXY_SERVER;

    public static List<PacketHook> PACKET_HOOKS = new ArrayList<>(); // แก้ตรงนี้

    @Inject
    public Hyprv(Logger logger, ProxyServer proxyServer) {
        LOGGER = logger;
        PROXY_SERVER = proxyServer;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        registerPacketHooks();
        registerListeners();
    }

    private void registerListeners() {
        PROXY_SERVER.getEventManager().register(this, new GenericListener());
    }

    private void registerPacketHooks() {
        try {
            MethodHandle packetIdToSupplierField = MethodHandles
                    .privateLookupIn(StateRegistry.PacketRegistry.ProtocolRegistry.class, MethodHandles.lookup())
                    .findGetter(StateRegistry.PacketRegistry.ProtocolRegistry.class, "packetIdToSupplier", IntObjectMap.class);

            MethodHandle packetClassToIdField = MethodHandles
                    .privateLookupIn(StateRegistry.PacketRegistry.ProtocolRegistry.class, MethodHandles.lookup())
                    .findGetter(StateRegistry.PacketRegistry.ProtocolRegistry.class, "packetClassToId", Object2IntMap.class);

            PACKET_HOOKS.add(new HandshakeHook());
            PACKET_HOOKS.add(new LoginHook()); // เพิ่มตรงนี้

            BiConsumer<? super ProtocolVersion, ? super StateRegistry.PacketRegistry.ProtocolRegistry> consumer = (version, registry) -> {
                try {
                    IntObjectMap<Supplier<? extends MinecraftPacket>> packetIdToSupplier
                            = (IntObjectMap<Supplier<? extends MinecraftPacket>>) packetIdToSupplierField.invoke(registry);

                    Object2IntMap<Class<? extends MinecraftPacket>> packetClassToId
                            = (Object2IntMap<Class<? extends MinecraftPacket>>) packetClassToIdField.invoke(registry);

                    PACKET_HOOKS.forEach(hook -> {
                        int packetId = packetClassToId.getInt(hook.getType());
                        packetClassToId.put(hook.getHookClass(), packetId);
                        packetIdToSupplier.remove(packetId);
                        packetIdToSupplier.put(packetId, hook.getHook());
                    });
                } catch (Throwable e) {
                    throw new RuntimeException("Failed to initialize packet hooks", e);
                }
            };

            MethodHandle clientboundGetter = MethodHandles.privateLookupIn(StateRegistry.class, MethodHandles.lookup())
                    .findGetter(StateRegistry.class, "clientbound", StateRegistry.PacketRegistry.class);

            MethodHandle serverboundGetter = MethodHandles.privateLookupIn(StateRegistry.class, MethodHandles.lookup())
                    .findGetter(StateRegistry.class, "serverbound", StateRegistry.PacketRegistry.class);

            StateRegistry.PacketRegistry handshakeClientbound = (StateRegistry.PacketRegistry) clientboundGetter.invokeExact(StateRegistry.CONFIG);
            StateRegistry.PacketRegistry configClientbound = (StateRegistry.PacketRegistry) clientboundGetter.invokeExact(StateRegistry.CONFIG);
            StateRegistry.PacketRegistry playClientbound = (StateRegistry.PacketRegistry) clientboundGetter.invokeExact(StateRegistry.PLAY);
            StateRegistry.PacketRegistry handshakeServerbound = (StateRegistry.PacketRegistry) serverboundGetter.invokeExact(StateRegistry.HANDSHAKE);
            StateRegistry.PacketRegistry configSeverbound = (StateRegistry.PacketRegistry) serverboundGetter.invokeExact(StateRegistry.CONFIG);
            StateRegistry.PacketRegistry playServerbound = (StateRegistry.PacketRegistry) serverboundGetter.invokeExact(StateRegistry.PLAY);

            MethodHandle versionsField = MethodHandles.privateLookupIn(StateRegistry.PacketRegistry.class, MethodHandles.lookup())
                    .findGetter(StateRegistry.PacketRegistry.class, "versions", Map.class);

            ((Map<ProtocolVersion, StateRegistry.PacketRegistry.ProtocolRegistry>) versionsField.invokeExact(handshakeClientbound)).forEach(consumer);
            ((Map<ProtocolVersion, StateRegistry.PacketRegistry.ProtocolRegistry>) versionsField.invokeExact(configClientbound)).forEach(consumer);
            ((Map<ProtocolVersion, StateRegistry.PacketRegistry.ProtocolRegistry>) versionsField.invokeExact(playClientbound)).forEach(consumer);
            ((Map<ProtocolVersion, StateRegistry.PacketRegistry.ProtocolRegistry>) versionsField.invokeExact(handshakeServerbound)).forEach(consumer);
            ((Map<ProtocolVersion, StateRegistry.PacketRegistry.ProtocolRegistry>) versionsField.invokeExact(configSeverbound)).forEach(consumer);
            ((Map<ProtocolVersion, StateRegistry.PacketRegistry.ProtocolRegistry>) versionsField.invokeExact(playServerbound)).forEach(consumer);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to initialize packet hooks", e);
        }
    }
}