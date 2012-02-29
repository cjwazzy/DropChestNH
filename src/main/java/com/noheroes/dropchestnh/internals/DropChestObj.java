/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.internals;

import org.bukkit.Location;
import org.bukkit.World;

/**
 *
 * @author PIETER
 */
public class DropChestObj {
    private final Location location;
    private final int chestID;
    
    DropChestObj(int chestID, Location location) {
        this.location = location;
        this.chestID = chestID;
    }
    
}
