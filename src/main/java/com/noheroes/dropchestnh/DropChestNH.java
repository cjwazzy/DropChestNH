/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh;

import com.noheroes.dropchestnh.internals.DropChestObj;
import java.util.HashMap;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author PIETER
 */
public class DropChestNH extends JavaPlugin {
    
    private static final HashMap<Integer, DropChestObj> dropChestHashMap = new HashMap<Integer, DropChestObj>();
    private static final HashMap<String, Integer> dcNameToIDMap = new HashMap<String, Integer>();
    
    @Override
    public void onDisable() {
        
    }
    
    @Override
    public void onEnable() {
        
    }
    
}
