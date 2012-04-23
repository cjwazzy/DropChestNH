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
    // These vars should be stored
    private Location primaryLocation;
    private Location secondaryLocation;
    private Integer chestID;
    private String chestName;
    private String ownerName;
    private Set<Integer> suckFilter;
    private Set<Integer> pullFilter;
    private Set<Integer> pushFilter;
    private Integer chestSuckDistance;
    private Integer chestSuckHeight;
    private boolean almostFullWarning;
    private Integer almostFullWarningThreshold;
    private Integer warningDelay;
    // These vars do not need to be stored
    private Long lastWarningTime;
    
    
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
        this.almostFullWarning = true;
        this.lastWarningTime = 0L;
        this.warningDelay = Properties.defaultWarningDelay;
        this.almostFullWarningThreshold = Properties.defaultAlmostFullWarningThreshold;
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
        if (primaryLocation == null)
            return null;
        if (!primaryLocation.getBlock().getType().equals(Material.CHEST))
            return null;
        return ((Chest)primaryLocation.getBlock().getState()).getInventory();
    }
    
    protected Inventory getSecondaryInventory() {
        if (secondaryLocation == null)
            return null;
        if (!secondaryLocation.getBlock().getType().equals(Material.CHEST))
            return null;
        return ((Chest)secondaryLocation.getBlock().getState()).getInventory();
    }
    
    protected String getName() {
        return chestName;
    }
    
    protected void setName(String chestName) {
        this.chestName = chestName;
    }
    
    protected String getOwner() {
        return ownerName;
    }
    
    protected Integer getID() {
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
            if (mat != Material.AIR)
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
        if (this.getPrimaryInventory() == null) 
            return null;
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
    
    protected Integer getSuckDistance() {
        return chestSuckDistance;
    }
    
    protected void setSuckDistance(int suckDistance) {
        if (suckDistance >= 0)
            this.chestSuckDistance = suckDistance;
    }
    
    protected Integer getSuckHeight() {
        return chestSuckHeight;
    }
    
    protected void setSuckHeight(int suckHeight) {
        if (suckHeight >= 0)
            this.chestSuckHeight = suckHeight;
    }
       
    protected boolean toggleAlmostFullWarning() {
        this.almostFullWarning = !this.almostFullWarning;
        return this.almostFullWarning;
    }
    
    protected void setAlmostFullWarning(boolean warning) {
        this.almostFullWarning = warning;
    }
    
    protected void setAlmostFullThreshold(Integer threshold) {
        if ((threshold < 0) || (threshold > 99)) {
            return;
        }
        this.almostFullWarningThreshold = threshold;
    }
    
    protected Integer getAlmostFullThreshold() {
        return this.almostFullWarningThreshold;
    }
    
    protected boolean getAlmostFullWarning() {
        return this.almostFullWarning;
    }
    
    protected boolean warnPlayer() {
        // Enough time must have elapsed and warning must be turned on for this to return true
        return((Math.abs(lastWarningTime - System.currentTimeMillis()) > toMillis(warningDelay)) && this.almostFullWarning);
    }
    
    protected void playerWasWarned() {
        lastWarningTime = System.currentTimeMillis();
    }
    
    protected void setWarnDelay(Integer delay) {
        if (delay <= 0) {
            return;
        }
        this.warningDelay = delay;
    }
    
    protected Integer getWarnDelay() {
        return this.warningDelay;
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
    
    private Long toMillis(Integer minutes) {
        return minutes * 60L * 1000L;
    }
}