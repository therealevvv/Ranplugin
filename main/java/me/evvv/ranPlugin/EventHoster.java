package me.evvv.ranPlugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EventHoster implements Listener {

    private final JavaPlugin plugin;
    private final List<Player> eventPlayers = new ArrayList<>();
    private String selectedMinigame;

    public EventHoster(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void openEventGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, "Select an Event");

        ItemStack minigame1 = new ItemStack(Material.QUARTZ_PILLAR);
        ItemMeta minigame1Data = minigame1.getItemMeta();
        minigame1Data.setDisplayName("Pillars of Fortune");
        minigame1Data.setItemName("Pillars of Fortune");
        minigame1.setItemMeta(minigame1Data);
        ItemStack minigame2 = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta minigame2Data = minigame2.getItemMeta();
        minigame2Data.setDisplayName("Block Hunt");
        minigame2Data.setItemName("Block Hunt");
        minigame2.setItemMeta(minigame2Data);
        gui.setItem(3, minigame1);
        gui.setItem(5, minigame2);

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (event.getView().getTitle().equals("Select an Event")) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

            switch (event.getCurrentItem().getType()) {
                case QUARTZ_PILLAR:
                    selectedMinigame = "Pillar";
                    break;
                case GRASS_BLOCK:
                    selectedMinigame = "BHunt";
                    break;
            }

            if (selectedMinigame != null) {
                startEventTimer("minigame1");
                eventPlayers.add(player);
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals("Select an Event")) {
            Player player = (Player) event.getPlayer();
            player.sendMessage("You closed the event selection menu.");
        }
    }

    private void startEventTimer(String serverName) {
        new BukkitRunnable() {
            int time = 8;

            @Override
            public void run() {
                if (time <= 0) {
                    for (Player player : eventPlayers) {
                        // Send the player to the minigame server using Velocity
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "sendplayer " + player.getName() + " " + serverName);

                        // Schedule the message to be sent 1 second (20 ticks) later
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                // Send the selected minigame to the minigame server via Velocity's BungeeCord channel
                                sendMinigameToServer(serverName);
                            }
                        }.runTaskLater(plugin, 40L); // 40L = 2 second delay
                    }
                    eventPlayers.clear();
                    cancel();
                } else {
                    Bukkit.broadcastMessage("Event starting in " + time + " seconds! Use /joinevent to join.");
                    time--;
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void sendMinigameToServer(String serverName) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            // Send your custom subchannel "MinigameChannel"
            out.writeUTF("MinigameChannel");

            ByteArrayOutputStream msgBytes = new ByteArrayOutputStream();
            DataOutputStream msgOut = new DataOutputStream(msgBytes);
            msgOut.writeUTF(selectedMinigame); // Send the minigame name

            out.writeShort(msgBytes.toByteArray().length);
            out.write(msgBytes.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Send the plugin message via Velocity's BungeeCord channel
        plugin.getServer().sendPluginMessage(plugin, "evvv:minigamechannel", b.toByteArray());
        plugin.getLogger().info("Sent minigame selection message to server: " + serverName);
    }


    public void addPlayerToEvent(Player player) {
        if (Objects.equals(selectedMinigame, "Pillar") && eventPlayers.size() == 8) {
            player.sendMessage("The event is full :(");
            return;
        }
        if (!eventPlayers.contains(player)) {
            eventPlayers.add(player);
            player.sendMessage("You have joined the event!");
        } else {
            player.sendMessage("You are already in the event!");
        }
    }
}