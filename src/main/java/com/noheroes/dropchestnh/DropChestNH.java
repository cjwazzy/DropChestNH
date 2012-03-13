/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh;

import com.noheroes.dropchestnh.commands.DCCommandExecutor;
import com.noheroes.dropchestnh.internals.DropChestEditor;
import com.noheroes.dropchestnh.internals.DropChestHandler;
import com.noheroes.dropchestnh.internals.ItemLoopTask;
import com.noheroes.dropchestnh.internals.MiniStorage;
import com.noheroes.dropchestnh.internals.Properties;
import com.noheroes.dropchestnh.internals.Utils.EditMode;
import com.noheroes.dropchestnh.internals.Utils.Filter;
import com.noheroes.dropchestnh.listeners.DCListener;
import java.util.HashMap;
import java.util.logging.Level;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author PIETER
 */

public class DropChestNH extends JavaPlugin {
    
    private DropChestHandler dcHandler;
    private DCListener dcListener = new DCListener(this);
    private HashMap<Player, DropChestEditor> playerEditMap = new HashMap<Player, DropChestEditor>();
    private Integer itemLoopTaskID;
    private Integer saveLoopTaskID;
    private MiniStorage minidb;
    
    private static DropChestNH instance;

    @Override
    public void onDisable() {
        dcHandler.saveChangedChests();
        if (itemLoopTaskID != null)
            this.getServer().getScheduler().cancelTask(itemLoopTaskID);
        if (saveLoopTaskID != null)
            this.getServer().getScheduler().cancelTask(saveLoopTaskID);
    }
    
    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(dcListener, this);
        instance = this;
        getCommand("dropchest").setExecutor(new DCCommandExecutor(this));
        initialConfigLoad(this.getConfig());
        minidb = new MiniStorage(this, this.getDataFolder().getPath());
        dcHandler = new DropChestHandler(this, minidb);
        dcHandler.loadChests();
        startItemLoop();
        startSaveLoop();
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
    
    public boolean addPlayerToEditor(Player player, EditMode mode) {
        if (checkEditMode(player)) {
            return false;
        }
        DropChestEditor editor = new DropChestEditor(player, mode);
        playerEditMap.put(player, editor);
        return true;
    }
    
    public boolean addPlayerToEditor(Player player, EditMode mode, String chestName) {
        if (checkEditMode(player)) {
            return false;
        }
        DropChestEditor editor = new DropChestEditor(player, mode, chestName);
        playerEditMap.put(player, editor);
        return true;
    }
    
    public boolean addPlayerToEditor(Player player, EditMode mode, Filter filter) {
        if (checkEditMode(player)) {
            return false;
        }
        DropChestEditor editor = new DropChestEditor(player, mode, filter);
        playerEditMap.put(player, editor);
        return true;
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
    
    private void startItemLoop() {
        itemLoopTaskID = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new ItemLoopTask(this), 
                Properties.itemLoopDelay, Properties.itemLoopDelay);
    }
    
    private void startSaveLoop() {
        if (Properties.saveInstantly) {
            return;
        }
        else {
            saveLoopTaskID = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                public void run() {
                    dcHandler.saveChangedChests();
                }
            }, Properties.saveDelay * 20L, Properties.saveDelay * 20L);
        }
    }
    
    private void initialConfigLoad(FileConfiguration config) {
        config.options().copyDefaults(true);
        loadConfig(config);
        this.saveConfig();
        config.options().copyDefaults(false);
    }

    private void loadConfig(FileConfiguration config) {
        Properties.itemLoopDelay = config.getInt("ItemLoopTimer");
        Properties.maxDistance = config.getInt("DropChestConfig.MaxDistance");
        Properties.maxHeight = config.getInt("DropChestConfig.MaxHeight");
        Properties.minecartFilterDistance = config.getInt("DropChestConfig.MinecartPickupDistance");
        Properties.minecartVerticalPickup = config.getBoolean("DropChestConfig.MinecartVerticalPickup");
        Properties.saveInstantly = config.getBoolean("SaveSettings.SaveInstantly");
        Properties.saveDelay = config.getInt("SaveSettings.AutosaveTimer");
    }
}
