package me.evvv.ranPlugin.commands;

import me.evvv.ranPlugin.RanPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NickCommand implements CommandExecutor {

    private final RanPlugin plugin;

    public NickCommand(RanPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("nick") && sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length > 0) {
                String nickname = String.join(" ", args);
                nickname = translateRGBColorCodes(nickname);
                nickname = ChatColor.translateAlternateColorCodes('&', nickname);
                player.setDisplayName(nickname);
                player.setPlayerListName(nickname);
                player.sendMessage("Your nickname has been changed to " + nickname);

                // Save the nickname to the config file
                plugin.getConfig().set("nicknames." + player.getUniqueId(), nickname);
                plugin.saveConfig();

                return true;
            } else {
                player.sendMessage("Usage: /nick <nickname>");
                return false;
            }
        }
        return false;
    }

    private String translateRGBColorCodes(String text) {
        Pattern pattern = Pattern.compile("&#([A-Fa-f0-9]{6})");
        Matcher matcher = pattern.matcher(text);
        StringBuilder buffer = new StringBuilder();

        while (matcher.find()) {
            String hex = matcher.group(1);
            String replacement = "§x§" + hex.charAt(0) + "§" + hex.charAt(1) + "§" + hex.charAt(2) + "§" + hex.charAt(3) + "§" + hex.charAt(4) + "§" + hex.charAt(5);
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

}
