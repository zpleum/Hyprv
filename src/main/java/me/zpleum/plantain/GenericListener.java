package me.zpleum.hyprv;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;

public class GenericListener {

    @Subscribe
    public void onPlayerCommandEvent(CommandExecuteEvent event) {
        if (!(event.getCommandSource() instanceof Player player)) {
            return;
        }

        event.setResult(CommandExecuteEvent.CommandResult.forwardToServer());

        Hyprv.LOGGER.info(
                "Player {} executed command '{}'",
                player.getUsername(),
                event.getCommand()
        );
    }

    @Subscribe
    public void onLogin(PostLoginEvent event) {
        Player player = event.getPlayer();
        String ip = player.getRemoteAddress().getAddress().getHostAddress();
        int port = player.getRemoteAddress().getPort();
        String msg = String.format("Player `%s` joined | IP: `%s:%d`",
            player.getUsername(), ip, port);
        Hyprv.LOGGER.info(msg);
    }

    @Subscribe
    public void onPlayerChatEvent(PlayerChatEvent event) {
        Hyprv.LOGGER.info(
                "Player {} sent chat message: {}",
                event.getPlayer().getUsername(),
                event.getMessage()
        );
    }
}