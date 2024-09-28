package me.evvv.ranPlugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class LoreCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by a player.");
            return false;
        }

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "You must be holding an item to add lore.");
            return false;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "You must provide the lore text. Usage: /lore <lore text>");
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new java.util.ArrayList<>();

        StringBuilder loreLine = new StringBuilder();
        for (String arg : args) {
            loreLine.append(arg).append(" ");
        }

        lore.add(ChatColor.translateAlternateColorCodes('&', loreLine.toString().trim()));
        meta.setLore(lore);
        item.setItemMeta(meta);

        player.sendMessage(ChatColor.GREEN + "Lore added to your item!");

        return true;
    }
}