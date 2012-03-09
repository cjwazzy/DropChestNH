/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.internals;

import com.noheroes.dropchestnh.DropChestNH;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author PIETER
 */
public class Utils {
    
    public static enum Filter {
        PUSH, PULL, SUCK
    }
    
    public static enum EditMode {
        ADD_CHEST, FILTER, INFO
    }
    // Temp set to true
    public static boolean hasPermission(CommandSender cs, String perm) {
        if (perm == null) {
            return true;
        }
        if (isAdmin(cs)) {
            return true;
        }
        if (perm.equals(Properties.createChestPerm)) {
            return (cs.hasPermission(Properties.createPullPerm) || 
                    cs.hasPermission(Properties.createPushPerm) || cs.hasPermission(Properties.createSuckPerm));
        }
        return cs.hasPermission(perm);
    }
    
    // Temp set to op only
    public static boolean isAdmin(CommandSender cs) {
        return (cs.hasPermission(Properties.fullAdmin) || (cs instanceof ConsoleCommandSender) || cs.isOp());
    }
    
    public static String getChestInfoMsg(CommandSender cs, Integer chestID) {
        String msg;
        DropChestNH dc = DropChestNH.getInstance();
        msg = getColor(cs, Properties.chestIDColor) + "Chest #" + chestID;
        if (dc.getDcHandler().getChestName(chestID) != null) {
            msg += getColor(cs, Properties.chestNameColor) + " - " + dc.getDcHandler().getChestName(chestID);
        }
        Location loc = dc.getDcHandler().getChestLocation(chestID);
        msg += getColor(cs, Properties.chestLocColor) + " X:" + loc.getBlockX() + " Z:" + loc.getBlockZ() + " Y:" + loc.getBlockY();
        for (Filter f : Filter.values()) {
            if (dc.getDcHandler().isFilterInUse(chestID, f)) {
                msg += getColor(cs, Properties.chestFilterColor) + " " + f.name().toString();
            }
        }
        InventoryData invData = dc.getDcHandler().getInventoryData(chestID);
        msg += getColor(cs, Properties.chestSlotsColor) + " Slots:" + invData.getFilledSlots() + "/" + invData.getTotalSlots();
        msg += getColor(cs, Properties.chestFilledColor) + " Filled:" + invData.getPercentageUsed() + "%";       
        return msg;
    }
    
    public static String getChestFilterInfoMsg(CommandSender cs, Integer chestID, Filter filter) {
        Set<Integer> filterSet;
        String msg;
        filterSet = DropChestNH.getInstance().getDcHandler().getFilter(chestID, filter);
        // Empty filter
        if ((filterSet == null) || filterSet.isEmpty()) {
            msg = getColor(cs, Properties.chestFilterColor) + filter.toString() + ": None";
        }
        // All materials besides Material.AIR added
        else if (filterSet.size() == (Material.values().length - 1)) {
            msg = getColor(cs, Properties.chestFilterColor) + filter.toString() + ": All";
        }
        // Non-empty, grab material list from filter
        else {
            msg = getColor(cs, Properties.chestFilterColor) + filter.toString() + ": ";
            boolean firstItem = true;
            // Loop through all items in filter and grab their material name
            for (Integer matID : filterSet) {
                if (firstItem) {
                    firstItem = false;
                }
                else {
                    msg += ", ";
                }
                msg += Material.getMaterial(matID).toString();
            }
        }
        return msg;
    }

    // Only colors messages sent to players
    public static String getColor(CommandSender cs, ChatColor color) {
        if (cs instanceof Player) {
            return color.toString();
        }
        else {
            return "";
        }
    }
}
