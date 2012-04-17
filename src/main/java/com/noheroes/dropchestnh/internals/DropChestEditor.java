/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.internals;

import com.noheroes.dropchestnh.DropChestNH;
import com.noheroes.dropchestnh.exceptions.MissingOrIncorrectParametersException;
import com.noheroes.dropchestnh.internals.Utils.EditMode;
import com.noheroes.dropchestnh.internals.Utils.Filter;
import com.noheroes.dropchestnh.internals.Utils.MsgType;
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
    private int sd;
    private int sh;
    private Filter filter;
    private static DropChestNH dc;

    public DropChestEditor(Player player, EditMode mode, Integer sd, Integer sh) {
        this.player = player;
        this.mode = mode;
        this.sd = sd;
        this.sh = sh;
        this.dc = DropChestNH.getInstance();
    }
    
    public DropChestEditor(Player player, EditMode mode, Filter filter) {
        this.player = player;
        this.mode = mode;
        this.sd = 0;
        this.sh = 0;
        this.dc = DropChestNH.getInstance();
        this.filter = filter;
    }
    
    public DropChestEditor(Player player, EditMode mode) {
        this.player = player;
        this.mode = mode;
        this.sd = 0;
        this.sh = 0;
        this.dc = DropChestNH.getInstance();
    }
    
    // Called when left click is performed on a chest while the player is in edit mode
    // Returns true if edit mode should be disabled after this click, false if edit mode should stay on
    public boolean leftClickEvent(Block block, Material mat) {
        switch(mode) {
            case ADD_CHEST:
                // Add chest
                try {
                    Integer chestID = dc.getDcHandler().addChest(block, player.getName());
                    if (chestID == null) {
                        return false;
                    }
                    if (sd != 0) {
                        dc.getDcHandler().setSuckDistance(chestID, sd);
                        dc.getDcHandler().addAllFilter(chestID, filter.SUCK);
                    }
                    if (sh != 0) {
                        dc.getDcHandler().setSuckHeight(chestID, sh);
                    }
                    Utils.sendMessage(player, "Dropchest added with ID " + chestID, MsgType.INFO);
                } catch (MissingOrIncorrectParametersException ex) {
                    Utils.sendMessage(player, ex.getMessage(), MsgType.ERROR);
                } finally {
                    return true;
                }
            case FILTER:
                try {
                    // Players can only edit their own chests unless they are an admin
                    if ((!dc.getDcHandler().ownsChest(block.getLocation(), player)) && !Utils.isAdmin(player)) {
                        Utils.sendMessage(player, "You cannot edit the filter for a chest that is not yours", MsgType.ERROR);
                        return false;
                    }
                    // Clicked chest without item in hand
                    if (mat.equals(Material.AIR)) {
                        return false;
                    }
                    if (dc.getDcHandler().updateFilter(mat, block.getLocation(), filter)) {
                        Utils.sendMessage(player, mat.toString() + " has been added to the " + filter.toString() + " filter", MsgType.INFO);
                        return false;
                    }
                    else {
                        Utils.sendMessage(player, mat.toString() + " has been removed from the " + filter.toString() + " filter", MsgType.INFO);
                        return false;
                    }
                } catch (MissingOrIncorrectParametersException ex) {
                    player.sendMessage(ex.getMessage());
                    return false;
                }
            case INFO:
                // Player needs to own the chest or have basic admin permission to look up information about it
                if ((!dc.getDcHandler().ownsChest(block.getLocation(), player)) && !Utils.hasPermission(player, Properties.basicAdmin)) {
                    Utils.sendMessage(player, "This is not your chest", MsgType.ERROR);
                    return true;
                }
                if (!dc.getDcHandler().chestExists(block.getLocation())) {
                    Utils.sendMessage(player, "This is not a dropchest", MsgType.ERROR);
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
                if (dc.getDcHandler().isFilterInUse(chestID, Filter.SUCK)) {
                    Utils.sendMessage(player, "This chest is picking up items in an area of " + dc.getDcHandler().getXArea(chestID) + "x"
                            + dc.getDcHandler().getZArea(chestID) + "x" + dc.getDcHandler().getYArea(chestID) + " (XxZxY)", MsgType.INFO);
                }
                try {
                    if (dc.getDcHandler().getWarnFull(chestID)) {
                        Utils.sendMessage(player, "This chest has almost full warning turned on with a threshold", MsgType.INFO);
                        Utils.sendMessage(player, "of " + dc.getDcHandler().getWarnThreshold(chestID) + "% and a delay of " + dc.getDcHandler().getWarnDelay(chestID) + " minutes", MsgType.INFO);
                    }
                    // This exception should never be thrown in this case
                } catch (MissingOrIncorrectParametersException ex) {}
                return true;
        }
        return false;
    }
    
    // Right now all right click events cancel edit mode, but this may change so we'll have this method decide whether or not to cancel just as left click
    public boolean rightClickEvent() {
        switch (mode) {
            case ADD_CHEST:
                Utils.sendMessage(player, "Cancelled adding a chest", MsgType.INFO);
                return true;
            case FILTER:
                Utils.sendMessage(player, "Finished editing filter", MsgType.INFO);
                return true;
            case INFO:
                Utils.sendMessage(player, "Cancelled chest info lookup", MsgType.INFO);
            default:
                return true;
        }
    }
}
