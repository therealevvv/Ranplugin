package me.evvv.ranPlugin;

import me.evvv.ranPlugin.commands.GenerateHouseCommand;
import me.evvv.ranPlugin.commands.LoreCommand;
import me.evvv.ranPlugin.commands.NickCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class RanPlugin extends JavaPlugin {

    private EventHoster eventHoster;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Plugin enabled");
        saveDefaultConfig();
        getServer().getMessenger().registerOutgoingPluginChannel(this, "velocity:main");
        getServer().getMessenger().registerOutgoingPluginChannel(this, "evvv:minigamechannel");

        // Initialize and register EventHoster
        eventHoster = new EventHoster(this);
        getServer().getPluginManager().registerEvents(eventHoster, this);
        getCommand("nick").setExecutor(new NickCommand(this));
        getCommand("house").setExecutor(new GenerateHouseCommand());
        getCommand("sign").setExecutor(new LoreCommand());

        // Register the /hostevent command
        getCommand("hostevent").setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                eventHoster.openEventGUI(player);
                return true;
            }
            sender.sendMessage("This command can only be used by a player.");
            return false;
        });

        // Register the /joinevent command
        getCommand("joinevent").setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                eventHoster.addPlayerToEvent(player);
                return true;
            }
            sender.sendMessage("This command can only be used by a player.");
            return false;
        });

        getCommand("sendplayer").setExecutor((sender, command, label, args) -> {
            if (args.length != 2) {
                sender.sendMessage("Usage: /sendplayer <playername> <servername>");
                return false;
            }

            String playerName = args[0];
            String serverName = args[1];

            sendPlayerToServer(playerName, serverName);
            return true;
        });
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Plugin disabled");
    }

    private void sendPlayerToServer(String playerName, String serverName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            getLogger().info("Player not found: " + playerName);
            return;
        }

        // Prepare the plugin message to send to Velocity
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF("sendplayer");
            out.writeUTF(playerName);
            out.writeUTF(serverName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Send the plugin message
        player.sendPluginMessage(this, "velocity:main", b.toByteArray());
    }
}