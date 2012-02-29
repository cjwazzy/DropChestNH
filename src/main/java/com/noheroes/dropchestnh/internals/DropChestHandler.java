/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.internals;

import java.util.HashMap;
import org.bukkit.Location;

/**
 *
 * @author PIETER
 */
public class DropChestHandler {
    
    private static final HashMap<Integer, DropChestObj> dropChestHashMap = new HashMap<Integer, DropChestObj>();
    private static final HashMap<String, DropChestObj> dcNameToIDMap = new HashMap<String, DropChestObj>();
    private static final HashMap<Location, DropChestObj> dcLocationToIDMap = new HashMap<Location, DropChestObj>();
    
    
}
