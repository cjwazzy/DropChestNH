/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.internals;

import com.noheroes.dropchestnh.internals.Utils.Filter;
import java.util.LinkedHashSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author PIETER
 */
public class DropChestObj {
    private Location primaryLocation;
    private Location secondaryLocation;
    private int chestID;
    private String chestName;
    private String ownerName;
    private Set<Integer> suckFilter;
    private Set<Integer> pullFilter;
    private Set<Integer> pushFilter;
    private int chestSuckDistance;
    private int chestSuckHeight;
    
    
    public DropChestObj(int chestID, String ownerName, String chestName, Location primaryLocation, Location secondaryLocation) {

        this.primaryLocation = primaryLocation;
        this.secondaryLocation = secondaryLocation;
        this.chestName = chestName;
        this.ownerName = ownerName;
        this.chestID = chestID;
        this.suckFilter = new LinkedHashSet<Integer>();
        this.pullFilter = new LinkedHashSet<Integer>();
        this.pushFilter = new LinkedHashSet<Integer>();
        this.chestSuckDistance = 0;
        this.chestSuckHeight = 0;
    }
    
    public DropChestObj(int chestID, String ownerName, String chestName, Location primaryLocation) {
        this(chestID, ownerName, chestName, primaryLocation, null);
    }
    
    public DropChestObj(int chestID, String ownerName, Location primaryLocation, Location secondaryLocation) {
        this(chestID, ownerName, null, primaryLocation, secondaryLocation);
    }
    
    public DropChestObj(int chestID, String ownerName, Location primaryLocation) {
        this(chestID, ownerName, null, primaryLocation, null);
    }
    
    public Location getPrimaryLocation() {
        return primaryLocation;
    }
    
    public Location getSecondaryLocation() {
        return secondaryLocation;
    }
    
    public Inventory getPrimaryInventory() {
        return ((primaryLocation == null) ? null : ((Chest)primaryLocation.getBlock().getState()).getInventory());
    }
    
    public Inventory getSecondaryInventory() {
        return ((secondaryLocation == null) ? null : ((Chest)secondaryLocation.getBlock().getState()).getInventory());
    }
    
    public String getName() {
        return chestName;
    }
    
    public String getOwner() {
        return ownerName;
    }
    
    public int getID() {
        return chestID;
    }
    
    public void setPrimaryLocation(Location location) {
        primaryLocation = location;
    }
    
    public void setSecondaryLocation(Location location) {
        secondaryLocation = location;
    }
    
    // Adds the material to the list if it's not present and returns true, removes the material if it's on the list and returns false
    public boolean updateFilter(Integer MatID, Filter filter) {
        if (getFilter(filter).contains(MatID)) {
            getFilter(filter).remove(MatID);
            return false;
        }
        else {
            getFilter(filter).add(MatID);
            return true;
        }
    }
    
    public void clearFilter(Filter filter) {
        getFilter(filter).clear();
    }
    
    public void addAllFilter(Filter filter) {
        for (Material mat : Material.values()) {
            getFilter(filter).add(mat.getId());
        }
    }
    
    public boolean filterContains(Integer matID, Filter filter) {
        return getFilter(filter).contains(matID);
    }
    
    public boolean isFilterInUse(Filter filter) {
        switch(filter) {
            case SUCK:
                return !suckFilter.isEmpty();
            case PUSH:
                return !pushFilter.isEmpty();
            case PULL:
                return !pullFilter.isEmpty();
            default:
                return false;
        }
    }
    
    public InventoryData getInventoryData() {
        return getInvData(getPrimaryInventory()).add(getInvData(getSecondaryInventory()));
    }
    
    public Set<Integer> getFilter(Filter filter) {
        switch(filter) {
            case SUCK: 
                return suckFilter;
            case PUSH:
                return pushFilter;
            case PULL:
                return pullFilter;
            default:
                return null;
        }
    } 
    
    public int getSuckDistance() {
        return chestSuckDistance;
    }
    
    public int getSuckHeight() {
        return chestSuckHeight;
    }

    private InventoryData getInvData(Inventory inv) {
        if (inv == null) {
            return null;
        }
        
        int filledSlots = 0;
        int freeSlots = 0;
        int usedSpace = 0;
        ItemStack is;
        for (int i = 0; i < inv.getSize(); i++) {
            is = inv.getItem(i);
            if ((is == null) || (is.getAmount() == 0)) {
                freeSlots++;
            }
            else {
                filledSlots++;
                usedSpace += is.getAmount();
            }
        }
        return new InventoryData(filledSlots, freeSlots, usedSpace);
    }
}