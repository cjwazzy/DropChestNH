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
                try {
                    dc.getDcHandler().addChest(block, player, chestName);
                    if (chestName == null) {
                        player.sendMessage("Dropchest added");
                    }
                    else {
                        player.sendMessage("Dropchest with name " + chestName + " added");
                    }
                } catch (MissingOrIncorrectParametersException ex) {
                    player.sendMessage(ex.getMessage());
                } finally {
                    return true;
                }
            case FILTER:
                try {
                    // Players can only edit their own chests unless they are an admin
                    if ((!dc.getDcHandler().ownsChest(block.getLocation(), player)) && !Utils.isAdmin(player)) {
                        player.sendMessage("You cannot edit the filter for a chest that is not yours");
                        return false;
                    }
                    // Clicked chest without item in hand
                    if (mat.equals(Material.AIR)) {
                        return false;
                    }
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
            case INFO:
                // Player needs to own the chest or have basic admin permission to look up information about it
                if ((!dc.getDcHandler().ownsChest(block.getLocation(), player)) && !Utils.hasPermission(player, Properties.basicAdmin)) {
                    player.sendMessage("This is not your chest");
                    return true;
                }
                if (!dc.getDcHandler().chestExists(block.getLocation())) {
                    player.sendMessage("This is not a dropchest");
                    return true;
                }
                String msg;
                Integer chestID = dc.getDcHandler().getChestID(block.getLocation());
                msg = Utils.getChestInfoMsg(player, chestID);
                player.sendMessage(msg);
                for (Filter f: Filter.values()) {
                    msg = Utils.getChestFilterInfoMsg(player, chestID, f);
                    player.sendMessage(msg);
                }
                return true;
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
            case INFO:
                player.sendMessage("Cancelled chest info lookup");
            default:
                return true;
        }
    }
}
