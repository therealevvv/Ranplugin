package me.evvv.ranPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.ChatColor;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.util.Vector;

public class GenerateHouseCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (args.length == 0 || (!args[0].equalsIgnoreCase("roof") && !args[0].equalsIgnoreCase("noroof"))) {
                sender.sendMessage(ChatColor.RED + "Usage: /generatehouse <roof|noroof>");
                return false;
            }

            Player player = (Player) sender;
            Location loc = player.getLocation();

            int width = 5;  // args[1]
            int height = 4; // args[2]
            int depth = 5;  // args[3]

            int startX = loc.getBlockX() - (width / 2);
            int startY = loc.getBlockY() - 1;
            int startZ = loc.getBlockZ() - (depth / 2);

            Block startBlock = loc.getWorld().getBlockAt(startX, startY, startZ);

            // Floor creation
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    startBlock.getRelative(x, 0, z).setType(Material.OAK_PLANKS);
                }
            }

            // Wall creation
            for (int y = 1; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    for (int z = 0; z < depth; z++) {
                        Block currentBlock = startBlock.getRelative(x, y, z);
                        if ((x == 0 || x == width - 1) && (z == 0 || z == depth - 1)) {
                            currentBlock.setType(Material.OAK_LOG);
                        } else if (x == 0 || x == width - 1 || z == 0 || z == depth - 1) {
                            currentBlock.setType(Material.OAK_PLANKS);
                        }
                    }
                }
            }

            // Roof creation
            if (args[0].equalsIgnoreCase("roof")) {
                for (int x = 0; x < width; x++) {
                    for (int z = 0; z < depth; z++) {
                        startBlock.getRelative(x, height, z).setType(Material.OAK_PLANKS);
                    }
                }

                // Outer stairs for the roof
                for (int x = -1; x <= width; x++) {
                    for (int z = -1; z <= depth; z++) {
                        if (x == -1 || x == width || z == -1 || z == depth) {
                            Block stairBlock = startBlock.getRelative(x, height, z);
                            stairBlock.setType(Material.OAK_STAIRS);
                            Stairs stairs = (Stairs) stairBlock.getBlockData();

                            if (x == -1) {
                                stairs.setFacing(BlockFace.EAST);
                                stairs.setShape(z == -1 ? Stairs.Shape.OUTER_LEFT : (z == depth ? Stairs.Shape.OUTER_RIGHT : Stairs.Shape.STRAIGHT));
                            } else if (x == width) {
                                stairs.setFacing(BlockFace.WEST);
                                stairs.setShape(z == -1 ? Stairs.Shape.OUTER_LEFT : (z == depth ? Stairs.Shape.OUTER_RIGHT : Stairs.Shape.STRAIGHT));
                            } else if (z == -1) {
                                stairs.setFacing(BlockFace.SOUTH);
                            } else if (z == depth) {
                                stairs.setFacing(BlockFace.NORTH);
                            }

                            stairBlock.setBlockData(stairs);
                        }
                    }
                }

                // Inner stairs for the roof
                for (int x = 0; x < width; x++) {
                    for (int z = 0; z < depth; z++) {
                        Block stairBlock = startBlock.getRelative(x, height + 1, z);
                        if (x == 0 || x == width - 1 || z == 0 || z == depth - 1) {
                            stairBlock.setType(Material.OAK_STAIRS);
                            Stairs stairs = (Stairs) stairBlock.getBlockData();

                            if (x == 0) {
                                stairs.setFacing(BlockFace.EAST);
                                stairs.setShape(z == 0 ? Stairs.Shape.OUTER_LEFT : (z == depth - 1 ? Stairs.Shape.OUTER_RIGHT : Stairs.Shape.STRAIGHT));
                            } else if (x == width - 1) {
                                stairs.setFacing(BlockFace.WEST);
                                stairs.setShape(z == 0 ? Stairs.Shape.OUTER_LEFT : (z == depth - 1 ? Stairs.Shape.OUTER_RIGHT : Stairs.Shape.STRAIGHT));
                            } else if (z == 0) {
                                stairs.setFacing(BlockFace.SOUTH);
                            } else if (z == depth - 1) {
                                stairs.setFacing(BlockFace.NORTH);
                            }

                            stairBlock.setBlockData(stairs);
                        } else {
                            stairBlock.setType(Material.OAK_PLANKS);
                        }
                    }
                }
            }

            // Door creation
            BlockFace direction = player.getFacing();
            int doorX = width / 2;
            int doorZ = 0;

            switch (direction) {
                case NORTH: doorX = width / 2; doorZ = 0; break;
                case SOUTH: doorX = width / 2; doorZ = depth - 1; break;
                case WEST: doorX = 0; doorZ = depth / 2; break;
                case EAST: doorX = width - 1; doorZ = depth / 2; break;
            }
            BlockFace oppositeDirection = direction.getOppositeFace();

            Block doorBottomBlock = startBlock.getRelative(doorX, 1, doorZ);

            // Create the bottom half of the door
            Door doorBottom = (Door) Bukkit.createBlockData(Material.OAK_DOOR);
            doorBottom.setFacing(oppositeDirection);
            doorBottom.setHalf(Bisected.Half.BOTTOM);
            doorBottomBlock.setBlockData(doorBottom);

            // Create the top half of the door
            Block doorTopBlock = doorBottomBlock.getRelative(0, 1, 0);
            Door doorTop = (Door) Bukkit.createBlockData(Material.OAK_DOOR);
            doorTop.setFacing(oppositeDirection);
            doorTop.setHalf(Bisected.Half.TOP);
            doorTopBlock.setBlockData(doorTop);

            player.sendMessage(ChatColor.GREEN + "A small house with " + args[0] + " has been generated around you!");
            return true;
        }

        sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
        return false;
    }
}