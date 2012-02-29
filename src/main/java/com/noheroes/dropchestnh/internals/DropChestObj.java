/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.internals;

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
    private Inventory primaryInventory;
    private Inventory secondaryInventory;
    private String chestName;
    private String ownerName;
    
    public DropChestObj(String ownerName, String chestName, Location primaryLocation, Location secondaryLocation) {

        this.primaryLocation = primaryLocation;
        this.secondaryLocation = secondaryLocation;
        this.chestName = chestName;
        this.ownerName = ownerName;
        
        if ((primaryLocation != null) && primaryLocation.getBlock().getType().equals(Material.CHEST)) {
            primaryInventory = ((Chest)primaryLocation.getBlock().getState()).getInventory();
        }
        else {
            primaryInventory = null;
        }
        if ((secondaryLocation != null) && secondaryLocation.getBlock().getType().equals(Material.CHEST)) {
            secondaryInventory = ((Chest)secondaryLocation.getBlock().getState()).getInventory();
        }
        else {
            secondaryInventory = null;
        }
    }
    
    public DropChestObj(String ownerName, String chestName, Location primaryLocation) {
        this(ownerName, chestName, primaryLocation, null);
    }
    
    public DropChestObj(String ownerName, Location primaryLocation, Location secondaryLocation) {
        this(ownerName, null, primaryLocation, secondaryLocation);
    }
    
    public DropChestObj(String ownerName, Location primaryLocation) {
        this(ownerName, null, primaryLocation, null);
    }
    
    public Location getPrimaryLocation() {
        return primaryLocation;
    }
    
    public Location getSecondaryLocation() {
        return secondaryLocation;
    }
    
    public Inventory getPrimaryInventory() {
        return primaryInventory;
    }
    
    public Inventory getSecondaryInventory() {
        return secondaryInventory;
    }
    
    public String getName() {
        return chestName;
    }
    
    public String getOwner() {
        return ownerName;
    }
}
