/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.internals;

import com.noheroes.dropchestnh.DropChestNH;
import com.noheroes.dropchestnh.exceptions.MissingOrIncorrectParametersException;
import com.noheroes.dropchestnh.internals.Utils.Filter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
    // Maps player name to a list of chest ID for easy lookup when finding all chests owned by a player, mostly for sake of lists
    private static final HashMap<String, LinkedList<Integer>> dcMapPlayerNameToID = new HashMap<String, LinkedList<Integer>>();
    private static int currentChestID = 1;
    private static Set<BlockFace> cardinalFaces = new LinkedHashSet<BlockFace>();
    static DropChestNH dc;
    
    public DropChestHandler(DropChestNH dc) {
        this.dc = dc;
        cardinalFaces.add(BlockFace.NORTH);
        cardinalFaces.add(BlockFace.EAST);
        cardinalFaces.add(BlockFace.SOUTH);
        cardinalFaces.add(BlockFace.WEST);
    }
    
    public boolean addChest(Block chest, Player player) {
        return addChest(chest, player, null);
    }
    
    public boolean addChest(Block chest, Player player, String chestName) {
        if (!chest.getType().equals(Material.CHEST)) {
            return false;
        }
        if (dcMapLocationToID.containsKey(chest.getLocation())) {
            return false;   // Chest is already a dropchest
        }
        
        Block adjacentChest = findAdjacentChest(chest);
        Location primaryLocation;
        Location secondaryLocation;
        DropChestObj dropChest;
        
        // Find the first free chest ID, this only resets on server reloads to back fill deleted chest IDs        
        while(dcHashMap.containsKey(currentChestID)) {
            currentChestID++;
        }
        // Single chest
        if (adjacentChest == null) {
            primaryLocation = chest.getLocation();
            secondaryLocation = null;
        }
        // Double chest
        else {
            // Find out which is primary and which is secondary chest so that inventory always fills from top down
            if (checkPrimaryChest(chest, adjacentChest)) {
                primaryLocation = chest.getLocation();
                secondaryLocation = adjacentChest.getLocation();
            }
            else {
                primaryLocation = adjacentChest.getLocation();
                secondaryLocation = chest.getLocation();
            }
        }
        dropChest = new DropChestObj(currentChestID, player.getName(), chestName, primaryLocation, secondaryLocation);
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
        if (dcMapPlayerNameToID.containsKey(player.getName().toLowerCase())) {
            dcMapPlayerNameToID.get(player.getName().toLowerCase()).offer(currentChestID);
            // Sort the list in acending order
            Collections.sort(dcMapPlayerNameToID.get(player.getName()));
        }
        else {
            LinkedList<Integer> playerList = new LinkedList<Integer>();
            playerList.offer(currentChestID);
            dcMapPlayerNameToID.put(player.getName().toLowerCase(), playerList);
        }
        return true;
    }
    
    // Owner or permission handling for removal should be done before calling these methods
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
        dcMapPlayerNameToID.get(dcHashMap.get(chestID).getOwner().toLowerCase()).remove(chestID);
        // Remove the chest from the hashmap
        dcHashMap.remove(chestID);
        return true;        
    }
    
    // Check blocks next to the parameter for dropchests and update the dropchest to a double dropchest if one is found
    public boolean doubleChestCheck(Block chest) {
        if (!chest.getType().equals(Material.CHEST)) {
            return false;
        }
        Block adjacentChest;
        adjacentChest = findAdjacentChest(chest);
        if (adjacentChest == null) {
            return false;
        }
        Integer chestID;
        chestID = dcMapLocationToID.get(adjacentChest.getLocation());
        
        // The second half of the double chest is not a dropchest
        if (chestID == null) {
            return false;
        }
        
        dcMapLocationToID.put(chest.getLocation(), chestID);
        //DropChestObj dropChest = dcHashMap.get(chestID);
        // Update the locations for dropchest object
        if (checkPrimaryChest(chest, adjacentChest)) {
            dcHashMap.get(chestID).setPrimaryLocation(chest.getLocation());
            dcHashMap.get(chestID).setSecondaryLocation(adjacentChest.getLocation());
            dc.log("Primary changing");
        }
        else {
            // The primary chest remains the same and is already stored, no need to add it again
            dcHashMap.get(chestID).setSecondaryLocation(chest.getLocation());
            dc.log("Primary remains");
        }
        return true;
    }
    
    // Adds item to chest with ID chestID.  Returns anything that didn't fit, or null if everything fit
    public ItemStack addItem(Integer chestID, ItemStack item) {
        if ((chestID == null) || (item == null)) {
            return null;
        }
        
        // TODO: item is passed as reference and being changed somehwere, look into
        ItemStack inputItem = item.clone();
        DropChestObj dropChest;
        dropChest = dcHashMap.get(chestID);
        HashMap<Integer, ItemStack> leftOverItems;
        // Check if the bottom half of the inventory already contains some of the item
        if (dropChest.getSecondaryInventory() == null) {
            leftOverItems = dropChest.getPrimaryInventory().addItem(inputItem);
        }
        else {
            if (dropChest.getSecondaryInventory().contains(inputItem.getType())) {
                leftOverItems = dropChest.getSecondaryInventory().addItem(inputItem);
                // Check if everything fit, put the rest in top if not
                if ((leftOverItems != null) && !leftOverItems.isEmpty()) {
                    // Because this method only accepts one ItemStack at a time we know leftover items are at index 0
                    leftOverItems = dropChest.getPrimaryInventory().addItem(leftOverItems.get(0));      
                }
            }
            else {
                leftOverItems = dropChest.getPrimaryInventory().addItem(inputItem);
                if ((leftOverItems != null) && !leftOverItems.isEmpty()) {
                    leftOverItems = dropChest.getSecondaryInventory().addItem(leftOverItems.get(0));
                }
            }
        }
        return ((leftOverItems == null) ? null : leftOverItems.get(0));
    }
    
    public ItemStack addItem(Location location, ItemStack item) {
        return addItem(dcMapLocationToID.get(location), item);
    }
    
    public boolean updateFilter(Material mat, Location location, Filter filter) throws MissingOrIncorrectParametersException {
        if (location == null) {
            throw new MissingOrIncorrectParametersException("That is not a dropchest");
        }
        Integer chestID = dcMapLocationToID.get(location);
        if (chestID == null) {
            throw new MissingOrIncorrectParametersException("That is not a dropchest");
        }
        return dcHashMap.get(chestID).updateFilter(mat.getId(), filter);
    }
    
    public boolean updateFilter(String mat, Integer chestID, Filter filter) throws MissingOrIncorrectParametersException {

        if ((chestID == null ) || (!dcHashMap.containsKey(chestID))) {
            throw new MissingOrIncorrectParametersException("That chest does not exist or is not a dropchest.");
        }
        
        Material material;
        material = getMaterialFromString(mat);
        if (material == null) {
            throw new MissingOrIncorrectParametersException("Material " + mat + " does not exist");
        }
        else {
            return dcHashMap.get(chestID).updateFilter(material.getId(), filter);
        }
    }
    
    public boolean updateFilter(String mat, Location location, Filter filter) throws MissingOrIncorrectParametersException {
        Integer chestID = dcMapLocationToID.get(location);
        return updateFilter(mat, chestID, filter);
    }
    
    // Attempts to find the chestID by matching indentifier against or chestID or chest name
    public boolean updateFilter(String mat, String identifier, Filter filter) throws MissingOrIncorrectParametersException {
        return updateFilter(mat, getChestID(identifier), filter);
    }
    
    
    
    public Material getMaterialFromString(String mat) {
        Material material;
        // Check if material is referenced by enum
        material = Material.getMaterial(mat.toUpperCase());
        // Material was matched
        if (material != null) {
            return material;
        }
        // Material was not matched, check if material is referenced by ID
        else {
            Integer materialID;
            try {
                materialID = Integer.valueOf(mat);
            }
            catch (NumberFormatException ex) {
                // mat is not an integer or valid material
                return null;
            }
            // Check if material ID exists
            material = Material.getMaterial(materialID);
            if (material == null) {
                return null;
            }
            return material;
        }
    }
    
    public void clearFilter(Integer chestID, Filter filter) throws MissingOrIncorrectParametersException {
        if (chestID == null) {
            throw new MissingOrIncorrectParametersException("That chest does not exist or is not a dropchest.");
        }
        dcHashMap.get(chestID).clearFilter(filter);
    }
    
    public void clearFilter(Location location, Filter filter) throws MissingOrIncorrectParametersException {
        clearFilter(dcMapLocationToID.get(location), filter);
    }
    
    public void clearFilter(String identifier, Filter filter) throws MissingOrIncorrectParametersException {
        clearFilter(getChestID(identifier), filter);
    }
    
    public void addAllFilter(Integer chestID, Filter filter) throws MissingOrIncorrectParametersException {
        if (chestID == null) {
            throw new MissingOrIncorrectParametersException("That chest does not exist or is not a dropchest.");
        }
        dcHashMap.get(chestID).addAllFilter(filter);
    }
    
    public void addAllFilter(Location location, Filter filter) throws MissingOrIncorrectParametersException {
        addAllFilter(dcMapLocationToID.get(location), filter);
    }
    
    public void addAllFilter(String identifier, Filter filter) throws MissingOrIncorrectParametersException {
        addAllFilter(getChestID(identifier), filter);
    }
        
    public boolean ownsChest(Location location, Player player) {
        Integer chestID = dcMapLocationToID.get(location);
        if ((chestID == null) || !dcHashMap.containsKey(chestID)) {
            return false;
        }
        return (dcHashMap.get(chestID).getOwner().equals(player.getName()));
    }
    
    public boolean ownsChest(String identifier, Player player) {
        Integer chestID = getChestID(identifier);
        if ((chestID == null) || !(dcHashMap.containsKey(chestID))) {
            return false;
        }
        return dcHashMap.get(chestID).getOwner().equals(player.getName());
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
    
    public boolean chestExists(String identifier) {
        Integer chestID = getChestID(identifier);
        if (chestID == null) {
            return false;
        }
        return dcHashMap.containsKey(chestID);
    }
    
    public LinkedList<Integer> getPlayerChestList(String playerName) {
        return dcMapPlayerNameToID.get(playerName.toLowerCase());
    }
    
    // Checks which chest is primary.  Returns true if the order is correct (first parameter primary, second secondary) or false it should be reversed
    // The primary chest holds the top 3 rows of the inventory and is always the north or east most chest.
    private boolean checkPrimaryChest(Block primaryChest, Block secondaryChest) {
        return (secondaryChest.getRelative(BlockFace.NORTH).equals(primaryChest)) || 
                secondaryChest.getRelative(BlockFace.EAST).equals(primaryChest);
    }
    
    private Block findAdjacentChest(Block block) {
        // Check the four cardinal directions for another chest to detect double chests
        for (BlockFace bf : cardinalFaces) {
            if (block.getRelative(bf).getType().equals(Material.CHEST)) {
                return block.getRelative(bf);
            }
        }
        return null;
    }
}
