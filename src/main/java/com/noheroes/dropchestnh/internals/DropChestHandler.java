/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.internals;

import com.noheroes.dropchestnh.DropChestNH;
import com.noheroes.dropchestnh.exceptions.MissingOrIncorrectParametersException;
import com.noheroes.dropchestnh.internals.Utils.Filter;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author PIETER
 */

    // TODO: Fix updateFilter (2 master methods)

public class DropChestHandler {
    // Stores all dropchests using a unique ID as key
    private static final HashMap<Integer, DropChestObj> dcHashMap = new HashMap<Integer, DropChestObj>();
    // Maps chest names to chest ID for easy lookup when chest is referenced by name
    private static final HashMap<String, Integer> dcMapChestNameToID = new HashMap<String, Integer>();
    // Maps chest location to chest ID for easy lookup when a chest is referenced by location
    private static final HashMap<Location, Integer> dcMapLocationToID = new HashMap<Location, Integer>();
    // Maps player name to a list of chest ID for easy lookup when finding all chests owned by a player, mostly for sake of lists
    private static final HashMap<String, LinkedList<Integer>> dcMapPlayerNameToID = new HashMap<String, LinkedList<Integer>>();
    // This hashmap maps all suck locations to the corresponding chest ID's
    private static final HashMap<Location, LinkedHashSet<Integer>> dcMapSuckLocationToID = new HashMap<Location, LinkedHashSet<Integer>>();
    // This hashmap maps push/pull locations to chest ID's
    private static final HashMap<Location, LinkedHashSet<Integer>> dcMapPullLocationToID = new HashMap<Location, LinkedHashSet<Integer>>();
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
    
    public boolean addChest(Block chest, Player player) throws MissingOrIncorrectParametersException {
        return addChest(chest, player, null);
    }
    
