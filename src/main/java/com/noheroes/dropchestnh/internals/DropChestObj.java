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
    
    
    protected DropChestObj(int chestID, String ownerName, String chestName, Location primaryLocation, Location secondaryLocation) {

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
    
    protected DropChestObj(int chestID, String ownerName, String chestName, Location primaryLocation) {
        this(chestID, ownerName, chestName, primaryLocation, null);
    }
    
    protected DropChestObj(int chestID, String ownerName, Location primaryLocation, Location secondaryLocation) {
        this(chestID, ownerName, null, primaryLocation, secondaryLocation);
    }
    
    protected DropChestObj(int chestID, String ownerName, Location primaryLocation) {
        this(chestID, ownerName, null, primaryLocation, null);
    }
    
    protected Location getPrimaryLocation() {
        return primaryLocation;
    }
    
    protected Location getSecondaryLocation() {
        return secondaryLocation;
    }
    
    protected Inventory getPrimaryInventory() {
        return ((primaryLocation == null) ? null : ((Chest)primaryLocation.getBlock().getState()).getInventory());
    }
    
    protected Inventory getSecondaryInventory() {
        return ((secondaryLocation == null) ? null : ((Chest)secondaryLocation.getBlock().getState()).getInventory());
    }
    
    protected String getName() {
        return chestName;
    }
    
    protected String getOwner() {
        return ownerName;
    }
    
    protected int getID() {
        return chestID;
    }
    
    protected void setPrimaryLocation(Location location) {
        primaryLocation = location;
    }
    
    protected void setSecondaryLocation(Location location) {
        secondaryLocation = location;
    }
    
    // Adds the material to the list if it's not present and returns true, removes the material if it's on the list and returns false
    protected boolean updateFilter(Integer MatID, Filter filter) {
        if (getFilter(filter).contains(MatID)) {
            getFilter(filter).remove(MatID);
            return false;
        }
        else {
            getFilter(filter).add(MatID);
            return true;
        }
    }
    
    protected void clearFilter(Filter filter) {
        getFilter(filter).clear();
    }
    
    protected void addAllFilter(Filter filter) {
        for (Material mat : Material.values()) {
            getFilter(filter).add(mat.getId());
        }
    }
    
    protected boolean filterContains(Integer matID, Filter filter) {
        return getFilter(filter).contains(matID);
    }
    
    protected boolean isFilterInUse(Filter filter) {
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
    
    protected InventoryData getInventoryData() {
        return getInvData(getPrimaryInventory()).add(getInvData(getSecondaryInventory()));
    }
    
    protected Set<Integer> getFilter(Filter filter) {
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
    
    protected int getSuckDistance() {
        return chestSuckDistance;
    }
    
    protected void setSuckDistance(int suckDistance) {
        if (suckDistance >= 0)
            this.chestSuckDistance = suckDistance;
    }
    
    protected int getSuckHeight() {
        return chestSuckHeight;
    }
    
    protected void setSuckHeight(int suckHeight) {
        if (suckHeight >= 0)
            this.chestSuckHeight = suckHeight;
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