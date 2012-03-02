/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.internals;

import com.noheroes.dropchestnh.DropChestNH;
import com.noheroes.dropchestnh.exceptions.MissingOrIncorrectParametersException;
import com.noheroes.dropchestnh.internals.Utils.EditMode;
import com.noheroes.dropchestnh.internals.Utils.Filter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 *
 * @author PIETER
 */
public class DropChestEditor {
    private Player player;
    private EditMode mode;
    private String chestName;
    private Filter filter;
    private static DropChestNH dc;

    public DropChestEditor(Player player, EditMode mode, String chestName) {
        this.player = player;
        this.mode = mode;
        this.chestName = chestName;
        this.dc = DropChestNH.getInstance();
    }
    
    public DropChestEditor(Player player, EditMode mode, Filter filter) {
        this.player = player;
        this.mode = mode;
        this.chestName = null;
        this.dc = DropChestNH.getInstance();
        this.filter = filter;
    }
    
    public DropChestEditor(Player player, EditMode mode) {
        this.player = player;
        this.mode = mode;
        this.chestName = null;
        this.dc = DropChestNH.getInstance();
    }
    
    // Called when left click is performed on a chest while the player is in edit mode
    // Returns true if edit mode should be disabled after this click, false if edit mode should stay on
    public boolean leftClickEvent(Block block, Material mat) {
        switch(mode) {
            case ADD_CHEST:
                // Add chest
                if (dc.getDcHandler().addChest(block, player, chestName)) {
                    if (chestName == null) {
                        player.sendMessage("Dropchest added");
                    }
                    else {
                        player.sendMessage("Dropchest with name " + chestName + " added");
                    }
                    return true;
                }
                else {
                    player.sendMessage("That is already a dropchest");
                    return true;
                }
            case FILTER:
                try {
                    if (dc.getDcHandler().updateFilter(mat, block.getLocation(), filter)) {
                        player.sendMessage(mat.toString() + " has been added to the " + filter.toString() + " filter");
                        return false;
                    }
                    else {
                        player.sendMessage(mat.toString() + " has been removed from the " + filter.toString() + " filter");
                                
                        return false;
                    }
                } catch (MissingOrIncorrectParametersException ex) {
                    player.sendMessage(ex.getMessage());
                    return false;
                }
        }
        return false;
    }
    
    // Right now all right click events cancel edit mode, but this may change so we'll have this method decide whether or not to cancel just as left click
    public boolean rightClickEvent() {
        switch (mode) {
            case ADD_CHEST:
                player.sendMessage("Cancelled adding a chest");
                return true;
            case FILTER:
                player.sendMessage("Finished editing filter");
                return true;
            default:
                return true;
        }
    }
}
