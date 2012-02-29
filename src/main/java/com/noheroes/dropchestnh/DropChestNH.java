/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh;

import com.noheroes.dropchestnh.internals.DropChestHandler;
import com.noheroes.dropchestnh.listeners.DCListener;
import java.util.logging.Level;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author PIETER
 */
public class DropChestNH extends JavaPlugin {
    
    private DropChestHandler dcHandler = new DropChestHandler(this);
    private DCListener dcListener = new DCListener(this);

    @Override
    public void onDisable() {
        
    }
    
    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(dcListener, this);
    }
    
    public void log(String message) {
        this.log(Level.INFO, message);
    }
    
    public void log(Level level, String message) {
        this.getLogger().log(level, message);
    }
    
    public DropChestHandler getDcHandler() {
        return dcHandler;
    }
    
}
