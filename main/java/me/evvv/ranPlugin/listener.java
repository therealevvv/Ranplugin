package me.evvv.ranPlugin;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Objects;

@SuppressWarnings("ALL")
public class listener implements Listener {

    private final RanPlugin plugin;

    public listener(RanPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Gets the player
        Player player = event.getPlayer();
        // Sends Message
        // Remove join message
        event.setJoinMessage(null);

        FileConfiguration config = this.plugin.getConfig();
        String nickname = config.getString("nicknames." + player.getUniqueId());
        if (nickname != null) {
            player.setDisplayName(nickname);
            player.setPlayerListName(nickname);
        }
        player.sendMessage("§l§9Welcome to the Server: " + nickname);
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        String message = event.getMessage();
        FileConfiguration config = this.plugin.getConfig();
        String nickname = config.getString("nicknames." + event.getPlayer().getUniqueId());
        event.setFormat(Objects.requireNonNullElseGet(nickname, () -> event.getPlayer().getDisplayName()) + ChatColor.RESET + ": " + ChatColor.stripColor(message));
    }
}
