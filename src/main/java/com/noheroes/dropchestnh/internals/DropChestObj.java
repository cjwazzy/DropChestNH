/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.internals;

import com.noheroes.dropchestnh.DropChestNH;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;

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
    
    public DropChestObj(int chestID, String ownerName, String chestName, Location primaryLocation, Location secondaryLocation) {

        this.primaryLocation = primaryLocation;
        this.secondaryLocation = secondaryLocation;
        this.chestName = chestName;
        this.ownerName = ownerName;
        this.chestID = chestID;
        DropChestHandler.dc.log("Created DC.  Primary loc: " + primaryLocation.toString());
        if (secondaryLocation != null) {
            DropChestHandler.dc.log("Created DC.  Secondary loc: " + secondaryLocation.toString());
        }
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
        DropChestHandler.dc.log("Updating primary to " + location.toString());
    }
    
    public void setSecondaryLocation(Location location) {
        secondaryLocation = location;
        DropChestHandler.dc.log("Updating secondary to " + location.toString());
    }
}
