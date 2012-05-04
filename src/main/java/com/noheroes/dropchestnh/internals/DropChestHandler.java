/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.internals;

import com.noheroes.dropchestnh.DropChestNH;
import com.noheroes.dropchestnh.exceptions.MissingOrIncorrectParametersException;
import com.noheroes.dropchestnh.interfaces.StorageInterface;
import com.noheroes.dropchestnh.internals.Utils.Filter;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.Bukkit;
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

// TODO: Clean up use of dcHashMap.get and replace with getChest

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
    // Set of chests that have been changed and need to be saved next time a save happens
    private static final Set<Integer> dcChangedChestID = new LinkedHashSet<Integer>();
    private static int currentChestID = 1;
    private static Set<BlockFace> cardinalFaces = new LinkedHashSet<BlockFace>();
    private static DropChestNH dc;
    private static StorageInterface storage;
    
    public DropChestHandler(DropChestNH dc, StorageInterface storage) {
        this.dc = dc;
        this.storage = storage;
        cardinalFaces.add(BlockFace.NORTH);
        cardinalFaces.add(BlockFace.EAST);
        cardinalFaces.add(BlockFace.SOUTH);
        cardinalFaces.add(BlockFace.WEST);
    }
    
    
    /********************************************
     *                  PUBLIC                  *
     ********************************************/
    
    public void loadChests() {
        List<DropChestObj> chestList = storage.loadAll();
        if ((chestList == null) || (chestList.isEmpty())) {
            dc.log("No drop chests found, skipping load");
            return;
        }
        Integer chestID;
        for (DropChestObj dropchest : chestList) {
            // Add the dropchest to all the hashmaps
            chestID = dropchest.getID();
            dcHashMap.put(chestID, dropchest);
            if (dropchest.getName() != null) {
                dcMapChestNameToID.put(dropchest.getName(), chestID);
            }
            dcMapLocationToID.put(dropchest.getPrimaryLocation(), chestID);
            if (dropchest.getSecondaryLocation() != null) {
                dcMapLocationToID.put(dropchest.getSecondaryLocation(), chestID);
            }
            if (dcMapPlayerNameToID.containsKey(dropchest.getOwner().toLowerCase())) {
                dcMapPlayerNameToID.get(dropchest.getOwner().toLowerCase()).offer(dropchest.getID());
            }
            else {
                LinkedList<Integer> playerList = new LinkedList<Integer>();
                playerList.offer(dropchest.getID());
                dcMapPlayerNameToID.put(dropchest.getOwner().toLowerCase(), playerList);
            }
            FilterBox fb = new FilterBox(dropchest.getSuckDistance(), dropchest.getSuckHeight(), 
                    dropchest.getPrimaryLocation(), dropchest.getSecondaryLocation());
            Location loc;
            while (fb.hasNext()) {
                loc = fb.next();
                addLocToMap(chestID, loc, Filter.SUCK);
            }
            if (dropchest.isFilterInUse(Filter.PUSH) || dropchest.isFilterInUse(Filter.PULL)) {
                addChestToPullMap(chestID);
            }
        }
    }
    
    public void saveChangedChests() {
        if (dcChangedChestID.isEmpty()) {
            return;     // Nothing has changed
        }
        for (Integer chestID : dcChangedChestID) {
            storage.save(getChest(chestID));
        }
        dcChangedChestID.clear();
        storage.write();
    }
    
    public Integer addChest(Block chest, String ownerName) throws MissingOrIncorrectParametersException {
        return addChest(chest, ownerName, null);
    }
    
    public Integer addChest(Block chest, String ownerName, String chestName) throws MissingOrIncorrectParametersException {
        if (!chest.getType().equals(Material.CHEST)) {
            return null;
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
        dropChest = new DropChestObj(currentChestID, ownerName, chestName, primaryLocation, secondaryLocation);
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
        if (dcMapPlayerNameToID.containsKey(ownerName.toLowerCase())) {
            dcMapPlayerNameToID.get(ownerName.toLowerCase()).offer(currentChestID);
        }
        else {
            LinkedList<Integer> playerList = new LinkedList<Integer>();
            playerList.offer(currentChestID);
            dcMapPlayerNameToID.put(ownerName.toLowerCase(), playerList);
        }
        chestChanged(currentChestID);
        return currentChestID;
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
        // Delete from storage
        storage.removeChest(getChest(chestID));
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
        FilterBox fb = new FilterBox(getChest(chestID).getSuckDistance(), getChest(chestID).getSuckHeight(), 
                getChest(chestID).getPrimaryLocation(), getChest(chestID).getSecondaryLocation());
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
        // Update suck filter area for double chest
        FilterBox fb = new FilterBox(getChest(chestID).getSuckDistance(), 
                getChest(chestID).getSuckHeight(), getChest(chestID).getPrimaryLocation(), getChest(chestID).getSecondaryLocation());
        Location loc;
        while (fb.hasNext()) {
            loc = fb.next();
            // Any locs that were already adding to the map previously will be skipped by the method
            addLocToMap(chestID, loc, Filter.SUCK);
        }
        chestChanged(chestID);
        return true;
    }
    
    public void setName(String identifier, String chestName) throws MissingOrIncorrectParametersException {
        setName(getChestID(identifier), chestName);
    }
    
    public void setName(Integer chestID, String chestName) throws MissingOrIncorrectParametersException {
        if (chestID == null) {
            throw new MissingOrIncorrectParametersException("That chest does not exist");
        }
        if (chestName == null) {
            throw new MissingOrIncorrectParametersException("You must specify a chest name");
        }
        // Names must be unique
        if (dcMapChestNameToID.containsKey(chestName)) {
            throw new MissingOrIncorrectParametersException("That chest name is already in use");
        }
        // Remove old chest name from hashmap if it exists
        if (getChest(chestID).getName() != null) {
            dcMapChestNameToID.remove(getChest(chestID).getName());
        }
        // Rename chest and add new name to hashmap
        getChest(chestID).setName(chestName);
        dcMapChestNameToID.put(chestName, chestID);
        chestChanged(chestID);
    }
    
    public boolean getWarnFull(String identifier) throws MissingOrIncorrectParametersException {
        return getWarnFull(getChestID(identifier));
    }
    
    public boolean getWarnFull(Integer chestID) throws MissingOrIncorrectParametersException {
        if (chestID == null) {
            throw new MissingOrIncorrectParametersException("That chest does not exist"); 
        }
        return getChest(chestID).getAlmostFullWarning();
    }
    
    public Integer getWarnDelay(String identifier) throws MissingOrIncorrectParametersException {
        return getWarnDelay(getChestID(identifier));
    }
    
    public Integer getWarnDelay(Integer chestID) throws MissingOrIncorrectParametersException {
        if (chestID == null) {
            throw new MissingOrIncorrectParametersException("That chest does not exist"); 
        }
        return getChest(chestID).getWarnDelay();
    }
    
    public Integer getWarnThreshold(String identifier) throws MissingOrIncorrectParametersException {
        return getWarnThreshold(getChestID(identifier));
    }
    
    public Integer getWarnThreshold(Integer chestID) throws MissingOrIncorrectParametersException {
        if (chestID == null) {
            throw new MissingOrIncorrectParametersException("That chest does not exist"); 
        }
        return getChest(chestID).getAlmostFullThreshold();
    }
    
    public void setWarning(String identifier, String threshold, String warnDelay) throws MissingOrIncorrectParametersException {
        setWarning(getChestID(identifier), threshold, warnDelay);
    }
    
    public void setWarning(String identifier, String threshold) throws MissingOrIncorrectParametersException {
        setWarning(getChestID(identifier), threshold, null);
    }
    
    public void setWarning(Integer chestID, String threshold, String warnDelay) throws MissingOrIncorrectParametersException {
        if (chestID == null) {
            throw new MissingOrIncorrectParametersException("That chest does not exist"); 
        }
        if (threshold != null) {
            Integer thresholdInt;
            try {
                thresholdInt = Integer.valueOf(threshold);
            } catch (NumberFormatException ex) {
                throw new MissingOrIncorrectParametersException("That is not a valid threshold, it should be a number between 0 and 100");
            }
            if ((thresholdInt < 0) || (thresholdInt > 99)) {
                throw new MissingOrIncorrectParametersException("That is not a valid threshold, it should be a number between 0 and 100");
            }
            getChest(chestID).setAlmostFullThreshold(thresholdInt);
        }
        if (warnDelay != null) {
            Integer warnDelayInt;
            try {
                warnDelayInt = Integer.valueOf(warnDelay);
            } catch (NumberFormatException ex) {
                throw new MissingOrIncorrectParametersException("That is not a valid warning delay, it should be a number greater than zero");
            }
            if (warnDelayInt <= 0) {
                throw new MissingOrIncorrectParametersException("That is not a valid warning delay, it should be a number greater than zero");
            }
            getChest(chestID).setWarnDelay(warnDelayInt);
        }
        getChest(chestID).setAlmostFullWarning(true);
        chestChanged(chestID);
    }
    
    public boolean toggleWarning(String identifier) throws MissingOrIncorrectParametersException {
        return toggleWarning(getChestID(identifier));
    }
    
    public boolean toggleWarning(Integer chestID) throws MissingOrIncorrectParametersException {
        if (chestID == null) {
            throw new MissingOrIncorrectParametersException("That chest does not exist");
        }
        boolean bool = getChest(chestID).toggleAlmostFullWarning();
        chestChanged(chestID);
        return bool;
    }
    
    // Adds item to chest with ID chestID.  Returns anything that didn't fit, or null if everything fit
    public ItemStack addItem(Integer chestID, ItemStack item) {
        if ((chestID == null) || (item == null)) {
            return item;
        }
        // Bukkit methods alter the ItemStack so we clone it first
        ItemStack inputItem = item.clone();
        DropChestObj dropChest;
        dropChest = dcHashMap.get(chestID);
        if ((dropChest == null) || (dropChest.getPrimaryInventory() == null)) {
            return item;
        }
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
        if ((leftOverItems == null) || (leftOverItems.get(0) == null) || (leftOverItems.get(0).getAmount() == 0)) {
            checkFullWarning(chestID, false);
            return null;
        }
        else {
            checkFullWarning(chestID, true);
            return leftOverItems.get(0);
        }
    }
    
    public ItemStack addItem(Location location, ItemStack item) {
        return addItem(dcMapLocationToID.get(location), item);
    }
    
    public boolean updateFilter(Material mat, Location location, Filter filter) throws MissingOrIncorrectParametersException {
        if (location == null) {
            throw new MissingOrIncorrectParametersException("That is not a dropchest");
        }
        Integer chestID = dcMapLocationToID.get(location);
        return updateFilter(mat, chestID, filter);
    }
    
    public boolean updateFilter(Material mat, Integer chestID, Filter filter) throws MissingOrIncorrectParametersException {

        if ((chestID == null ) || (!dcHashMap.containsKey(chestID))) {
            throw new MissingOrIncorrectParametersException("That chest does not exist or is not a dropchest.");
        }

        // If push or pull filter is used add to push/pull minecart location map
        if ((filter == Filter.PULL) || (filter == Filter.PUSH)) {
            addChestToPullMap(chestID);
        }
        chestChanged(chestID);
        return dcHashMap.get(chestID).updateFilter(mat.getId(), filter);
    }
    
    // Attempts to find the chestID by matching indentifier against or chestID or chest name
    public boolean updateFilter(String mat, String identifier, Filter filter) throws MissingOrIncorrectParametersException {
        return updateFilter(mat, getChestID(identifier), filter);
    }
    
    public boolean updateFilter(String mat, Integer chestID, Filter filter) throws MissingOrIncorrectParametersException {
        Material material = getMaterialFromString(mat);
        if (material == null) {
            throw new MissingOrIncorrectParametersException("Material " + mat + " does not exist");
        }
        return updateFilter(material, chestID, filter);
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
        if ((chestID == null) || !dcHashMap.containsKey(chestID)) {
            throw new MissingOrIncorrectParametersException("That chest does not exist or is not a dropchest.");
        }
        getChest(chestID).clearFilter(filter);
        chestChanged(chestID);
    }
    
    public void clearFilter(Location location, Filter filter) throws MissingOrIncorrectParametersException {
        clearFilter(dcMapLocationToID.get(location), filter);
    }
    
    public void clearFilter(String identifier, Filter filter) throws MissingOrIncorrectParametersException {
        clearFilter(getChestID(identifier), filter);
    }
    
    public void addAllFilter(Integer chestID, Filter filter) throws MissingOrIncorrectParametersException {
        if ((chestID == null) || !dcHashMap.containsKey(chestID)) {
            throw new MissingOrIncorrectParametersException("That chest does not exist or is not a dropchest.");
        }
        if ((filter == Filter.PULL) || (filter == Filter.PUSH)) {
            addChestToPullMap(chestID);
        }
        getChest(chestID).addAllFilter(filter);
        chestChanged(chestID);
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
    
    public String getOwner(Location location) {
        return getOwner(dcMapLocationToID.get(location));
    }
    
    public String getOwner(Integer chestID) {
        if ((chestID == null) || !dcHashMap.containsKey(chestID)) {
            return null;
        }
        else {
            return getChest(chestID).getOwner();
        }
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
    
    public Integer getXArea(Integer chestID) {
        if ((chestID == null) || !dcHashMap.containsKey(chestID)) {
            return null;
        }
        // Single chest, area is distance * 2 + 1 (for the chest's own location)
        if (getChest(chestID).getSecondaryLocation() == null) {
            return ((getChest(chestID).getSuckDistance() * 2) + 1);
        }
        // Double chest but along the Z axis
        if (getChest(chestID).getPrimaryLocation().getBlockX() == getChest(chestID).getSecondaryLocation().getBlockX()) {
            return ((getChest(chestID).getSuckDistance() * 2) + 1);
        }
        // Double chest along the X axis
        else {
            return ((getChest(chestID).getSuckDistance() * 2) + 2);
        }
    }

    public Integer getZArea(Integer chestID) {
        if ((chestID == null) || !dcHashMap.containsKey(chestID)) {
            return null;
        }
        // Single chest, area is distance * 2 + 1 (for the chest's own location)
        if (getChest(chestID).getSecondaryLocation() == null) {
            return ((getChest(chestID).getSuckDistance() * 2) + 1);
        }
        // Double chest but along the X axis
        if (getChest(chestID).getPrimaryLocation().getBlockZ() == getChest(chestID).getSecondaryLocation().getBlockZ()) {
            return ((getChest(chestID).getSuckDistance() * 2) + 1);
        }
        // Double chest along the Z axis
        else {
            return ((getChest(chestID).getSuckDistance() * 2) + 2);
        }
    }

    public Integer getYArea(Integer chestID) {
        if ((chestID == null) || !dcHashMap.containsKey(chestID)) {
            return null;
        }
        return ((getChest(chestID).getSuckHeight() * 2) + 1);
    }
    
    public boolean isFilterInUse(Integer chestID, Filter filter) {
        return dcHashMap.get(chestID).isFilterInUse(filter);
    }
    
    public InventoryData getInventoryData(Integer chestID) {
        if ((chestID == null) || !dcHashMap.containsKey(chestID))
            return null;
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
            FilterBox fb = new FilterBox(newDistance, height, 
                    getChest(chestID).getPrimaryLocation(), getChest(chestID).getSecondaryLocation());
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
            FilterBox fb = new FilterBox(oldDistance, height, 
                    getChest(chestID).getPrimaryLocation(), getChest(chestID).getSecondaryLocation());
            Location loc;
            while (fb.hasNext()) {
                loc = fb.next();
                // Remove any locations that are not within range of the new filter box
                if (!checkDistance(newDistance, height, loc, 
                        getChest(chestID).getPrimaryLocation(), getChest(chestID).getSecondaryLocation())) {
                    removeLocFromMap(chestID, loc, Filter.SUCK);
                }
            }
        }
        // Update chest
        getChest(chestID).setSuckDistance(newDistance);
        chestChanged(chestID);
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
            FilterBox fb = new FilterBox(distance, newHeight, 
                    getChest(chestID).getPrimaryLocation(), getChest(chestID).getSecondaryLocation());
            Location loc;
            while (fb.hasNext()) {
                loc = fb.next();
                // Any locs that were already adding to the map previously will be skipped by the method
                addLocToMap(chestID, loc, Filter.SUCK);
            }
        }
        // Chest suck height is decreasing
        else {
            FilterBox fb = new FilterBox(distance, oldHeight, 
                    getChest(chestID).getPrimaryLocation(), getChest(chestID).getSecondaryLocation());
            Location loc;
            while (fb.hasNext()) {
                loc = fb.next();
                if (!checkDistance(distance, newHeight, loc, 
                        getChest(chestID).getPrimaryLocation(), getChest(chestID).getSecondaryLocation())) {
                    removeLocFromMap(chestID, loc, Filter.SUCK);
                }                
            }
        }
        getChest(chestID).setSuckHeight(newHeight);
        chestChanged(chestID);
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
    
    
    
    /********************************************
     *                PRIVATE                   *
     ********************************************/
    
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

    // Checks the distance for double chests
    private boolean checkDistance (int distance, int height, Location checkLoc, Location primaryChestLoc, Location secondaryChestLoc) {
        if (secondaryChestLoc == null) {
            return checkDistance(distance, height, checkLoc, primaryChestLoc);
        }
        else {
            return (checkDistance(distance, height, checkLoc, primaryChestLoc) || checkDistance(distance, height, checkLoc, secondaryChestLoc));
        }
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
        LinkedList<Location> locList = getLocationCross(getChest(chestID).getPrimaryLocation(), 
                Properties.minecartFilterDistance, Properties.minecartVerticalPickup);
        for (Location loc : locList) {
            addLocToMap(chestID, loc, Filter.PULL);
        }
        if (getChest(chestID).getSecondaryLocation() != null) {
            locList = getLocationCross(getChest(chestID).getSecondaryLocation(), 
                    Properties.minecartFilterDistance, Properties.minecartVerticalPickup);
            for (Location loc : locList) {
                addLocToMap(chestID, loc, Filter.PULL);
            }            
        }     
    }
    
    private void removeChestFromPullMap(Integer chestID) {
        LinkedList<Location> locList = getLocationCross(getChest(chestID).getPrimaryLocation(), 
                Properties.minecartFilterDistance, Properties.minecartVerticalPickup);
        for (Location loc : locList) {
            removeLocFromMap(chestID, loc, Filter.PULL);
        }
        if (getChest(chestID).getSecondaryLocation() != null) {
            locList = getLocationCross(getChest(chestID).getSecondaryLocation(), 
                    Properties.minecartFilterDistance, Properties.minecartVerticalPickup);
            for (Location loc : locList) {
                removeLocFromMap(chestID, loc, Filter.PULL);
            }            
        }
    }
    
    // Constructs a list of locations in a cross form around the given location for the push/pull filter
    private LinkedList<Location> getLocationCross(Location location, Integer distance, boolean vertical) {
        LinkedList<Location> locList = new LinkedList<Location>();
        Location loc;
        Integer currentX = location.getBlockX();
        Integer currentY = location.getBlockY();
        Integer currentZ = location.getBlockZ();
        // Add blocks along x axis
        for (currentX = (location.getBlockX() - distance); currentX <= (location.getBlockX() + distance); currentX++) {
            loc = new Location (location.getWorld(), currentX, currentY, currentZ);
            locList.add(loc);
        }
        currentX = location.getBlockX();
        // Add blocks along z axis
        for (currentZ = (location.getBlockZ() - distance); currentZ <= (location.getBlockZ() + distance); currentZ++) {
            loc = new Location (location.getWorld(), currentX, currentY, currentZ);
            locList.add(loc);
        }
        currentZ = location.getBlockZ();
        // If vertical is true, add blocks along y axis
        if (vertical) {
            for (currentY = (location.getBlockY() - distance); currentY <= (location.getBlockY() + distance); currentY++) {
                loc = new Location (location.getWorld(), currentX, currentY, currentZ);
                locList.add(loc);
            }            
        }
        return locList;
    }
    
    private void minecartPassDropChest(Integer chestID, StorageMinecart cart) {
        Inventory cartInv = cart.getInventory();
        Inventory chestPrimaryInv = getChest(chestID).getPrimaryInventory();
        Inventory chestSecondaryInv = getChest(chestID).getSecondaryInventory();
        ItemStack is;
        // Pull filter loop.  Check all items in minecart
        for (int i = 0; i < cartInv.getSize(); i++) {
            is = cartInv.getItem(i);
            // No item in that slot, move on to next slot
            if ((is == null) || (is.getAmount() == 0))
                continue;
            // If this item is part of the filter we add it to the dropchest
            if (getChest(chestID).filterContains(is.getTypeId(), Filter.PULL)) {
                ItemStack leftover = addItem(chestID, is);
                cartInv.setItem(i, leftover);
            }
        }
        // Push filter loop for primary inventory
        for (int i = 0; i < chestPrimaryInv.getSize(); i++) {
            is = chestPrimaryInv.getItem(i);
            // No item in that slot, move on to next one
            if ((is == null) || (is.getAmount() == 0))
                continue;
            if (getChest(chestID).filterContains(is.getTypeId(), Filter.PUSH)) {
                ItemStack leftover = addToCart(is, cartInv);
                chestPrimaryInv.setItem(i, leftover);
            }
        }
        if (chestSecondaryInv != null) {
            // Push filter loop for secondary inventory
            for (int i = 0; i < chestSecondaryInv.getSize(); i++) {
                is = chestSecondaryInv.getItem(i);
                // No item in that slot, move on to next one
                if ((is == null) || (is.getAmount() == 0))
                    continue;
                if (getChest(chestID).filterContains(is.getTypeId(), Filter.PUSH)) {
                    ItemStack leftover = addToCart(is, cartInv);
                    chestSecondaryInv.setItem(i, leftover);
                }
            }            
        }
    }
    
    private ItemStack addToCart(ItemStack is, Inventory cartInv) {
        HashMap<Integer, ItemStack> leftoverStack;
        ItemStack inputItem = is.clone();
        leftoverStack = cartInv.addItem(inputItem);
        return ((leftoverStack == null) ? null : leftoverStack.get(0));
    }
    
    private void chestChanged(Integer chestID) {
        if (Properties.saveInstantly) {
            storage.save(getChest(chestID));
            storage.write();
        }
        else {
            dcChangedChestID.add(chestID);
        }
    }
    
    private void checkFullWarning(Integer chestID, boolean couldNotPickup) {
        if (!getChest(chestID).warnPlayer()) {
            return;
        }
        // Could not pick up an item, attempt to message player
        if (couldNotPickup) {
            Player player = Bukkit.getPlayer(getChest(chestID).getOwner());
            if ((player != null) && player.isOnline()) {
                player.sendMessage("Your dropchest #" + chestID + " is full and could not pick up an item");
                getChest(chestID).playerWasWarned();
            }
        }
        else {
            Integer filled = getChest(chestID).getInventoryData().getPercentageUsed();
            if (filled > getChest(chestID).getAlmostFullThreshold()) {
                Player player = Bukkit.getPlayer(getChest(chestID).getOwner());
                if ((player != null) && player.isOnline()) {                
                    player.sendMessage("Your dropchest #" + chestID + " is " + filled + "% full");
                    getChest(chestID).playerWasWarned();
                }
            }
        }
    }
}
