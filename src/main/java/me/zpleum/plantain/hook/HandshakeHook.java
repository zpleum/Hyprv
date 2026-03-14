package me.zpleum.hyprv.hook;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.packet.HandshakePacket;
import io.netty.buffer.ByteBuf;
import me.zpleum.hyprv.Hyprv;

import java.lang.reflect.Field;
import java.util.function.Supplier;

public final class HandshakeHook extends HandshakePacket implements PacketHook {
    private final Field SERVER_ADDRESS_FIELD;
    private final Field PORT_FIELD;

    public HandshakeHook() {
        try {
            SERVER_ADDRESS_FIELD = HandshakePacket.class.getDeclaredField("serverAddress");
            SERVER_ADDRESS_FIELD.setAccessible(true);

            PORT_FIELD = HandshakePacket.class.getDeclaredField("port");
            PORT_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Failed to initialize handshake hook", e);
        }
    }

    @Override
    public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion ignored) {
        RegisteredServer server = Hyprv.PROXY_SERVER.getAllServers().stream().iterator().next();
        if (server == null) {
            throw new IllegalArgumentException("No registered server found.");
        }

        String host = server.getServerInfo().getAddress().getHostString();
        int port = server.getServerInfo().getAddress().getPort();

        try {
            SERVER_ADDRESS_FIELD.set(this, host);
            PORT_FIELD.set(this, port);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to initialize handshake hook", e);
        }
    }

    @Override
    public Supplier<MinecraftPacket> getHook() {
        return HandshakeHook::new;
    }

    @Override
    public Class<? extends MinecraftPacket> getType() {
        return HandshakePacket.class;
    }

    @Override
    public Class<? extends MinecraftPacket> getHookClass() {
        return this.getClass();
    }
}
