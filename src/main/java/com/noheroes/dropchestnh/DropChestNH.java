/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh;

import com.noheroes.dropchestnh.commands.DCCommandExecutor;
import com.noheroes.dropchestnh.internals.DropChestEditor;
import com.noheroes.dropchestnh.internals.DropChestHandler;
import com.noheroes.dropchestnh.internals.Utils.EditMode;
import com.noheroes.dropchestnh.internals.Utils.Filter;
import com.noheroes.dropchestnh.listeners.DCListener;
import java.util.HashMap;
import java.util.logging.Level;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author PIETER
 */
public class DropChestNH extends JavaPlugin {
    
    private DropChestHandler dcHandler = new DropChestHandler(this);
    private DCListener dcListener = new DCListener(this);
    private HashMap<Player, DropChestEditor> playerEditMap = new HashMap<Player, DropChestEditor>();
    
    private static DropChestNH instance;

    @Override
    public void onDisable() {
        
    }
    
    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(dcListener, this);
        instance = this;
        getCommand("dropchest").setExecutor(new DCCommandExecutor(this));
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
    
    public static DropChestNH getInstance() {
        return instance;
    }
    
    public DropChestEditor getPlayerEditor(Player player) {
        return playerEditMap.get(player);
    }
    
    public void addPlayerToEditor(Player player, EditMode mode) {
        if (checkEditMode(player)) {
            return;
        }
        DropChestEditor editor = new DropChestEditor(player, mode);
        playerEditMap.put(player, editor);
    }
    
    public void addPlayerToEditor(Player player, EditMode mode, String chestName) {
        if (checkEditMode(player)) {
            return;
        }
        DropChestEditor editor = new DropChestEditor(player, mode, chestName);
        playerEditMap.put(player, editor);
    }
    
    public void addPlayerToEditor(Player player, EditMode mode, Filter filter) {
        if (checkEditMode(player)) {
            return;
        }
        DropChestEditor editor = new DropChestEditor(player, mode, filter);
        playerEditMap.put(player, editor);
    }
    
    public void removePlayerFromEditor(Player player) {
        playerEditMap.remove(player);
    }
    
    public boolean isInEditMode(Player player) {
        return playerEditMap.containsKey(player);
    }
    
    private boolean checkEditMode(Player player) {
        if (playerEditMap.containsKey(player)) {
            player.sendMessage("You are already in edit mode, right click to cancel first");
            return true;
        }        
        return false;
    }
}
