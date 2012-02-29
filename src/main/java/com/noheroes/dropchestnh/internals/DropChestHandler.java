/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.internals;

import com.noheroes.dropchestnh.DropChestNH;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

/**
 *
 * @author PIETER
 */
public class DropChestHandler {
    // Stores all dropchests using a unique ID as key
    private static final HashMap<Integer, DropChestObj> dcHashMap = new HashMap<Integer, DropChestObj>();
    // Maps chest names to chest ID for easy lookup when chest is referenced by name
    private static final HashMap<String, Integer> dcMapChestNameToID = new HashMap<String, Integer>();
    // Maps chest location to chest ID for easy lookup when a chest is referenced by location
    private static final HashMap<Location, Integer> dcMapLocationToID = new HashMap<Location, Integer>();
    // Maps player name to a list of chest ID for easy lookup when finding all chests owned by a player, mostly for sake of admin information lookup
    private static final HashMap<String, LinkedHashSet<Integer>> dcMapPlayerNameToID = new HashMap<String, LinkedHashSet<Integer>>();
    private static int currentChestID = 1;
    private static Set<BlockFace> cardinalFaces = new LinkedHashSet<BlockFace>();
    private static DropChestNH dc;
    
    public DropChestHandler(DropChestNH dc) {
        this.dc = dc;
        cardinalFaces.add(BlockFace.NORTH);
        cardinalFaces.add(BlockFace.EAST);
        cardinalFaces.add(BlockFace.SOUTH);
        cardinalFaces.add(BlockFace.WEST);
    }
    
    public boolean addChest(Block block, Player player) {
        return addChest(block, player, null);
    }
    
    public boolean addChest(Block block, Player player, String chestName) {
        if (!block.getType().equals(Material.CHEST)) {
            return false;
        }
        if (dcMapLocationToID.containsKey(block.getLocation())) {
            return false;   // Chest is already a dropchest
        }
        
        Block adjacentChest = findAdjacentChest(block);
        Location primaryLocation;
        Location secondaryLocation;
        DropChestObj dropChest;
        
        // Find the first free chest ID, this only resets on server reloads to back fill deleted chest IDs        
        while(dcHashMap.containsKey(currentChestID)) {
            currentChestID++;
        }
        // Single chest
        if (adjacentChest == null) {
            primaryLocation = block.getLocation();
            secondaryLocation = null;
        }
        // Double chest
        else {
            // The North-most or East-most chest holds the top 3 rows of the inventory and is the primary chest
            if ((block.getRelative(BlockFace.NORTH).equals(adjacentChest)) || block.getRelative(BlockFace.EAST).equals(adjacentChest)) {
                primaryLocation = adjacentChest.getLocation();
                secondaryLocation = block.getLocation();
            }
            else {
                primaryLocation = block.getLocation();
                secondaryLocation = adjacentChest.getLocation();
            }
        }
        dropChest = new DropChestObj(player.getName(), chestName, primaryLocation, secondaryLocation);
        // Add chest to hashmap
        dcHashMap.put(currentChestID, dropChest);
        // Add chest name mapping if applicable
        if (chestName != null) {
            dcMapChestNameToID.put(chestName, currentChestID);
        }
        // Add primary location mapping
        dcMapLocationToID.put(primaryLocation, currentChestID);
        // Add secondary location mapping (double chest) if applicable
        if (secondaryLocation != null) {
            dcMapLocationToID.put(secondaryLocation, currentChestID);
        }
        // Add chest ID to list of chests owned by player
        if (dcMapPlayerNameToID.containsKey(player.getName())) {
            dcMapPlayerNameToID.get(player.getName()).add(currentChestID);
        }
        else {
            LinkedHashSet<Integer> playerList = new LinkedHashSet<Integer>();
            playerList.add(currentChestID);
            dcMapPlayerNameToID.put(player.getName(), playerList);
        }
        return true;
    }
    
    // Owner or permission handling for removal should be done before calling this method
    public boolean removeChest(String identifier) {
        return removeChest(getChestID(identifier));
    }
    
    public boolean removeChest(Location location) {
        return removeChest(dcMapLocationToID.get(location));
    }
    
    public boolean removeChest (Integer chestID) {
        if ((chestID == null) || !dcHashMap.containsKey(chestID)) {
            return false;  // Chest doesn't exist
        }
        // Remove name mapping if present
        if (dcHashMap.get(chestID).getName() != null) {
            dcMapChestNameToID.remove(dcHashMap.get(chestID).getName());
        }
        // Remove location mapping
        dcMapLocationToID.remove(dcHashMap.get(chestID).getPrimaryLocation());
        if (dcHashMap.get(chestID).getSecondaryLocation() != null) {
            dcMapLocationToID.remove(dcHashMap.get(chestID).getSecondaryLocation());
        }
        // Remove chest from player's list of owned chests
        dcMapPlayerNameToID.get(dcHashMap.get(chestID).getOwner()).remove(chestID);
        // Remove the chest from the hashmap
        dcHashMap.remove(chestID);
        return true;        
    }
    
    public boolean ownsChest(Location location, Player player) {
        Integer chestID = dcMapLocationToID.get(location);
        if ((chestID == null) || !dcHashMap.containsKey(chestID)) {
            return false;
        }
        return (dcHashMap.get(chestID).getOwner().equals(player.getName()));
    }
    
    public Integer getChestID(String identifier) {
        Integer chestID;
        try {
            chestID = Integer.parseInt(identifier);
        }
        catch (NumberFormatException ex) {
            chestID = dcMapChestNameToID.get(identifier);
        }
        
        return chestID;
    }
    
    public Block findAdjacentChest(Block block) {
        // Check the four cardinal directions for another chest to detect double chests
        for (BlockFace bf : cardinalFaces) {
            if (block.getRelative(bf).getType().equals(Material.CHEST)) {
                return block.getRelative(bf);
            }
        }
        return null;
    }
}