    public boolean addChest(Block chest, Player player, String chestName) throws MissingOrIncorrectParametersException {
        if (!chest.getType().equals(Material.CHEST)) {
            return false;
        }
        if (dcMapLocationToID.containsKey(chest.getLocation())) {
            throw new MissingOrIncorrectParametersException("That is already a dropchest");
        }
        if ((chestName != null) && dcMapChestNameToID.containsKey(chestName)) {
            throw new MissingOrIncorrectParametersException("A chest with that name already exists");
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
        if (dcMapLocationToID.containsKey(location)) {
            return removeChest(dcMapLocationToID.get(location));
        }
        else {
            return false;
        }
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
        // Remove all locations associated with this chest's suck area
        FilterBox fb = new FilterBox(getChest(chestID).getSuckDistance(), getChest(chestID).getSuckHeight(), getChest(chestID).getOriginalLocation());
        while (fb.hasNext()) {
            removeLocFromMap(chestID, fb.next(), Filter.SUCK);
        }
        // Remove all locations associated with this chest's push/pull area
        removeChestFromPullMap(chestID);
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
        // Update the locations for dropchest object
        if (checkPrimaryChest(chest, adjacentChest)) {
            dcHashMap.get(chestID).setPrimaryLocation(chest.getLocation());
            dcHashMap.get(chestID).setSecondaryLocation(adjacentChest.getLocation());
        }
        else {
            // The primary chest remains the same and is already stored, no need to add it again
            dcHashMap.get(chestID).setSecondaryLocation(chest.getLocation());
        }
        // If the chest uses a push or pull filter update the push/pull map
        if (isFilterInUse(chestID, Filter.PULL) || isFilterInUse(chestID, Filter.PUSH)) {
            addChestToPullMap(chestID);
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
            // If push or pull filter is used add to push/pull minecart location map
            if ((filter == Filter.PULL) || (filter == Filter.PUSH)) {
                addChestToPullMap(chestID);
            }
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
    
    public Integer getChestID(Location location) {
        return dcMapLocationToID.get(location);
    }
    
    public boolean chestExists(String identifier) {
        Integer chestID = getChestID(identifier);
        if (chestID == null) {
            return false;
        }
        return dcHashMap.containsKey(chestID);
    }
    
    public boolean chestExists(Location location) {
        return dcMapLocationToID.containsKey(location);
    }
    
    public LinkedList<Integer> getPlayerChestList(String playerName) {
        return dcMapPlayerNameToID.get(playerName.toLowerCase());
    }
    
    public String getChestName(Integer chestID) {
        return dcHashMap.get(chestID).getName();
    }
    
    public Location getChestLocation(Integer chestID) {
        return dcHashMap.get(chestID).getPrimaryLocation();
    }
    
    public boolean isFilterInUse(Integer chestID, Filter filter) {
        return dcHashMap.get(chestID).isFilterInUse(filter);
    }
    
    public InventoryData getInventoryData(Integer chestID) {
        return dcHashMap.get(chestID).getInventoryData();
    }
    
    public HashMap<String, Integer> getAllChestList() {
        HashMap<String, Integer> playerList = new HashMap<String, Integer>();
        for (Entry<String, LinkedList<Integer>> listEntry : dcMapPlayerNameToID.entrySet()) {
            playerList.put(listEntry.getKey(), listEntry.getValue().size());
        }
        return playerList;
    }
    
    public Set<Integer> getFilter(Integer chestID, Filter filter) {
        if ((chestID == null) || !dcHashMap.containsKey(chestID)) {
            return null;
        }
        return dcHashMap.get(chestID).getFilter(filter);
    }
    
    public void setSuckDistance(String identifier, int newDistance) {
        Integer chestID = getChestID(identifier);
        setSuckDistance(chestID, newDistance);
    }
    
    public void setSuckDistance(Integer chestID, int newDistance) {
        if (chestID == null) {
            return;
        }
        int oldDistance = getChest(chestID).getSuckDistance();
        int height = getChest(chestID).getSuckHeight(); 
        // Distance was not changed or is negative
        if ((newDistance == oldDistance) || (newDistance < 0)) {
            return;
        }
        // Chest suck distance is increasing
        if (newDistance > oldDistance) {
            // Create the box in which the chest filters
            FilterBox fb = new FilterBox(newDistance, height, getChest(chestID).getPrimaryLocation());
            Location loc;
            // Add all locations in the box to the hashmap
            while (fb.hasNext()) {
                loc = fb.next();
                // Any locs that were already adding to the map previously will be skipped by the method
                addLocToMap(chestID, loc, Filter.SUCK);
            }
        }
        // Chest suck distance is decreasing
        else {
            FilterBox fb = new FilterBox(oldDistance, height, getChest(chestID).getPrimaryLocation());
            Location loc;
            while (fb.hasNext()) {
                loc = fb.next();
                // Remove any locations that are not within range of the new filter box
                if (!checkDistance(newDistance, height, loc, getChest(chestID).getPrimaryLocation())) {
                    removeLocFromMap(chestID, loc, Filter.SUCK);
                }
            }
        }
        // Update chest
        getChest(chestID).setSuckDistance(newDistance);
    }
    
    public void setSuckHeight(String identifier, int newHeight) {
        Integer chestID = getChestID(identifier);
        setSuckHeight(chestID, newHeight);
    }
    
    public void setSuckHeight(Integer chestID, int newHeight) {
        if (chestID == null) {
            return;
        }
        int oldHeight = getChest(chestID).getSuckHeight();
        int distance = getChest(chestID).getSuckDistance();
        if ((newHeight == oldHeight) || (newHeight < 0)) {
            return;
        }
        // Chest suck height is increasing
        if (newHeight > oldHeight) {
            FilterBox fb = new FilterBox(distance, newHeight, getChest(chestID).getPrimaryLocation());
            Location loc;
            while (fb.hasNext()) {
                loc = fb.next();
                // Any locs that were already adding to the map previously will be skipped by the method
                addLocToMap(chestID, loc, Filter.SUCK);
            }
        }
        // Chest suck height is decreasing
        else {
            FilterBox fb = new FilterBox(distance, oldHeight, getChest(chestID).getPrimaryLocation());
            Location loc;
            while (fb.hasNext()) {
                loc = fb.next();
                if (!checkDistance(distance, newHeight, loc, getChest(chestID).getPrimaryLocation())) {
                    removeLocFromMap(chestID, loc, Filter.SUCK);
                }                
            }
        }
        getChest(chestID).setSuckHeight(newHeight);
    }
    
    public ItemStack pickupItem(ItemStack is, Location location) {
        // Round the location so we can compare it to locations in the hashmap
        Location newLoc = roundLoc(location);
        if (!dcMapSuckLocationToID.containsKey(newLoc)) {
            return is;
        }
        // Some bukkit methods change the item stack so we clone it first
        ItemStack iss = is.clone();
        LinkedHashSet<Integer> chestList = dcMapSuckLocationToID.get(newLoc);
        for (Integer chestID : chestList) {
            // Check each chest filter if it is sucking up the item
            if (chestFilterContains(chestID, iss.getType(), Filter.SUCK)) {
                // Add item to chest, if nothing is left we can return now without looping through the other chests
                iss = addItem(chestID, iss);
                if ((iss == null) || (iss.getAmount() == 0)) {
                    return iss;
                }
            }
        }
        return iss;
    }
    
    public void minecartMovement(StorageMinecart cart) {
        if (cart == null) {
            return;
        }
        Location cartLoc = roundLoc(cart.getLocation());
        if (dcMapPullLocationToID.containsKey(cartLoc)) {
            LinkedHashSet<Integer> chestList = dcMapPullLocationToID.get(cartLoc);
            for (Integer chestID : chestList) {
                minecartPassDropChest(chestID, cart);
            }
        }
        
    }
    
    private void addLocToMap(Integer chestID, Location location, Filter filter) {
        //location.getBlock().setType(Material.GLASS);          // DEBUG -- Easily allows visualization of area covered
        
        HashMap<Location, LinkedHashSet<Integer>> locMap;
        if (filter == Filter.SUCK) {
            locMap = dcMapSuckLocationToID;
        }
        else {
            locMap = dcMapPullLocationToID;
        }
        // Location is already mapped, add chest to the set
        if (locMap.containsKey(location)) {
            locMap.get(location).add(chestID);
        }
        // Location is not mapped yet, create new set and add chest to it
        else {
            LinkedHashSet<Integer> chestSet = new LinkedHashSet<Integer>();
            chestSet.add(chestID);
            locMap.put(location, chestSet);
        }
    }
    
    private void removeLocFromMap(Integer chestID, Location location, Filter filter) {
        //location.getBlock().setType(Material.AIR);            // DEBUG -- Easily allows visualization of area covered

        HashMap<Location, LinkedHashSet<Integer>> locMap;
        if (filter == Filter.SUCK) {
            locMap = dcMapSuckLocationToID;
        }
        else {
            locMap = dcMapPullLocationToID;
        }
        if (!locMap.containsKey(location)) {
            return;
        }
        // Remove chest ID from the list
        locMap.get(location).remove(chestID);
        // If set is empty, removing location mapping
        if (locMap.get(location).isEmpty()) {
            locMap.remove(location);
        }
    }
    
    // Checks if <checkLoc> is within <distance> and <height> of <chestLoc>
    private boolean checkDistance(int distance, int height, Location checkLoc, Location chestLoc) {
        int xDist = Math.abs(checkLoc.getBlockX() - chestLoc.getBlockX());
        int yDist = Math.abs(checkLoc.getBlockY() - chestLoc.getBlockY());
        int zDist = Math.abs(checkLoc.getBlockZ() - chestLoc.getBlockZ());
        return ((xDist <= distance) && (yDist <= height) && (zDist <= distance));
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
    
    private DropChestObj getChest(Integer chestID) {
        return dcHashMap.get(chestID);
    }
    
    private Location roundLoc(Location location) {
        if (location == null) {
            return null;
        }
        Location newLoc = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        return newLoc;
    }
    
    private boolean chestFilterContains(Integer chestID, Material mat, Filter filter) {
        return (getChest(chestID).filterContains(mat.getId(), filter));
    }
    
    private void addChestToPullMap(Integer chestID) {
        // Add to push/pull loc map for primary chest
        FilterBox fb = new FilterBox(Properties.minecartFilterDistance, Properties.minecartFilterDistance, getChest(chestID).getPrimaryLocation());
        while (fb.hasNext()) {
            addLocToMap(chestID, fb.next(), Filter.PULL);
        }
        // Add to push/pull loc map for secondary chest if it exists
        if (getChest(chestID).getSecondaryLocation() != null) {
            fb = new FilterBox(Properties.minecartFilterDistance, Properties.minecartFilterDistance, getChest(chestID).getSecondaryLocation());
            while (fb.hasNext()) {
                addLocToMap(chestID, fb.next(), Filter.PULL);
            }
        }        
    }
    
    private void removeChestFromPullMap(Integer chestID) {
        FilterBox fb = new FilterBox(Properties.minecartFilterDistance, Properties.minecartFilterDistance, getChest(chestID).getPrimaryLocation());
        while (fb.hasNext()) {
            removeLocFromMap(chestID, fb.next(), Filter.PULL);
        }
        if (getChest(chestID).getSecondaryLocation() != null) {
            fb = new FilterBox(Properties.minecartFilterDistance, Properties.minecartFilterDistance, getChest(chestID).getSecondaryLocation());
            while (fb.hasNext()) {
                removeLocFromMap(chestID, fb.next(), Filter.PULL);
            }
        }
    }
    
    private void minecartPassDropChest(Integer chestID, StorageMinecart cart) {
        Inventory cartInv = cart.getInventory();
        ItemStack is;
        // Pull filter code
        for (int i = 0; i < cartInv.getSize(); i++) {
            is = cartInv.getItem(i);
            // No item in that slot, move on to next slot
            if ((is == null) || (is.getAmount() == 0))
                continue;
            // If this item is part of the filter we add it to the dropchest
            if (getChest(chestID).filterContains(is.getTypeId(), Filter.PULL)) {
                ItemStack leftover = addItem(chestID, is);
                if (leftover == null) {
                    cartInv.setItem(i, null);
                }
                else {
                    cartInv.setItem(i, leftover);
                }
            }
        }
        
    }
}
