package me.zpleum.hyprv.hook;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.packet.ServerLoginPacket;
import io.netty.buffer.ByteBuf;
import me.zpleum.hyprv.Hyprv;

import java.util.function.Supplier;

public final class LoginHook extends ServerLoginPacket implements PacketHook {

    @Override
    public void decode(ByteBuf buf, ProtocolUtils.Direction dir, ProtocolVersion ver) {
        super.decode(buf, dir, ver);
        Hyprv.LOGGER.info("[LOGIN PACKET] Username: {}",
            this.getUsername());
    }

    @Override
    public Supplier<MinecraftPacket> getHook() {
        return LoginHook::new;
    }

    @Override
    public Class<? extends MinecraftPacket> getType() {
        return ServerLoginPacket.class;
    }

    @Override
    public Class<? extends MinecraftPacket> getHookClass() {
        return this.getClass();
    }
}